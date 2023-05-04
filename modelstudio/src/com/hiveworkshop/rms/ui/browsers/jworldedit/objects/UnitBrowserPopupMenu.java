package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

public class UnitBrowserPopupMenu extends JPopupMenu {
	private final Supplier<MutableGameObject> objectSupplier;
	private final JMenu projectileMenu;
	private final JMenuItem portraitMenu;
	private final JMenuItem portraitExp;
	private final JMenuItem projectile1;
	private final JMenuItem projectile1Exp;
	private final JMenuItem projectile2;
	private final JMenuItem projectile2Exp;

	public UnitBrowserPopupMenu(Supplier<MutableGameObject> objectSupplier) {
		this.objectSupplier = objectSupplier;
		add(getMenuItem("Open", KeyEvent.VK_O, e -> openSelectedSubPart(WE_Field.MODEL_FILE.getId(), false)));
		portraitMenu = getMenuItem("Open Portrait", KeyEvent.VK_P, e -> openSelectedSubPart(WE_Field.MODEL_FILE.getId(), true));
		add(portraitMenu);

		projectileMenu = new JMenu("Open Projectile");
		projectileMenu.setMnemonic(KeyEvent.VK_J);
		projectile1 = getMenuItem("Attack 1", KeyEvent.VK_1, e -> openSelectedSubPart(WE_Field.UNIT_PROJECTILE_1.getId(), false));
		projectile2 = getMenuItem("Attack 2", KeyEvent.VK_2, e -> openSelectedSubPart(WE_Field.UNIT_PROJECTILE_2.getId(), false));
		projectileMenu.add(projectile1);
		projectileMenu.add(projectile2);
		add(projectileMenu);

		addSeparator();
		JMenu extractMenu = new JMenu("Extract...");

		JMenuItem modelExp = getMenuItem("Model", KeyEvent.VK_E, e -> extractFile(WE_Field.MODEL_FILE.getId(), false));
		portraitExp = getMenuItem("Portrait", KeyEvent.VK_P, e -> extractFile(WE_Field.MODEL_FILE.getId(), true));
		projectile1Exp = getMenuItem("Attack 1", KeyEvent.VK_1, e -> extractFile(WE_Field.UNIT_PROJECTILE_1.getId(), false));
		projectile2Exp = getMenuItem("Attack 2", KeyEvent.VK_2, e -> extractFile(WE_Field.UNIT_PROJECTILE_2.getId(), false));
		extractMenu.add(modelExp);
		extractMenu.add(portraitExp);
		extractMenu.add(projectile1Exp);
		extractMenu.add(projectile2Exp);
		add(extractMenu);


		addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				MutableGameObject obj = objectSupplier.get();
				if (obj != null) {
					boolean proj1Exists = !obj.getFieldAsString(WE_Field.UNIT_PROJECTILE_1.getId(), 0).isEmpty();
					boolean proj2Exists = !obj.getFieldAsString(WE_Field.UNIT_PROJECTILE_2.getId(), 0).isEmpty();
					projectileMenu.setEnabled(proj1Exists || proj2Exists);
					projectile1.setEnabled(proj1Exists);
					projectile2.setEnabled(proj2Exists);
					projectile1Exp.setEnabled(proj1Exists);
					projectile2Exp.setEnabled(proj2Exists);

					String portraitPath = ModelUtils.getPortrait(obj.getFieldAsString(WE_Field.MODEL_FILE.getId(), 0));
					portraitPath = ImportFileActions.convertPathToMDX(portraitPath);
					boolean portraitExists = GameDataFileSystem.getDefault().has(portraitPath);
					portraitMenu.setEnabled(portraitExists);
					portraitExp.setEnabled(portraitExists);
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});
	}

	private JMenuItem getMenuItem(String text, int mnemonic, ActionListener actionListener) {
		JMenuItem item = new JMenuItem(text, mnemonic);
		item.addActionListener(actionListener);
		return item;
	}

	private void openSelectedSubPart(War3ID id, boolean portrait) {
		MutableGameObject obj = objectSupplier.get();
		if (obj != null) {
			String filepath = getFilePath(id, portrait, obj);

			System.err.println("loading: " + filepath);
			InternalFileLoader.loadFilepathMdx(filepath, true, true, IconUtils.getIconScaled(obj), false);
		}
	}

	private void extractFile(War3ID id, boolean portrait) {
		MutableGameObject obj = objectSupplier.get();
		if (obj != null) {
			String filepath = getFilePath(id, portrait, obj);
			ExportInternal.exportInternalFile(filepath, "Unit", this);
		}
	}
	private String getFilePath(War3ID id, boolean portrait, MutableGameObject obj) {
		String filepath = obj.getFieldAsString(id, 0);
		filepath = portrait ? ModelUtils.getPortrait(filepath) : filepath;
		return filepath;
	}
}
