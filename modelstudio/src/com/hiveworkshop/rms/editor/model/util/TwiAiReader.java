package com.hiveworkshop.rms.editor.model.util;

import jassimp.AiIOStream;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

public class TwiAiReader implements AiIOStream {
	private final ByteArrayOutputStream os;
	File file;
	long nextOutputTime;

	public TwiAiReader(URI uri) throws IOException {
		this(uri.toURL());
	}

	public TwiAiReader(URL url) throws IOException {
		this(url.openStream());
	}

	public TwiAiReader(InputStream inputStream) throws IOException {
		this.os = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];

		int i;
		while ((i = inputStream.read(bytes, 0, bytes.length)) != -1) {
			this.os.write(bytes, 0, i);
//			printProgress();
		}

		this.os.flush();
		inputStream.close();
//		printProgress();

//		int size = os.size();
//		double d1 = 7.8;
//		double d2 = 0.00005 *size;
//		double d3 =  1.06 * Math.pow(10, -13) *size*size;
//		double d4 = d1 + d2 + d3;
//		(782475 + (4.87108*size) + (1.059718*size*size))/100000

//		System.out.println("all in byteArray, est load time: "+ d4 + "ms");
	}

	@Override
	public boolean read(ByteBuffer byteBuffer) {
		TwiOutputStream twiOutputStream = new TwiOutputStream(byteBuffer);
		try {
			this.os.writeTo(twiOutputStream);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public int getFileSize() {
		return this.os.size();
	}

	public TwiAiReader setFile(File file) {
		this.file = file;
		return this;
	}

	private void printProgress() {
		if (nextOutputTime < System.currentTimeMillis()) {
			System.out.println(os.size() + " read");
			nextOutputTime = System.currentTimeMillis() + 500;
		}
	}

	private static class TwiOutputStream extends OutputStream {
		private final ByteBuffer buffer;
		long nextOutputTime;

		public TwiOutputStream(ByteBuffer byteBuffer) {
			this.buffer = byteBuffer;
		}

		public void write(int i) throws IOException {
			this.buffer.put((byte) i);
//			printProgress();
		}

		public void write(byte[] bytes, int off, int len) throws IOException {
			this.buffer.put(bytes, off, len);
//			printProgress();
		}

		private void printProgress() {
			if (nextOutputTime < System.currentTimeMillis()) {
				System.out.println(buffer.position() + " of " + buffer.capacity() + " read");
				nextOutputTime = System.currentTimeMillis() + 500;
			}
		}
	}
}
