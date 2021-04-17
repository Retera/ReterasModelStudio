package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData.WorldEditorDataType;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.AbstractSortingFolderTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.PreModelCreationTreeNodeLinker;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinker;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class UnitEditorTree extends JTree {
	private TopLevelCategoryFolder root;
	private MutableObjectData unitData;
	private final ObjectTabTreeBrowserBuilder browserBuilder;

	public UnitEditorTree(final MutableObjectData unitData, final ObjectTabTreeBrowserBuilder browserBuilder,
			final UnitEditorSettings settings, final WorldEditorDataType dataType) {
		super(makeTreeModel(unitData, browserBuilder));
		this.unitData = unitData;
		this.browserBuilder = browserBuilder;
		root = (TopLevelCategoryFolder) getModel().getRoot();
		setCellRenderer(new WarcraftObjectTreeCellRenderer(settings, dataType));
		addTreeExpansionListener(new TreeExpansionListener() {

			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				final TreePath expandedPath = event.getPath();
				final Object lastPathComponent = expandedPath.getLastPathComponent();
				if (lastPathComponent instanceof AbstractSortingFolderTreeNode) {
					final AbstractSortingFolderTreeNode folderTreeNode = (AbstractSortingFolderTreeNode) lastPathComponent;
					if (!folderTreeNode.isHasExpandedFirstTime()) {
						if (folderTreeNode.getChildCount() > 0) {
							final TreeNode childAt = folderTreeNode.getChildAt(0);
							expandPath(expandedPath.pathByAddingChild(childAt));
						}
						folderTreeNode.setHasExpandedFirstTime(true);
					}
				}
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {

			}
		});
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
//		setFont(getFont().deriveFont(24f));
	}

	public void loadHotkeys() {
		this.getActionMap().put("deleteUnit", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final List<MutableGameObject> objectsToDelete = new ArrayList<>();
				for (final TreePath path : getSelectionPaths()) {
					final Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof DefaultMutableTreeNode) {
						final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathComponent;
						if (treeNode.getUserObject() instanceof MutableGameObject) {
							final MutableGameObject gameObject = (MutableGameObject) treeNode.getUserObject();
							objectsToDelete.add(gameObject);
						}
					}
				}
				unitData.remove(objectsToDelete);
			}
		});
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteUnit");
	}

	public void reloadAllObjectDataVerySlowly() {
		setModel(makeTreeModel(unitData, browserBuilder));
		root = (TopLevelCategoryFolder) getModel().getRoot();
		selectFirstUnit();
	}

	public TopLevelCategoryFolder getRoot() {
		return root;
	}

	public void find(String text, final boolean displayAsRawData, final boolean caseSensitive) {
		if (!caseSensitive) {
			text = text.toLowerCase();
		}

		final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();

		boolean foundSelection = selectedNode == null;
		Enumeration<TreeNode> depthFirstEnum = root.depthFirstEnumeration();
		while (depthFirstEnum.hasMoreElements()) {
			final Object nextElement = depthFirstEnum.nextElement();
			if (foundSelection) {
				if (nextElement instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nextElement;
					final LinkedList<TreeNode> nodesForPath = new LinkedList<>();
					if (matches(node, text, displayAsRawData, caseSensitive)) {
						TreePath path = new TreePath(root);
						TreeNode treeNode = node;
						while (treeNode.getParent() != null) {
							nodesForPath.addFirst(treeNode);
							treeNode = treeNode.getParent();
						}
						for (final TreeNode treeNodeForPath : nodesForPath) {
							path = path.pathByAddingChild(treeNodeForPath);
						}
						setSelectionPath(path);
						scrollPathToVisible(path);
						return;
					}
				}
			} else {
				foundSelection = nextElement == selectedNode;
			}
		}
		if (foundSelection && (selectedNode != null)) {
			depthFirstEnum = root.depthFirstEnumeration();
			while (depthFirstEnum.hasMoreElements()) {
				final Object nextElement = depthFirstEnum.nextElement();
				if (nextElement instanceof DefaultMutableTreeNode) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nextElement;
					final LinkedList<TreeNode> nodesForPath = new LinkedList<>();
					if (matches(node, text, displayAsRawData, caseSensitive)) {
						TreePath path = new TreePath(root);
						TreeNode treeNode = node;
						while (treeNode.getParent() != null) {
							nodesForPath.addFirst(treeNode);
							treeNode = treeNode.getParent();
						}
						for (final TreeNode treeNodeForPath : nodesForPath) {
							path = path.pathByAddingChild(treeNodeForPath);
						}
						setSelectionPath(path);
						scrollPathToVisible(path);
						return;
					}
				}
			}
		}
	}

	public boolean matches(final DefaultMutableTreeNode node, final String text, final boolean displayAsRawData,
			final boolean caseSensitive) {
		if ((node != null) && (node.getUserObject() instanceof MutableGameObject)) {
			final MutableGameObject obj = (MutableGameObject) node.getUserObject();
			String name = displayAsRawData ? MutableObjectData.getDisplayAsRawDataName(obj) : obj.getName();
			if (!caseSensitive) {
				name = name.toLowerCase();
			}
			return name.contains(text);
		}
		return false;
	}

	@Override
	public UnitEditorTreeModel getModel() {
		return (UnitEditorTreeModel) super.getModel();
	}

	public void selectFirstUnit() {
		TreePath topTreePath = new TreePath(root);
		while (((TreeNode) topTreePath.getLastPathComponent()).getChildCount() > 0) {
			topTreePath = topTreePath.pathByAddingChild(((TreeNode) topTreePath.getLastPathComponent()).getChildAt(0));
		}
		setSelectionPath(topTreePath);
	}

	private static UnitEditorTreeModel makeTreeModel(final MutableObjectData unitData,
			final ObjectTabTreeBrowserBuilder browserBuilder) {
		final TopLevelCategoryFolder root = browserBuilder.build();
		final TreeNodeLinker linker = new PreModelCreationTreeNodeLinker();
		for (final War3ID alias : unitData.keySet()) {
			final MutableGameObject unit = unitData.get(alias);
			root.insertObjectInto(unit, linker);
		}
		return new UnitEditorTreeModel(root);
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

	public char getWar3ObjectDataChangesetKindChar() {
		return unitData.getEditorData().getExpectedKind();
	}

	public WorldEditorDataType getDataType() {
		return unitData.getWorldEditorDataType();
	}

	public void setUnitDataAndReloadVerySlowly(final MutableObjectData newUnitData) {
		this.unitData = newUnitData;
		reloadAllObjectDataVerySlowly();
	}

	public MutableGameObject getSelectedGameObject() {
		final DefaultMutableTreeNode o = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if ((o != null) && (o.getUserObject() instanceof MutableGameObject)) {
			return (MutableGameObject) o.getUserObject();
		}
		return null;
	}
}
