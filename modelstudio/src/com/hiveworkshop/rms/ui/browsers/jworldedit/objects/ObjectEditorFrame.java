package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import net.sf.image4j.codec.ico.ICODecoder;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ObjectEditorFrame extends JFrame {

	ObjectEditorPanel panel;

	public ObjectEditorFrame() {
		super("Object Editor");
		try {
			InputStream resourceAsStream = this.getClass().getResourceAsStream("worldedit.ico");
			System.out.println("image stream (\"this.in\"): " + resourceAsStream);
			final List<BufferedImage> images = ICODecoder.read(resourceAsStream);
			final List<BufferedImage> finalImages = new ArrayList<>();
			BufferedImage lastImage = null;
			for (final BufferedImage image : images) {
				if ((lastImage != null) && (image.getWidth() != lastImage.getWidth())) {
					finalImages.add(lastImage);
				}
				lastImage = image;
			}
			finalImages.add(lastImage);
			setIconImages(finalImages);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		setContentPane(panel = new ObjectEditorPanel());
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pack();
		setLocationRelativeTo(null);
		// setIconImage(BLPHandler.get().getGameTex(""));
	}

	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (final Exception exc) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}

		final ObjectEditorFrame frame = new ObjectEditorFrame();
		frame.setVisible(true);
		final JMenuBar menubar = new JMenuBar();
		menubar.add(new JMenu("File"));
		menubar.add(new JMenu("Edit"));
		menubar.add(new JMenu("View"));
		menubar.add(new JMenu("Module"));
		menubar.add(new JMenu("Window"));
		frame.setJMenuBar(menubar);
		frame.panel.loadHotkeys();
	}

	public void loadHotkeys() {
		panel.loadHotkeys();
	}

}
