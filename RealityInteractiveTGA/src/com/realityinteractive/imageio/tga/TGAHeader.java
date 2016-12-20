package com.realityinteractive.imageio.tga;

/*
 * TGAHeader.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 26, 2003
 */

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;

/**
 * <p>The header to a TGA image file.</p>
 * 
 * <p>See <a href="http://organicbit.com/closecombat/formats/tga.html">format</a>,
 * <a href="http://www.opennet.ru/docs/formats/targa.pdf">format</a> or 
 * <a href="http://netghost.narod.ru/gff/graphics/summary/tga.htm">format</a>
 * for header information.</p>
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAHeader.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
public class TGAHeader
{
    /**
     * <p>The length of the TGA identifier.  This is a <code>byte</code> in 
     * length.</p>
     */
    private int idLength;

    /**
     * <p>The image identifier with length <code>idLength</code>.</p>
     */
    private byte[] id;

    /**
     * <p>Does this TGA have an associated color map?  <code>1</code> indicates
     * that there is a color map.  <code>0</code> indicates no color map.</p>
     */
    private boolean hasColorMap;

    /**
     * <p>The type of image.  See the image type constants in {@link com.realityinteractive.imageio.tga.TGAConstants}
     * for allowed values.</p> 
     */
    private int imageType;

    /**
     * <p>An image is compressed if its image type is {@link TGAConstants#RLE_COLOR_MAP}, 
     * {@link TGAConstants#RLE_TRUE_COLOR}, or {@link TGAConstants#RLE_MONO}.</p>
     */
    private boolean isCompressed;

    /**
     * <p>The index of the first color map entry.</p>
     */
    private int firstColorMapEntryIndex;

    /**
     * <p>The total number of color map entries.</p>
     */
    private int numberColorMapEntries;

    /**
     * <p>The number of bits per color map entry.</p>
     */
    private int bitsPerColorMapEntry;

    /**
     * <p>The computed size of a color map entry in <code>byte</code>s.</p>
     */
    private int colorMapEntrySize;

    /**
     * <p>The computed size of the color map field in <code>byte</code>s.  This
     * is determined from the <code>numberColorMapEntries</code> and 
     * <code>colorMapEntrySize</code>.</p>
     */
    private int colorMapSize;

    /**
     * <p>The horizontal coordinate for the lower-left corner of the image.</p>
     */
    private int xOrigin;

    /**
     * <p>The vertical coordinate for the lower-left corner of the image.</p>
     */
    private int yOrigin;

    /**
     * <p>The width of the image in pixels.</p>
     */
    private int width;

    /**
     * <p>The height of the image in pixels.</p>
     */
    private int height;

    /**
     * <p>The number of bits per pixel.</p>
     */
    private int bitsPerPixel;

    /**
     * <p>The number of attribute bits per pixel.</p>
     */
    private int imageDescriptor;

    /**
     * <p>The horizontal ordering of the pixels as determined from the image
     * descriptor.  By default the order is left to right.</p> 
     */
    // NOTE:  true -> left-to-right; false -> right-to-left
    private boolean leftToRight;

    /**
     * <p>The horizontal ordering of the pixels as determined from the image
     * descriptor.  By default the order is left to right.</p> 
     */
    // NOTE:  true -> bottom-to-top; false -> top-to-bottom
    private boolean bottomToTop;

    /**
     * <p>The offset to the color map data.  This value is not defined if there 
     * is no color map (see <code>hasColorMap</code>).</p>
     */
    private int colorMapDataOffset;

    /**
     * <p>The offset to the pixel data.</p>
     */
    private int pixelDataOffset;

    // =========================================================================
    /**
     * <p>Constructs a TGA header that will be populated by setters or directly
     * (via a static "factory" method).</p>
     */
    public TGAHeader() {}

    /**
     * <p>Constructs and populates a TGA header from the specified {@link javax.imageio.stream.ImageInputStream}.</p>
     * 
     * @param  inputStream the <code>ImageInputStream</code> from which the 
     *         header data is read
     * @throws IOException if there was an I/O error while reading the header
     *         data
     */
    public TGAHeader(final ImageInputStream inputStream)
        throws IOException
    {
        // read the data
        readHeader(inputStream);
    }

    /**
     * <p>Reads and populates the header from the specifed {@link javax.imageio.stream.ImageInputStream}.
     * Any existing values will be over written.</p>
     * 
     * <p>The <code>ImageInputStream</code> will be changed as a result of this
     * operation (the offset will be moved).</p>
     * 
     * @param  inputStream the <code>ImageInputStream</code> from which the 
     *         header data is read
     * @throws IOException if there was an I/O error while reading the header
     *         data
     */
    public void readHeader(final ImageInputStream inputStream)
        throws IOException
    {
        // read in the header as per the spec
        idLength = inputStream.readUnsignedByte();    

        hasColorMap = (inputStream.readUnsignedByte() == 1); // 1 == true, 0 == false    
        imageType = inputStream.readUnsignedByte();    

        firstColorMapEntryIndex = inputStream.readUnsignedShort();
        numberColorMapEntries = inputStream.readUnsignedShort();
        bitsPerColorMapEntry = inputStream.readByte();

        xOrigin = inputStream.readUnsignedShort();
        yOrigin = inputStream.readUnsignedShort();
        width = inputStream.readUnsignedShort();
        height = inputStream.readUnsignedShort();

        bitsPerPixel = inputStream.readByte();
        imageDescriptor = inputStream.readByte();

        // determine if the image is compressed
        isCompressed = ( (imageType == TGAConstants.RLE_COLOR_MAP) ||
                         (imageType == TGAConstants.RLE_TRUE_COLOR) ||
                         (imageType == TGAConstants.RLE_MONO) );

        // compute the size of the color map field in bytes
        switch(bitsPerColorMapEntry)
        {
            case 8:
            default:
                colorMapEntrySize = 1;
                break;
            case 15:
            case 16:
                colorMapEntrySize = 2;
                break;
            case 24:
            case 32:
                colorMapEntrySize = 3;
                break;
        }
        colorMapSize = colorMapEntrySize * numberColorMapEntries; // in bytes 

        // set the pixel ordering from the imageDescriptor bit mask
        // (bit set indicates false)
        leftToRight = ((imageDescriptor & TGAConstants.LEFT_RIGHT_BIT) == 0);
        bottomToTop = ((imageDescriptor & TGAConstants.BOTTOM_TOP_BIT) == 0);

        // read the image id based whose length is idLength
        if(idLength > 0)
        {
            // allocate the space for the id
            id = new byte[idLength];

            // read the id
            inputStream.read(id, 0, idLength);
        } /* else -- the idLength was not positive */

        // compute the color map and pixel data offsets.  The color map data 
        // offset is the current offset.
        // NOTE:  the conversion to int is OK since the maximum size of the
        //        color map data is 65536 bytes.
        final long currentOffset = inputStream.getStreamPosition();
        colorMapDataOffset = (int)currentOffset;
        if(hasColorMap)
        {
            // there is a color map so the pixel data offset is the current
            // offset + the size of the color map data
            pixelDataOffset = colorMapDataOffset + colorMapSize;
        } else /* there is no color map */
        {
            // there is no color map so the pixel data offset is the current
            // offset
            pixelDataOffset = (int)currentOffset;
        }
    }

    /**
     * <p>The length of the TGA identifier.  This is a <code>byte</code> in 
     * length.</p>
     */
    public int getIdLength()
    {
        return idLength;
    }

    /**
     * <p>Does this TGA have an associated color map?</p>
     */
    public boolean hasColorMap()
    {
        return hasColorMap;
    }

    /**
     * <p>Retrieves the type of image.  See the image type constants in {@link com.realityinteractive.imageio.tga.TGAConstants}
     * for allowed values.</p> 
     */
    public int getImageType()
    {
        return imageType;
    }

    /**
     * <p>Retrieves a string that represents the image type.</p>
     */
    public String getImageTypeString()
    {
        switch(imageType)
        {
            case TGAConstants.NO_IMAGE:
                return "NO IMAGE";

            case TGAConstants.COLOR_MAP:
                return "COLOR MAP";

            case TGAConstants.TRUE_COLOR:
                return "TRUE COLOR";

            case TGAConstants.MONO:
                return "MONOCHROME";

            case TGAConstants.RLE_COLOR_MAP:
                return "RLE COMPRESSED COLOR MAP";

            case TGAConstants.RLE_TRUE_COLOR:
                return "RLE COMPRESSED TRUE COLOR";

            case TGAConstants.RLE_MONO:
                return "RLE COMPRESSED MONOCHROME";

            default:
                return "UNKNOWN";
        }
    }

    /**
     * <p>Retrieves if this image is compressed.  If the image type is 
     * {@link TGAConstants#RLE_COLOR_MAP}, {@link TGAConstants#RLE_TRUE_COLOR},
     * or {@link TGAConstants#RLE_MONO} then the image is compressed.</p>
     */
    public boolean isCompressed()
    {
        return isCompressed;
    }

    /**
     * <p>Retrieves the index of the first color map entry.</p>
     */
    public int getFirstColorMapEntryIndex()
    {
        return firstColorMapEntryIndex;
    }

    /**
     * <p>Retrieves ttotal number of color map entries.</p>
     */
    public int getColorMapLength()
    {
        return numberColorMapEntries;
    }

    /**
     * <p>Retrieves the number of bits per color map entry.</p>
     */
    public int getBitsPerColorMapEntry()
    {
        return bitsPerColorMapEntry;
    }

    /**
     * <p>Retrieves the horizontal coordinate for the lower-left corner of the 
     * image.</p>
     */
    public int getXOrigin()
    {
        return xOrigin;
    }

    /**
     * <p>Retrieves the vertical coordinate for the lower-left corner of the image.</p>
     */
    public int getYOrigin()
    {
        return yOrigin;
    }

    /**
     * <p>Retrieves the width of the image in pixels.</p>
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * <p>Retrieves the height of the image in pixels.</p>
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * <p>Retrieves the number of bits per pixel.</p>
     */
    public int getBitsPerPixel()
    {
        return bitsPerPixel;
    }

    /**
     * <p>Retrieves the number of samples per pixel.</p>
     */
    public int getSamplesPerPixel()
    {
        // FIXME:  this is overly simplistic but it is accurate
        return (bitsPerPixel == 32) ? 4 : 3;
    }

    /**
     * <p>Retrieves the number of attribute bits per pixel.</p>
     */
    public int getImageDescriptor()
    {
        return imageDescriptor;
    }

    /**
     * <p>Returns if this image is left-to-right (<code>true</code>) or right-
     * to-left (<code>false</code>).</p>
     */
    public boolean isLeftToRight()
    {
        return leftToRight;
    }

    /**
     * <p>Returns if this image is bottom-to-top (<code>true</code>) or top-to-
     * bottom (<code>false</code>).</p>
     */
    public boolean isBottomToTop()
    {
        return bottomToTop;
    }

    /**
     * <p>Retrieves the offset to the color map data.  If there is no color 
     * map ({@link #hasColorMap()} returns <code>false</code>) then this is
     * undefined.</p>
     */
    public int getColorMapDataOffset()
    {
        return colorMapDataOffset;
    }

    /**
     * <p>Retrieves the offset to the pixel data.</p>
     */
    public int getPixelDataOffset()
    {
        return pixelDataOffset;
    }

    // =========================================================================
    /**
     * <p>Retrieves a string useful for debugging.</p>
     * 
     * @return a string useful for debugging
     */
    public String debugString()
    {
        return "TGAHeader[" +
                          "type=" + getImageTypeString() + ", " +
                          (hasColorMap ?
                              ("firstColorMapEntryIndex=" + firstColorMapEntryIndex + ", " +
                               "numberColorMapEntries=" + numberColorMapEntries + ", " +
                               "bitsPerColorMapEntry=" + bitsPerColorMapEntry + ", " +
                               "totalColorMapEntrySize=" + colorMapSize + ", " ) :
                              "") +
                          "isCompressed=" + isCompressed + ", " + 
                          "xOrigin=" + xOrigin + ", " +
                          "yOrigin=" + yOrigin + ", " +
                          "width=" + width + ", " + 
                          "height=" + height + ", " + 
                          "bitsPerPixel=" + bitsPerPixel +  ", " + 
                          "samplesPerPixel=" + getSamplesPerPixel() +
                         "]";
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