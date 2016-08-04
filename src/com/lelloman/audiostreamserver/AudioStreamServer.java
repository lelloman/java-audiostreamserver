package com.lelloman.audiostreamserver;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.lelloman.audiostreamserver.Constants.MSG_PING;
import static com.lelloman.audiostreamserver.Constants.MSG_PONG;
import static com.lelloman.audiostreamserver.Constants.MSG_STREAM_INFO;

public class AudioStreamServer extends Thread implements AudioCapture.AudioCaptureListener {

	protected int clientPort;
	protected int port, sampleRate, bitDepth, bufferSize;
	protected boolean running = false;

	protected InetAddress clientAddress;
	private DatagramSocket serverSocket;
	private byte[] buffer;
	DatagramPacket dataPacket;


	public AudioStreamServer(int port, int sampleRate, int bitDepth, int bufferSize) {

		this.port = port;
		this.sampleRate = sampleRate;
		this.bitDepth = bitDepth;
		this.bufferSize = bufferSize;
	}

	public void serveForever() {
		try {
			serverSocket = new DatagramSocket(port);
			serverSocket.setSoTimeout(3000);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void run() {
		running = true;

		byte[] intBytes = new byte[Integer.BYTES];
		DatagramPacket intPacket = new DatagramPacket(intBytes, intBytes.length);

		while (running) {
			try {
				log("waiting for read...");

				boolean waitForRead = true;
				int msg = 0;
				while (waitForRead) {
					try {
						serverSocket.receive(intPacket);
						msg = ByteBuffer.wrap(intBytes).asIntBuffer().get();
						if (msg != 0) {
							waitForRead = false;
							Arrays.fill(intBytes, (byte) 0);
						}

					} catch (SocketTimeoutException e) {
						if (!running)
							waitForRead = false;
					}
				}

				log("connection accepted readline = %s", msg);
				log("client address %s port %s", intPacket.getAddress().getHostAddress(), intPacket.getPort());
				clientAddress = intPacket.getAddress();
				clientPort = intPacket.getPort();

				if (msg == MSG_STREAM_INFO)
					sendStreamInfo(intPacket);
				else if (msg == MSG_PING)
					sendPong(intPacket);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private void sendPong(DatagramPacket intPacket) throws IOException {

		ByteBuffer.wrap(intPacket.getData()).asIntBuffer().put(MSG_PONG);
		serverSocket.send(intPacket);
	}

	private void sendStreamInfo(DatagramPacket intPacket) throws IOException {

		ByteBuffer.wrap(intPacket.getData()).asIntBuffer().put(sampleRate);
		serverSocket.send(intPacket);

		ByteBuffer.wrap(intPacket.getData()).asIntBuffer().put(bufferSize);
		serverSocket.send(intPacket);

		ByteBuffer.wrap(intPacket.getData()).asIntBuffer().put(bitDepth);
		serverSocket.send(intPacket);

	}

	public void stopServer() {
		running = false;
		this.interrupt();
	}

	private void log(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	@Override
	public void onAudioDataRead(byte[] data, int read) {
		if (clientAddress == null || clientPort == 0)
			return;

		try {
			if (dataPacket == null) {
				dataPacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
			} else {
				dataPacket.setData(data, 0, data.length);
				dataPacket.setAddress(clientAddress);
				dataPacket.setPort(clientPort);
			}

			serverSocket.send(dataPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}