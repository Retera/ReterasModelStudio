package com.hiveworkshop.rms.ui.icons;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class RMSIcons {
    public static Image MAIN_PROGRAM_ICON           = RMSIcons.loadProgramImage("retera.jpg");
    public static final ImageIcon MDLIcon           = new ImageIcon(loadTabImage("MDLIcon_16.png"));
    public static final ImageIcon AnimIcon          = new ImageIcon(loadTabImage("Anim.png"));
    public static final ImageIcon animIcon          = new ImageIcon(loadTabImage("anim_small.png"));
    public static final ImageIcon boneIcon          = new ImageIcon(loadTabImage("Bone_small.png"));
    public static final ImageIcon geoIcon           = new ImageIcon(loadTabImage("geo_small.png"));
    public static final ImageIcon bigGeoIcon        = new ImageIcon(loadTabImage("Geo.png"));
    public static final ImageIcon objIcon           = new ImageIcon(loadTabImage("Obj_small.png"));
    public static final ImageIcon greenIcon         = new ImageIcon(loadTabImage("Blank_small.png"));
    public static final ImageIcon redIcon           = new ImageIcon(loadTabImage("BlankRed_small.png"));
    public static final ImageIcon orangeIcon        = new ImageIcon(loadTabImage("BlankOrange_small.png"));
    public static final ImageIcon cyanIcon          = new ImageIcon(loadTabImage("BlankCyan_small.png"));
    public static final ImageIcon redXIcon          = new ImageIcon(loadImage("redX.png"));
    public static final ImageIcon greenArrowIcon    = new ImageIcon(loadImage("greenArrow.png"));
    public static final ImageIcon moveUpIcon        = new ImageIcon(loadImage("moveUp.png"));
    public static final ImageIcon moveDownIcon      = new ImageIcon(loadImage("moveDown.png"));
    public static final ImageIcon setKeyframeIcon   = new ImageIcon(loadImage("setkey.png"));
    public static final ImageIcon setTimeBoundsIcon = new ImageIcon(loadImage("setbounds.png"));
    public static final ImageIcon PLAY              = new ImageIcon(loadImage("btn_play.png"));
    public static final ImageIcon PAUSE             = new ImageIcon(loadImage("btn_pause.png"));

    public static BufferedImage getNoImage(){
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.RED);
        g2.drawPolyline(new int[]{3, 20, 20, 3, 3}, new int[]{3, 3, 20, 20, 3}, 5);
        g2.drawPolyline(new int[]{7, 16}, new int[]{7, 16}, 2);
        g2.drawPolyline(new int[]{16, 7}, new int[]{7, 16}, 2);
        g2.dispose();
        return image;
    }



    public static ImageIcon loadImageIcon(final String path) {
        BufferedImage image = loadImage(path);
        return new ImageIcon(image);
    }

    public static ImageIcon loadHiveBrowserImageIcon(final String path) {
        return loadImageIcon("HiveBrowser\\" + path);
    }

    public static ImageIcon loadToolBarImageIcon(final String path) {
        return loadImageIcon("ToolBarIcons\\" + path);
    }

    public static BufferedImage loadImage(final String path) {
        try {
            InputStream stream = GameDataFileSystem.getDefault().getResourceAsStream("UI\\Widgets\\ReteraStudio\\" + path);
            if (stream != null){
                return ImageIO.read(stream);
            } else {
                return getNoImage();
            }
        } catch (final IOException e) {
            System.err.println("Failed to read image from path: \"" + path + "\"");
            return getNoImage();
        } catch (final Exception e) {
            System.err.println("Failed to load image from path: \"" + path + "\"");
            return getNoImage();
        }
    }

    public static Image loadNodeImage(final String path) {
        return loadImage("NodeIcons\\" + path);
    }

    public static Image loadProgramImage(final String path) {
        return loadImage("ProgramIcons\\" + path);
    }

    public static Image loadDeprecatedImage(final String path) {
        return loadImage("Deprecated\\" + path);
    }

    public static Image loadTabImage(final String path) {
        return loadImage("TabIcons\\" + path);
    }
}
