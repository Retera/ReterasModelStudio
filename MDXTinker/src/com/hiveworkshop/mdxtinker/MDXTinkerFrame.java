package com.hiveworkshop.mdxtinker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hiveworkshop.mdxtinker.OkCancelPanel.OkCancelListener;
import com.hiveworkshop.mdxtinker.PreferencesPanel.Theme;
import com.hiveworkshop.mdxtinker.PreferencesPanel.ThemeChangeListener;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ModelScale;
import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.GeosetVertexSelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorNotifier;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.PivotPointModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.PivotPointSelectionManager;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon.ColorListener;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.matrixeater.src.EditorDisplayManager;

public final class MDXTinkerFrame extends JFrame {
	private Theme theme = Theme.LIGHT;
	private static final Image FLIP_ICON, RESIZE_ICON, ROTATE_ICON;
	static {
		try {
			FLIP_ICON = ImageIO.read(MDXTinkerFrame.class.getResource("img/UI_FlipIcon.png"));
			RESIZE_ICON = ImageIO.read(MDXTinkerFrame.class.getResource("img/UI_ResizeIcon.png"));
			ROTATE_ICON = ImageIO.read(MDXTinkerFrame.class.getResource("img/UI_RotateIcon.png"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	private final PerspDisplayPanel viewport;
	private JFileChooser fileChooser;
	private MDL currentModel;
	private ProgramPreferences programPreferences;
	private ModelEditorNotifier modelEditor;

	public MDXTinkerFrame() {
		super("MDX Tinker");
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("MDX model", "mdx"));
		currentModel = new MDL();
		final ModelViewManager modelView = new ModelViewManager(currentModel);
		programPreferences = new ProgramPreferences();
		viewport = new PerspDisplayPanel("NONE", modelView, programPreferences);
		viewport.setPreferredSize(new Dimension(640, 480));
		setContentPane(viewport);
		try {
			setIconImage(ImageIO.read(MDXTinkerFrame.class.getResource("img/MDXTinkerIcon.png")));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		setJMenuBar(createJMenuBar());
	}

	/**
	 * @return
	 */
	public JMenuBar createJMenuBar() {
		final JMenuBar jMenuBar = new JMenuBar();
		final JMenu fileMenu = new JMenu("File");

		final JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				fileChooser.setDialogTitle("Open Model");
				final int result = fileChooser.showOpenDialog(MDXTinkerFrame.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					final File file = fileChooser.getSelectedFile();
					if (file != null) {
						currentModel = MDL.read(file);
						final ModelViewManager modelView = new ModelViewManager(currentModel);
						final GeosetVertexModelEditor geosetVertexModelEditor = new GeosetVertexModelEditor(modelView,
								programPreferences, new GeosetVertexSelectionManager());
						final PivotPointModelEditor pivotPointModelEditor = new PivotPointModelEditor(modelView,
								programPreferences, new PivotPointSelectionManager());
						modelEditor = new ModelEditorNotifier();
						modelEditor.subscribe(geosetVertexModelEditor);
						modelEditor.subscribe(pivotPointModelEditor);
						modelEditor.selectAll();
						viewport.setViewport(modelView);
					}
				}
			}
		});
		fileMenu.add(openItem);
		final JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				fileChooser.setDialogTitle("Save Model");
				final int result = fileChooser.showSaveDialog(MDXTinkerFrame.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					final File file = fileChooser.getSelectedFile();
					if (file != null) {
						currentModel.printTo(file);
					}
				}
			}
		});
		fileMenu.add(saveItem);
		jMenuBar.add(fileMenu);

		jMenuBar.add(new JMenu("Recent Files"));

		final JMenu editMenu = new JMenu("Edit");
		final JMenuItem flipItem = new JMenuItem("Flip...");
		flipItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final FlipPanel flipPanel = new FlipPanel();
				final JDialog dialog = new JDialog(MDXTinkerFrame.this, "Flip", true);
				dialog.setIconImage(FLIP_ICON);
				dialog.setContentPane(new OkCancelPanel(flipPanel, new OkCancelListener() {
					@Override
					public void ok() {
						final boolean flipXSelected = flipPanel.isFlipXSelected();
						final boolean flipYSelected = flipPanel.isFlipYSelected();
						final boolean flipZSelected = flipPanel.isFlipZSelected();
						final CenterOfManipulation centerOfManipulation = flipPanel.getCenterOfManipulation();
						final Vertex center = computeCenterOfManipulation(centerOfManipulation);

						if (flipXSelected) {
							modelEditor.mirror((byte) 1, true, center.x, center.y, center.z);
						}
						if (flipYSelected) {
							modelEditor.mirror((byte) 2, true, center.x, center.y, center.z);
						}
						if (flipZSelected) {
							modelEditor.mirror((byte) 0, true, center.x, center.y, center.z);
						}
					}

					@Override
					public void cancel() {

					}
				}));
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent we) {
					}
				});
				dialog.pack();
				dialog.setLocationRelativeTo(MDXTinkerFrame.this);
				dialog.setVisible(true);
			}
		});
		editMenu.add(flipItem);
		final JMenuItem resizeItem = new JMenuItem("Resize...");
		resizeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final ResizePanel panel = new ResizePanel();
				final JDialog dialog = new JDialog(MDXTinkerFrame.this, "Resize", true);
				dialog.setIconImage(RESIZE_ICON);
				dialog.setContentPane(new OkCancelPanel(panel, new OkCancelListener() {
					@Override
					public void ok() {
						final double xValue = panel.getXValue();
						final double yValue = panel.getYValue();
						final double zValue = panel.getZValue();
						final CenterOfManipulation centerOfManipulation = panel.getCenterOfManipulation();
						final Vertex center = computeCenterOfManipulation(centerOfManipulation);
						ModelScale.scale(currentModel, zValue, xValue, yValue, center.x, center.y, center.z);
					}

					@Override
					public void cancel() {

					}
				}));
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent we) {
					}
				});
				dialog.pack();
				dialog.setLocationRelativeTo(MDXTinkerFrame.this);
				dialog.setVisible(true);
			}
		});
		editMenu.add(resizeItem);
		final JMenuItem rotateItem = new JMenuItem("Rotate...");
		rotateItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final RotatePanel panel = new RotatePanel();
				final JDialog dialog = new JDialog(MDXTinkerFrame.this, "Rotate", true);
				dialog.setIconImage(ROTATE_ICON);
				dialog.setContentPane(new OkCancelPanel(panel, new OkCancelListener() {
					@Override
					public void ok() {
						final int xValue = panel.getXValue();
						final int yValue = panel.getYValue();
						final int zValue = panel.getZValue();
						final CenterOfManipulation centerOfManipulation = panel.getCenterOfManipulation();
						final Vertex center = computeCenterOfManipulation(centerOfManipulation);
						modelEditor.rotate(center, Math.toRadians(xValue), Math.toRadians(yValue),
								Math.toRadians(zValue));
					}

					@Override
					public void cancel() {

					}
				}));
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent we) {
					}
				});
				dialog.pack();
				dialog.setLocationRelativeTo(MDXTinkerFrame.this);
				dialog.setVisible(true);
			}
		});
		editMenu.add(rotateItem);
		editMenu.addSeparator();
		final JMenuItem preferencesItem = new JMenuItem("Preferences");
		preferencesItem.addActionListener(new ActionListener() {
			private Theme nextWantedTheme = theme;

			@Override
			public void actionPerformed(final ActionEvent e) {
				nextWantedTheme = theme;
				final PreferencesPanel panel = new PreferencesPanel(new ThemeChangeListener() {
					@Override
					public void themeChanged(final Theme theme) {
						nextWantedTheme = theme;
					}
				}, new ColorListener() {
					@Override
					public void colorChanged(final Color color) {
						viewport.setViewportBackground(color);
					}
				}, theme, viewport.getViewportBackground());
				final JDialog dialog = new JDialog(MDXTinkerFrame.this, "Rotate", true);
				dialog.setIconImage(ROTATE_ICON);
				dialog.setContentPane(new OkCancelPanel(panel, new OkCancelListener() {
					@Override
					public void ok() {
						if (theme != nextWantedTheme) {
							theme = nextWantedTheme;
							switch (theme) {
							case DARK:
								EditorDisplayManager.setupLookAndFeel();
								break;
							default:
							case LIGHT:
								try {
									UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
								} catch (final ClassNotFoundException e2) {
									e2.printStackTrace();
								} catch (final InstantiationException e2) {
									e2.printStackTrace();
								} catch (final IllegalAccessException e2) {
									e2.printStackTrace();
								} catch (final UnsupportedLookAndFeelException e2) {
									e2.printStackTrace();
								}
								break;
							}
							setVisible(false);
							final MDXTinkerFrame frame = spawnWindow();
							frame.viewport.setViewport(new ModelViewManager(currentModel));
							frame.theme = nextWantedTheme;
							frame.fileChooser = fileChooser;
							frame.modelEditor = modelEditor;
							frame.programPreferences = programPreferences;
							frame.currentModel = currentModel;
							frame.viewport.setViewportBackground(viewport.getViewportBackground());
						}
					}

					@Override
					public void cancel() {

					}
				}));
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(final WindowEvent we) {
					}
				});
				dialog.pack();
				dialog.setLocationRelativeTo(MDXTinkerFrame.this);
				dialog.setVisible(true);
			}
		});
		editMenu.add(preferencesItem);
		jMenuBar.add(editMenu);

		return jMenuBar;
	}

	public Vertex computeCenterOfManipulation(final CenterOfManipulation centerOfManipulation) {
		Vertex center;
		if (centerOfManipulation == CenterOfManipulation.ORIGIN) {
			center = new Vertex(0, 0, 0);
		} else {
			center = modelEditor.getSelectionCenter();
		}
		return center;
	}

	public static void main(final String[] args) {
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

		spawnWindow();
	}

	public static MDXTinkerFrame spawnWindow() {
		final MDXTinkerFrame frame = new MDXTinkerFrame();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		return frame;
	}
}
