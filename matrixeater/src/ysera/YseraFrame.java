package ysera;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.localizationmanager.localization.LocalizationManager;

public class YseraFrame extends JFrame {
	public YseraFrame() {
		super(LocalizationManager.getInstance().get("ysera.app.title"));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		final YseraPanel contentPane = new YseraPanel();
		setContentPane(contentPane);
		setIconImage(BLPHandler.get().getGameTex("war3.w3mod/ReplaceableTextures\\CommandButtons\\BTNGreenDragon.blp"));
		setJMenuBar(contentPane.createJMenuBar());
		// listen for locale changes and rebuild UI strings
		LocalizationManager.getInstance().addPropertyChangeListener(evt -> {
			if ("locale".equals(evt.getPropertyName())) {
				setJMenuBar(contentPane.createJMenuBar());
				setTitle(LocalizationManager.getInstance().get("ysera.app.title"));
				SwingUtilities.updateComponentTreeUI(this);
			}
		});
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
