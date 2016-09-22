/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.owens.oobjloader.lwjgl;

import static java.util.logging.Level.SEVERE;

// This code was written by myself, Sean R. Owens, sean at guild dot net,
// and is released to the public domain. Share and enjoy. Since some
// people argue that it is impossible to release software to the public
// domain, you are also free to use this code under any version of the
// GPL, LPGL, Apache, or BSD licenses, or contact me for use of another
// license.  (I generally don't care so I'll almost certainly say yes.)
// In addition this code may also be used under the "unlicense" described
// at http://unlicense.org/ .  See the file UNLICENSE in the repo.

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 *
 * @author sean
 */
public class TextureLoader {
    private Logger log = Logger.getLogger(TextureLoader.class.getName());

    private final static int TEXTURE_LEVEL = 0;
    private HashMap<String, Integer> loadedTextures = new HashMap<String, Integer>();

    public int convertToTexture(BufferedImage img) {
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        PixelGrabber grabber = new PixelGrabber(img, 0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
        try {
            grabber.grabPixels();
        } catch (InterruptedException e) {
            log.log(SEVERE, "InterruptedException while trying to grab pixels, e=" + e);
            e.printStackTrace();
            return -1;
        }

        int bufLen = 0;
        bufLen = pixels.length * 4;

        ByteBuffer oglPixelBuf = BufferUtils.createByteBuffer(bufLen);

        for (int y = img.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = pixels[y * img.getWidth() + x];
                oglPixelBuf.put((byte) ((pixel >> 16) & 0xFF));
                oglPixelBuf.put((byte) ((pixel >> 8) & 0xFF));
                oglPixelBuf.put((byte) ((pixel >> 0) & 0xFF));
                oglPixelBuf.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        oglPixelBuf.flip();

        ByteBuffer temp = ByteBuffer.allocateDirect(4);
        temp.order(ByteOrder.nativeOrder());
        IntBuffer textBuf = temp.asIntBuffer();
        GL11.glGenTextures(textBuf);
        int textureID = textBuf.get(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,
                TEXTURE_LEVEL,
                GL11.GL_RGBA8,
                img.getWidth(),
                img.getHeight(),
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                oglPixelBuf);

        return textureID;
    }

    public int load(String filename) throws IOException {
        if (loadedTextures.containsKey(filename)) {
            return loadedTextures.get(filename);
        }

        File imageFile = new File(filename);

        if (!imageFile.exists()) {
            log.log(SEVERE, "FIle " + filename + " does not exist");
            return 0;
        }
        if (!imageFile.canRead()) {
            log.log(SEVERE, "FIle " + filename + " is not readable");
            return 0;
        }

        BufferedImage img = null;
        img = ImageIO.read(imageFile);
        return convertToTexture(img);
    }
}
