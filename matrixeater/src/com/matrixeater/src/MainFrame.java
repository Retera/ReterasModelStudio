package com.matrixeater.src;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.user.SaveProfile;

import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.gui.laf.InfoNodeLookAndFeelTheme;
import net.infonode.gui.laf.InfoNodeLookAndFeelThemes;

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
		try {
			LwjglNativesLoader.load();
//		try {
//			MpqCodebase.get().loadMPQ(Paths.get("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Maps\\Altered Melee\\(6)HFNeonCity.w3x"));
//		} catch (MPQException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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
			final ProgramPreferences preferences = SaveProfile.get().getPreferences();
			switch (preferences.getTheme()) {
			case DARK:
				EditorDisplayManager.setupLookAndFeel();
				break;
			case HIFI:
				EditorDisplayManager.setupLookAndFeel("HiFi");
				break;
			case ACRYL:
				EditorDisplayManager.setupLookAndFeel("Acryl");
				break;
			case ALUMINIUM:
				EditorDisplayManager.setupLookAndFeel("Aluminium");
				break;
			case FOREST_GREEN:
				try {
					final InfoNodeLookAndFeelTheme theme = new InfoNodeLookAndFeelTheme("Retera Studio",
							new Color(44, 46, 20), new Color(116, 126, 36), new Color(44, 46, 20),
							new Color(220, 202, 132), new Color(116, 126, 36), new Color(220, 202, 132));
					theme.setShadingFactor(-0.8);
					theme.setDesktopColor(new Color(60, 82, 44));

					UIManager.setLookAndFeel(new InfoNodeLookAndFeel(theme));
				} catch (final UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				break;
			case WINDOWS:
				try {
					UIManager.put("desktop", new ColorUIResource(Color.WHITE));
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					System.out.println(UIManager.getLookAndFeel());
				} catch (final UnsupportedLookAndFeelException e) {
					// handle exception
				} catch (final ClassNotFoundException e) {
					// handle exception
				} catch (final InstantiationException e) {
					// handle exception
				} catch (final IllegalAccessException e) {
					// handle exception
				}
				break;
			case WINDOWS_CLASSIC:
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
				break;
			case JAVA_DEFAULT:
//				UIManager.getLookAndFeel().initialize();
//				UIManager.getLookAndFeel().getDefaults().put("TabbedPane.background", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("InternalFrame.activeTitleBackground", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("InternalFrame.activeTitleForeground", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("InternalFrame.inactiveTitleBackground", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("InternalFrame.inactiveTitleForeground", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("Button.select", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("Button.disabledText", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("ScrollBar.background", Color.GREEN);
//				UIManager.getLookAndFeel().getDefaults().put("ScrollBar.shadow", Color.GREEN);
				break;
			case SOFT_GRAY:
				try {
					final InfoNodeLookAndFeelTheme softGrayTheme = InfoNodeLookAndFeelThemes.getSoftGrayTheme();
					UIManager.setLookAndFeel(new InfoNodeLookAndFeel(softGrayTheme));
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

				break;
			case BLUE_ICE:
				try {
					final InfoNodeLookAndFeelTheme blueIceTheme = InfoNodeLookAndFeelThemes.getBlueIceTheme();
					UIManager.setLookAndFeel(new InfoNodeLookAndFeel(blueIceTheme));
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

				break;
			case DARK_BLUE_GREEN:
				try {
					final InfoNodeLookAndFeelTheme darkBlueGreenTheme = InfoNodeLookAndFeelThemes
							.getDarkBlueGreenTheme();
					UIManager.setLookAndFeel(new InfoNodeLookAndFeel(darkBlueGreenTheme));
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

				break;
			case GRAY:
				try {
					final InfoNodeLookAndFeelTheme grayTheme = InfoNodeLookAndFeelThemes.getGrayTheme();
					UIManager.setLookAndFeel(new InfoNodeLookAndFeel(grayTheme));
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

				break;
			}
			final String autoWarcraftDirectory = SaveProfile.getWarcraftDirectory();
			if (!SaveProfile.testTargetFolderReadOnly(autoWarcraftDirectory)) {
				SaveProfile.requestNewWc3Directory();
			}
			System.out.println(autoWarcraftDirectory + " appears valid");

			// TechshaperFrame.main(args);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame = new MainFrame("Retera Model Studio v0.03 Public Beta");
					panel.init();
				}
			});
		} catch (final Throwable th) {
			th.printStackTrace();
			ExceptionPopup.display(th);
		}
	}

	public MainFrame(final String title) {
		super(title);
		// setDefaultCloseOperation(EXIT_ON_CLOSE);

		setBounds(0, 0, 1000, 650);
		panel = new MainPanel();
		setContentPane(panel);
		menuBar = panel.createMenuBar();
		setJMenuBar(menuBar);// MainFrame.class.getResource("ImageBin/DDChicken2.png")
		setIconImage(new ImageIcon(MainFrame.class.getResource("ImageBin/retera.jpg")).getImage());

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
