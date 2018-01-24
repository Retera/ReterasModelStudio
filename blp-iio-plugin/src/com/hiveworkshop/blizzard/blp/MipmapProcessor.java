package com.hiveworkshop.blizzard.blp;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.hiveworkshop.lang.LocalizedFormatedString;

/**
 * A class that is responsible for processing between mipmap data and
 * BufferedImage.
 * <p>
 * Implementations of this class are responsible for the types of images that
 * can be processed. A single instance is responsible for processing all mipmaps
 * of the same BLP file.
 * 
 * @author Imperial Good
 */
abstract class MipmapProcessor {
	/**
	 * Set by subclasses when the MipmapProcessor is ready to call decodeMipmap.
	 */
	protected boolean canDecode = false;

	/**
	 * Determines whether this MipmapProcessor requires encoded mipmaps to be
	 * post processed.
	 * <p>
	 * If true then all encoded mipmap data must be passed through
	 * postProcessMipmapData once all mipmap levels have been encoded. If false
	 * then then encoded mipmap data can be further used as is.
	 * <p>
	 * Default assumes false.
	 * 
	 * @return if postProcessMipmapData must be called once all encoding is
	 *         complete.
	 */
	public boolean mustPostProcess() {
		return false;
	}

	/**
	 * Post processes encoded mipmap data, allowing it to be decoded.
	 * <p>
	 * The order which the mipmap data is provided does not matter. The List
	 * returned can be the same as the input and the mipmap data might not be
	 * modified. The ordering of mipmap data in the output List is not changed.
	 * <p>
	 * If mustFinalize is true then after calling successfully canDecode will be
	 * true.
	 * <p>
	 * Default returns the input unchanged.
	 * 
	 * @param mmDataList
	 *            unprocessed mipmap data arrays.
	 * @param handler
	 *            warning handler.
	 * @return list of processed mipmap data.
	 * @throws IllegalArgumentException
	 *             if encodedmmData does not contain at least 1 element.
	 */
	public List<byte[]> postProcessMipmapData(List<byte[]> mmDataList,
			Consumer<LocalizedFormatedString> handler) {
		if (mmDataList.size() < 1)
			throw new IllegalArgumentException("No mipmap data.");
		return mmDataList;
	}

	/**
	 * Encodes an image into mipmap data.
	 * <p>
	 * The input image should use similar SampleModel and ColorModel to those
	 * returned by getSupportedImageTypes. Other image types might have a best
	 * effort attempt to encode but there is no guarantee of meaningful success
	 * or accuracy.
	 * <p>
	 * It is assumed the input image is the correct size. No clipping or
	 * subsampling is performed. Input pixel data is assumed to be in a
	 * CS_LINEAR_RGB ColorSpace with no automatic ColorSpace conversion.
	 * Compression quality of the ImageWriteParam may apply and may be lossy.
	 * <p>
	 * If mustFinalize is false then after calling successfully canDecode will
	 * be true.
	 * 
	 * @param img
	 *            input image to encode.
	 * @param param
	 *            image write parameter to control encode behavior.
	 * @param handler
	 *            warning handler.
	 * @return encoded mipmap data.
	 * @throws IIOException
	 *             if an image cannot be encoded.
	 */
	public abstract byte[] encodeMipmap(BufferedImage img,
			ImageWriteParam param, Consumer<LocalizedFormatedString> handler)
			throws IOException;

	/**
	 * Determines whether this MipmapProcessor can call decodeMipmap.
	 * <p>
	 * If canDecode is true then the object has a writable state and cann call
	 * decodeMipmap.
	 * 
	 * @return true if calls to decodeMipmap are valid.
	 */
	public final boolean canDecode() {
		return canDecode;
	}

	/**
	 * Decodes mipmap data into an image.
	 * <p>
	 * The image produced has very strict requirements. It must be exactly the
	 * dimensions of width and height. It must also be in the format of one of
	 * the ImageTypeSpecifier advertised by the class. The returned image might
	 * be backed by the mipmap data array for efficiency, hence the data it
	 * contains should be considered final after calling.
	 * <p>
	 * There is no guarantee that mmData contains exactly the data needed to
	 * fully produce an image. An attempt should be made to produce an image
	 * from as much of the data as possible. Missing pixel data must be assigned
	 * band values of 0.
	 * <p>
	 * No clipping or subsampling is performed. Output pixel data is assumed to
	 * be in a CS_LINEAR_RGB ColorSpace with no automatic ColorSpace conversion.
	 * <p>
	 * Calling when canDecode is false results in unspecified behavior, usually
	 * an exception.
	 * 
	 * @param mmData
	 *            the mipmap data to decode.
	 * @param param
	 *            image read parameter to control decode behavior.
	 * @param width
	 *            the width of the decoded image in pixels.
	 * @param height
	 *            the height of the decoded image in pixels.
	 * @param handler
	 *            warning handler.
	 * @return the decoded mipmap image.
	 * @throws IIOException
	 *             if an image cannot be produced.
	 */
	public abstract BufferedImage decodeMipmap(byte[] mmData,
			ImageReadParam param, int width, int height,
			Consumer<LocalizedFormatedString> handler) throws IOException;

	/**
	 * Am iterator of the image types supported by this processor.
	 * <p>
	 * The types in the iterator can be used to both encode and decode mipmaps.
	 * 
	 * @param width
	 *            the width of the image in pixels.
	 * @param height
	 *            the height of the image in pixels.
	 * @return iterator of supported image types.
	 */
	public abstract Iterator<ImageTypeSpecifier> getSupportedImageTypes(
			int width, int height);

	public abstract void readObject(ImageInputStream src,
			Consumer<LocalizedFormatedString> warning) throws IOException;

	public abstract void writeObject(ImageOutputStream dst) throws IOException;
}
