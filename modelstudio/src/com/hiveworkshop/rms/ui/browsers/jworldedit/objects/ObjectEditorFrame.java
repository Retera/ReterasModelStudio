package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
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
		setIconImages(getBufferedImages());
		System.out.println("Panelur?");
		panel = new ObjectEditorPanel();
		System.out.println("Panelur done!");
		setContentPane(panel);

		pack();
		setLocationRelativeTo(null);
		// setIconImage(BLPHandler.get().getGameTex(""));
	}

	private List<BufferedImage> getBufferedImages() {
		List<BufferedImage> finalImages = new ArrayList<>();
		try {
			if (GameDataFileSystem.getDefault().has("UI\\worldedit.ico")) {
				InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream("UI\\worldedit.ico");
//				InputStream resourceAsStream = this.getClass().getResourceAsStream("worldedit.ico");
				System.out.println("image stream (\"this.in\"): " + resourceAsStream);

				List<BufferedImage> images = ICODecoder.read(resourceAsStream);
				BufferedImage lastImage = null;
				for (BufferedImage image : images) {
					if ((lastImage != null) && (image.getWidth() != lastImage.getWidth())) {
						finalImages.add(lastImage);
					}
					lastImage = image;
				}
				finalImages.add(lastImage);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return finalImages;
	}

	public static void main(final String[] args) {
		setUpLookAndFeel();

		ObjectEditorFrame frame = new ObjectEditorFrame();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setJMenuBar(getjMenuBar());
		frame.panel.loadHotkeys();
	}

	public static void showObjectEditor() {
		ObjectEditorFrame frame = new ObjectEditorFrame();
		frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.setJMenuBar(getjMenuBar());
		frame.panel.loadHotkeys();
	}

	private static JMenuBar getjMenuBar() {
		final JMenuBar menubar = new JMenuBar();
		menubar.add(new JMenu("File"));
		menubar.add(new JMenu("Edit"));
		menubar.add(new JMenu("View"));
		menubar.add(new JMenu("Module"));
		menubar.add(new JMenu("Window"));
		return menubar;
	}

	public void loadHotkeys() {
		panel.loadHotkeys();
	}

	private static void setUpLookAndFeel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		} catch (final Exception exc) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
	}
}
