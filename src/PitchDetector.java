import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.*;
import be.tarsos.dsp.pitch.*;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;


/* A pitch detector based on TarsosDSP API
 * that can be found at https://github.com/JorenSix/TarsosDSP
 */

public class PitchDetector implements PitchDetectionHandler
{
  float targetFreq;
  ArduinoController controller;
  AudioDispatcher dispatcher;
  PitchEstimationAlgorithm algo;

  
  public AudioDispatcher setup(ArduinoController controller, float targetFreq){
	  

    System.out.println("Setup started");
    
    this.controller = controller; 
    this.targetFreq = targetFreq;
    
    try{
      System.out.println("debug: initializing computer microphone");
      
      //The TarsosDSP API allows for easy detection of default microphone
      dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(4096, 0);

      //The type of pitch detection algorithm, this one comes with the TarsosDSP library
      algo = PitchEstimationAlgorithm.YIN;
      dispatcher.addAudioProcessor(new PitchProcessor(algo, 44100, 4096, this));


      return dispatcher;

    } catch(Exception e){
      System.out.println("an error occured");
      return dispatcher;
    }
  }

  public void run(){
    dispatcher.run();
  }


  //This function is in charge of handling the pitch once the sound volume threshold is reached
  //Every time the microphone picks up a pitch, this handle will trigger. 
  //This is an override of the function that comes with the PitchDetectionHandler component of TarsosDSP
  public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
	  targetFreq = 147; //For now, let's try to tune to 147 Hz
		if(pitchDetectionResult.getPitch() != -1){
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();

			if(pitch > targetFreq+5){
				//Send data to Arduino
				controller.sendData("d"); //tune down
			}else if (pitch < targetFreq-5){
				//Send data to Arduino
				controller.sendData("u"); //tune up
			}else {
				//Send data to Arduino
				controller.sendData("n"); //neutral
				
				//We have reached the required pitch, so close connection with Arduino. 
				System.out.println("In Tune!");
				controller.close(); 
				return;
			}
			
			String message = String.format("Frequency: %.2fHz ( %.2f probability)", pitch,probability);
	    System.out.println(message);
	   }
	}
}
