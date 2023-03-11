package com.hiveworkshop.rms.editor.model.util;

import jassimp.AiIOSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TwiAiIoSys implements AiIOSystem<TwiAiReader> {
	private final Class<?> clazz;
	private final ClassLoader classLoader;

	public TwiAiIoSys() {
		this.clazz = null;
		this.classLoader = null;
	}

	public TwiAiIoSys(ClassLoader var1) {
		this.clazz = null;
		this.classLoader = var1;
	}

	public TwiAiIoSys(Class<?> var1) {
		this.clazz = var1;
		this.classLoader = null;
	}

	@Override
	public TwiAiReader open(String path, String ioMode) {
		try {
			File file = new File(path);
			InputStream inputStream = new FileInputStream(file);
			TwiAiReader twiAiReader = new TwiAiReader(inputStream);


//			int kbSize = twiAiReader.getFileSize() / 1000;
//			double d1 = 10;
//			double d2 = 0.1 * kbSize;
//			double d22 = 0.0001 * kbSize;
//			double d3 = 2.5 * d22 * d22;
//			double d4 = d1 + d2 + d3;
//			float timeEst = ((int) d4)/1000f;

//			LocalDateTime localDateTime = LocalDateTime.now();
//			LocalDateTime localDateTimeNow = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
//			LocalDateTime localDateTimeDone = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (System.currentTimeMillis() + d4)), ZoneId.systemDefault());

//			LocalTime localTimeNow = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()).toLocalTime().truncatedTo(ChronoUnit.SECONDS);
//			LocalTime localTimeDone = LocalDateTime.ofInstant(Instant.ofEpochMilli((long) (System.currentTimeMillis() + d4)), ZoneId.systemDefault()).toLocalTime().truncatedTo(ChronoUnit.SECONDS);
//
//			System.out.println("File size: " + twiAiReader.getFileSize() + " bytes");
//			System.out.println("(" + localTimeNow + ") " + "est load time: " + timeEst + "s" + "(~"+ localTimeDone + ")");
//			System.out.println("file " + file.length());

			return twiAiReader;
//			if (inputStream != null) {
//				return new TwiAiReader(inputStream);
//			} else {
//				PrintStream var10000 = System.err;
//				String var10001 = this.getClass().getSimpleName();
//				var10000.println("[" + var10001 + "] Cannot find " + path);
//				return null;
//			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean exists(String s) {
//		URL var2 = null;
//		if (this.clazz != null) {
//			var2 = this.clazz.getResource(var1);
//		} else if (this.classLoader != null) {
//			var2 = this.classLoader.getResource(var1);
//		}
//
//		return var2 != null;
		return new File(s).exists();
	}

	@Override
	public char getOsSeparator() {
//		File.pathSeparator;
//		File.separatorChar
		return File.separatorChar;
	}

	@Override
	public void close(TwiAiReader twiAiReader) {
		System.out.println("done reading file");
	}

}
