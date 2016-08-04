package com.lelloman.audiostreamserver;

import org.apache.commons.cli.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import static com.lelloman.audiostreamserver.Constants.DEFAULT_PORT;


public class Main {

	public static final int DEFAULT_MIXER_LINE = 0;
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	public static final int DEFAULT_SAMPLE_RATE = 22050;
	public static final int DEFAULT_BIT_DEPTH = 2;

	public static void main(String[] args) {

		log("look for local address...");
		InetAddress localAddress = getLocalAddress();
		log("local address = <%s>", localAddress.getHostAddress());

		if (localAddress == null)
			throw new RuntimeException("local address is null and i'm hungry");

		Options options = createCliOptions();

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("AudioCapture.jar", options);

			System.exit(1);
			return;
		}

		int port = getIntOption(cmd, "port", DEFAULT_PORT);
		int sampleRate = getIntOption(cmd,"sample_rate", DEFAULT_SAMPLE_RATE);
		int mixerLine = getIntOption(cmd,"mixer_line", DEFAULT_MIXER_LINE);
		int bufferSize = getIntOption(cmd,"buffer_size", DEFAULT_BUFFER_SIZE);
		int bitDepth = getIntOption(cmd, "bit_depth", DEFAULT_BIT_DEPTH);
		boolean verbose = cmd.hasOption("verbose");

		log("args- port<%s> sampleRte<%s> mixerLine<%s> bufferSize<%s> bitDepth<%s>", port,sampleRate,mixerLine,bufferSize, bitDepth);

		try {
			start(mixerLine,bufferSize,sampleRate,bitDepth,port,verbose);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}


	private static void start(int mixerLine, int bufferSize, int sampleRate, int bitDepth, int port, boolean verbose) throws UnknownHostException {

		AudioStreamServer server = new AudioStreamServer(port,sampleRate,bitDepth,bufferSize);

		server.serveForever();

		AudioCapture audioCapture = new AudioCapture(mixerLine, bufferSize, sampleRate, bitDepth, verbose, server);
		if(audioCapture.init())
			audioCapture.start();
		else
			throw new RuntimeException("something's gone wrong");
	}


	private static int getIntOption(CommandLine cmd, String name, int defaultValue){
		String t = cmd.getOptionValue(name);
		if(t == null)
			return defaultValue;

		try {
			return Integer.parseInt(t);
		}catch(Exception e){
			log("error while parsing argument <%s> with value <%s>", name, t);
			return defaultValue;
		}
	}

	private static Options createCliOptions(){
		Options options = new Options();

		Option mixerLine = new Option("m", "mixer_line", true, "mixer line index default is "+ String.valueOf(DEFAULT_MIXER_LINE));
		mixerLine.setRequired(false);
		mixerLine.setType(Integer.class);
		options.addOption(mixerLine);

		Option bufferSize = new Option("b", "buffer_size", true, "buffer size in byte, default is " + String.valueOf(DEFAULT_BUFFER_SIZE));
		bufferSize.setRequired(false);

		options.addOption(bufferSize);

		Option sampleRate = new Option("s", "sample_rate", true, "sample rate in hz, default is " + String.valueOf(DEFAULT_SAMPLE_RATE));
		sampleRate.setRequired(false);
		options.addOption(sampleRate);

		Option port = new Option("p", "port", true, "server port, default is " + String.valueOf(DEFAULT_PORT));
		port.setRequired(false);
		port.setType(Integer.class);
		options.addOption(port);

		Option bitDepth = new Option("d", "bit_depth", true, "bit depth in byte, default is " + String.valueOf(DEFAULT_BIT_DEPTH));
		bitDepth.setRequired(false);
		bitDepth.setType(Integer.class);
		options.addOption(bitDepth);

		Option verbose = new Option("verbose","verbose",false,"with verbose captured pcm data will be logged");
		options.addOption(verbose);

		return options;
	}


	/**
	 * retrieve the first ipv4 InetAddress which is not a loopback
	 * among the available network interfaces
	 * which should be something like 192.168.0.100 and not 127.0.0.1
	 */
	public static InetAddress getLocalAddress(){
		InetAddress localAddress = null;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			for(NetworkInterface networkInterface : Collections.list(interfaces)){
				log("interface <%s>", networkInterface.getDisplayName());

				for(InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
					log("\taddress: %s", address.getHostAddress());
					if (address instanceof Inet4Address && !address.isLoopbackAddress())
						localAddress = address;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return localAddress;
	}

	public static void log(String msg, Object...args){
		System.out.println(String.format(msg, args));
	}
}
