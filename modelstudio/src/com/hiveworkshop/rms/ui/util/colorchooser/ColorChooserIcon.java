package com.hiveworkshop.rms.ui.util.colorchooser;

import com.hiveworkshop.rms.util.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class ColorChooserIcon extends ImageIcon {
	private int arc = 0;
	private final BufferedImage image;
	private final BufferedImage checkerImage;
	private final int[] pixels;
	private RoundRectangle2D.Float roundRect;
	private Rectangle rectangle;
	private Color borderColor = Color.BLACK;
	private Color color;

	public ColorChooserIcon(final Color color, int width, int height) {
		this.color = color;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		checkerImage = getCheckerImage(width, height);
		pixels = new int[width*height];
		Arrays.fill(pixels, 0);
		setArc(0);
//		paintImage(color);
//		roundRect = new RoundRectangle2D.Float(0, 0, width-1, height-1, arc+1, arc+1);
//		rectangle = new Rectangle(0, 0, width, height);
		setImage(image);

	}

	public BufferedImage getCheckerImage(int width, int height){
		int squareSize = 5;
		int[] pixels = new int[width*height+squareSize];
		Arrays.fill(pixels, MathUtils.uint8ToUint32((byte) 255, (byte) 192, (byte) 192, (byte) 192));
//		int color = MathUtils.uint8ToUint32((byte) 255, (byte) 64, (byte) 64, (byte) 64);
		int color = MathUtils.uint8ToUint32((byte) 255, (byte) 127, (byte) 127, (byte) 127);
		for (int h = 0; h < height; h++){
			int hPix = h*width;
			int wStart = squareSize - (h % (squareSize * 2)) <= 0 ? squareSize : 0;
			for (int w = wStart; w < width; w += (squareSize * 2)){
				Arrays.fill(pixels, hPix + w, hPix + w + squareSize, color);
			}
		}

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		bufferedImage.setRGB(0,0, width, height, pixels, 0, width);
		return bufferedImage;
	}

	public void setCurrentColor(final Color currentColor) {
		color = currentColor;
		paintImage(currentColor);
	}

	public ColorChooserIcon setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		return this;
	}

	public ColorChooserIcon setArc(int arc) {
		this.arc = arc;

		int width = image.getWidth();
		int height = image.getHeight();
		roundRect = new RoundRectangle2D.Float(0, 0, width-1, height-1, arc+1, arc+1);
		rectangle = new Rectangle(0, 0, width, height);
		paintImage(color);
		return this;
	}


	private void paintImage(Color color) {
		int width = image.getWidth();
		int height = image.getHeight();
		image.setRGB(0,0, width, height, pixels, 0, width);

		Graphics g = image.getGraphics();
		if(color.getAlpha()<255){
			g.setClip(roundRect);
			g.drawImage(checkerImage, 0,0, null);
			g.setClip(rectangle);
		}


		g.setColor(color);
		g.fillRoundRect(0, 0, width-1, height-1, arc+1, arc+1);
		g.setColor(borderColor);
		g.drawRoundRect(0, 0, width-1, height-1, arc, arc);
		g.dispose();
	}
}
