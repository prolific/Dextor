import java.util.Locale;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineStateError;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;

public class Mouth {

	private Synthesizer synthesizer;
	private Voice voice = new Voice("kevin16", Voice.GENDER_DONT_CARE,
			Voice.AGE_DONT_CARE, null);

	public void initialize() {

		System.setProperty("freetts.voices",
				"com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		try {
			Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
			synthesizer = Central.createSynthesizer(new SynthesizerModeDesc(
					Locale.US));
			synthesizer.allocate();
			synthesizer.getSynthesizerProperties().setVoice(voice);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void speak(String output) {
		try {
			synthesizer.resume();
			synthesizer.speakPlainText(output, null);
			synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
		} catch (AudioException | EngineStateError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
