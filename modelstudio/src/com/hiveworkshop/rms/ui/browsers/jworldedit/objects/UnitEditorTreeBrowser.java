package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.function.Function;

public class UnitEditorTreeBrowser extends UnitEditorTree {
	private int rightClickX, rightClickY;

	public UnitEditorTreeBrowser(MutableObjectData unitData, ObjectTabTreeBrowserBuilder browserBuilder,
	                             UnitEditorSettings settings, WorldEditorDataType dataType) {
		super(unitData, browserBuilder, settings);

		selectFirstUnit();
		JPopupMenu popupMenu = new JPopupMenu();

		popupMenu.add(getMenuItem("Open", e -> openSelectedSubPart((p) -> p, "umdl")));
		popupMenu.add(getMenuItem("Open Portrait", e -> openSelectedSubPart(ModelUtils::getPortrait, "umdl")));

		JMenu projectileArtMenu = new JMenu("Open Projectile");
		projectileArtMenu.add(getMenuItem("Attack 1", e -> openSelectedSubPart((p) -> p, "ua1m")));
		projectileArtMenu.add(getMenuItem("Attack 2", e -> openSelectedSubPart((p) -> p, "ua2m")));
		popupMenu.add(projectileArtMenu);

		popupMenu.addSeparator();

		popupMenu.add(getMenuItem("Extract", e -> extractFile()));

		MouseAdapter umdl = getMouseAdapter(popupMenu);
		addMouseListener(umdl);
	}

	private JMenuItem getMenuItem(String text, ActionListener actionListener) {
		JMenuItem item = new JMenuItem(text);
		item.addActionListener(actionListener);
		return item;
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

	private MouseAdapter getMouseAdapter(JPopupMenu popupMenu) {
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
							openUnit();
						}

					}
				} catch (Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		};
	}

	private void openUnit() {
		MutableGameObject obj = getMutableGameObject();
		if (obj != null) {
			String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString("umdl"), 0));
			String portrait = ModelUtils.getPortrait(path);
			ImageIcon icon = getImageIcon(obj);

			System.err.println("loading: " + path);
			loadFile(path, true, true, icon);
			if (ProgramGlobals.getPrefs().isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
				loadFile(portrait, true, false, icon);
			}
		}
	}

	private void openSelectedSubPart(Function<String, String> resolvePath, String unitFieldRawcode) {
		MutableGameObject obj = getMutableGameObject();
		if (obj != null) {
			String path = convertPathToMDX(obj.getFieldAsString(War3ID.fromString(unitFieldRawcode), 0));

			System.err.println("loading: " + path);
			loadFile(resolvePath.apply(path), true, true, getImageIcon(obj));
		}
	}

	private ImageIcon getImageIcon(MutableGameObject obj) {
		BufferedImage iconTexture = IconUtils.getIcon(obj, WorldEditorDataType.UNITS);
		if(iconTexture == null){
			return null;
		}
		return new ImageIcon(iconTexture.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
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


	private void loadFile(String filePathMdx, boolean temporary, boolean selectNewTab, ImageIcon icon){
		InputStream resourceAsStream = GameDataFileSystem.getDefault().getResourceAsStream(filePathMdx);
		InternalFileLoader.loadStreamMdx(resourceAsStream, temporary, selectNewTab, icon);
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
