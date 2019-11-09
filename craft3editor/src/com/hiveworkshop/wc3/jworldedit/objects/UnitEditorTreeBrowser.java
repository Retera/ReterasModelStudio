package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.user.SaveProfile;
import com.hiveworkshop.wc3.util.ModelUtils;

public class UnitEditorTreeBrowser extends UnitEditorTree {
	private int rightClickX, rightClickY;

	public UnitEditorTreeBrowser(final MutableObjectData unitData, final ObjectTabTreeBrowserBuilder browserBuilder,
			final UnitEditorSettings settings, final WorldEditorDataType dataType, final MDLLoadListener listener,
			final ProgramPreferences prefs) {
		super(unitData, browserBuilder, settings, dataType);

		selectFirstUnit();
		final JPopupMenu popupMenu = new JPopupMenu();
		final JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
				if (currentUnitTreePath != null) {
					final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
							.getLastPathComponent();
					if (o.getUserObject() instanceof MutableGameObject) {
						final MutableGameObject obj = (MutableGameObject) o.getUserObject();
						final String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
						final BufferedImage iconTexture = com.hiveworkshop.wc3.util.IconUtils.getIcon(obj,
								WorldEditorDataType.UNITS);
						final ImageIcon icon = iconTexture == null ? null
								: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
						listener.loadFile(MpqCodebase.get().getFile(path), true, true, icon);
					}
				}
			}
		});
		popupMenu.add(openItem);
		final JMenuItem openPortraitItem = new JMenuItem("Open Portrait");
		openPortraitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
				if (currentUnitTreePath != null) {
					final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
							.getLastPathComponent();
					if (o.getUserObject() instanceof MutableGameObject) {
						final MutableGameObject obj = (MutableGameObject) o.getUserObject();
						final String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
						final String portrait = ModelUtils.getPortrait(path);
						final BufferedImage iconTexture = com.hiveworkshop.wc3.util.IconUtils.getIcon(obj,
								WorldEditorDataType.UNITS);
						final ImageIcon icon = iconTexture == null ? null
								: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
						listener.loadFile(MpqCodebase.get().getFile(portrait), true, true, icon);
					}
				}
			}
		});
		popupMenu.add(openPortraitItem);
		popupMenu.addSeparator();
		final JMenuItem extract = new JMenuItem("Extract");
		popupMenu.add(extract);
		extract.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
				if (currentUnitTreePath != null) {
					final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
							.getLastPathComponent();
					if (o.getUserObject() instanceof MutableGameObject) {
						final MutableGameObject obj = (MutableGameObject) o.getUserObject();
						final String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
						final BufferedImage iconTexture = com.hiveworkshop.wc3.util.IconUtils.getIcon(obj,
								WorldEditorDataType.UNITS);
						final ImageIcon icon = iconTexture == null ? null
								: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
						try {
							final JFileChooser jFileChooser = new JFileChooser(SaveProfile.get().getPath());
							final int response = jFileChooser.showSaveDialog(UnitEditorTreeBrowser.this);
							if (response == JFileChooser.APPROVE_OPTION) {
								final File selectedFile = jFileChooser.getSelectedFile();
								if (selectedFile != null) {
									Files.copy(MpqCodebase.get().getResourceAsStream(path), selectedFile.toPath());
								}
							}

						} catch (final IOException e1) {
							e1.printStackTrace();
							ExceptionPopup.display(e1);
						}
					}
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				try {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClickX = e.getX();
						rightClickY = e.getY();
						popupMenu.show(UnitEditorTreeBrowser.this, e.getX(), e.getY());
					} else {
						if (e.getClickCount() >= 2) {
							final TreePath currentUnitTreePath = getPathForLocation(e.getX(), e.getY());
							if (currentUnitTreePath != null) {
								final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
										.getLastPathComponent();
								if (o.getUserObject() instanceof MutableGameObject) {
									final MutableGameObject obj = (MutableGameObject) o.getUserObject();
									final String path = convertPathToMDX(
											obj.getFieldAsString(War3ID.fromString("umdl"), 0));
									final String portrait = ModelUtils.getPortrait(path);
									final BufferedImage iconTexture = com.hiveworkshop.wc3.util.IconUtils.getIcon(obj,
											WorldEditorDataType.UNITS);
									final ImageIcon icon = iconTexture == null ? null
											: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
									System.err.println("loading: " + path);
									listener.loadFile(MpqCodebase.get().getFile(path), true, true, icon);
									if (prefs.isLoadPortraits() && MpqCodebase.get().has(portrait)) {
										listener.loadFile(MpqCodebase.get().getFile(portrait), true, false, icon);
									}
								}
							}
						}

					}
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
	}

	public static interface MDLLoadListener {
		public void loadFile(File file, boolean b, boolean c, ImageIcon icon);
	}

	private String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

}
