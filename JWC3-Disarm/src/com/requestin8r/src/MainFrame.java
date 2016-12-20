package com.requestin8r.src;

import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.hiveworkshop.wc3.gui.BLPHandler;

public class MainFrame extends JFrame {
	public static final Image ADVSTRUCT = BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNAdvStruct.blp");
	public MainFrame() {
		super("Simple Model Workshop");
		setContentPane(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(ADVSTRUCT);
		pack();
		setLocationRelativeTo(null);
	}

	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}

	public MainPanel mainPanel = new MainPanel(this);
	public NewRequestPanel reqPanel = new NewRequestPanel(this);

	public void jumpToPanel(final JPanel what) {
		setContentPane(what);
		revalidate();
		pack();
	}
}
