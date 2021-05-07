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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class UnitEditorTreeBrowser extends UnitEditorTree {
	private int rightClickX, rightClickY;

	public UnitEditorTreeBrowser(MutableObjectData unitData, ObjectTabTreeBrowserBuilder browserBuilder,
	                             UnitEditorSettings settings, WorldEditorDataType dataType, MDLLoadListener listener) {
		super(unitData, browserBuilder, settings, dataType);

		selectFirstUnit();
		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(e -> openSelectedSubPart((p) -> p, "umdl", listener));
		popupMenu.add(openItem);

		JMenuItem openPortraitItem = new JMenuItem("Open Portrait");
		openPortraitItem.addActionListener(e -> openSelectedSubPart((p) -> ModelUtils.getPortrait(p), "umdl", listener));
		popupMenu.add(openPortraitItem);

		JMenu projectileArtMenu = new JMenu("Open Projectile");

		JMenuItem openProjectileItem = new JMenuItem("Attack 1");
		openProjectileItem.addActionListener(e -> openSelectedSubPart((p) -> p, "ua1m", listener));
		projectileArtMenu.add(openProjectileItem);

		JMenuItem openProjectile2Item = new JMenuItem("Attack 2");
		openProjectile2Item.addActionListener(e -> openSelectedSubPart((p) -> p, "ua2m", listener));
		projectileArtMenu.add(openProjectile2Item);
		popupMenu.add(projectileArtMenu);
		popupMenu.addSeparator();

		JMenuItem extract = new JMenuItem("Extract");
		popupMenu.add(extract);

		extract.addActionListener(e -> extractFile());
		MouseAdapter umdl = getMouseAdapter(listener, popupMenu);
		addMouseListener(umdl);
	}

	private MutableGameObject getMutableGameObject() {
		MutableGameObject obj = null;
		TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
		if (currentUnitTreePath != null) {
			DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
			if (o.getUserObject() instanceof MutableGameObject) {
				obj = (MutableGameObject) o.getUserObject();
			}
		}
		return obj;
	}

	private MouseAdapter getMouseAdapter(MDLLoadListener listener, JPopupMenu popupMenu) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClickX = e.getX();
						rightClickY = e.getY();
						popupMenu.show(UnitEditorTreeBrowser.this, e.getX(), e.getY());
					} else {
						if (e.getClickCount() >= 2) {
							rightClickX = e.getX();
							rightClickY = e.getY();
							openUnit(listener);
						}

					}
				} catch (Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		};
	}

	private void openUnit(MDLLoadListener listener) {
		MutableGameObject obj = getMutableGameObject();
		if (obj != null) {
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

	private void openSelectedSubPart(Function<String, String> resolvePath, String unitFieldRawcode, MDLLoadListener listener) {
		MutableGameObject obj = getMutableGameObject();
		if (obj != null) {
			String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString(unitFieldRawcode), 0));

			System.err.println("loading: " + path);
			BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
			ImageIcon icon = iconTexture == null ? null : new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
			listener.loadFile(resolvePath.apply(path), true, true, icon);
		}
	}

	private void extractFile() {
		MutableGameObject obj = getMutableGameObject();
		if (obj != null) {
			System.out.println("objString: " + obj.getFieldAsString(War3ID.fromString("umdl"), 0));
			String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
			System.out.println("path: " + path);

			FileDialog fileDialog = new FileDialog(this);
			fileDialog.exportInternalFile(path);
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
}
