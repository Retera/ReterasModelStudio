package com.hiveworkshop.rms.ui.util.colorchooser;


import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class CharIcon extends ImageIcon {
	private boolean doPrint = false;
	private int arc = 18;
	private BufferedImage image;
	private int[] pixels;
	private Color borderColor;
	private Color fgColor;
	private Color bgColor;
	private String text;
	private int size;
	private int imageCreationSize;
	Font font;

	public CharIcon(final String text, int arc, final Color color, int size) {
		this(text, arc, Color.BLACK, color, size);
	}
	public CharIcon(final String text, int arc, final Color color, int size, int margin) {
		this(text, arc, Color.BLACK, color, size, margin);
	}
	public CharIcon(final String text, int arc, final Color fgColor, final Color bgColor, int size) {
		this(text, arc, fgColor, bgColor, size, 2);
	}
	public CharIcon(final String text, int arc, final Color fgColor, final Color bgColor, int size, int margin) {
		this.text = text;
		this.fgColor = fgColor;
		this.borderColor = fgColor;
		this.bgColor = bgColor;
		this.size = size;
		this.imageCreationSize = size;

		font = new JLabel().getFont().deriveFont((float) (size-2*margin));
//		font = new JLabel().getFont();
		this.arc = arc;
		pixels = new int[imageCreationSize*imageCreationSize];
		Arrays.fill(pixels, 0);
		BufferedImage image = paintImage();
		setImage(image);
	}


	public static void main(final String[] args) {
		JPanel panel = new JPanel(new MigLayout(""));
		// 👁🤚

		Color c1 = new Color(200, 255, 255);
		Color c2 = new Color(220, 180, 255);

		panel.add(new JLabel(new CharIcon("\uD83D\uDC41", 18, c1,  18)));
		panel.add(new JLabel(new CharIcon("\uD83E\uDD1A", 7, c2,  14)));
		panel.add(new JLabel("Geoset 1"), "wrap");


		panel.add(new JLabel(" Stand -1", new CharIcon("@", 18, c1,  18), JLabel.LEADING), "wrap");
		panel.add(new JLabel(" Stand -1", new CharIcon("+", 18, c1,  18), JLabel.LEADING), "wrap");

		panel.add(new JLabel(" Stand -1", new CharIcon("@", 7, c1,  14), JLabel.LEADING), "wrap");
		panel.add(new JLabel(" Stand -1", new CharIcon("+", 7, c1,  14), JLabel.LEADING), "wrap");

		panel.add(new JLabel(" Stand -1", new CharIcon("#", 7, c1,  14), JLabel.LEADING), "wrap");
		panel.add(new JLabel(" Stand -1", new CharIcon("&", 7, c1,  14), JLabel.LEADING), "wrap");

		panel.add(new JLabel(" Stand -1", new CharIcon("#", 7, c1,  16, 2), JLabel.LEADING), "wrap");
		panel.add(new JLabel(" Stand -1", new CharIcon("&", 7, c1,  16, 2), JLabel.LEADING), "wrap");
//		JOptionPane.showMessageDialog(null, panel);


		JPanel panel1 = new JPanel(new MigLayout("fill", "[center]", "[center]"));
		panel1.add(panel);
		JFrame frame = new JFrame("Test labels");
		frame.setContentPane(panel1);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(300, 100);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}



	public CharIcon setFgColor(Color fgColor) {
		this.fgColor = fgColor;
		return this;
	}

	public CharIcon setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
		return this;
	}

	public CharIcon setBgColor(Color bgColor) {
		this.bgColor = bgColor;
		return this;
	}

	public CharIcon setArc(int arc) {
		this.arc = arc;
		return this;
	}

	public CharIcon updateIcon(){
		BufferedImage image = paintImage();
		setImage(image);
		return this;
	}


	private BufferedImage paintImage() {
		BufferedImage image = new BufferedImage(imageCreationSize, imageCreationSize, BufferedImage.TYPE_INT_ARGB);
		int width = image.getWidth();
		int height = image.getHeight();
		// set transparent
		image.setRGB(0,0, width, height, pixels, 0, width);

		Graphics2D g = (Graphics2D) image.getGraphics();
//		g.setStroke(new BasicStroke(2));

		// fill icon
		g.setColor(bgColor);
		g.fillRoundRect(0, 0, width-1, height-1, arc+1, arc+1);


		// draw border
//		g.setStroke(new BasicStroke(1));
		g.setColor(borderColor);
		g.drawRoundRect(0, 0, width-1, height-1, arc, arc);

		// draw char
		g.setColor(bgColor.darker());
		Font font = g.getFont().deriveFont(Font.BOLD, (int) (this.font.getSize()*1.2));
		g.setFont(font);
		Rectangle2D stringBounds = font.getStringBounds(text, g.getFontRenderContext());
		float descent = font.getLineMetrics(text, g.getFontRenderContext()).getDescent();
		int textX = (int) ((width-stringBounds.getWidth())/2.0);
		int textY = (int) ((height + stringBounds.getHeight())/2.0-descent);

		g.drawString(text, textX, textY);
		Font font2 = font.deriveFont(Font.PLAIN, (int) (font.getSize()));
		g.setFont(font2);
		g.setColor(fgColor);
		print(textX + ", " + textY);
		g.drawString(text, textX, textY);
		g.dispose();
		return image;
	}

	private void print(String s){
		if(doPrint){
			System.out.println(s);
		}

	}

	@Override
	public int getIconWidth() {
		return imageCreationSize;
	}

	@Override
	public int getIconHeight() {
		return imageCreationSize;
	}
}
