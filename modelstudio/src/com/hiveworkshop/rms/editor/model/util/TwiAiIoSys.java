package com.hiveworkshop.rms.editor.model.util;

import jassimp.AiIOSystem;

import java.io.*;

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
			PrintStream var10000;
			String var10001;
			InputStream inputStream;
			File file = new File(path);
			inputStream = new FileInputStream(file);


			TwiAiReader twiAiReader = new TwiAiReader(inputStream);

//			int kbSize = twiAiReader.getFileSize()/1000;
//			double d1 = 10;
//			double d2 = 0.05 *kbSize;
//			double d22 = 0.001 *kbSize;
////			double d3 =  1.065 * Math.pow(10, -13) *size*size;
//			double d3 =  .1065*d22*d22;
////			double d3 =  .1065 *d22*d22;
//			double d4 = d1 + d2 + d3;
////			double d4 = d22*d22*d22*.0004678;
//			System.out.println("est load time: "+ d4 + "ms");
//			System.out.println("file "+ file.length());

			int kbSize = twiAiReader.getFileSize() / 1000;
			double d1 = 10;
			double d2 = 0.1 * kbSize;
			double d22 = 0.0001 * kbSize;
			double d3 = 2.5 * d22 * d22;
			double d4 = d1 + d2 + d3;
			System.out.println("est load time: " + d4 + "ms");
			System.out.println("file " + file.length());

			return twiAiReader;
//			if (inputStream != null) {
//				return new TwiAiReader(inputStream);
//			} else {
//				var10000 = System.err;
//				var10001 = this.getClass().getSimpleName();
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
