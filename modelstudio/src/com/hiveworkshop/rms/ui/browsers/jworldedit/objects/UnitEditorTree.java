package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.WorldEditorDataType;
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
import java.util.*;

public class UnitEditorTree extends JTree {
	private TopLevelCategoryFolder root;
	private MutableObjectData unitData;
	private final ObjectTabTreeBrowserBuilder browserBuilder;
	private WorldEditorDataType dataType;

	public UnitEditorTree(MutableObjectData unitData,
	                      ObjectTabTreeBrowserBuilder browserBuilder,
	                      UnitEditorSettings settings) {
		super(makeTreeModel(unitData, browserBuilder));
		this.unitData = unitData;
		this.browserBuilder = browserBuilder;
		this.dataType = unitData.getWorldEditorDataType();
		root = (TopLevelCategoryFolder) getModel().getRoot();
		setCellRenderer(new WarcraftObjectTreeCellRenderer(settings, unitData.getWorldEditorDataType()));
		addTreeExpansionListener(getTreeExpansionListener());
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

	private TreeExpansionListener getTreeExpansionListener() {
		return new TreeExpansionListener() {

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				TreePath expandedPath = event.getPath();
				Object lastPathComponent = expandedPath.getLastPathComponent();
				if (lastPathComponent instanceof AbstractSortingFolderTreeNode) {
					AbstractSortingFolderTreeNode folderTreeNode = (AbstractSortingFolderTreeNode) lastPathComponent;
					if (!folderTreeNode.isHasExpandedFirstTime()) {
						if (folderTreeNode.getChildCount() > 0) {
							TreeNode childAt = folderTreeNode.getChildAt(0);
							expandPath(expandedPath.pathByAddingChild(childAt));
						}
						folderTreeNode.setHasExpandedFirstTime(true);
					}
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {

			}
		};
	}

	private static UnitEditorTreeModel makeTreeModel(MutableObjectData unitData,
	                                                 ObjectTabTreeBrowserBuilder browserBuilder) {
		TopLevelCategoryFolder root = browserBuilder.build();
		TreeNodeLinker linker = new PreModelCreationTreeNodeLinker();
		for (War3ID alias : unitData.keySet()) {
			MutableGameObject unit = unitData.get(alias);
			if (unitData.getWorldEditorDataType().equals(WorldEditorDataType.UPGRADES)) {
				System.out.println("alias: " + alias + ", unit: " + unit);
			}
			root.insertObjectInto(unit, linker);
		}
		return new UnitEditorTreeModel(root);
	}

	public void reloadAllObjectDataVerySlowly() {
		setModel(makeTreeModel(unitData, browserBuilder));
		root = (TopLevelCategoryFolder) getModel().getRoot();
		selectFirstUnit();
	}

	public TopLevelCategoryFolder getRoot() {
		return root;
	}

	public void loadHotkeys() {
		this.getActionMap().put("deleteUnit", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				List<MutableGameObject> objectsToDelete = new ArrayList<>();
				for (TreePath path : getSelectionPaths()) {
					Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathComponent;
						if (treeNode.getUserObject() instanceof MutableGameObject) {
							MutableGameObject gameObject = (MutableGameObject) treeNode.getUserObject();
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

	public void find(String text, boolean displayAsRawData, boolean caseSensitive) {
		if (!caseSensitive) {
			text = text.toLowerCase();
		}

		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();

		boolean foundSelection = selectedNode == null;

		Enumeration<TreeNode> depthFirstEnum = root.depthFirstEnumeration();

		while (depthFirstEnum.hasMoreElements()) {
			TreeNode nextElement = depthFirstEnum.nextElement();
			if (foundSelection) {
				if (getSelectionPath111(text, displayAsRawData, caseSensitive, nextElement)) return;
			} else {
				foundSelection = nextElement == selectedNode;
			}
		}
		if (foundSelection && (selectedNode != null)) {
			depthFirstEnum = root.depthFirstEnumeration();
			while (depthFirstEnum.hasMoreElements()) {
				TreeNode nextElement = depthFirstEnum.nextElement();
				if (getSelectionPath111(text, displayAsRawData, caseSensitive, nextElement)) return;
			}
		}
	}

	private boolean getSelectionPath111(String text, boolean displayAsRawData, boolean caseSensitive, TreeNode nextElement) {
		System.out.println("getSelectionPath111");
		if (nextElement instanceof DefaultMutableTreeNode) {
			System.out.println("next element: " + nextElement);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) nextElement;
			LinkedList<TreeNode> nodesForPath = new LinkedList<>();
			if (matches(node, text, displayAsRawData, caseSensitive)) {
				TreePath path = new TreePath(root);
				TreeNode treeNode = node;
				while (treeNode.getParent() != null) {
					nodesForPath.addFirst(treeNode);
					treeNode = treeNode.getParent();
				}
				for (TreeNode treeNodeForPath : nodesForPath) {
					System.out.println("nodesForPath: " + nodesForPath);
					path = path.pathByAddingChild(treeNodeForPath);
				}
				setSelectionPath(path);
				scrollPathToVisible(path);
				return true;
			}
		}
		return false;
	}

	@Override
	public UnitEditorTreeModel getModel() {
		return (UnitEditorTreeModel) super.getModel();
	}

	public void selectFirstUnit() {
//		TreePath topTreePath = new TreePath(root);
//		while (((TreeNode) topTreePath.getLastPathComponent()).getChildCount() > 0) {
//			topTreePath = topTreePath.pathByAddingChild(((TreeNode) topTreePath.getLastPathComponent()).getChildAt(0));
//		}
//		setSelectionPath(topTreePath);
		TreeNode[] path = root.getFirstLeaf().getPath();
		System.out.println("TreeNodes: " + Arrays.toString(path));
		TreePath path1 = new TreePath(path);
		System.out.println("TreePath: " + path1);

		setSelectionPath(path1);

		System.out.println("getSelectionPath111");
	}

	public boolean matches(DefaultMutableTreeNode node, String text, boolean displayAsRawData, boolean caseSensitive) {
		if (node != null && node.getUserObject() instanceof MutableGameObject) {
			MutableGameObject obj = (MutableGameObject) node.getUserObject();
			String name = displayAsRawData ? MutableObjectData.getDisplayAsRawDataName(obj) : obj.getName();
			if (!caseSensitive) {
				name = name.toLowerCase();
			}
			return name.contains(text);
		}
		return false;
	}

	public void acceptPastedObjectData(War3ObjectDataChangeset changeset) {
		// ObjectMap custom = changeset.getCustom();
		// for(War3ID unitId: custom.keySet()) {
		// ObjectDataChangeEntry objectDataEntry = custom.get(unitId);
		// }
		unitData.mergeChangset(changeset);
	}

	public War3ObjectDataChangeset copySelectedObjects() {
		List<MutableGameObject> objectsToCopy = new ArrayList<>();
		for (TreePath path : getSelectionPaths()) {
			Object lastPathComponent = path.getLastPathComponent();
			if (lastPathComponent instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathComponent;
				if (treeNode.getUserObject() instanceof MutableGameObject) {
					MutableGameObject gameObject = (MutableGameObject) treeNode.getUserObject();
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

	public void setUnitDataAndReloadVerySlowly(MutableObjectData newUnitData) {
		this.unitData = newUnitData;
		reloadAllObjectDataVerySlowly();
	}

	public MutableGameObject getSelectedGameObject() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
		if (node != null && node.getUserObject() instanceof MutableGameObject) {
			return (MutableGameObject) node.getUserObject();
		}
		return null;
	}
}
