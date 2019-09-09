package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.image4j.codec.ico.ICODecoder;

public class ObjectEditorFrame extends JFrame {

	ObjectEditorPanel panel;

	public ObjectEditorFrame() {
		super("Object Editor");
		try {
			final List<BufferedImage> images = ICODecoder.read(this.getClass().getResourceAsStream("worldedit.ico"));
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
		} catch (final IOException e) {
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
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			} catch (final InstantiationException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			} catch (final UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}

		final ObjectEditorFrame frame = new ObjectEditorFrame();
		frame.setVisible(true);
//		final JMenuBar menubar = new JMenuBar();
//		menubar.add(new JMenu("File"));
//		menubar.add(new JMenu("Edit"));
//		menubar.add(new JMenu("View"));
//		menubar.add(new JMenu("Module"));
//		menubar.add(new JMenu("Window"));
//		frame.setJMenuBar(menubar);
		frame.panel.loadHotkeys();
	}

	public void loadHotkeys() {
		panel.loadHotkeys();
	}

}
