package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import com.hiveworkshop.rms.parsers.twiImageStuff.ReaderUtils;
import com.hiveworkshop.rms.util.BinaryWriter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DDSFile {
	DDSHeader header;
	List<MipMap> mipmaps = new ArrayList<>();
	ByteBuffer buffer;
	int totSize;

//https://github.com/castano/nvidia-texture-tools
//https://github.com/castano/icbc
	public DDSFile(BufferedImage image, boolean ugg){
		int width = image.getWidth();
		int height = image.getHeight();
		header = new DDSHeader().setSize(width, height);
//		int[] pixels = image.getRaster().getPixels(0, 0, width, height, new int[width * height*4]);
		int[] pixels = new int[width * height];
		for (int h = 0; h<height; h++) {
			int rowOffs = h*width;
			for (int w = 0; w < width; w++) {
				pixels[rowOffs + w] = image.getRGB(w, h);
			}
		}
		buffer = new Compressor().CompressImageDXT5(pixels, width, height);
		totSize = 8 + 124 + (width/4 * height/4)*16;

	}
	public DDSFile(BufferedImage image, int ugg){
//		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		int width = image.getWidth();
		int height = image.getHeight();
		header = new DDSHeader().setSize(width, height);
//		int[] pixels = image.getRaster().getPixels(0, 0, width, height, new int[width * height*4]);
		float[][] pixels = new float[width * height][];
		for (int h = 0; h<height; h++) {
			int rowOffs = h*width;
			for (int w = 0; w < width; w++) {
//				pixels[rowOffs + w] = image.getRGB(w, h);
				pixels[rowOffs + w] = image.getRaster().getPixel(w, h, new float[4]);
			}
		}
//		buffer = new Compressor().CompressImageDXT5(pixels, width, height);
//		buffer = new GPTCompressor().CompressImageDXT5(pixels, width, height);
//		buffer = new GPTCompressor().CompressImageDXT1(pixels, width, height);
		buffer = new TwiDtx1Compressor().CompressImageDXT1(pixels, width, height);
		totSize = 8 + 124 + (width/4 * height/4)*16;

	}
	public DDSFile(BufferedImage image){
//		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		int width = image.getWidth();
		int height = image.getHeight();
		header = new DDSHeader().setSize(width, height);
		totSize = 4 + 124;
//		int[] pixels = image.getRaster().getPixels(0, 0, width, height, new int[width * height*4]);

		int mm = Math.max(Math.getExponent(width), Math.getExponent(height));
//		int mm = 2;

		int cW = width;
		int cH = height;
		int nMipMaps = 0;
		BufferedImage currImg = image;
		for (; 0 <= mm && (0<cW||0<cH); mm--){
//			System.out.println("mipMap: " + mm);
			MipMap mipMap = new MipMap(currImg);
			mipmaps.add(mipMap);
			totSize += mipMap.size;
			nMipMaps++;
			cW = cW/2;
			cH = cH/2;
			if(0<cW||0<cH){
				Image scaledInstance = image.getScaledInstance(Math.max(1, cW), Math.max(1, cH), Image.SCALE_SMOOTH);
				currImg = new BufferedImage(Math.max(1, cW), Math.max(1, cH), BufferedImage.TYPE_INT_ARGB);
				currImg.getGraphics().drawImage(scaledInstance, 0,0,null);
			}
		}
		header.setDwMipMapCount(nMipMaps);
//		System.out.println("tot size: " + totSize + ", mipMaps: " + nMipMaps);

	}

	public void write(BinaryWriter writer){
		writer.writeWithNulls("DDS ", 4);
		header.write(writer);
		int temp = 0;
		for(MipMap mipMap : mipmaps){
			temp++;
//			System.out.println("writing mipMap nr " + temp + ", mipMapB: " + mipMap.buffer.position() + ", writerB: " + writer.buffer.position() + ", writerBCap: " + writer.buffer.capacity()  + ", mipMBCap: " + writer.buffer.capacity());
			mipMap.buffer.clear(); // reset buffer position
//			System.out.println("mipMapB: " + mipMap.buffer.position());

			writer.buffer.put(mipMap.buffer);
//			System.out.println("writerB: " + writer.buffer.position() + ", mipMapB: " + mipMap.buffer.position());
		}
//		buffer.clear(); // reset buffer position
//		writer.buffer.put(buffer);
	}

	public int getTotSize() {
		return totSize;
	}


	public static void writeDDS(BufferedImage bufferedImage, File file) throws IOException {
		DDSFile ddsFile = new DDSFile(bufferedImage);
//		try (FileOutputStream outputStream = new FileOutputStream(file)) {
//
//			final BinaryWriter writer = new BinaryWriter(ddsFile.getTotSize());
//			ddsFile.write(writer);
//			outputStream.write(writer.buffer.array());
//		} catch (final Exception exc) {
//			ExceptionPopup.display(exc);
//			exc.printStackTrace();
//		}

		final BinaryWriter writer = new BinaryWriter(ddsFile.getTotSize());
		ddsFile.write(writer);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.write(writer.buffer.array());
		raf.setLength(raf.getFilePointer()); // trim
		raf.close();
	}


	public DDSFile(InputStream stream){

		if(stream != null){
//		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			int[] magicWord = ReaderUtils.getIntArray(stream, 1);
			int[] headerArray = ReaderUtils.getIntArray(stream, 31);
//			System.out.println("headerArray[0]: " + headerArray[0]);
			header = new DDSHeader(headerArray);
			int width = header.dwWidth;
			int height = header.dwHeight;
			totSize = 4 + 124;
//		int[] pixels = image.getRaster().getPixels(0, 0, width, height, new int[width * height*4]);

			String magW = new String(new char[]{(char) ((magicWord[0])&255), (char) ((magicWord[0]>>8)&255), (char) ((magicWord[0]>>16)&255), (char) ((magicWord[0]>>24)&255)});
			System.out.println("magicWord: " + magicWord[0] + ", '" + magW + "'");
			System.out.println(header);

//			int mm = Math.max(Math.getExponent(width), Math.getExponent(height));
////		int mm = 2;
//
//			int cW = width;
//			int cH = height;
//			int nMipMaps = 0;
//			BufferedImage currImg = image;
//			for (; 0 <= mm && (0<cW||0<cH); mm--){
////			System.out.println("mipMap: " + mm);
//				MipMap mipMap = new MipMap(currImg);
//				mipmaps.add(mipMap);
//				totSize += mipMap.size;
//				nMipMaps++;
//				cW = cW/2;
//				cH = cH/2;
//				if(0<cW||0<cH){
//					Image scaledInstance = image.getScaledInstance(Math.max(1, cW), Math.max(1, cH), Image.SCALE_SMOOTH);
//					currImg = new BufferedImage(Math.max(1, cW), Math.max(1, cH), BufferedImage.TYPE_INT_ARGB);
//					currImg.getGraphics().drawImage(scaledInstance, 0,0,null);
//				}
//			}
//		System.out.println("tot size: " + totSize + ", mipMaps: " + nMipMaps);
		}

	}

	private static class MipMap {
		ByteBuffer buffer;
		int size;
		MipMap(BufferedImage image) {
			int width = image.getWidth();
			int height = image.getHeight();
//			if(width%4 != 0){
//				width += (4-width%4);
//			}
//			if(height%4 != 0){
//				height += (4-height%4);
//			}
//			size = ((width+3)/4 * (height+3)/4) * 16;
			int w2 = (width + 3) / 4;
			int h2 = (height + 3) / 4;
			size = (w2 *  h2) * 8;

			int imgW = image.getWidth()-1;
			int imgH = image.getHeight()-1;
//			System.out.println("w: " + imgW + " (" + width + "), h: "  + imgH + " (" + height + ")" );
			float[][] pixels = new float[width * height][];
			for (int h = 0; h<height; h++) {
				int rowOffs = h*width;
				for (int w = 0; w < width; w++) {
//				pixels[rowOffs + w] = image.getRGB(w, h);
					pixels[rowOffs + w] = image.getRaster().getPixel(w, h, new float[4]);
				}
			}

			buffer = new TwiDtx1Compressor().CompressImageDXT1(pixels, width, height);
//			System.out.println("mipmap size: " +  size);
		}
	}

}
