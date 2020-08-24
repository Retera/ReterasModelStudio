package com.hiveworkshop.rms.ui.icons;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class RMSIcons {
    public static final ImageIcon MDLIcon = new ImageIcon(loadTabImage("MDLIcon_16.png"));
    public static final ImageIcon AnimIcon = new ImageIcon(loadTabImage("Anim.png"));
    public static final ImageIcon animIcon = new ImageIcon(loadTabImage("anim_small.png"));
    public static final ImageIcon boneIcon = new ImageIcon(loadTabImage("Bone_small.png"));
    public static final ImageIcon geoIcon = new ImageIcon(loadTabImage("geo_small.png"));
    public static final ImageIcon bigGeoIcon = new ImageIcon(loadTabImage("Geo.png"));
    public static final ImageIcon objIcon = new ImageIcon(loadTabImage("Obj_small.png"));
    public static final ImageIcon greenIcon = new ImageIcon(loadTabImage("Blank_small.png"));
    public static final ImageIcon redIcon = new ImageIcon(loadTabImage("BlankRed_small.png"));
    public static final ImageIcon orangeIcon = new ImageIcon(
            loadTabImage("BlankOrange_small.png"));
    public static final ImageIcon cyanIcon = new ImageIcon(
            loadTabImage("BlankCyan_small.png"));
    public static final ImageIcon redXIcon = new ImageIcon(loadImage("redX.png"));
    public static final ImageIcon greenArrowIcon = new ImageIcon(
            loadImage("greenArrow.png"));
    public static final ImageIcon moveUpIcon = new ImageIcon(loadImage("moveUp.png"));
    public static final ImageIcon moveDownIcon = new ImageIcon(loadImage("moveDown.png"));
    public static final ImageIcon setKeyframeIcon = new ImageIcon(loadImage("setkey.png"));
    public static final ImageIcon setTimeBoundsIcon = new ImageIcon(
            loadImage("setbounds.png"));
    public static final ImageIcon PLAY = new ImageIcon(loadImage("btn_play.png"));
    public static final ImageIcon PAUSE = new ImageIcon(loadImage("btn_pause.png"));


    public static ImageIcon loadImageIcon(final String path) {
        try {
            return new ImageIcon(ImageIO.read(GameDataFileSystem.getDefault().getResourceAsStream("UI\\Widgets\\ReteraStudio\\" + path)));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageIcon loadHiveBrowserImageIcon(final String path) {
        return loadImageIcon("HiveBrowser\\" + path);
    }

    public static ImageIcon loadToolBarImageIcon(final String path) {
        return loadImageIcon("ToolBarIcons\\" + path);
    }

    public static Image loadImage(final String path) {
        try {
            return ImageIO.read(GameDataFileSystem.getDefault().getResourceAsStream("UI\\Widgets\\ReteraStudio\\" + path));
        } catch (final IOException e) {
            throw new RuntimeException(e);
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
