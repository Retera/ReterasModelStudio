package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public class InternalFileLoader {
	public static void loadStreamMdx(InputStream f, boolean temporary, boolean selectNewTab, ImageIcon icon) {
		ModelPanel temp;
		try {
			final EditableModel model = MdxUtils.loadEditable(f);
			model.setFileRef(null);
			temp = ModelLoader.newTempModelPanel(icon, model);
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		}

		ModelLoader.loadModel(temporary, selectNewTab, temp);
	}

	static void loadMdxStream(MutableObjectData.MutableGameObject obj, String prePath, boolean b) {
		final String path = ImportFileActions.convertPathToMDX(prePath);
		final String portrait = ModelUtils.getPortrait(path);
		final ImageIcon icon = new ImageIcon(IconUtils
				.getIcon(obj, MutableObjectData.WorldEditorDataType.DOODADS)
				.getScaledInstance(16, 16, Image.SCALE_DEFAULT));

		loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(path), true, b, icon);

		if (ProgramGlobals.getPrefs().isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
			loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
		}
	}

	public static void OpenDoodadViewer() {
		UnitEditorTree unitEditorTree = new UnitEditorTree(
				MenuBarActions.getDoodadData(),
				new DoodadTabTreeBrowserBuilder(),
				MainLayoutCreator.getUnitEditorSettings(),
				MutableObjectData.WorldEditorDataType.DOODADS);
		unitEditorTree.selectFirstUnit();

		unitEditorTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				try {
					dodadViewerMouseClick(e, unitEditorTree);
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		View doodadBrowserView = new View("Doodad Browser", new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)), new JScrollPane(unitEditorTree));
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(), doodadBrowserView));
	}

	private static void dodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree) {
		if (e.getClickCount() >= 2) {
			TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
			if (currentUnitTreePath != null) {

				DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
				if (o.getUserObject() instanceof MutableObjectData.MutableGameObject) {

					MutableObjectData.MutableGameObject obj = (MutableObjectData.MutableGameObject) o.getUserObject();
					int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
					if (numberOfVariations > 1) {
						for (int i = 0; i < numberOfVariations; i++) {
							String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + i + ".mdl";
							loadMdxStream(obj, prePath, i == 0);
						}
					} else {
						String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0);
						loadMdxStream(obj, prePath, true);
					}
					MenuBar.setToolsMenuEnabled(true);
				}
			}
		}
	}

	public static void fetchObject() {
		MutableObjectData.MutableGameObject objectFetched = ImportFileActions.fetchObject();
		if (objectFetched != null) {

			String filepath = ImportFileActions.convertPathToMDX(objectFetched.getFieldAsString(UnitFields.MODEL_FILE, 0));
			ImageIcon icon = new ImageIcon(BLPHandler.get().getGameTex(objectFetched.getFieldAsString(UnitFields.INTERFACE_ICON, 0)).getScaledInstance(16, 16, Image.SCALE_FAST));

			loadFromStream(filepath, icon);
		}
	}

	public static void fetchModel() {
//		ModelOptionPane.ModelElement model = ImportFileActions.fetchModel();
		ModelOptionPane.ModelElement model = ModelOptionPane.fetchModel(ProgramGlobals.getMainPanel());
		if (model != null) {

			String filepath = ImportFileActions.convertPathToMDX(model.getFilepath());
			ImageIcon icon = model.hasCachedIconPath() ? new ImageIcon(BLPHandler.get().getGameTex(model.getCachedIconPath()).getScaledInstance(16, 16, Image.SCALE_FAST)) : ModelLoader.MDLIcon;

			loadFromStream(filepath, icon);
		}
	}

	public static void fetchUnit() {
//		GameObject unitFetched = ImportFileActions.fetchUnit();
		GameObject unitFetched = UnitOptionPane.fetchUnit(ProgramGlobals.getMainPanel());;
		if (unitFetched != null) {

			String filepath = ImportFileActions.convertPathToMDX(unitFetched.getField("file"));
			ImageIcon icon = unitFetched.getScaledIcon(16);

			loadFromStream(filepath, icon);
		}
	}

	public static void loadFromStream(String filepath, ImageIcon icon) {
		if (filepath != null) {

			loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(filepath), true, true, icon);

			String portrait = filepath.substring(0, filepath.lastIndexOf('.')) + "_portrait" + filepath.substring(filepath.lastIndexOf('.'));

			if (ProgramGlobals.getPrefs().isLoadPortraits() && GameDataFileSystem.getDefault().has(portrait)) {
				loadStreamMdx(GameDataFileSystem.getDefault().getResourceAsStream(portrait), true, false, icon);
			}
			MenuBar.setToolsMenuEnabled(true);
		}
	}
}
