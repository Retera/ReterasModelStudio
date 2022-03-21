package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDoodadData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoodadBrowserView extends View {
	private static ImageIcon icon = new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST));

	public DoodadBrowserView() {
		super("Doodad Browser", icon, new JScrollPane(getUnitEditorTree()));
	}

	private static UnitEditorTree getUnitEditorTree() {
		UnitEditorTree unitEditorTree = new UnitEditorTree(getDoodadData(), new DoodadTabTreeBrowserBuilder(), new UnitEditorSettings());
		unitEditorTree.selectFirstUnit();

		unitEditorTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				try {
					doodadViewerMouseClick(e, unitEditorTree);
				} catch (final Exception exc) {
					exc.printStackTrace();
					ExceptionPopup.display(exc);
				}
			}
		});
		return unitEditorTree;
	}

	public static MutableObjectData getDoodadData() {
//		War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
//		try {
//			CompoundDataSource fs = GameDataFileSystem.getDefault();
//			if (fs.has("war3map.w3d")) {
//				BlizzardDataInputStream stream = new BlizzardDataInputStream(fs.getResourceAsStream("war3map.w3d"));
//				WTS wts = fs.has("war3map.wts") ? new WTSFile(fs.getResourceAsStream("war3map.wts")) : null;
//				editorData.load(stream, wts, true);
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
		return new MutableDoodadData();
	}

	private static void doodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree) {
		if (e.getClickCount() >= 2) {
			TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
			if (currentUnitTreePath != null) {

				DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
				if (o.getUserObject() instanceof MutableGameObject) {

					MutableGameObject obj = (MutableGameObject) o.getUserObject();
					loadAllVariations(obj);
				}
			}
		}
	}

	private static void loadAllVariations(MutableGameObject obj) {
		int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("dvar"), 0);
		boolean addVarIndex = 1 < numberOfVariations;
		ImageIcon icon = new ImageIcon(IconUtils
				.getIcon(obj)
				.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		for (int i = 0; i < numberOfVariations; i++) {
			String prePath = obj.getFieldAsString(War3ID.fromString("dfil"), 0) + (addVarIndex ? i : "");
			InternalFileLoader.loadFromStream(prePath, icon, i == 0);
		}
	}
}
