package com.realityinteractive.imageio.tga;

/*
 * TGAImageReaderSpi.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 26, 2003
 */

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * <p>A service provider for reading TGA images.  Only {@link javax.imageio.stream.ImageInputStream}
 * input types are allowed.  See {@link com.realityinteractive.imageio.tga.TGAImageReader}
 * for supported features.</p>
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAImageReaderSpi.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
public class TGAImageReaderSpi extends ImageReaderSpi
{
    // =========================================================================
    // NOTE:  these should be package (default protected) as it is used 
    //        frequently elsewhere
    /**
     * <p>The vendor name:  Reality Interactive, Inc.</p>
     */
    static final String VENDOR_NAME = "Reality Interactive, Inc.";

    /**
     * <p>The plugin version number.</p>
     */
    static final String VERSION = "1.00";

    /**
     * <p>The class name for the TGA image reader.</p>
     */
    static final String READER_CLASSNAME = 
        "com.realityinteractive.imageio.tga.TGAImageReader";

    /**
     * <p>The format names.</p>
     */
    static final String[] FORMAT_NAMES = { "tga", "targa" };

    /**
     * <p>The canonical suffix names.</p>
     */
    static final String[] SUFFIXES = { "tga", "targa" };

    /**
     * <p>The supported mime types.</p>
     */
    static final String[] MIME_TYPES =
        { "application/tga", "application/x-tga", "application/x-targa", 
          "image/tga", "image/x-tga", "image/targa", "image/x-targa" };

    /**
     * <p>This is a read-only TGA plugin.</p>
     */
    static final String[] WRITER_SPI_CLASSNAMES = null;

    /**
     * <p>The standard stream metadata format is not supported.</p>
     */
    static final boolean SUPPORTS_STANDARD_STREAM_METADATA_FORMAT = false;

    /**
     * <p>There is no "native" stream metadata formats supported by the TGA 
     * plugin.</p>
     */
    static final String NATIVE_STREAM_METADATA_FORMAT_NAME = null;

    /**
     * <p>There is no "native" stream metadata formats supported by the TGA 
     * plugin.</p>
     */
    static final String NATIVE_STREAM_METADATA_FORMAT_CLASSNAME = null;

    /**
     * <p>There are no stream metadata formats other than the standard.</p>
     */
    static final String[] EXTRA_STREAM_METADATA_FORMAT_NAMES = null;

    /**
     * <p>There are no stream metadata formats other than the standard.</p>
     */
    static final String[] EXTRA_STREAM_METADATA_FORMAT_CLASSNAMES = null;

    /**
     * <p>The standard image metadata format is not supported.</p>
     */
    static final boolean SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT = false;

    /**
     * <p>There are no "native" image metadata formats supported by the TGA 
     * plugin.</p>
     */
    static final String NATIVE_IMAGE_METADATA_FORMAT_NAME = null;

    /**
     * <p>There are no "native" image metadata formats supported by the TGA 
     * plugin.</p>
     */
    static final String NATIVE_IMAGE_METADATA_FORMAT_CLASSNAME = null;

    /**
     * <p>There are no image metadata formats supported other than the standard.</p>
     */
    static final String[] EXTRA_IMAGE_METADATA_FORMAT_NAMES = null;

    /**
     * <p>There are no image metadata formats supported other than the standard.</p>
     */
    static final String[] EXTRA_IMAGE_METADATA_FORMAT_CLASSNAMES = null;

    // =========================================================================
    /**
     * <p>Constructs an {@link javax.imageio.spi.ImageReaderSpi} that accepts
     * {@link javax.imageio.stream.ImageInputStream} as its input type.</p>
     * 
     * @see javax.imageio.spi.ImageReaderSpi#ImageReaderSpi()
     */
    public TGAImageReaderSpi()
    {
        super(VENDOR_NAME, VERSION, FORMAT_NAMES, SUFFIXES, MIME_TYPES, 
              READER_CLASSNAME,
              ImageReaderSpi.STANDARD_INPUT_TYPE,
              WRITER_SPI_CLASSNAMES,
              SUPPORTS_STANDARD_STREAM_METADATA_FORMAT,
              NATIVE_STREAM_METADATA_FORMAT_NAME,
              NATIVE_STREAM_METADATA_FORMAT_CLASSNAME,
              EXTRA_STREAM_METADATA_FORMAT_NAMES,
              EXTRA_STREAM_METADATA_FORMAT_CLASSNAMES,
              SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT,
              NATIVE_IMAGE_METADATA_FORMAT_NAME,
              NATIVE_IMAGE_METADATA_FORMAT_CLASSNAME,
              EXTRA_IMAGE_METADATA_FORMAT_NAMES,
              EXTRA_IMAGE_METADATA_FORMAT_CLASSNAMES);
    }

    /**
     * @see javax.imageio.spi.ImageReaderSpi#canDecodeInput(java.lang.Object)
     */
    public boolean canDecodeInput(final Object source) 
        throws IOException
    {
        // NOTE:  the input source must be left in the same state as it started
        //        at (mark() and reset() should be used on ImageInputStream)

        // ensure that the input type is a ImageInputStream as that is all that
        // is supported
        if(!(source instanceof ImageInputStream))
            return false;
        /* else -- source is a ImageInputStream */

        // cast to ImageInputStream for convenience
        final ImageInputStream inputStream = (ImageInputStream)source;

        try
        {
            // set a mark at the current position so that the stream can be reset
            inputStream.mark();
    
            // there's no ideidentifiable header on a TGA file so a punt must 
            // occur.  This will attempt to read the image type and if it is
            // not known or allowed then false is returned.
            // NOTE:  1.0.0 only supports un/compressed true color
            inputStream.readUnsignedByte(); // idLength
            
            int colourMapType = inputStream.readUnsignedByte();
            if (colourMapType != 0 && colourMapType != 1)
            {
                return false;
            }
            
            int imageType = inputStream.readUnsignedByte();
            if( (imageType != TGAConstants.NO_IMAGE) && 
                (imageType != TGAConstants.COLOR_MAP) && 
                (imageType != TGAConstants.TRUE_COLOR) &&
                (imageType != TGAConstants.MONO) &&
                (imageType != TGAConstants.RLE_TRUE_COLOR) &&
                (imageType != TGAConstants.RLE_COLOR_MAP) &&
                (imageType != TGAConstants.RLE_MONO) ) 
            {
                return false;
            } 
            
            inputStream.skipBytes(4);
            int colourMapBits = inputStream.readUnsignedByte(); // Offset 7
            
            // Defined as being 15, 16, 24 or 32 but I saw 0 in reality.
            if (colourMapBits != 0 &&
                colourMapBits != 15 &&
                colourMapBits != 16 &&
                colourMapBits != 24 && 
                colourMapBits != 32)
            {
                return false;
            }
            
            inputStream.skipBytes(8);
            int bits = inputStream.readUnsignedByte(); // Offset 16
            if (bits != 8 && bits != 16 && bits != 24 && bits != 32)
            {
                return false;
            }
            
            /* else -- it's *possible* (though not known) that this is a TGA */

            return true;
        } finally
        {            
            // reset so that the ImageInputStream is put back where it was
            inputStream.reset();
        }
    }

    /**
     * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
     */
    public ImageReader createReaderInstance(final Object extension)
        throws IOException
    {
        // construct and return an ImageReader using this SPI
        return new TGAImageReader(this);
    }

    /**
     * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
     */
    public String getDescription(final Locale locale)
    {
        return "TGA"; // FIXME:  localize
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