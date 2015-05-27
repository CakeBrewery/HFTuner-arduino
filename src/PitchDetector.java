import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.*;
import be.tarsos.dsp.pitch.*;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;


//Trying stuff from the manual of Tarsos

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
      System.out.println("debug: initializing try");
      dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(4096, 0);
      //dispatcher.addAudioProcessor(nl);


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


  public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
	  targetFreq = 147;
		if(pitchDetectionResult.getPitch() != -1){
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();

			if(pitch > targetFreq+5){
				controller.sendData("d"); //tune down
			}else if (pitch < targetFreq-5){
				controller.sendData("u"); //tune up
			}else {
				System.out.println("INTUNE");
				controller.sendData("n"); //neutral
				controller.close(); 
			}
			
			String message = String.format("Frequency: %.2fHz ( %.2f probability)", pitch,probability);
	    System.out.println(message);
	   }
	}
}
