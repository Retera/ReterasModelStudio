package com.hiveworkshop.wc3.jworldedit.objects;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.etheller.collections.ArrayList;
import com.etheller.collections.List;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.PreModelCreationTreeNodeLinker;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.TreeNodeLinker;
import com.hiveworkshop.wc3.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.wc3.units.objectdata.War3ID;
import com.hiveworkshop.wc3.units.objectdata.War3ObjectDataChangeset;

public final class UnitEditorTree extends JTree {

	private final TopLevelCategoryFolder root;
	private final MutableObjectData unitData;

	public UnitEditorTree(final MutableObjectData unitData, final ObjectTabTreeBrowserBuilder browserBuilder,
			final UnitEditorSettings settings, final WorldEditorDataType dataType) {
		super(makeTreeModel(unitData, browserBuilder));
		this.unitData = unitData;
		root = (TopLevelCategoryFolder) getModel().getRoot();
		setCellRenderer(new WarcraftObjectTreeCellRenderer(settings, dataType));
		setRootVisible(false);
		setScrollsOnExpand(true);
		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				repaint();
			}

			@Override
			public void focusGained(final FocusEvent e) {
				repaint();
			}
		});
	}

	public TopLevelCategoryFolder getRoot() {
		return root;
	}

	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	public void selectFirstUnit() {
		TreePath topTreePath = new TreePath(root);
		while (((TreeNode) topTreePath.getLastPathComponent()).getChildCount() > 0) {
			topTreePath = topTreePath.pathByAddingChild(((TreeNode) topTreePath.getLastPathComponent()).getChildAt(0));
		}
		setSelectionPath(topTreePath);
	}

	private static DefaultTreeModel makeTreeModel(final MutableObjectData unitData,
			final ObjectTabTreeBrowserBuilder browserBuilder) {
		final TopLevelCategoryFolder root = browserBuilder.build();
		final TreeNodeLinker linker = new PreModelCreationTreeNodeLinker();
		for (final War3ID alias : unitData.keySet()) {
			final MutableGameObject unit = unitData.get(alias);
			root.insertObjectInto(unit, linker);
		}
		return (new DefaultTreeModel(root));
	}

	public void acceptPastedObjectData(final War3ObjectDataChangeset changeset) {
		// ObjectMap custom = changeset.getCustom();
		// for(War3ID unitId: custom.keySet()) {
		// ObjectDataChangeEntry objectDataEntry = custom.get(unitId);
		// }
		unitData.mergeChangset(changeset);
	}

	public War3ObjectDataChangeset copySelectedObjects() {
		final List<MutableGameObject> objectsToCopy = new ArrayList<>();
		for (final TreePath path : getSelectionPaths()) {
			final Object lastPathComponent = path.getLastPathComponent();
			if (lastPathComponent instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathComponent;
				if (treeNode.getUserObject() instanceof MutableGameObject) {
					final MutableGameObject gameObject = (MutableGameObject) treeNode.getUserObject();
					objectsToCopy.add(gameObject);
				}
			}
		}
		return unitData.copySelectedObjects(objectsToCopy);
	}

	public MutableGameObject getSelectedGameObject() {
		final DefaultMutableTreeNode o = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (o != null && o.getUserObject() instanceof MutableGameObject) {
			final MutableGameObject obj = (MutableGameObject) o.getUserObject();
			return obj;
		}
		return null;
	}
}
