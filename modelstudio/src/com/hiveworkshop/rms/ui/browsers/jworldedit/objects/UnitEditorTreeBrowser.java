package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.icons.IconUtils;
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

	public UnitEditorTreeBrowser(MutableObjectData unitData, ObjectTabTreeBrowserBuilder browserBuilder,
	                             UnitEditorSettings settings, WorldEditorDataType dataType, MDLLoadListener listener) {
		super(unitData, browserBuilder, settings, dataType);

		selectFirstUnit();
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new OpenUnitStandardModelField(listener, "umdl"));
		popupMenu.add(openItem);

		JMenuItem openPortraitItem = new JMenuItem("Open Portrait");
		openPortraitItem.addActionListener(new OpenUnitPortraitModelField(listener));
		popupMenu.add(openPortraitItem);

		JMenu projectileArtMenu = new JMenu("Open Projectile");

		JMenuItem openProjectileItem = new JMenuItem("Attack 1");
		openProjectileItem.addActionListener(new OpenUnitStandardModelField(listener, "ua1m"));
		projectileArtMenu.add(openProjectileItem);

		JMenuItem openProjectile2Item = new JMenuItem("Attack 2");
		openProjectile2Item.addActionListener(new OpenUnitStandardModelField(listener, "ua2m"));
		projectileArtMenu.add(openProjectile2Item);
		popupMenu.add(projectileArtMenu);
		popupMenu.addSeparator();

		JMenuItem extract = new JMenuItem("Extract");
		popupMenu.add(extract);

		extract.addActionListener(e -> extractFile());
		MouseAdapter umdl = getMouseAdapter(listener, popupMenu);
		addMouseListener(umdl);
	}

	private MouseAdapter getMouseAdapter(MDLLoadListener listener, JPopupMenu popupMenu) {
		MouseAdapter umdl = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClickX = e.getX();
						rightClickY = e.getY();
						popupMenu.show(UnitEditorTreeBrowser.this, e.getX(), e.getY());
					} else {
						if (e.getClickCount() >= 2) {
							openFile(e, listener);
						}

					}
				} catch (Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		};
		return umdl;
	}

	private void openFile(MouseEvent e, MDLLoadListener listener) {
		TreePath currentUnitTreePath = getPathForLocation(e.getX(), e.getY());
		if (currentUnitTreePath != null) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
			if (o.getUserObject() instanceof MutableGameObject) {
				MutableGameObject obj = (MutableGameObject) o.getUserObject();
				String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
				String portrait = ModelUtils.getPortrait(path);
				BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
				ImageIcon icon = iconTexture == null ? null : new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

				System.err.println("loading: " + path);
				listener.loadFile(path, true, true, icon);
				if (ProgramGlobals.getPrefs().isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
					listener.loadFile(portrait, true, false, icon);
				}
			}
		}
	}

	private void extractFile() {
		TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
		if (currentUnitTreePath != null) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
		    if (o.getUserObject() instanceof MutableGameObject) {
			    MutableGameObject obj = (MutableGameObject) o.getUserObject();
			    System.out.println("objString: " + obj.getFieldAsString(War3ID.fromString("umdl"), 0));
			    String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
			    System.out.println("path: " + path);

			    FileDialog fileDialog = new FileDialog(this);
			    fileDialog.exportInternalFile(path);
		    }
		}
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

	private class OpenUnitStandardModelField extends OpenUnitModelField {
		public OpenUnitStandardModelField(MDLLoadListener listener, String unitFieldRawcode) {
			super(listener, unitFieldRawcode);
		}

		@Override
		public String resolvePath(String path) {
			return path;
		}
	}

	private class OpenUnitPortraitModelField extends OpenUnitModelField {
		public OpenUnitPortraitModelField(MDLLoadListener listener) {
			super(listener, "umdl");
		}

		@Override
		public String resolvePath(String path) {
			return ModelUtils.getPortrait(path);
		}
	}

	private abstract class OpenUnitModelField implements ActionListener {
		private MDLLoadListener listener;
		private String unitFieldRawcode;

		public OpenUnitModelField(MDLLoadListener listener, String unitFieldRawcode) {
			this.listener = listener;
			this.unitFieldRawcode = unitFieldRawcode;
		}

		public abstract String resolvePath(String path);

		@Override
		public void actionPerformed(ActionEvent e) {
			openStuff();
//			openStuff((p) -> resolvePath(p), unitFieldRawcode, listener);
		}

		private void openStuff() {
			TreePath currentUnitTreePath = UnitEditorTreeBrowser.this.getPathForLocation(rightClickX, rightClickY);
			if (currentUnitTreePath != null) {
				DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
				if (o.getUserObject() instanceof MutableGameObject) {
					MutableGameObject obj = (MutableGameObject) o.getUserObject();
					String path = UnitEditorTreeBrowser.this.convertPathToMDX(obj.getFieldAsString(War3ID.fromString(unitFieldRawcode), 0));
					String resolvedPath = resolvePath(path);
					BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
					ImageIcon icon = iconTexture == null ? null : new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
					listener.loadFile(resolvedPath, true, true, icon);
				}
			}
		}
	}

//	private void openStuff(Function<String, String> resolvePath, String unitFieldRawcode, MDLLoadListener listener) {
//		TreePath currentUnitTreePath = UnitEditorTreeBrowser.this.getPathForLocation(rightClickX, rightClickY);
//		if (currentUnitTreePath != null) {
//			DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
//			if (o.getUserObject() instanceof MutableGameObject) {
//				MutableGameObject obj = (MutableGameObject) o.getUserObject();
//				String path = UnitEditorTreeBrowser.this.convertPathToMDX(obj.getFieldAsString(War3ID.fromString(unitFieldRawcode), 0));
//				String resolvedPath = resolvePath.apply(path);
//				BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
//				ImageIcon icon = iconTexture == null ? null : new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
//				listener.loadFile(resolvedPath, true, true, icon);
//			}
//		}
//	}
}
