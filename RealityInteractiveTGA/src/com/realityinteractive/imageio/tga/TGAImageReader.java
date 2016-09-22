package com.realityinteractive.imageio.tga;

/*
 * TGAImageReader.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 26, 2003
 */

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * <p>The {@link javax.imageio.ImageReader} that exposes the TGA image reading.
 * 8, 15, 16, 24 and 32 bit true color or color mapped (RLE compressed or 
 * uncompressed) are supported.  Monochrome images are not supported.</p>
 * 
 * <p>Great care should be employed with {@link javax.imageio.ImageReadParam}s.
 * Little to no effort has been made to correctly handle sub-sampling or 
 * specified bands.</p> 
 * 
 * <p>{@link javax.imageio.ImageIO#setUseCache(boolean)} should be set to <code>false</code>
 * when using this reader.  Also, {@link javax.imageio.ImageIO#read(java.io.InputStream)}
 * is the preferred read method if used against a buffered array (for performance
 * reasons).</p>
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAImageReader.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
// TODO:  incorporate the x and y origins
public class TGAImageReader extends ImageReader
{
    /**
     * <p>The {@link javax.imageio.stream.ImageInputStream} from which the TGA
     * is read.  This may be <code>null</code> if {@link javax.imageio.ImageReader#setInput(java.lang.Object)}
     * (or the other forms of <code>setInput()</code>) has not been called.  The
     * stream will be set litle-endian when it is set.</p>
     */
    private ImageInputStream inputStream;

    /**
     * <p>The {@link com.realityinteractive.imageio.tga.TGAHeader}.  If <code>null</code>
     * then the header has not been read since <code>inputStream</code> was 
     * last set.  This is created lazily.</p>
     */
    private TGAHeader header;

    // =========================================================================
    /**
     * @see javax.imageio.ImageReader#ImageReader(javax.imageio.spi.ImageReaderSpi)
     */
    public TGAImageReader(final ImageReaderSpi originatingProvider)
    {
        super(originatingProvider);
    }

    /**
     * <p>Store the input if it is an {@link javax.imageio.stream.ImageInputStream}.
     * Otherwise {@link java.lang.IllegalArgumentException} is thrown.  The
     * stream is set to little-endian byte ordering.</p>  
     * 
     * @see javax.imageio.ImageReader#setInput(java.lang.Object, boolean, boolean)
     */
    // NOTE:  can't read the header in here as there would be no place for
    //        exceptions to go.  It must be read lazily.
    public void setInput(final Object input, final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        // delegate to the partent
        super.setInput(input, seekForwardOnly, ignoreMetadata);

        // if the input is null clear the inputStream and header
        if(input == null)
        {
            inputStream = null;
            header = null;
        } /* else -- the input is non-null */

        // only ImageInputStream are allowed.  If other throw IllegalArgumentException
        if(input instanceof ImageInputStream)
        {
            // set the inputStream
            inputStream = (ImageInputStream)input;

            // put the ImageInputStream into little-endian ("Intel byte ordering")
            // byte ordering
            inputStream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        } else /* input is not an instance of ImageInputStream */
        {
            throw new IllegalArgumentException("Only ImageInputStreams are accepted.");  // FIXME:  localize
        }
    }

    /**
     * <p>Create and read the {@link com.realityinteractive.imageio.tga.TGAHeader}
     * only if there is not one already.</p>
     * 
     * @return the <code>TGAHeader</code> (for convenience)
     * @throws IOException if there is an I/O error while reading the header
     */
    private synchronized TGAHeader getHeader()
        throws IOException
    {
        // if there is already a header (non-null) then there is nothing to be
        // done
        if(header != null)
            return header;
        /* else -- there is no header */

        // ensure that there is an ImageInputStream from which the header is
        // read
        if(inputStream == null)
            throw new IllegalStateException("There is no ImageInputStream from which the header can be read."); // FIXME:  localize
        /* else -- there is an input stream */

        header = new TGAHeader(inputStream);

        return header;
    }

    /**
     * <p>Only a single image can be read by this reader.  Validate the 
     * specified image index and if not <code>0</code> then {@link java.lang.IndexOutOfBoundsException}
     * is thrown.</p>
     * 
     * @param  imageIndex the index of the image to validate
     * @throws IndexOutOfBoundsException if the <code>imageIndex</code> is not
     *         <code>0</code>
     */
    private void checkImageIndex(final int imageIndex)
    {
        // if the imageIndex is not 0 then throw an exception
        if(imageIndex != 0)
            throw new IndexOutOfBoundsException("Image index out of bounds (" + imageIndex + " != 0)."); // FIXME:  localize
        /* else -- the index is in bounds */
    }

    // =========================================================================
    // Required ImageReader methods
    /**
     * @see javax.imageio.ImageReader#getImageTypes(int)
     */
    public Iterator/*<ImageTypeSpecifier>*/ getImageTypes(final int imageIndex) 
        throws IOException
    {
        // validate the imageIndex (this will throw if invalid)
        checkImageIndex(imageIndex);

        // read / get the header
        final TGAHeader header = getHeader();

        // get the ImageTypeSpecifier for the image type
        // FIXME:  finish
        final ImageTypeSpecifier imageTypeSpecifier;
        switch(header.getImageType())
        {
            case TGAConstants.COLOR_MAP:
            case TGAConstants.RLE_COLOR_MAP:
            case TGAConstants.TRUE_COLOR:
            case TGAConstants.RLE_TRUE_COLOR:
            {
                // determine if there is an alpha mask based on the number of
                // samples per pixel
                final int alphaMask;
                if(header.getSamplesPerPixel() == 4)
                    alphaMask = 0xFF000000;
                else /* no alpha channel (less than 32 bits or 4 samples) */
                    alphaMask = 0;

                // packed RGB(A) pixel data (more specifically (A)BGR)
                // TODO:  split on 16, 24, and 32 bit images otherwise there
                //        will be wasted space
                final ColorSpace rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
                imageTypeSpecifier = ImageTypeSpecifier.createPacked(rgb,
                                                                     0x000000FF,
                                                                     0x0000FF00,
                                                                     0x00FF0000,
                                                                     alphaMask,
                                                                     DataBuffer.TYPE_INT,
                                                                     false /*not pre-multiplied by an alpha*/);
            
                break;
            }

            case TGAConstants.MONO:
            case TGAConstants.RLE_MONO:
                throw new IllegalArgumentException("Monochrome image type not supported.");
            
            case TGAConstants.NO_IMAGE:
            default:
                throw new IllegalArgumentException("The image type is not known."); // FIXME:  localize
        }

        // create a list and add the ImageTypeSpecifier to it
        final List/*<ImageTypeSpecifier>*/ imageSpecifiers = new ArrayList/*<ImageTypeSpecifier>*/();
        imageSpecifiers.add(imageTypeSpecifier);

        return imageSpecifiers.iterator();
    }

    /**
     * <p>Only a single image is supported.</p>
     * 
     * @see javax.imageio.ImageReader#getNumImages(boolean)
     */
    public int getNumImages(final boolean allowSearch) 
        throws IOException
    {
        // see javadoc
        // NOTE:  1 is returned regardless if a search is allowed or not
        return 1;
    }

    /**
     * <p>There is no stream metadata (i.e.  <code>null</code> is returned).</p>
     * 
     * @see javax.imageio.ImageReader#getStreamMetadata()
     */
    public IIOMetadata getStreamMetadata() 
        throws IOException
    {
        // see javadoc
        return null;
    }

    /**
     * <p>There is no image metadata (i.e.  <code>null</code> is returned).</p>
     * 
     * @see javax.imageio.ImageReader#getImageMetadata(int)
     */
    public IIOMetadata getImageMetadata(final int imageIndex) 
        throws IOException
    {
        // see javadoc
        return null;
    }

    /**
     * @see javax.imageio.ImageReader#getHeight(int)
     */
    public int getHeight(final int imageIndex) 
        throws IOException
    {
        // validate the imageIndex (this will throw if invalid)
        checkImageIndex(imageIndex);

        // get the header and return the height
        return getHeader().getHeight();
    }

    /**
     * @see javax.imageio.ImageReader#getWidth(int)
     */
    public int getWidth(final int imageIndex) 
        throws IOException
    {
        // validate the imageIndex (this will throw if invalid)
        checkImageIndex(imageIndex);

        // get the header and return the width
        return getHeader().getHeight();
    }

    /**
     * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param)
        throws IOException
    {
        // ensure that the image is of a supported type
        // NOTE:  this will implicitly ensure that the imageIndex is valid
        final Iterator imageTypes = getImageTypes(imageIndex);
        if(!imageTypes.hasNext())
        {
            throw new IOException("Unsupported Image Type");
        }

        // read and get the header
        final TGAHeader header = getHeader();

        // ensure that the ImageReadParam hasn't been set to other than the 
        // defaults (this will throw if not acceptible)
        checkImageReadParam(param, header);

        // get the height and width from the header for convenience
        final int width = header.getWidth();
        final int height = header.getHeight();

        // read the color map data.  If the image does not contain a color map
        // then null will be returned.
        final int[] colorMap = readColorMap(header);

        // seek to the pixel data offset
        // TODO:  read the color map
        inputStream.seek(header.getPixelDataOffset());

        // get the destination image and WritableRaster for the image type and 
        // size
        final BufferedImage image = getDestination(param, imageTypes, 
                                                   width, height);
        final WritableRaster imageRaster = image.getRaster();         

        // get and validate the number of image bands
        final int numberOfImageBands = image.getSampleModel().getNumBands();
        checkReadParamBandSettings(param, header.getSamplesPerPixel(), 
                                          numberOfImageBands);

        // get the destination bands
        final int[] destinationBands;
        if(param != null)
        {
            // there is an ImageReadParam -- use its destination bands
            destinationBands = param.getDestinationBands();
        } else
        {
            // there are no destination bands
            destinationBands = null;
        }

        // create the destination WritableRaster
        final WritableRaster raster = imageRaster.createWritableChild(0, 0, 
                                                                      width,
                                                                      height, 
                                                                      0, 0,
                                                                      destinationBands);

        // set up to read the data
        final int[] intData = ((DataBufferInt)raster.getDataBuffer()).getData(); // CHECK:  is this valid / acceptible?
        int index = 0; // the index in the intData array
        int runLength = 0; // the number of pixels in a run length
        boolean readPixel = true; // if true then a raw pixel is read.  Used by the RLE.
        boolean isRaw = false; // if true then the next pixels should be read.  Used by the RLE.
        int pixel = 0; // the current pixel data

        // TODO:  break out the case of 32 bit non-RLE as it can be read 
        //        directly and 24 bit non-RLE as it can be read simply.  If
        //        subsampling and ROI's are implemented then selection must be
        //        done per pixel for RLE otherwise it's possible to miss the
        //        repetition count field.

        // TODO:  account for TGAHeader.firstColorMapEntryIndex

        // loop over the rows
        // TODO:  this should be destinationROI.height (right?)
        for(int y=0; y<height; y++)
        {
            // if the image is flipped top-to-bottom then set the index in 
            // intData appropriately
            if(header.isBottomToTop())
                //index = y;
				index = (height - y) - 1;
            else /* is top-to-bottom */
				index = y;
                //index = (height - y) - 1;

            // account for the width
            // TODO:  this doesn't take into account the destination size or bands
            index *= width;

            // loop over the columns
            // TODO:  this should be destinationROI.width (right?)
            // NOTE:  *if* destinations are used the RLE will break as this will
            //        cause the repetition count field to be missed.
            for(int x=0; x<width; x++)
            {
                // if the image is compressed (run length encoded) then determine
                // if a pixel should be read or if the current one should be
                // used (using the current one is part of the RLE'ing).
                if(header.isCompressed())
                {
                    // if there is a non-zero run length then there are still
                    // compressed pixels
                    if(runLength > 0)
                    {
                        // decrement the run length and flag that a pixel should
                        // not be read
                        // NOTE:  a pixel is only read from the input if the
                        //        packet was raw.  If it was a run length packet
                        //        then the previous (current) pixel is used.
                        runLength--;
                        readPixel = isRaw;
                    } else /* non-positive run length */
                    {
                        // read the repetition count field 
                        runLength = inputStream.readByte() & 0xFF; // unsigned

                        // determine which packet type:  raw or runlength
                        isRaw = ( (runLength & 0x80) == 0); // bit 7 == 0 -> raw; bit 7 == 1 -> runlength

                        // if a run length packet then shift to get the number
                        if(!isRaw)
                            runLength -= 0x80;
                        /* else -- is raw so there's no need to shift */ 

                        // the next field is always read (it's the pixel data)
                        readPixel = true;
                    }
                }

                // read the next pixel
                // NOTE:  only don't read when in a run length packet
                if(readPixel)
                {
                    // NOTE:  the alpha must hav a default value since it is
                    //        not guaranteed to be present for each pixel read
                    int red = 0, green = 0, blue = 0, alpha = 0xFF;

                    // read based on the number of bits per pixel
                    switch(header.getBitsPerPixel())
                    {
                        // grey scale (R = G = B)
                        case 8:
                        default:
                        {
                            // read the data -- it is either the color map index
                            // or the color for each pixel
                            final int data = inputStream.readByte() & 0xFF; // unsigned

                            // if the image is a color mapped image then the
                            // resulting pixel is pulled from the color map, 
                            // otherwise each pixel gets the data 
                            if(header.hasColorMap())
                            {
                                // the pixel is pulled from the color map
                                // CHECK:  do sanity bounds check?
                                pixel = colorMap[data];
                            } else /* no color map */
                            {
                                // each color component is set to the color
                                red = green = blue = data;
                                
                                // combine each component into the result
                                pixel = (red << 0) | (green << 8) | (blue << 16);
                            }
                            
                            break;
                        }

                        // 5-5-5 (RGB)
                        case 15:
                        case 16:
                        {
                            // read the two bytes 
                            final int data = inputStream.readShort() & 0xFFFF; // unsigned

                            // get each color component -- each is 5 bits
                            red   = ((data >> 10) & 0x1F) << 3;
                            green = ((data >> 5)  & 0x1F) << 3;
                            blue  =  (data        & 0x1F) << 3;

                            // combine each component into the result
                            pixel = (red << 0) | (green << 8) | (blue << 16);

                            break;
                        }

                        // true color RGB(A) (8 bits per pixel)
                        case 24:
                        case 32:
                            // read each color component -- the alpha is only
                            // read if there are 32 bits per pixel
                            blue  = inputStream.readByte() & 0xFF; // unsigned
                            green = inputStream.readByte() & 0xFF; // unsigned
                            red   = inputStream.readByte() & 0xFF; // unsigned
                            if(header.getBitsPerPixel() == 32)
                                alpha = (inputStream.readByte() & 0xFF); // unsigned
                            /* else -- 24 bits per pixel (i.e. no alpha) */

                            // combine each component into the result
                            pixel = (red << 0) | (green << 8) | (blue << 16) | (alpha << 24);

                            break;
                    }
                }

                // put the pixel in the data array
                intData[index] = pixel;

                // advance to the next pixel
                // TODO:  the right-to-left switch
                index++;
            }
        }

        return image;
    }

    /**
     * <p>Reads and returns an array of color mapped values.  If the image does
     * not contain a color map <code>null</code> will be returned</p>
     * 
     * @param  header the <code>TGAHeader</code> for the image
     * @return the array of <code>int</code> color map values or <code>null</code>
     *         if the image does not contain a color map
     * @throws IOException if there is an I/O error while reading the color map
     */
    private int[] readColorMap(final TGAHeader header)
        throws IOException
    {
        // determine if the image contains a color map.  If not, return null
        if(!header.hasColorMap())
            return null;
        /* else -- there is a color map */

        // seek to the start of the color map in the input stream
        inputStream.seek(header.getColorMapDataOffset());

        // get the number of colros in the color map and the number of bits
        // per color map entry
        final int numberOfColors = header.getColorMapLength();
        final int bitsPerEntry = header.getBitsPerColorMapEntry();

        // create the array that will contain the color map data
        // CHECK:  why is tge explicit +1 needed here ?!? 
        final int[] colorMap = new int[numberOfColors + 1];

        // read each color map entry
        for(int i=0; i<numberOfColors; i++)
        {
            int red = 0, green = 0, blue = 0;

            // read based on the number of bits per color map entry
            switch(bitsPerEntry)
            {
                // grey scale (R = G = B)
                case 8:
                default:
                {
                    final int data = inputStream.readByte() & 0xFF; // unsigned
                    red = green = blue = data;
                    
                    break;
                }

                // 5-5-5 (RGB)
                case 15:
                case 16:
                {
                    // read the two bytes 
                    final int data = inputStream.readShort() & 0xFFFF; // unsigned

                    // get each color component -- each is 5 bits
                    red   = ((data >> 10) & 0x1F) << 3;
                    green = ((data >> 5)  & 0x1F) << 3;
                    blue  =  (data        & 0x1F) << 3;

                    break;
                }

                // true color RGB(A) (8 bits per pixel)
                case 24:
                case 32:
                    // read each color component 
                    // CHECK:  is there an alpha?!?
                    blue  = inputStream.readByte() & 0xFF; // unsigned
                    green = inputStream.readByte() & 0xFF; // unsigned
                    red   = inputStream.readByte() & 0xFF; // unsigned

                    break;
            }

            // combine each component into the result
            colorMap[i] = (red << 0) | (green << 8) | (blue << 16);
        }

        return colorMap;
    }

    /**
     * <p>Validate that the specified {@link javax.imageio.ImageReadParam} 
     * contains only the default values.  If non-default values are present,
     * {@link java.io.IOException} is thrown.</p>
     * 
     * @param  param the <code>ImageReadParam</code> to be validated
     * @param  head the <code>TGAHeader</code> that contains information about
     *         the source image
     * @throws IOException if the <code>ImageReadParam</code> contains non-default
     *         values
     */
    private void checkImageReadParam(final ImageReadParam param,
                                     final TGAHeader header)
        throws IOException
    {
        if(param != null)
        {
            // get the image height and width from the header for convenience
            final int width = header.getWidth();
            final int height = header.getHeight();
            
            // ensure that the param contains only the defaults
            final Rectangle sourceROI = param.getSourceRegion();
            if( (sourceROI != null) && 
                ( (sourceROI.x != 0) || (sourceROI.y != 0) ||
                  (sourceROI.width != width) || (sourceROI.height != height) ) )
            {
                throw new IOException("The source region of interest is not the default."); // FIXME:  localize
            } /* else -- the source ROI is the default */

            final Rectangle destinationROI = param.getSourceRegion();
            if( (destinationROI != null) &&
                ( (destinationROI.x != 0) || (destinationROI.y != 0) ||
                  (destinationROI.width != width) || (destinationROI.height != height) ) )
            {
                throw new IOException("The destination region of interest is not the default."); // FIXME:  localize
            } /* else -- the destination ROI is the default */

            if( (param.getSourceXSubsampling() != 1) || 
                (param.getSourceYSubsampling() != 1) )
            {
                throw new IOException("Source sub-sampling is not supported."); // FIXME:  localize
            } /* else -- sub-sampling is the default */
        } /* else -- the ImageReadParam is null so the defaults *are* used */
    }
}
// =============================================================================
/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */