package com.realityinteractive.imageio.tga;

/*
 * TGAImageMetadataFormat.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 27, 2003
 */

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

/**
 * <p>The image metadata format for a TGA image type.  At this time there are
 * no elements in the format (i.e. {@link javax.imageio.metadata.IIOMetadataFormat#canNodeAppear(java.lang.String, javax.imageio.ImageTypeSpecifier)}
 * always returns <code>false</code>).</p>
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAImageMetadataFormat.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
// NOTE:  this is currently unused
public class TGAImageMetadataFormat extends IIOMetadataFormatImpl
{
    /**
     * <p>The singleton instance of this {@linkjavax.imageio.metadata.IIOMetadataFormat}.
     * It is created lazily.</p> 
     */
    private static TGAImageMetadataFormat instance;

    // =========================================================================
    /**
     * <p>A private constructor to enforce the singleton pattern.</p>
     */
    private TGAImageMetadataFormat()
    {
        // set the name of the root document node.  The child elements may
        // repeat
        super(TGAImageReaderSpi.NATIVE_IMAGE_METADATA_FORMAT_NAME,
              CHILD_POLICY_REPEAT);
        
        // TODO:  add the full metadata
    }

    /**
     * <p>Retrieves the singleton instance of <code>TGAMetadataformat</code>.
     * The instance is created lazily.</p>
     * 
     * @return the singleton instnace 
     */
    public static synchronized TGAImageMetadataFormat getInstance()
    {
        // if the instance doesn't already exist then create it
        if(instance == null)
        {
            instance = new TGAImageMetadataFormat();
        } /* else -- there is a singleton instance */

        return instance;
    }

    // =========================================================================
    /**
     * @see javax.imageio.metadata.IIOMetadataFormat#canNodeAppear(java.lang.String, javax.imageio.ImageTypeSpecifier)
     */
    public boolean canNodeAppear(final String elementName,
                                 final ImageTypeSpecifier imageType)
    {
        // NOTE:  since there are no elements, none are allowed
        return false;
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