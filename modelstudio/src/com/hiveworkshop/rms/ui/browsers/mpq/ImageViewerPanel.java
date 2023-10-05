package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.ImageUtils;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.ImageUtils.GU;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewerPanel extends JPanel {
	private ZoomableImagePreviewPanel comp;
	private String selectedPath;
	private ImageUtils.ColorMode colorMode = ImageUtils.ColorMode.RGBA;

	public ImageViewerPanel() {
		super(new MigLayout("fill", "[]", "[grow][]"));
		add(getImageViewerPanel(), "growx, growy, wrap");

		TwiComboBox<ImageUtils.ColorMode> colorModeBox = getColorModeBox();
		add(colorModeBox);
	}

	private JPanel getImageViewerPanel() {
		JPanel imageViewerPanel = new JPanel();
		imageViewerPanel.setBorder(new TitledBorder(null, "Image Viewer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		imageViewerPanel.setLayout(new BorderLayout());
		comp = new ZoomableImagePreviewPanel(null);
		imageViewerPanel.add(comp);
		return imageViewerPanel;
	}


	private TwiComboBox<ImageUtils.ColorMode> getColorModeBox() {
		TwiComboBox<ImageUtils.ColorMode> colorModeGroup = new TwiComboBox<>(ImageUtils.ColorMode.values(), ImageUtils.ColorMode.GREEN_GREEN);
		colorModeGroup.addMouseWheelListener(e -> colorModeGroup.incIndex(e.getWheelRotation()));
		colorModeGroup.addOnSelectItemListener(this::setColorMode);
		colorModeGroup.selectOrFirst(ImageUtils.ColorMode.RGBA);
		return colorModeGroup;
	}
	private void setColorMode(ImageUtils.ColorMode colorMode) {
		this.colorMode = colorMode;
		loadBitmap(selectedPath);
	}

	public ImageViewerPanel setSelectedPath(String selectedPath) {
		this.selectedPath = selectedPath;
		loadBitmap(selectedPath);
		return this;
	}

	private void loadBitmap(String filepath) {
		if (filepath != null) {
			BufferedImage image = getImage(filepath);
			comp.setImage(image);
			comp.resetZoom();
			comp.revalidate();
			comp.repaint();
		}
	}


	private BufferedImage getImage(String filepath) {
		BufferedImage texture = BLPHandler.getImage(filepath);
		if (texture != null) {
			if (colorMode == ImageUtils.ColorMode.RGBA) {
				return texture;
			} else {
				return ImageUtils.getBufferedImageIsolateChannel(texture, colorMode);
			}
		} else {
			int imageSize = 128;
			final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.BLACK);
			int size = imageSize-6;
			GU.drawCenteredSquare(g2, imageSize/2, imageSize/2, size);
			int dist1 = (imageSize - size)/2;
			int dist2 = imageSize-dist1;
			GU.drawLines(g2, dist1, dist1, dist2, dist2, dist1, dist2, dist2, dist1);
//			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 15, 15);
			return image;
		}
	}

}
