/**
 * Package for BLP service providers and their support classes.
 * <p>
 * A BLP ImageReader is provided that can load '.blp' files into BufferedImage
 * objects. The blp image format is intended as a texel source for hardware
 * accelerated graphics and so can supply a number of mipmap levels for an
 * image. Each mipmap level can be accessed as a separate image index where
 * index 0 is the full resolution image. Due to the nature of mipmaps each
 * subsequent index is half the resolution in each dimension as the previous
 * index with each dimension being at least 1 pixel in size. Each mipmap is
 * returned as a BufferedImage in an image type as close as possible to the how
 * it was stored.
 * <p>
 * The BLP ImageReader supports the reading of BLP version 0 and 1. In addition
 * to the required ImageInputStream, it also accepts File and Path objects as
 * valid input. When specifying a File or Path object as input it should
 * represent a valid path to a file with '.blp' extension.
 * <p>
 * Version 0 was only used by the 'Warcraft III: Reign of Chaos' beta which
 * ended long ago. Each mipmap level is stored in a separate file accompanying
 * the main file with a level specific file extension. Such files can only be
 * read by specifying a path to a '.blp' file on a standard compliant
 * FileSystem. Each file can have up to 100 mipmap levels in theory. The exact
 * specification and limitations of this format are unknown so is assumed to be
 * similar to version 1. Due to the obsolete nature of such images they should
 * seldom be encountered and support is only for compatibility purposes.
 * <p>
 * Version 1 is used by the release versions of 'Warcraft III'. Each file is
 * self-contained allowing them to be read like any other image file format.
 * Each file can contain up to 16 mipmap levels. Mipmap data is stored either in
 * BGR or BGRA JFIF (part of JPEG) or with an index color model. When stored as
 * JFIF the separate mipmap levels often share a common header to save space and
 * alpha component can be 8 bit. When stored by index color model a 256 color 8
 * bit BGR palate is used. A separate 1, 4 or 8 bit alpha channel can be used.
 * This version of file will be encountered fairly regularly when dealing with
 * Warcraft III modding.
 * <p>
 * Version 2 is used by 'World of Warcraft'. It extends on version 1 by adding
 * support for storing mipmaps as 32 bit colour bitmaps or with DXTC
 * compression. Reading of this version is currently not supported.
 * <p>
 * BLP specific image formating related classes are included. These are used to
 * hold and process images from/to BLP files. The classes are intended to
 * represent BLP images as accurately as possible at as low level as possible
 * rather than for speed. If using the images for composing purposes it is
 * recommended to draw or convert them into a more native optimized image
 * format. These classes give full control over image data written to BLP files.
 * Writers will still handle images in other SampleModel and ColorModel however
 * conversion losses may apply.
 * 
 * @author ImperialGood
 */
package com.hiveworkshop.blizzard.blp;