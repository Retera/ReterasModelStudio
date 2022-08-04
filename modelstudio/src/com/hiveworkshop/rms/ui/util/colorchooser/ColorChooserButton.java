package com.hiveworkshop.rms.ui.util.colorchooser;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class ColorChooserButton extends JButton {
	private Color currentColor;
	private final Consumer<Color> colorListener;
	private static final ColorChooserPopup COLOR_CHOOSER_POPUP = new ColorChooserPopup();
	private ColorChooserIcon icon;

	public ColorChooserButton(final Color color, final Consumer<Color> colorListener) {
		this(null, color, colorListener);
	}

	public ColorChooserButton(String text, final Color color, final Consumer<Color> colorListener) {
		super(text);
		currentColor = color;
		icon = new ColorChooserIcon(color, 24, 24);
		setIcon(icon);
		this.colorListener = colorListener;
		addMouseListener(getMouseAdapter());
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				Color newColor = COLOR_CHOOSER_POPUP.getNewColor(currentColor, ColorChooserButton.this);
				if(!currentColor.equals(newColor)){
					setCurrentColor(newColor);
				}
//				COLOR_CHOOSER_POPUP.getNewColor1(currentColor, ColorChooserButton.this, ColorChooserButton.this::setCurrentColor, ColorChooserButton.this::setCurrentColor);
			}
		};
	}

	public void setCurrentColor(final Color currentColor) {
		this.currentColor = currentColor;
		icon.setCurrentColor(currentColor);
		colorListener.accept(currentColor);
		repaint();
	}

	public ColorChooserButton setButtonText(String text){
		setText(text);
		return this;
	}
	public ColorChooserButton setIconSize(int width, int height){
		icon = new ColorChooserIcon(currentColor, width, height);
		setIcon(icon);
		return this;
	}

	public static void main(final String[] args) {
		final ColorChooserButton chooser = new ColorChooserButton("ugg", Color.YELLOW, System.out::println);
		chooser.setCurrentColor(Color.ORANGE);
		JOptionPane.showMessageDialog(null, chooser);
	}

}
