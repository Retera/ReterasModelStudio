package com.realityinteractive.imageio.tga;

/*
 * TGAConstants.java
 * Copyright (c) 2003 Reality Interactive, Inc.  
 *   See bottom of file for license and warranty information.
 * Created on Sep 26, 2003
 */

/**
 * <p>Various header and such constants for the TGA image format.</p> 
 * 
 * @author Rob Grzywinski <a href="mailto:rgrzywinski@realityinteractive.com">rgrzywinski@realityinteractive.com</a>
 * @version $Id: TGAConstants.java,v 1.1 2005/04/12 11:23:53 ornedan Exp $
 * @since 1.0
 */
public interface TGAConstants
{
    // =========================================================================
    // image types
    /**
     * <p>An image type indicating no image data.</p>
     */
    int NO_IMAGE = 0;

    /**
     * <p>An image type indicating an uncompressed color mapped (indexed) image.</p>
     */
    int COLOR_MAP = 1;

    /**
     * <p>An image type indicating an uncompressed true-color image.</p>
     */
    int TRUE_COLOR = 2;

    /**
     * <p>An image type indicating a black and white (monochrome) image.</p>
     */
    int MONO = 3;

    /**
     * <p>An image type indicating an RLE (run-length encoded) color-mapped
     * (indexed) image.</p>
     */
    int RLE_COLOR_MAP = 9;

    /**
     * <p>An image type indicating an RLE (run-length encoded) true-color
     * image.</p>
     */
    int RLE_TRUE_COLOR = 10;

    /**
     * <p>An image type indicating an RLE (run-length encoded) black and white
     * (monochrome) image.</p>
     */
    int RLE_MONO = 11;

    // =========================================================================
    // Image descriptor bit
    /**
     * <p>The bit of the image descriptor field (5.5) indicating that the first
     * pixel should be at the left or the right.</p>
     */
    int LEFT_RIGHT_BIT = 0x10;

    /**
     * <p>The bit of the image descriptor field (5.5) indicating that the first
     * pixel should be at the bottom or the top.</p>
     */
    int BOTTOM_TOP_BIT = 0x20;
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