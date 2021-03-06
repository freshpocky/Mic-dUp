import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioTransmitter {
	
	//.wav audio format
	private AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
	
	private DataLine.Info info;
	
	private SourceDataLine sourceLine;
	
	private TargetDataLine targetLine;
	
	private ByteArrayOutputStream out;
	
	private volatile boolean running;
	
	private void terminate() {
		running = false;
	}
	
	private Thread sourceThread = new Thread() {
		@Override
		public void run() {
			running = true;
			sourceLine.start();
			while (running) {
				sourceLine.write(out.toByteArray(), 0, out.size());
			}
		}
	};

	private Thread targetThread = new Thread() {		
		@Override
		public void run() {
			running = true;
			targetLine.start();
			byte[] data = new byte[targetLine.getBufferSize() / 5];
			int readBytes;
			while (running) {
				readBytes = targetLine.read(data, 0, data.length);
				out.write(data, 0, readBytes);
			}
		}
	};

	public AudioTransmitter() {
		try {
		info = new DataLine.Info(SourceDataLine.class, audioFormat);
		sourceLine = (SourceDataLine) AudioSystem.getLine(info);
		
		info = new DataLine.Info(TargetDataLine.class, audioFormat);
		targetLine = (TargetDataLine) AudioSystem.getLine(info);
		
		out = new ByteArrayOutputStream();	
		
		}
		catch (LineUnavailableException lue) {
			lue.printStackTrace();			
		}
	}

	public void StartRecording() {
		try {
			sourceLine.open();
			targetLine.open();	
		} 
		
		catch (LineUnavailableException lue) {
			lue.printStackTrace();			
		}
		
		targetThread.start();
		System.out.println("Started Recording...");
		sourceThread.start();
		System.out.println("Starting Playback...");
	}

	public void StopRecording() {
		terminate();
		targetLine.stop();
		targetLine.close();
		System.out.println("Ended Recording...");
			
		sourceLine.stop();
		sourceLine.close();
		System.out.println("Ended Playback...");
	}	
}
