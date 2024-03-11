package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.ui.icons.IconUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import net.infonode.docking.View;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoodadBrowserView extends View {
	private static final ImageIcon icon = new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST));

	public DoodadBrowserView() {
		super("Doodad Browser", icon, new JScrollPane(getUnitEditorTree()));
	}

	private static UnitEditorTree getUnitEditorTree() {
		UnitEditorTree unitEditorTree = new UnitEditorTree(new DoodadTabTreeBrowserBuilder(), new UnitEditorSettings());
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

	private static void doodadViewerMouseClick(MouseEvent e, UnitEditorTree unitEditorTree) {
		if (2 <= e.getClickCount()) {
			TreePath currentUnitTreePath = unitEditorTree.getSelectionPath();
			if (currentUnitTreePath != null
					&& currentUnitTreePath.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode
					&& treeNode.getUserObject() instanceof MutableGameObject gameObject) {
				loadAllVariations(gameObject);
			}
		}
	}

	private static void loadAllVariations(MutableGameObject obj) {
		int numberOfVariations = obj.getFieldAsInteger(WE_Field.DOODAD_VARIATIONS_FIELD.getId(), 0);
		boolean addVarIndex = 1 < numberOfVariations;
		ImageIcon icon = new ImageIcon(IconUtils
				.getIcon(obj)
				.getScaledInstance(16, 16, Image.SCALE_DEFAULT));
		for (int i = 0; i < numberOfVariations; i++) {
			String prePath = obj.getFieldAsString(WE_Field.DOODAD_FILE.getId(), 0) + (addVarIndex ? i : "");
			InternalFileLoader.loadFromStream(prePath, icon, i == 0);
		}
	}
}
