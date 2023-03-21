package com.matrixeater.src;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;

import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceChooserPanel;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.resources.Resources;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.ModelOptionPanel;
import com.hiveworkshop.wc3.units.UnitOptionPanel;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.parser.Parse;

import de.wc3data.image.TgaFile;
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
	private static final String RETERA_MODEL_STUDIO_VERSION = "Retera Model Studio v0.04.4k";
	static MainFrame frame;
	static MainPanel panel;
	static JMenuBar menuBar;

	public static MainPanel getPanel() {
		return panel;
	}

	public static void main(final String[] args) {
		final boolean hasArgs = args.length >= 1;
		final List<String> startupModelPaths = new ArrayList<>();
		if (hasArgs) {
			if (args.length > 1 && args[0].equals("-convert")) {
				final String path = args[1];
				final boolean mdxInput = path.toLowerCase().endsWith(".mdx");
				final boolean mdlInput = path.toLowerCase().endsWith(".mdl");
				final boolean objInput = path.toLowerCase().endsWith(".obj");
				if (mdxInput || mdlInput || objInput) {
					if (mdxInput) {
						final EditableModel model = EditableModel.read(new File(path));
						String destination = path.substring(0, path.lastIndexOf('.')) + ".mdl";
						if (args.length > 2) {
							destination = args[2];
						}
						final File result = new File(destination);
						model.printTo(result);
						System.out.println(RETERA_MODEL_STUDIO_VERSION + " converted: " + result);
					} else if (mdlInput) {
						final EditableModel model = EditableModel.read(new File(path));
						String destination = path.substring(0, path.lastIndexOf('.')) + ".mdx";
						if (args.length > 2) {
							destination = args[2];
						}
						final File result = new File(destination);
						model.printTo(result);
						System.out.println(RETERA_MODEL_STUDIO_VERSION + " converted: " + result);
					} else {
						String destination = path.substring(0, path.lastIndexOf('.')) + ".mdx";
						if (args.length > 2) {
							destination = args[2];
						}
						// Unfortunately obj convert does popups right now
						final Build builder = new Build();
						try {
							final Parse obj = new Parse(builder, path);
							final EditableModel mdl = builder.createMDL();
							final File result = new File(destination);
							mdl.printTo(result);
							System.out.println(RETERA_MODEL_STUDIO_VERSION + " converted: " + result);
						} catch (final FileNotFoundException e) {
							ExceptionPopup.display(e);
							e.printStackTrace();
						} catch (final IOException e) {
							ExceptionPopup.display(e);
							e.printStackTrace();
						}
					}
				} else if (args.length > 2) {
					final String destination = args[2];
					final File result = new File(destination);
					try {
						final BufferedImage sourceImage = path.toLowerCase().endsWith(".tga")
								? TgaFile.readTGA(new File(path))
								: ImageIO.read(new File(path));
						if (destination.toLowerCase().endsWith(".tga")) {
							TgaFile.writeTGA(sourceImage, result);
						} else {
							ImageIO.write(sourceImage, destination.substring(destination.lastIndexOf('.') + 1), result);
						}
						System.out.println(RETERA_MODEL_STUDIO_VERSION + " converted: " + result);
					} catch (final IOException e) {
						e.printStackTrace();
					}

				} else {
					System.out.println(
							"Unable to convert. If you are converting an image, provide an output path to tell me what file extension to save as.");
				}
				return;
			} else if (args[0].endsWith(".mdx") || args[0].endsWith(".mdl") || args[0].endsWith(".blp")
					|| args[0].endsWith(".dds") || args[0].endsWith(".obj")) {
				for (final String arg : args) {
					startupModelPaths.add(arg);
				}
			}
		}
		final boolean dataPromptForced = hasArgs && args[0].equals("-forcedataprompt");
		try {
			final ProgramPreferences preferences = SaveProfile.get().getPreferences();
			if (preferences.getDisableDirectXToSolveVisualArtifacts() != null
					&& preferences.getDisableDirectXToSolveVisualArtifacts()) {
				System.setProperty("sun.java2d.opengl", "True");
			}
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
			case DEMONIC_MEME:
				try {
					final InfoNodeLookAndFeelTheme theme = new InfoNodeLookAndFeelTheme("DRMS", new Color(46, 20, 20),
							new Color(126, 50, 36), new Color(46, 20, 20), new Color(220, 172, 52),
							new Color(126, 56, 36), new Color(220, 172, 52));
					theme.setShadingFactor(-0.8);
					theme.setDesktopColor(new Color(82, 60, 44));

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
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						final List<DataSourceDescriptor> dataSources = SaveProfile.get().getDataSources();
						if (dataSources == null || dataPromptForced) {
							final DataSourceChooserPanel dataSourceChooserPanel = new DataSourceChooserPanel(
									dataSources);
//							JF
							final JFrame jFrame = new JFrame("Retera Model Studio: Setup");
//							jFrame.setContentPane(dataSourceChooserPanel);
							jFrame.setUndecorated(true);
							jFrame.pack();
							jFrame.setSize(0, 0);
							jFrame.setLocationRelativeTo(null);
							jFrame.setIconImage(MainPanel.MAIN_PROGRAM_ICON);
							jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
							jFrame.setVisible(true);
							try {
								if (JOptionPane.showConfirmDialog(jFrame, dataSourceChooserPanel,
										"Retera Model Studio: Setup", JOptionPane.OK_CANCEL_OPTION,
										JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
									return;
								}
							} finally {
								jFrame.setVisible(false);
							}
							SaveProfile.get().setDataSources(dataSourceChooserPanel.getDataSourceDescriptors());
							SaveProfile.save();
							MpqCodebase.get().refresh(SaveProfile.get().getDataSources());
							// cache priority order...
							UnitOptionPanel.dropRaceCache();
							DataTable.dropCache();
							ModelOptionPanel.dropCache();
							WEString.dropCache();
							Resources.dropCache();
							BLPHandler.get().dropCache();
						}

						JPopupMenu.setDefaultLightWeightPopupEnabled(false);
						frame = new MainFrame(RETERA_MODEL_STUDIO_VERSION);
						panel.init();
						if (!startupModelPaths.isEmpty()) {
							for (final String path : startupModelPaths) {
								panel.openFile(new File(path));
							}
						}
					} catch (final Throwable th) {
						th.printStackTrace();
						ExceptionPopup.display(th);
						if (!dataPromptForced) {
							new Thread(new Runnable() {
								@Override
								public void run() {
									main(new String[] { "-forcedataprompt" });
								}
							}).start();
						} else {
							JOptionPane.showMessageDialog(null,
									"Retera Model Studio startup sequence has failed for two attempts. The program will now exit.",
									"Error", JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					}
				}
			});
		} catch (final Throwable th) {
			th.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ExceptionPopup.display(th);
				}
			});
			if (!dataPromptForced) {
				main(new String[] { "-forcedataprompt" });
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
								"Retera Model Studio startup sequence has failed for two attempts. The program will now exit.",
								"Error", JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
					}
				});
			}
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
		setIconImage(MainPanel.MAIN_PROGRAM_ICON);

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
