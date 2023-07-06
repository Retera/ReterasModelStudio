package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.Ray;
import com.hiveworkshop.rms.util.Vec3;

import java.nio.ByteBuffer;

public class TwiDtx1Compressor {
	Vec3ColorBlock vec3ColorBlock = new Vec3ColorBlock();

	public ByteBuffer CompressImageDXT1(float[][] inBuf, int width, int height) {
		float[] minColor = new float[4];
		float[] maxColor = new float[4];
		int w2 = (width + 3) / 4;
		int h2 = (height + 3) / 4;
		int totBytes = (w2 * h2)*8;
//		System.out.println("width: " + width + ", height: " + height);
		ByteBuffer globalOutData = ByteBuffer.allocateDirect(totBytes);

		TwiBlock_solv blockSolver = new TwiBlock_solv();

		for ( int j = 0; j < height; j += 4) {
			for ( int i = 0; i < width; i += 4 ) {
//				System.out.println("\n[" + (j/4) + "]"+"[" + (i/4) + "]");
//				System.out.println("\n[" + (j) + "]"+"[" + (i) + "]");

				Vec3[] block = vec3ColorBlock.set(inBuf, j, i, width);

				Ray fittedRay = blockSolver.getFittedRay(vec3ColorBlock);
//				System.out.println("color dir: " + fittedRay.getDir() + " color offset: " + fittedRay.getPoint());
				if(fittedRay.getDir().length() == 0){
					fittedRay.getDir().set(1,1,1).normalize();
				}

				Vec3[] pointsOnLine = blockSolver.getPointsOnLine(block, fittedRay);

				Vec3[] colors = blockSolver.getColors(pointsOnLine, fittedRay);

				int[] colorIndices = blockSolver.getColorIndices(block, colors);

				int maxC = DDSUtils.ColorTo565(colors[0].toArray(maxColor));

				globalOutData.put((byte) (( maxC >>  0 ) & 255));
				globalOutData.put((byte) (( maxC >>  8 ) & 255));

				int minC = DDSUtils.ColorTo565(colors[1].toArray(minColor));
				globalOutData.put((byte) (( minC >>  0 ) & 255));
				globalOutData.put((byte) (( minC >>  8 ) & 255));
//				if(28 < j && 20 < i){
//					System.out.println(maxC + "  " + Arrays.toString(maxColor));
//					System.out.println(minC + "  " + Arrays.toString(minColor));
//				}

//				if(35 < j && j < 50 && 35 < i && i < 50){
//					System.out.println("block: " + Arrays.toString(block));
//					System.out.println("PoL:   " + Arrays.toString(pointsOnLine));
////					System.out.println(maxC + "  " + Arrays.toString(maxColor));
////					System.out.println(minC + "  " + Arrays.toString(minColor));
//				}

//				System.out.println(maxC + "  " + Arrays.toString(maxColor) + ", " + Arrays.toString(DDSUtils.getColorCompsFrom565(maxC)));
//				System.out.println(minC + "  " + Arrays.toString(minColor) + ", " + Arrays.toString(DDSUtils.getColorCompsFrom565(minC)));
//				System.out.println(maxC + "  " + Arrays.toString(maxColor) + ", " + DDSUtils.ColorFrom565(DDSUtils.getColorComps565(maxC)));
//				System.out.println(minC + "  " + Arrays.toString(minColor) + ", " + DDSUtils.ColorFrom565(DDSUtils.getColorComps565(minC)));

				long colorInds = 0;
				for(int colorInd : colorIndices){
					colorInds = (colorInds<<2) | colorInd;
				}
				for(int pxI = 0; pxI<16 && pxI<inBuf.length; pxI++){
					colorInds = (colorInds<<2) | colorIndices[pxI];
				}

				globalOutData.put((byte) (( colorInds >>  0 ) & 255));
				if(4<inBuf.length){
					globalOutData.put((byte) (( colorInds >>  8 ) & 255));
					if(8<inBuf.length){
						globalOutData.put((byte) (( colorInds >> 16 ) & 255));
						if(12<inBuf.length){
							globalOutData.put((byte) (( colorInds >> 24 ) & 255));
						}
					}
				}

			}
		}
		return globalOutData;
	}

	int[] dummyColorIndices(){
		int[] colorIndices = new int[16];
		for(int i = 0; i<16; i++){
			colorIndices[i] = i%4;
		}
//		for(int i = 0; i<16; i+= 4){
//			colorIndices[i + 0] = 0;
//			colorIndices[i + 1] = 2;
//			colorIndices[i + 2] = 3;
//			colorIndices[i + 3] = 1;
//		}
//		System.out.println(Arrays.toString(colorIndices));
		return colorIndices;
	}

}
