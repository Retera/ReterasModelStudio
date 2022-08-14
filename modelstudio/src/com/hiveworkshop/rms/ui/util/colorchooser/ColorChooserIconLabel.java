package com.hiveworkshop.rms.ui.util.colorchooser;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class ColorChooserIconLabel extends JLabel {
	private Color currentColor;
	private Color orgColor;
	private final Consumer<Color> colorListener;
	private static final ColorChooserPopup COLOR_CHOOSER_POPUP = new ColorChooserPopup();
	private ColorChooserIcon icon;

	public ColorChooserIconLabel(final Color color, final Consumer<Color> colorListener) {
		this(null, color, colorListener);
	}
	public ColorChooserIconLabel(final String text, final Color color, final Consumer<Color> colorListener) {
		currentColor = color;
		orgColor = color;
		this.colorListener = colorListener;
		this.setBackground(Color.red);
		icon = new ColorChooserIcon(color, 24, 24);
		setIcon(icon);
		setText(text);
		addMouseListener(getMouseAdapter());

		setMinimumSize(new Dimension(24, 24));
		setPreferredSize(new Dimension(24, 24));
		setMaximumSize(new Dimension(24, 24));
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				Color newColor = COLOR_CHOOSER_POPUP.getNewColor(currentColor, ColorChooserIconLabel.this);
				if(!currentColor.equals(newColor)){
					setCurrentColor(newColor);
				}
			}
		};
	}

	public void setCurrentColor(final Color currentColor) {
		this.currentColor = currentColor;
		if(currentColor != orgColor){
			icon.setBorderColor(Color.GRAY);
		} else {
			icon.setBorderColor(Color.BLACK);
		}
		icon.setCurrentColor(currentColor);
		colorListener.accept(currentColor);
		repaint();
	}

//	@Override
//	protected void paintComponent(final Graphics g) {
//		super.paintComponent(g);
//		g.setColor(currentColor);
//		g.fillRect(0, 0, getWidth(), getHeight());
//		g.setColor(Color.BLACK);
//		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
//	}

	public static void main(final String[] args) {
		final ColorChooserIconLabel chooser = new ColorChooserIconLabel(Color.YELLOW, System.out::println);
		chooser.setCurrentColor(Color.ORANGE);
		JOptionPane.showMessageDialog(null, chooser);
	}

}
