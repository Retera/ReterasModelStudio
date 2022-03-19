package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class TinyToggleButton extends JButton {
	GradientPaint gPaint;
	Color onColor;
	Color offColor;
	boolean isOn = false;
	Dimension dimension;
	String text;
	Font font;

	public TinyToggleButton(String s, Color onColor, Color offColor, Consumer<Boolean> booleanConsumer) {
		super(s);
		this.text = s;
		setContentAreaFilled(false);
		this.onColor = onColor;
		this.offColor = offColor;
		int size = getFont().getSize();
		font = getFont().deriveFont(size/1.5f);
		setFont(font);
		setActive(true);
//		addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentResized(final ComponentEvent e) {
//				if (gPaint != null) {
//					gPaint = new GradientPaint(
//							new Point(0, 10), gPaint.getColor1(),
//							new Point(0, getHeight()), gPaint.getColor2(), true);
//				}
//			}
//		});
//		setMargin(new Insets(2,2,2,2));
		setMargin(new Insets(0,0,0,0));
		setIconTextGap(0);
		getInsets().set(0,0,0,0);
		addActionListener(e -> toggleOn(booleanConsumer));


	}

	@Override
	public void paintComponent(final Graphics g) {


		if(dimension == null){
//		int height = getHeight();
//		int width = getWidth()-10;
			dimension = new Dimension(Math.max(getWidth()-10, 10), getHeight());
//			System.out.println(height);
			setPreferredSize(dimension);
			setSize(dimension);
			setText("");
		}

		int height = getHeight();
		int width = getWidth();
//		setMaximumSize(dimension);
//		setMinimumSize(dimension);
		final Graphics2D g2 = (Graphics2D) g.create();
//		if (gPaint != null) {
//			g2.setPaint(gPaint);
//		}
		if(isOn){
			g2.setColor(onColor);
		} else {
			g2.setColor(offColor);
		}
		final int amt = 4;
		final int inIndent = 2;
		final int outIndent = 1;
//		g2.fillRoundRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3, amt, amt);
		g2.fillRoundRect(inIndent, inIndent, width-inIndent, height-inIndent, 1, 1);
		g2.setColor(Color.black);
//		g2.drawRoundRect(indent, indent, getWidth() - indent * 3, getHeight() - indent * 3, amt, amt);
		g2.drawRoundRect(outIndent, outIndent, width-inIndent, height-inIndent, amt, amt);
		FontMetrics fontMetrics = getFontMetrics(font);
		int ascent = fontMetrics.getAscent();
		int stringWidth = fontMetrics.stringWidth(text);

		int y = height - (height - ascent)/2;
		int x1 = (width - fontMetrics.stringWidth(text)) / 2;
		g2.drawString(text, x1, y);
		g2.dispose();
		super.paintComponent(g);
	}

	public void setColors(Color activeColor1, Color activeColor2) {
		this.onColor = activeColor1;
		this.offColor = activeColor2;
	}

	public TinyToggleButton setOn(boolean on){
		isOn = on;
		return this;
	}

	private void toggleOn(Consumer<Boolean> booleanConsumer){
		isOn = !isOn;
		booleanConsumer.accept(isOn);


		System.out.println("getBorder():" + getBounds() + ", " + getUI());
		for (int i = 0; i< getComponentCount(); i++){
			System.out.println("Comp "+ i + ":" + getComponent(i));
		}
	}

	public void setActive(boolean active){
		if(active) {
			setContentAreaFilled(false);
			gPaint = new GradientPaint(new Point(0, 10), onColor, new Point(0, getHeight()), offColor, true);
		}
		else {
			gPaint = null;
			setContentAreaFilled(false);
		}
	}
}
