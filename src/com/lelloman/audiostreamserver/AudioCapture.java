package com.lelloman.audiostreamserver;

import javax.sound.sampled.*;
import java.util.Arrays;

public class AudioCapture {

	public interface AudioCaptureListener {
		void onAudioDataRead(byte[] data, int read);
	}

	private int bufferSize, sampleRate, mixerLine, bitDepth;
	private TargetDataLine targetDataLine;
	protected boolean running = false;
	private byte[] buffer;
	private AudioCaptureListener listener;
	private boolean verbose;

	public AudioCapture(int mixerLine, int bufferSize, int sampleRate, int bitDepth, boolean verbose, AudioCaptureListener listener) {
		this.mixerLine = mixerLine;
		this.bufferSize = bufferSize;
		this.sampleRate = sampleRate;
		this.buffer = new byte[bufferSize];
		this.bitDepth = bitDepth;
		this.verbose = verbose;
		this.listener = listener;
	}

	public boolean init() {
		try {
			AudioFormat format;
			Mixer mixer;

			Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

			int bits = bitDepth == 1 ? 8 : 16;
			boolean signed = bits == 16;
			format = new AudioFormat(sampleRate, bits, 1, signed, false);
			DataLine.Info dataLineInfo = new DataLine.Info(
					TargetDataLine.class, format);

			mixer = AudioSystem.getMixer(mixerInfo[mixerLine]);

			targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
			targetDataLine.open();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void start() {
		this.running = true;

		try {
			this.targetDataLine.start();

			while (this.running) {
				int read = targetDataLine.read(buffer, 0, bufferSize);
				listener.onAudioDataRead(buffer, read);
				if(verbose)
					System.out.println(Arrays.toString(buffer));
			}

		} catch (Exception e) {
			e.printStackTrace();
			this.targetDataLine.stop();
			this.running = false;
		}

	}

	public void stop() {
		this.running = false;
		this.targetDataLine.stop();
	}
}
