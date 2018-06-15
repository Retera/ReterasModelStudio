package com.matrixeater.src;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

/**
 * Write a description of class MainFrame here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MainFrame extends JFrame {
	static MainFrame frame;
	static MainPanel panel;
	static JMenuBar menuBar;

	public static MainPanel getPanel() {
		return panel;
	}

	public static void main(final String[] args) {
		// try {
		// new File("logs").mkdir();
		// System.setOut(new PrintStream(new File("logs/MatrixEater.log")));
		// System.setErr(new PrintStream(new File("logs/Errors.log")));
		// } catch (final FileNotFoundException e) {
		// e.printStackTrace();
		// ExceptionPopup.display(e);
		// }

		// IIORegistry registry = IIORegistry.getDefaultInstance();
		// registry.registerServiceProvider(
		// new com.realityinteractive.imageio.tga.TGAImageReaderSpi());

		// try {
		// // Set cross-platform Java L&F (also called "Metal")
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (final UnsupportedLookAndFeelException e) {
		// // handle exception
		// } catch (final ClassNotFoundException e) {
		// // handle exception
		// } catch (final InstantiationException e) {
		// // handle exception
		// } catch (final IllegalAccessException e) {
		// // handle exception
		// }
		EditorDisplayManager.setupLookAndFeel();
		// try {
		// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
		// } catch (final Exception exc) {
		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (final ClassNotFoundException e) {
		// e.printStackTrace();
		// } catch (final InstantiationException e) {
		// e.printStackTrace();
		// } catch (final IllegalAccessException e) {
		// e.printStackTrace();
		// } catch (final UnsupportedLookAndFeelException e) {
		// e.printStackTrace();
		// }
		// }
		// try {
		// final InfoNodeLookAndFeelTheme theme = new InfoNodeLookAndFeelTheme("Matrix Eater", new Color(44, 46, 20),
		// new Color(116, 126, 36), new Color(44, 46, 20), new Color(220, 202, 132), new Color(116, 126, 36),
		// new Color(220, 202, 132));
		// theme.setDesktopColor(new Color(60, 82, 44));
		// theme.setTreeOpenIcon(new IconUIResource(new ImageIcon(
		// BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup-Open.blp"))));
		// theme.setTreeClosedIcon(new IconUIResource(new ImageIcon(
		// BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\Editor-TriggerGroup.blp"))));
		// UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
		// } catch (final UnsupportedLookAndFeelException e) {
		// e.printStackTrace();
		// }

		frame = new MainFrame("Just another Warcraft III model editor");
		panel.init();
	}

	public MainFrame(final String title) {
		super(title);
		// setDefaultCloseOperation(EXIT_ON_CLOSE);

		setBounds(0, 0, 1000, 650);
		panel = new MainPanel();
		setContentPane(panel);
		menuBar = panel.createMenuBar();
		setJMenuBar(menuBar);// MainFrame.class.getResource("ImageBin/MatrixEaterMEBasic.png")
		setIconImage((new ImageIcon(MainFrame.class.getResource("ImageBin/DDChicken2.png"))).getImage());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (panel.closeAll()) {
					System.exit(0);
				}
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}
