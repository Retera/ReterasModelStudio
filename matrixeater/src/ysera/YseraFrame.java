package ysera;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.BLPHandler;

public class YseraFrame extends JFrame {
	public YseraFrame() {
		// super("The Great and Powerful Warcraft 3 Model Editor Written in Java and
		// Chosen By Retera to Have the
		// Greatest of All Names Given to a Model Editor of All Time");
		super("Ysera War3 Model Editor Pre-Alpha .01");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		final YseraPanel contentPane = new YseraPanel();
		setContentPane(contentPane);
		// setIconImage(GlobalIcons.bigGeoIcon.getImage());
		setIconImage(BLPHandler.get().getGameTex("ReplaceableTextures\\CommandButtons\\BTNGreenDragon.blp"));
		setJMenuBar(contentPane.createJMenuBar());
		pack();
		setLocationRelativeTo(null);
	}

	public static void main(final String[] args) {
		LwjglNativesLoader.load();

		try {
			// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception exc) {
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final YseraFrame frame = new YseraFrame();
				frame.setVisible(true);
			}
		});
	}
}
