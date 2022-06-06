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
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class UnitEditorTree extends JTree {
	private TopLevelCategoryFolder root;
	private MutableObjectData unitData;
	private final ObjectTabTreeBrowserBuilder browserBuilder;
	private WorldEditorDataType dataType;

	public UnitEditorTree(ObjectTabTreeBrowserBuilder browserBuilder,
	                      UnitEditorSettings settings) {
		super(makeTreeModel(browserBuilder));
		this.browserBuilder = browserBuilder;
		this.unitData = browserBuilder.getUnitData();
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

	public JMenuBar getSearchBar(){
		JMenuBar menuBar = new JMenuBar();
		JTextField searchField = new JTextField();
		Dimension prefSize = searchField.getPreferredSize();
		prefSize.width = 100;
		searchField.setMinimumSize(prefSize);
		searchField.setPreferredSize(prefSize);
		searchField.addKeyListener(getSearchOnEnter(searchField));
		menuBar.add(searchField);
		return menuBar;
	}
	public KeyAdapter getSearchOnEnter(JTextField searchField) {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keyCode: " + e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					find(searchField.getText(), false, false);
				}
			}
		};
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

	private static UnitEditorTreeModel makeTreeModel(ObjectTabTreeBrowserBuilder browserBuilder) {
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
		setModel(makeTreeModel(browserBuilder));
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
				deleteUnit();
			}
		});
		this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteUnit");
	}

	private void deleteUnit() {
		List<MutableGameObject> objectsToDelete = getSelectedGameObjects();
		unitData.remove(objectsToDelete);
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
		TreeNode[] path = root.getFirstLeaf().getPath();
		TreePath path1 = new TreePath(path);

		setSelectionPath(path1);
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
		List<MutableGameObject> objectsToCopy = getSelectedGameObjects();
		return unitData.copySelectedObjects(objectsToCopy);
	}

	public List<MutableGameObject> getSelectedGameObjects() {
		List<MutableGameObject> selectedObjects = new ArrayList<>();
		TreePath[] selectionPaths = getSelectionPaths();
		if (selectionPaths != null) {
			for (TreePath path : selectionPaths) {
				Object lastPathComponent = path.getLastPathComponent();
				if (lastPathComponent instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) lastPathComponent;
					if (treeNode.getUserObject() instanceof MutableGameObject) {
						MutableGameObject gameObject = (MutableGameObject) treeNode.getUserObject();
						selectedObjects.add(gameObject);
					}
				}
			}
		}
		return selectedObjects;
	}

	public char getWar3ObjectDataChangesetKindChar() {
		return unitData.getEditorData().getExpectedKind();
	}

	public WorldEditorDataType getDataType() {
		return unitData.getWorldEditorDataType();
	}

	public void setUnitDataAndReloadVerySlowly() {
		this.unitData = browserBuilder.reloadAndGetUnitData();
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
