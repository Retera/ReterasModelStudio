package com.realityinteractive.imageio.tga;

/*
 * TGAImageMetadata.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 27, 2003
 */

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * <p>The image metadata for a TGA image type.  At this time there are no
 * elements in the format (i.e. {@link javax.imageio.metadata.IIOMetadataFormat#canNodeAppear(java.lang.String, javax.imageio.ImageTypeSpecifier)}
 * always returns <code>false</code>).</p>
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAImageMetadata.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
// NOTE:  this is currently unused
public class TGAImageMetadata extends IIOMetadata
{
    // =========================================================================
    /**
     * @see javax.imageio.metadata.IIOMetadata#IIOMetadata()
     */
    public TGAImageMetadata()
    {
        super(TGAImageReaderSpi.SUPPORTS_STANDARD_IMAGE_METADATA_FORMAT,
              TGAImageReaderSpi.NATIVE_IMAGE_METADATA_FORMAT_NAME,
              TGAImageReaderSpi.NATIVE_IMAGE_METADATA_FORMAT_CLASSNAME,
              TGAImageReaderSpi.EXTRA_IMAGE_METADATA_FORMAT_NAMES,
              TGAImageReaderSpi.EXTRA_IMAGE_METADATA_FORMAT_CLASSNAMES);
    }

    /**
     * <p>Ensure that the specified format name is supported by this metadata.
     * If the format is not supported {@link java.lang.IllegalArgumentException}
     * is thrown.</p>
     * 
     * @param  formatName the name of the metadata format that is to be validated 
     */
    private void checkFormatName(final String formatName)
    {
        // if the format name is not known, throw an exception
        if(!TGAImageReaderSpi.NATIVE_IMAGE_METADATA_FORMAT_NAME.equals(formatName))
        {
            throw new IllegalArgumentException("Unknown image metadata format name \"" + formatName + "\"."); // FIXME:  localize
        } /* else -- the format name is valid */
    }

    /**
     * @see javax.imageio.metadata.IIOMetadata#getAsTree(java.lang.String)
     */
    public Node getAsTree(final String formatName)
    {
        // validate the format name (this will throw if invalid)
        checkFormatName(formatName);

        // create and return a root node
        // NOTE:  there are no children at this time
        final IIOMetadataNode root = new IIOMetadataNode(TGAImageReaderSpi.NATIVE_IMAGE_METADATA_FORMAT_NAME);

        return root;
    }

    /**
     * @see javax.imageio.metadata.IIOMetadata#getMetadataFormat(java.lang.String)
     */
    public IIOMetadataFormat getMetadataFormat(final String formatName)
    {
        // validate the format name (this will throw if invalid)
        checkFormatName(formatName);

        // return the metadata format
        return TGAImageMetadataFormat.getInstance();
    }

    /**
     * <p>This is read-only metadata.</p>
     * 
     * @see javax.imageio.metadata.IIOMetadata#isReadOnly()
     */
    public boolean isReadOnly()
    {
        // see javadoc
        return true;
    }

    /**
     * @see javax.imageio.metadata.IIOMetadata#mergeTree(java.lang.String, org.w3c.dom.Node)
     */
    public void mergeTree(final String formatName, final Node root)
        throws IIOInvalidTreeException
    {
        // validate the format name (this will throw if invalid)
        checkFormatName(formatName);

        // since there are no elements in the tree, there is nothing to merge
    }

    /**
     * @see javax.imageio.metadata.IIOMetadata#reset()
     */
    public void reset()
    {
        // NOTE:  nothing to do since there are no elements
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