package com.hiveworkshop.rms.ui.util.colorchooser;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.function.Consumer;

public class ColorTrackerPanel extends JPanel {
	private static final ArrayDeque<Color> colors = new ArrayDeque<>();
	private static int numSaved = 20;
	private int size = 40;
	private int smallSize = size / 2;
	private String sizeString = "[" + size + ":" + size + ":" + size + "]";
	private String smallSizeString = "[" + smallSize + ":" + smallSize + ":" + smallSize + "]";
	private MigLayout colorSwatchLayout = new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]");

	private final JPanel colorSwatchesPanel;

	private JPanel oldColorLabel = new JPanel(new MigLayout("ins 0, gap 0, fill"));
	private JPanel newColorLabel = new JPanel(new MigLayout("ins 0, gap 0, fill"));
	private final Consumer<Color> colorConsumer;

	public ColorTrackerPanel(Consumer<Color> colorConsumer){
		super(new MigLayout("ins 0, gap 0, fill", "[grow]", "[][]"));
		this.colorConsumer = colorConsumer;

		oldColorLabel.setOpaque(true);
		oldColorLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				colorConsumer.accept(oldColorLabel.getBackground());
			}
		});
		newColorLabel.setOpaque(true);

		JPanel currentSwatchesPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", sizeString, sizeString));
		currentSwatchesPanel.add(oldColorLabel, "grow");
		currentSwatchesPanel.add(newColorLabel, "grow");

		add(currentSwatchesPanel, "align center, wrap");

		colorSwatchesPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", smallSizeString, smallSizeString));
		fillColorSwatchesPanel();

		add(colorSwatchesPanel);
	}

	public void addAndUpdateSwatchesPanel(Color color){
		if(!color.equals(newColorLabel.getBackground())){
			addColorToLatest(color);
			newColorLabel.setBackground(color);
			if (colorSwatchesPanel != null) {
				colorSwatchesPanel.removeAll();
				fillColorSwatchesPanel();
				colorSwatchesPanel.revalidate();
				colorSwatchesPanel.repaint();
			}
		}
	}

	public void setOldColor(Color color){
		oldColorLabel.setBackground(color);
		addColorToLatest(color);
	}

	public void fillColorSwatchesPanel() {
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
				colorConsumer.accept(colorSwatch.getBackground());
			}
		});
		return colorSwatch;
	}


	public void addColorToLatest(Color color) {
		if(color != null){
			colors.remove(color);
			colors.add(color);
			if (colors.size() > numSaved) {
				colors.remove();
			}
		}
	}
}
