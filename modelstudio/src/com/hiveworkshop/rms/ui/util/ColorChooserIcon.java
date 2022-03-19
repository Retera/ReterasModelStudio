package com.hiveworkshop.rms.ui.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;

public final class ColorChooserIcon extends JLabel {
	private Color currentColor;
	private final ColorListener colorListener;
	private static final JColorChooser colorChooser = new JColorChooser();

	public ColorChooserIcon(final Color color, final ColorListener colorListener) {
		currentColor = color;
		this.colorListener = colorListener;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				colorChooser.setColor(currentColor);
				colorChooser.setPreviewPanel(getLatestColorsPanel());
				int option = JOptionPane.showConfirmDialog(ColorChooserIcon.this, colorChooser, "Choose Background Color", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
				if (option == JOptionPane.OK_OPTION && colorChooser.getColor() != null) {
					setCurrentColor(colorChooser.getColor());
				}
//				final Color chosenColor = JColorChooser.showDialog(ColorChooserIcon.this, "Choose Background Color",
//						currentColor);
//				if (chosenColor != null) {
//					setCurrentColor(chosenColor);
//				}
			}
		});


		ChangeListener changeListener = e -> {
			selectedColor = colorChooser.getColor();
			oldColorLabel.setBackground(currentColor);
			newColorLabel.setBackground(selectedColor);
			colorChooser.getSelectionModel().setSelectedColor(selectedColor);
			if (!mouseDown) {
				addColorToLatest(selectedColor);
				if (colorSwatchesPanel != null) {
					colorSwatchesPanel.removeAll();
					fillColorSwatchesPanel();
					colorSwatchesPanel.revalidate();
					colorSwatchesPanel.repaint();
				}
			}

//			colorChooser.setPreviewPanel(getLatestColorsPanel());
//			colorChooser.repaint();
		};

		colorChooser.getSelectionModel().addChangeListener(changeListener);
		MouseAdapter l = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				mouseDown = true;
				System.out.println("mouse pressed");
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
				addColorToLatest(selectedColor);
				System.out.println("mouse released");
			}
		};
		colorChooser.addMouseListener(l);
		for (AbstractColorChooserPanel ugg : colorChooser.getChooserPanels()) {
			ugg.addMouseListener(l);
			for (int i = 0; i < ugg.getComponentCount(); i++) {
				Component component = ugg.getComponent(i);
				component.addMouseListener(l);
			}
		}

		setMinimumSize(new Dimension(24, 24));
		setPreferredSize(new Dimension(24, 24));
		setMaximumSize(new Dimension(24, 24));
	}

	public void setCurrentColor(final Color currentColor) {
		this.currentColor = currentColor;
		colorListener.colorChanged(currentColor);
		repaint();
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(currentColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	public static void main(final String[] args) {
		final ColorChooserIcon chooser = new ColorChooserIcon(Color.YELLOW, System.out::println);
		chooser.setCurrentColor(Color.ORANGE);
		JOptionPane.showMessageDialog(null, chooser);
	}

	public interface ColorListener {
		void colorChanged(Color color);
	}


	private boolean mouseDown = false;
	private static ArrayDeque<Color> colors = new ArrayDeque<>();
	private static int numSaved = 20;
	int size = 40;
	int smallSize = size / 2;
	String sizeString = "[" + size + ":" + size + ":" + size + "]";
	String smallSizeString = "[" + smallSize + ":" + smallSize + ":" + smallSize + "]";
	private MigLayout colorSwatchLayout = new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]");

	JPanel colorSwatchesPanel;

	private Color selectedColor;
	JPanel oldColorLabel = new JPanel(new MigLayout("ins 0, gap 0, fill"));
	JPanel newColorLabel = new JPanel(new MigLayout("ins 0, gap 0, fill"));

	private JPanel getLatestColorsPanel() {
		JPanel colorPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]", "[][]"));

		oldColorLabel.setOpaque(true);
		oldColorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				colorChooser.setColor(oldColorLabel.getBackground());
			}
		});
		newColorLabel.setOpaque(true);

		JPanel currentSwatchesPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", sizeString, sizeString));
		currentSwatchesPanel.add(oldColorLabel, "grow");
		currentSwatchesPanel.add(newColorLabel, "grow");

		colorPanel.add(currentSwatchesPanel, "align center, wrap");

		colorSwatchesPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", smallSizeString, smallSizeString));
		fillColorSwatchesPanel();

		colorPanel.add(colorSwatchesPanel);

		return colorPanel;
	}

	private void fillColorSwatchesPanel() {
		for (Color color : colors) {
			colorSwatchesPanel.add(getColorSwatch(color), "grow");
		}
	}


	private JPanel getColorSwatch(Color color) {
		JPanel colorSwatch = new JPanel(colorSwatchLayout);
		colorSwatch.setBackground(color);
		colorSwatch.setOpaque(true);
		colorSwatch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				colorChooser.setColor(colorSwatch.getBackground());
			}
		});
		return colorSwatch;
	}


	private void addColorToLatest(Color color) {
		colors.remove(color);
		colors.add(color);
		if (colors.size() > numSaved) {
			colors.remove();
		}
	}
}
