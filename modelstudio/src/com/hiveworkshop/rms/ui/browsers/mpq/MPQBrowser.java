package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.ui.application.ModelLoader;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MPQBrowser extends MPQFilterableBrowser {

	private static final String[] imageExtensions = {".bmp", ".tga", ".tif", ".tiff", ".jpg", ".jpeg", ".pcx", ".blp", ".dds", ".png"};
	private static final String isImageRegex = ("(" + String.join(")|(", imageExtensions) + ")").replaceAll("\\.", "\\\\.");
	private static final String hasImageRegex = ".+(" + isImageRegex + ")$";

	public MPQBrowser() {
		super(p -> ModelLoader.loadFile(p, true));

		tree.addKeyListener(getKeyListener());

		MPQMouseAdapter MPQMouseAdapter = new MPQMouseAdapter(this, expansionListener::setExpansionPropagateKeyDown, tree);
		tree.addMouseListener(MPQMouseAdapter);
		tree.addMouseMotionListener(MPQMouseAdapter);
		addMouseWheelListener(MPQMouseAdapter);
	}

	protected JMenuBar getMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(filterHandeler.getFilterMenu());
		menuBar.add(filterHandeler.getSearchMenu());
		return menuBar;
	}

	protected void addFilters() {
		filterHandeler.addFilter("Text", ".txt", ".ini", ".json", ".fdf");
		filterHandeler.addFilter("Sylk", ".slk");
		filterHandeler.addFilter("Script", ".ai", ".wai", ".j", ".js", ".pld");
		filterHandeler.addFilter("Html", ".htm", ".html");
		filterHandeler.addFilter("Models", ".mdl", ".mdx");
		filterHandeler.addFilter("Images", imageExtensions);
		filterHandeler.addFilter("Maps", ".w3m", ".w3x", ".w3n");
		filterHandeler.addFilter("Sounds", ".wav");
		filterHandeler.addFilter("Music", ".mp3", ".mid", ".flac");
		filterHandeler.addFilter("Movies", ".avi", ".webm");
		filterHandeler.addFilter("Popcorn", ".pkb", ".pkfx");
		filterHandeler.addFilter("FaceFx", ".facefx", ".facefx_ingame", ".animset", ".animset_ingame");
		filterHandeler.addFilter("Captions", ".srt");
		filterHandeler.addFilter("Fonts", ".ttf");
		filterHandeler.addFilter("Shaders", ".bls");
		filterHandeler.addFilter("Div", ".exe", ".pak", ".dll", ".bin", ".so");
		filterHandeler.setUseOtherFilter(true);
	}

	public static boolean isImagePath(String path) {
		return path.toLowerCase().matches(hasImageRegex);
	}

	private KeyListener getKeyListener() {
		return new KeyListener() {
			boolean stillPressed = false;

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !stillPressed) {
					stillPressed = true;
					TreePath treePath = tree.getSelectionPath();
					openTreePath(treePath);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && stillPressed) {
					stillPressed = false;
				}
			}
		};
	}
}