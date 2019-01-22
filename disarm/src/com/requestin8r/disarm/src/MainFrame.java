package com.requestin8r.disarm.src;

import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.user.SaveProfile;

public class MainFrame extends JFrame {
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SaveProfile.testTargetFolder(SaveProfile.getWarcraftDirectory());

		ADVSTRUCT = BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNCurse.blp");
	}

	public static final Image ADVSTRUCT;

	public MainFrame() {
		super("MDXScale: Scale models in style");
		setContentPane(mainPanel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIconImage(ADVSTRUCT);
		setJMenuBar(mainPanel.createJMenuBar());
		pack();
		setLocationRelativeTo(null);
	}

	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}

	public MainPanel mainPanel = new MainPanel(this);

	public void jumpToPanel(final JPanel what) {
		setContentPane(what);
		revalidate();
		pack();
	}
}
