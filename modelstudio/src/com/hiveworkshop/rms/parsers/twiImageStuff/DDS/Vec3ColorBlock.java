package com.hiveworkshop.rms.parsers.twiImageStuff.DDS;

import com.hiveworkshop.rms.util.Vec3;

public class Vec3ColorBlock {
	private int colorChanelReduction = 1;
	private int hCCRed = colorChanelReduction /2;
	private int colorMax = 255/ colorChanelReduction;
	private Vec3 colMin = new Vec3();
	private Vec3 colMax = new Vec3(colorMax, colorMax, colorMax);
	private Vec3 centerP = new Vec3();
	private float totDistToCenter;
	private Vec3[] block = {
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
	};
	private Vec3[] blockSubCenter = {
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
			new Vec3(), new Vec3(), new Vec3(), new Vec3(),
	};

	public Vec3[] set(float[][] inBuf, int img_row, int img_col, int width){
		for ( int blockRow = 0; blockRow < 4; blockRow++ ) {
			int pixelRowStartInd = (img_row + (3 - blockRow)) * width + img_col;
			for ( int blockCol = 0; blockCol < 4; blockCol++ ) {
				int pixelInd = Math.min(inBuf.length-1, pixelRowStartInd + (3 - blockCol));
//				System.out.println(Arrays.toString(inBuf[pixelInd]));

				float[] data = inBuf[pixelInd];
				data[0] = ((int)(((data[0]+hCCRed)/ colorChanelReduction))&colorMax);
				data[1] = ((int)(((data[1]+hCCRed)/ colorChanelReduction))&colorMax);
				data[2] = ((int)(((data[2]+hCCRed)/ colorChanelReduction))&colorMax);
//				data[0] = ((int)((data[0])));
//				data[1] = ((int)((data[1])));
//				data[2] = ((int)((data[2])));
				block[blockRow*4 + blockCol].set(data);
//				System.out.println(Arrays.toString(inBuf[pixelInd]) + " -> " + block[blockRow + blockCol]);
			}
		}
		int n = block.length;
		centerP.set(Vec3.ZERO);
		for (Vec3 point : block) {
			centerP.add(point);
		}
		centerP.scale(1f/(float) n);
		totDistToCenter = 0;
		for(int i = 0; i<block.length; i++) {
			blockSubCenter[i].set(block[i]).sub(centerP);
			totDistToCenter += blockSubCenter[i].length();
		}


		return block;
	}

	public Vec3 getCenterP() {
		return centerP;
	}

	public Vec3[] getBlock() {
		return block;
	}

	public Vec3[] getBlockSubCenter() {
		return blockSubCenter;
	}

	public int getColorMax() {
		return colorMax;
	}

	public float getTotDistToCenter() {
		return totDistToCenter;
	}
}
