package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableDestructibleData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.War3ID;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DestructibleBrowserView extends View {
	private static ImageIcon icon = new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST));

	public DestructibleBrowserView() {
		super("Destructible Browser", icon, new JScrollPane(getUnitEditorTree()));
	}

	private static UnitEditorTree getUnitEditorTree() {
		UnitEditorTree unitEditorTree = new UnitEditorTree(getDestructibleData(), new DestructableTabTreeBrowserBuilder(), new UnitEditorSettings());
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

	public static MutableObjectData getDestructibleData() {
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
		return new MutableDestructibleData();
	}

	private static void doodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree) {
		if (e.getClickCount() >= 2) {
			TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
			if (currentUnitTreePath != null) {

				DefaultMutableTreeNode o = (DefaultMutableTreeNode) currentUnitTreePath.getLastPathComponent();
				if (o.getUserObject() instanceof MutableGameObject) {

					MutableGameObject obj = (MutableGameObject) o.getUserObject();
					int numberOfVariations = obj.getFieldAsInteger(War3ID.fromString("bvar"), 0);
					if (numberOfVariations > 1) {
						for (int i = 0; i < numberOfVariations; i++) {
							String prePath = obj.getFieldAsString(War3ID.fromString("bfil"), 0) + i + ".mdl";
							InternalFileLoader.loadMdxStream(obj, prePath, i == 0);
						}
					} else {
						String prePath = obj.getFieldAsString(War3ID.fromString("bfil"), 0);
						InternalFileLoader.loadMdxStream(obj, prePath, true);
					}
					MenuBar.setToolsMenuEnabled(true);
				}
			}
		}
	}
}
