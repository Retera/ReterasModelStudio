package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class UnitEditorTreeBrowser extends UnitEditorTree {
	private int rightClickX, rightClickY;

	public UnitEditorTreeBrowser(final MutableObjectData unitData, final ObjectTabTreeBrowserBuilder browserBuilder,
			final UnitEditorSettings settings, final WorldEditorDataType dataType, final MDLLoadListener listener,
			final ProgramPreferences prefs) {
		super(unitData, browserBuilder, settings, dataType);

		selectFirstUnit();
		final JPopupMenu popupMenu = new JPopupMenu();
		final JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new OpenUnitStandardModelField(listener, "umdl"));
		popupMenu.add(openItem);
		final JMenuItem openPortraitItem = new JMenuItem("Open Portrait");
		openPortraitItem.addActionListener(new OpenUnitPortraitModelField(listener));
		popupMenu.add(openPortraitItem);
		JMenu projectileArtMenu = new JMenu("Open Projectile");
		final JMenuItem openProjectileItem = new JMenuItem("Attack 1");
		openProjectileItem.addActionListener(new OpenUnitStandardModelField(listener, "ua1m"));
		projectileArtMenu.add(openProjectileItem);
		final JMenuItem openProjectile2Item = new JMenuItem("Attack 2");
		openProjectile2Item.addActionListener(new OpenUnitStandardModelField(listener, "ua2m"));
		projectileArtMenu.add(openProjectile2Item);
		popupMenu.add(projectileArtMenu);
		popupMenu.addSeparator();
		final JMenuItem extract = new JMenuItem("Extract");
		popupMenu.add(extract);
		extract.addActionListener(e -> {
            final TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
            if (currentUnitTreePath != null) {
	            final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
                if (o.getUserObject() instanceof MutableGameObject) {
	                final MutableGameObject obj = (MutableGameObject) o.getUserObject();
	                System.out.println("objString: " + obj.getFieldAsString(War3ID.fromString("umdl"), 0));
	                final String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
	                System.out.println("path: " + path);
//	                final BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
//                    final ImageIcon icon = iconTexture == null ? null : new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

	                FileDialog fileDialog = new FileDialog(this);
	                fileDialog.exportInternalFile(path);

//                    try {
//                        final JFileChooser jFileChooser = new JFileChooser(SaveProfile.get().getPath());
//                        final int response = jFileChooser.showSaveDialog(UnitEditorTreeBrowser.this);
//                        if (response == JFileChooser.APPROVE_OPTION) {
//                            final File selectedFile = jFileChooser.getSelectedFile();
//                            if (selectedFile != null) {
//                                Files.copy(GameDataFileSystem.getDefault().getResourceAsStream(path), selectedFile.toPath());
//                            }
//                        }
//
//                    } catch (final IOException e1) {
//                        e1.printStackTrace();
//                        ExceptionPopup.display(e1);
//                    }
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
									final BufferedImage iconTexture = IconUtils.getIcon(obj,
											WorldEditorDataType.UNITS);
									final ImageIcon icon = iconTexture == null ? null
											: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
									System.err.println("loading: " + path);
									listener.loadFile(path, true, true, icon);
									if (prefs.isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
										listener.loadFile(portrait, true, false, icon);
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

	public interface MDLLoadListener {
		void loadFile(String filePathMdx, boolean b, boolean c, ImageIcon icon);
	}

	private String convertPathToMDX(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}

	private final class OpenUnitStandardModelField extends OpenUnitModelField {
		public OpenUnitStandardModelField(MDLLoadListener listener, String unitFieldRawcode) {
			super(listener, unitFieldRawcode);
		}

		@Override
		public String resolvePath(String path) {
			return path;
		}
	}

	private final class OpenUnitPortraitModelField extends OpenUnitModelField {
		public OpenUnitPortraitModelField(MDLLoadListener listener) {
			super(listener, "umdl");
		}

		@Override
		public String resolvePath(String path) {
			return ModelUtils.getPortrait(path);
		}
	}

	private abstract class OpenUnitModelField implements ActionListener {
		private final MDLLoadListener listener;
		private final String unitFieldRawcode;

		public OpenUnitModelField(MDLLoadListener listener, String unitFieldRawcode) {
			this.listener = listener;
			this.unitFieldRawcode = unitFieldRawcode;
		}

		public abstract String resolvePath(String path);

		@Override
		public void actionPerformed(ActionEvent e) {
			final TreePath currentUnitTreePath = UnitEditorTreeBrowser.this.getPathForLocation(rightClickX, rightClickY);
			if (currentUnitTreePath != null) {
				final DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath
						.getLastPathComponent();
				if (o.getUserObject() instanceof MutableGameObject) {
					final MutableGameObject obj = (MutableGameObject) o.getUserObject();
					final String path = UnitEditorTreeBrowser.this.convertPathToMDX(obj.getFieldAsString(War3ID.fromString(unitFieldRawcode), 0));
					final String resolvedPath = resolvePath(path);
					final BufferedImage iconTexture = IconUtils.getIcon(obj,
							WorldEditorDataType.UNITS);
					final ImageIcon icon = iconTexture == null ? null
							: new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
					listener.loadFile(resolvedPath, true, true, icon);
				}
			}
		}
	}
}
