package com.hiveworkshop.blizzard.blp;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;

/**
 * SampleModel to help process BLP indexed content. Acts like a multi banded non
 * standard MultiPixelPackedSampleModel.
 * <p>
 * All samples for each band are stored in a block. Band blocks are stored
 * sequentially in the same bank. Pixel packing occurs from least significant
 * bit towards most significant bit.
 * <p>
 * Although intended for use with samples with power of 2 bit length, other bit
 * lengths are supported. The only restriction is that sample bit length is less
 * than 8. Sample bit lengths that do not divide 8 by a whole number will pad
 * the most significant bits.
 * <p>
 * This SampleModel is not intended to be fast.
 * 
 * @author Imperial Good
 */
public class BLPPackedSampleModel extends SampleModel {
	/**
	 * Band sizes array.
	 */
	private final int[] bandSizes;

	/**
	 * Band offset array.
	 */
	private final int[] bandOffsets;

	/**
	 * Bands redirection array used to determine the number of advertised bands.
	 */
	private final int[] bands;

	/**
	 * Constructs a SampleModel for the given dimension with the specified bits
	 * per band.
	 * <p>
	 * The bandSizes field determines the number of bits per band. Bands must be
	 * between 1 and 8 bits.
	 * <p>
	 * The bands field allows band redirection, such as if only a subset of
	 * bands or different ordering is required. Available band numbers to
	 * reference is determined by the length of bandSizes. Due to the variable
	 * packing density of this SampleModel it is not possible to forego a full
	 * sequential set of bandSizes even if only a subset of the available bands
	 * are used. Although bands does check if a band number is valid, it does
	 * not check for duplicates. A value of null will automatically assign bands
	 * in a natural way as determined by bandSizes.
	 * 
	 * @param w
	 *            width in pixels.
	 * @param h
	 *            height in pixels.
	 * @param bandSizes
	 *            array of bits per band.
	 * @param bands
	 *            band redirection array.
	 * @throws IllegalArgumentException
	 *             if w or h are not greater than 0.
	 * @throws IllegalArgumentException
	 *             if bandSizes contains an invalid value.
	 * @throws IllegalArgumentException
	 *             if bands contains an invalid band number.
	 */
	public BLPPackedSampleModel(int w, int h, int[] bandSizes, int[] bands) {
		super(DataBuffer.TYPE_BYTE, w, h, bands != null ? bands.length : bandSizes.length);

		// validate arguments
		for (int i = 0; i < bandSizes.length; i += 1) {
			final int bandSize = bandSizes[i];
			if (bandSize < 1 || 8 < bandSize)
				throw new IllegalArgumentException("Invalid bandSizes.");
		}

		this.bandSizes = bandSizes.clone();

		// compute band offsets
		bandOffsets = new int[bandSizes.length + 1];
		for (int i = 0; i < bandSizes.length; i += 1) {
			final int baseOffset = bandOffsets[i];
			final int bandSize = bandSizes[i];
			bandOffsets[i + 1] = baseOffset + (w * h * bandSize + 7) / 8;
		}

		// process bands
		if (bands == null) {
			bands = new int[bandSizes.length];
			for (int i = 0; i < bands.length; i += 1)
				bands[i] = i;
		} else {
			bands = bands.clone();
			for (int i = 0; i < bands.length; i += 1) {
				final int bandref = bands[i];
				if (bandref < 0 || bandSizes.length <= bandref)
					throw new IllegalArgumentException("Invalid bands.");
			}
		}
		this.bands = bands;
	}

	@Override
	public int getNumDataElements() {
		return numBands;
	}

	@Override
	public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
		// process obj
		if (obj == null)
			obj = new byte[numBands];
		byte[] pixel = (byte[]) (obj);

		// get pixel
		for (int i = 0 ; i < numBands ; i+= 1) {
			pixel[i] = (byte) getSample(x, y, i, data);
		}

		return obj;
	}

	@Override
	public void setDataElements(int x, int y, Object obj, DataBuffer data) {
		// process obj
		byte[] pixel = (byte[]) (obj);

		// set pixel
		for (int i = 0 ; i < numBands ; i+= 1) {
			setSample(x, y, i, pixel[i], data);
		}
	}

	private int getPixelNumber(int x, int y) {
		return x + width * y;
	}

	private int getSamplePacking(int b) {
		return 8 / bandSizes[b];
	}

	private int getElementNumber(int pixelNumber, int samplePacking, int b) {
		return bandOffsets[b] + pixelNumber / samplePacking;
	}

	private int getSampleOffset(int pixelNumber, int samplePacking, int b) {
		return (pixelNumber % samplePacking) * bandSizes[b];
	}

	private int getSampleMask(int b) {
		return (1 << bandSizes[b]) - 1;
	}

	@Override
	public int getSample(int x, int y, int b, DataBuffer data) {
		b = bands[b];
		final int pixelNumber = getPixelNumber(x, y);
		final int samplePacking = getSamplePacking(b);
		return data.getElem(getElementNumber(pixelNumber, samplePacking, b)) >> getSampleOffset(
				pixelNumber, samplePacking, b) & getSampleMask(b);
	}

	@Override
	public void setSample(int x, int y, int b, int s, DataBuffer data) {
		b = bands[b];
		final int pixelNumber = getPixelNumber(x, y);
		final int samplePacking = getSamplePacking(b);
		final int elementNumber = getElementNumber(pixelNumber, samplePacking,
				b);
		final int sampleOff = getSampleOffset(pixelNumber, samplePacking, b);
		final int sampleMask = getSampleMask(b);
		data.setElem(elementNumber, data.getElem(elementNumber)
				& ~(sampleMask << sampleOff) | (s & sampleMask) << sampleOff);
	}

	@Override
	public BLPPackedSampleModel createCompatibleSampleModel(int w, int h) {
		return new BLPPackedSampleModel(w, h, bandSizes, bands);
	}

	@Override
	public SampleModel createSubsetSampleModel(int[] bands) {
		// validation
		if (bands.length > numBands)
			throw new IllegalArgumentException("Too many bands.");
		
		// process band redirection
		final boolean[] bandUsed = new boolean[this.bands.length];
		final int[] destBands = new int[bands.length];
		for (int i = 0 ; i < bands.length ; i+= 1) {
			final int bandref = bands[i];
			if (bandref < 0 || this.bands.length <= bandref || bandUsed[bandref])
				throw new IllegalArgumentException("Invalid bands.");
			bandUsed[bandref] = true;
			destBands[i] = this.bands[bandref];
		}
		
		return new BLPPackedSampleModel(width, height, bandSizes, destBands);
	}

	public int getBufferSize() {
		return bandOffsets[bandOffsets.length - 1];
	}

	@Override
	public DataBuffer createDataBuffer() {
		return new DataBufferByte(getBufferSize());
	}

	@Override
	public int[] getSampleSize() {
		// generate band size array
		final int[] bandSizes = new int[numBands];
		for (int i = 0 ; i < numBands ; i+= 1) {
			bandSizes[i] = this.bandSizes[bands[i]];
		}
		
		return bandSizes;
	}

	@Override
	public int getSampleSize(int band) {
		return bandSizes[bands[band]];
	}

}
