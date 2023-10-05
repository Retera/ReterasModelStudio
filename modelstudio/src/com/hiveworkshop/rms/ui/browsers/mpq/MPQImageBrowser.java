package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.util.FramePopup;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

public class MPQImageBrowser extends JPanel {
	public MPQImageBrowser() {
		super(new MigLayout("fill", "[grow]", "[grow]"));

		ImageViewerPanel imageViewerPanel = new ImageViewerPanel();
		MPQImageFilterBrowser browser = new MPQImageFilterBrowser(imageViewerPanel::setSelectedPath);
		browser.setPreferredSize(new Dimension(350, 650));


		JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, browser, imageViewerPanel);
		add(jSplitPane, "growx, growy");

	}

	public static void showPanel() {
		MPQImageBrowser mpqImageBrowser = new MPQImageBrowser();
		mpqImageBrowser.setPreferredSize(new Dimension(1000, 650));
		FramePopup.show(mpqImageBrowser, ProgramGlobals.getMainPanel(), "Browse Textures");
	}


	private static class MPQImageFilterBrowser extends MPQFilterableBrowser {

		public MPQImageFilterBrowser(Consumer<String> pathConsumer) {
			super(pathConsumer);
			tree.addKeyListener(getKeyListener());

			MPQImageMouseAdapter mouseAdapterExtension = new MPQImageMouseAdapter(this, expansionListener::setExpansionPropagateKeyDown, tree);
			tree.addMouseListener(mouseAdapterExtension);
			tree.addMouseMotionListener(mouseAdapterExtension);
			addMouseWheelListener(mouseAdapterExtension);
		}

		@Override
		protected void addFilters() {
//			filterHandeler.addFilter("Images", ".bmp", ".tga", ".tif", ".tiff", ".jpg", ".jpeg", ".pcx", ".blp", ".dds", ".png");
			filterHandeler.addFilter("BLP Image", ".blp");
			filterHandeler.addFilter("DDS Image", ".dds");
			filterHandeler.addFilter("TGA Image", ".tga");
			filterHandeler.addFilter("PNG Image", ".png");
			filterHandeler.addFilter("JPG Image", ".jpg", ".jpeg");
			filterHandeler.addFilter("BMP Image", ".bmp");
			filterHandeler.addFilter("TIF Image", ".tif", ".tiff");
			filterHandeler.addFilter("GIF Image", ".gif", ".giff");
			filterHandeler.addFilter("PCX Image", ".pcx");
		}

		@Override
		protected JMenuBar getMenuBar() {
			JMenuBar menuBar = new JMenuBar();
			menuBar.add(filterHandeler.getFilterMenu());
			menuBar.add(filterHandeler.getSearchMenu());
			return menuBar;
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
					} else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
						TreePath treePath = tree.getSelectionPath();
						openTreePath(treePath);
					}
				}
			};
		}
	}
}