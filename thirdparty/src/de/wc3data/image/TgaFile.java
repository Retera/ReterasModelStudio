/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.wc3data.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;

/**
 *
 * @author Riven, modified by Oger-Lord
 */
public class TgaFile {

    /**
     * Read a TGA image from a file
     */
    public static BufferedImage readTGA(File file) throws IOException{
        return readTGA(file.getName(),new FileInputStream(file));

    }
    
    /**
     * Read a TGA image from an input stream.
     */
    public static BufferedImage readTGA(String name, InputStream stream) throws IOException {

        //Read Header
        byte[] header = new byte[18];
        stream.read(header);

        byte idFieldLength = header[0];
        boolean hasColorMap = header[1] == 1;


        // header[3-7] - Color map specification

        // header[8-17] - Image specification
        // header[8-9] - X-origin
        // header[10-11] - Y-origin
        // header[12-13] - Image width
        // header[14-15] - Image height
        // header[16] - Pixel depth
        // header[17] - Image descriptor (1 byte): bits 3-0 give the alpha channel depth, bits 5-4 give direction

        int w = 0, h = 0;
        w |= (header[12] & 0xFF) << 0;
        w |= (header[13] & 0xFF) << 8;
        h |= (header[14] & 0xFF) << 0;
        h |= (header[15] & 0xFF) << 8;

        byte pixelDepth = header[16];

        //Read pixel data
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = stream.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
        buffer.flush();
        data = buffer.toByteArray();

        
        // Verify Header
        // header[0] - Length of the image ID field (0-255)
        // header[1] - Whether a color map is included (0/1)
        if ((header[0] | header[1]) != 0) {
            throw new IllegalStateException("Error " + name + ", " + (header[0] | header[1]) +" != " + 0);
        }
        // header[2] - Compression and color types (0- No Image Data Included. 1- Uncompressed, Color mapped image, 2- Uncompressed, True Color Image, 9- Run-length encoded, Color mapped image, 11- Run-Length encoded, Black and white image)
        if (header[2] != 2) {
            throw new IllegalStateException("Error " + name + ", header[2] = " + header[2]);
        }

        boolean alpha;
        if((header[16] == 24)){
            alpha = false;
        }else if(header[16] == 32){
            alpha=true;
        }else{
             throw new IllegalStateException("Error " + name + " invalid pixel depth: " + header[16] );
        }

        if ((header[17] & 15) != (alpha ? 8 : 0)) {
            throw new IllegalStateException("Error " + name + " (header[17] & 15) != " + (alpha ? 8 : 0));
        }

        
        BufferedImage dst = new BufferedImage(w, h, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        int[] pixels = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
        if (pixels.length != w * h) {
            throw new IllegalStateException("Error " + name + " number of pixels does no correspond to imaga size: " + pixels.length + " != " + (w * h));
        }
        if (data.length < pixels.length * (alpha ? 4 : 3)) {
            throw new IllegalStateException("Error " + name + " not enaugh pixel data");
        }

        int pixelBytes = pixelDepth/8;
        int p1 = (pixels.length - 1) * pixelBytes;
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j<pixelBytes; j++){
                pixels[i] |= ((data[p1 + j]) & 0xFF) << (8*j);
            }
            p1 -= pixelBytes;
        }
//        if (alpha) {
//            for (int i = 0, p = (pixels.length - 1) * 4; i < pixels.length; i++, p -= 4) {
//                pixels[i] |= ((data[p + 0]) & 0xFF) << 0;
//                pixels[i] |= ((data[p + 1]) & 0xFF) << 8;
//                pixels[i] |= ((data[p + 2]) & 0xFF) << 16;
//                pixels[i] |= ((data[p + 3]) & 0xFF) << 24;
//            }
//        } else {
//            for (int i = 0, p = (pixels.length - 1) * 3; i < pixels.length; i++, p -= 3) {
//                pixels[i] |= ((data[p + 0]) & 0xFF) << 0;
//                pixels[i] |= ((data[p + 1]) & 0xFF) << 8;
//                pixels[i] |= ((data[p + 2]) & 0xFF) << 16;
//            }
//        }

        if ((header[17] >> 4) == 1) {
            // ok
        } else if ((header[17] >> 4) == 0) {
            // flip horizontally

            for (int y = 0; y < h; y++) {
                int w2 = w / 2;
                for (int x = 0; x < w2; x++) {
                    int a = (y * w) + x;
                    int b = (y * w) + (w - 1 - x);
                    int t = pixels[a];
                    pixels[a] = pixels[b];
                    pixels[b] = t;
                }
            }
        } else {
            System.out.println("header[17]=" + header[17]);
            System.out.println("header[17]= >> 4" + (header[17] >> 4));
//            throw new UnsupportedOperationException("Error " + name);
        }

        return dst;
    }

    /**
     * Write a BufferedImage to a TGA file
     * BufferedImages should be TYPE_INT_ARGB or TYPE_INT_RGB
     */
    public static void writeTGA(BufferedImage src, File file) throws IOException {
        
         boolean alpha = src.getColorModel().hasAlpha();
        src = ImageUtils.convertStandardImageType(src, alpha);
        
        DataBuffer buffer = src.getRaster().getDataBuffer();
        byte[] data;

        if (buffer instanceof DataBufferByte) {

            //Not used anymore because convert to standard image type => Buffer is int
            byte[] pixels = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
            if (pixels.length != src.getWidth() * src.getHeight() * (alpha ? 4 : 3)) {
                throw new IllegalStateException();
            }

            data = pixels;
            
        } else if (buffer instanceof DataBufferInt) {

            int[] pixels = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
            if (pixels.length != src.getWidth() * src.getHeight()) {
                throw new IllegalStateException();
            }

            data = new byte[pixels.length * (alpha ? 4 : 3)];

            if (alpha) {

                for (int p = 0; p < pixels.length; p++) {
                    int i = p*4;
                    data[i + 0] = (byte) ((pixels[p] >> 0) & 0xFF);
                    data[i + 1] = (byte) ((pixels[p] >> 8) & 0xFF);
                    data[i + 2] = (byte) ((pixels[p] >> 16) & 0xFF);
                    data[i + 3] = (byte) ((pixels[p] >> 24) & 0xFF);
                }
            } else {

                for (int p = 0; p < pixels.length; p++) {
                    int i = p*3;
                    data[i + 0] = (byte) ((pixels[p] >> 0) & 0xFF);
                    data[i + 1] = (byte) ((pixels[p] >> 8) & 0xFF);
                    data[i + 2] = (byte) ((pixels[p] >> 16) & 0xFF);
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }

        byte[] header = new byte[18];
        header[2] = 2; // uncompressed, true-color image
        header[12] = (byte) ((src.getWidth() >> 0) & 0xFF);
        header[13] = (byte) ((src.getWidth() >> 8) & 0xFF);
        header[14] = (byte) ((src.getHeight() >> 0) & 0xFF);
        header[15] = (byte) ((src.getHeight() >> 8) & 0xFF);
        header[16] = (byte) (alpha ? 32 : 24); // bits per pixel
        header[17] = (byte) ((alpha ? 8 : 0) | (2 << 4));

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.write(header);
        raf.write(data);
        raf.setLength(raf.getFilePointer()); // trim
        raf.close();
    }
}
