import java.io.ByteArrayOutputStream;

import javax.sound.sampled.*;

public class Main {	

	public static void main(String[] args) {
		AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			final SourceDataLine sourceLine = (SourceDataLine)AudioSystem.getLine(info);
			sourceLine.open();
			
			info = new DataLine.Info(TargetDataLine.class, format);
			final TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(info);
			targetLine.open();
			
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			Thread sourceThread = new Thread() {
				@Override public void run() {
					sourceLine.start();
					while(true) {
						sourceLine.write(out.toByteArray(), 0, out.size());
					}
				}
			};
			
			Thread targetThread = new Thread() {
				@Override public void run() {
					targetLine.start();
					byte[] data = new byte[targetLine.getBufferSize() / 5];
					int readBytes;
					while(true) {
						readBytes = targetLine.read(data, 0, data.length);
						out.write(data, 0, readBytes);
					}
					
				}
			};
			
			targetThread.start();
			System.out.println("Started Recording...");
			Thread.sleep(5000);
			targetLine.stop();
			targetLine.close();
			
			System.out.println("Ended Recording...");
			System.out.println("Starting Playback...");
			
			sourceThread.start();
			Thread.sleep(5000);
			sourceLine.stop();
			sourceLine.close();
			
			System.out.println("Ended Playback...");
		}
		catch(LineUnavailableException lue){
			lue.printStackTrace();
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}		
	}
}
