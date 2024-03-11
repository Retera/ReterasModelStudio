package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.WE_Field;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UnitEditorTreeBrowser extends UnitEditorTree {
	private int rightClickX, rightClickY;

	public UnitEditorTreeBrowser(ObjectTabTreeBrowserBuilder browserBuilder,
	                             UnitEditorSettings settings) {
		super(browserBuilder, settings);

		selectFirstUnit();

		UnitBrowserPopupMenu popupMenu = new UnitBrowserPopupMenu(this::getMutableGameObject);

		MouseAdapter mouseAdapter = getMouseAdapter(popupMenu);
		addMouseListener(mouseAdapter);
	}

	private MutableGameObject getMutableGameObject() {
		TreePath currentUnitTreePath = getPathForLocation(rightClickX, rightClickY);
		if (currentUnitTreePath != null
				&& currentUnitTreePath.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode
				&& treeNode.getUserObject() instanceof MutableGameObject gameObject) {

			return gameObject;
		}
		return null;
	}

	private MouseAdapter getMouseAdapter(UnitBrowserPopupMenu popupMenu) {
		return new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (SwingUtilities.isRightMouseButton(e)) {
						rightClickX = e.getX();
						rightClickY = e.getY();
						popupMenu.show(UnitEditorTreeBrowser.this, e.getX(), e.getY());
					} else {
						if (2 <= e.getClickCount()) {
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
			String filepath = obj.getFieldAsString(WE_Field.MODEL_FILE.getId(), 0);
			InternalFileLoader.loadMdxStream(obj, filepath, true);
		}
	}
}
