package com.lelloman.audiostreamserver;

public class Constants {
	private Constants(){}

	public static final int MSG_PING = 0xd00b;
	public static final int MSG_PONG = 0xbebbe;
	public static final int MSG_STREAM_INFO = 123;
	public static final int MSG_START_STREAM = 321;

	public static final int SO_TIMEOUT = 500;
	public static final int MAX_SO_TIMEOUT_COUNT = 5;
	public static final int DEFAULT_SKIP_FRAME = 500;
	public static final int DEFAULT_PORT = 8383;
}
