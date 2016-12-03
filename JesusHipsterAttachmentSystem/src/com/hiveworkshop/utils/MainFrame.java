package com.hiveworkshop.utils;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.hiveworkshop.wc3.gui.ExceptionPopup;

public class MainFrame extends JFrame {
	public static final Image HIPSTER_ICON;
	static {
		Image hipster = null;
		try {
			hipster = ImageIO.read(MainFrame.class.getResource("hipster.png"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
			System.exit(0);
			e.printStackTrace();
		}
		HIPSTER_ICON = hipster;
	}

	public MainFrame() throws IOException {
		super(Resources.getString("program.name"));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(new StartPanel(this));
		setIconImage(HIPSTER_ICON);
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

		MainFrame frame;
		try {
			frame = new MainFrame();
			frame.setVisible(true);
		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null,"Program error: failed to load required resource.");
			e.printStackTrace();
		}
	}

	StartPanel startPanel = new StartPanel(this);
	BaseChoicePanel baseChoicePanel = new BaseChoicePanel(this);

	public void jumpToPanel(final JPanel what) {
		setContentPane(what);
		revalidate();
		pack();
	}
}
