package com.hiveworkshop.rms.ui.preferences.dataSourceChooser;

import com.hiveworkshop.rms.filesystem.sources.CascDataSourceDescriptor;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.util.uiFactories.Button;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class DataSourceTree extends JTree {
	private final DefaultMutableTreeNode root;
	private final DefaultTreeModel model;
	private final List<DataSourceDescriptor> dataSourceDescriptors = new ArrayList<>();
	private final Component popupParent;

	private JButton addDefaultCascPrefixes;
	private JButton addSpecificCascPrefix;
	private JButton deleteButton;
	private JButton moveUpButton;
	private JButton moveDownButton;

	public DataSourceTree(List<DataSourceDescriptor> dataSourceDescriptors, Component popupParent) {
		this.dataSourceDescriptors.addAll(dataSourceDescriptors);
		this.root = new DefaultMutableTreeNode();
		this.model = new DefaultTreeModel(this.root);
		this.popupParent = popupParent;
		setModel(this.model);

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		setRootVisible(false);
		createButtons();
		addTreeSelectionListener(this::updateButtons);
		setCellRenderer(new DataTreeRenderer());
		buildTree();
	}

	public void move(boolean up) {
		TreePath[] selectionPaths = getSelectionPaths();
		int dir = up ? -1 : 1;
		if (selectionPaths != null && 0 < selectionPaths.length) {
			for (TreePath selectionPath : selectionPaths) {
				DataSourceTreeNode<?> node = getNode(selectionPath);
				if (node != null) {
					DataSourceDescriptor descriptor = node.descriptor;
					if (descriptor != null) {
						int indexOf = dataSourceDescriptors.indexOf(descriptor);
						if (indexOf + dir >= 0 && indexOf + dir < dataSourceDescriptors.size()) {
							Collections.swap(dataSourceDescriptors, indexOf, indexOf + dir);
						}
					}
					node.move(model, dir);
					TreePath path = new TreePath(model.getPathToRoot(node));
					expandPath(path);
					setSelectionPath(path);
				}
			}
		}
	}

	private void createButtons() {
		addDefaultCascPrefixes = Button.create("Add Default CASC Mod", e -> addDefaultCASCMod());
		addSpecificCascPrefix = Button.create("Add Specific CASC Mod", e -> addSpecificCASCMod());
		deleteButton = Button.create("Delete Selection", e -> deleteSelection());
		moveUpButton = Button.create("Move Up", e -> move(true));
		moveDownButton = Button.create("Move Down", e -> move(false));
	}

	private void updateButtons(TreeSelectionEvent e) {
		TreePath selectionPath = e.getNewLeadSelectionPath();
		boolean cascSelected = isCascSelected(selectionPath);
		addDefaultCascPrefixes.setEnabled(cascSelected);
		addSpecificCascPrefix.setEnabled(cascSelected);

		deleteButton.setEnabled(selectionPath != null);

		moveUpButton.setEnabled(canMoveUp(selectionPath));
		moveDownButton.setEnabled(canMoveDown(selectionPath));
	}
	public JButton getAddDefaultCascButton() {
		return addDefaultCascPrefixes;
	}
	public JButton getAddSpecificCascButton() {
		return addSpecificCascPrefix;
	}
	public JButton getDeleteButton() {
		return deleteButton;
	}
	public JButton getMoveUpButton() {
		return moveUpButton;
	}
	public JButton getMoveDownButton() {
		return moveDownButton;
	}


	public void deleteSelection() {
		TreePath[] selectionPaths = getSelectionPaths();
		for (TreePath selectionPath : selectionPaths) {
			DataSourceTreeNode<?> node = getNode(selectionPath);
			if (node != null) {
				DataSourceDescriptor descriptor = node.getDescriptor();
				if (descriptor != null) {
					dataSourceDescriptors.remove(descriptor);
				}
				node.remove(model);
			}
		}
	}

	public void addDefaultCASCMod() {
		DataSourceTreeNode<?> node = getNode(getSelectionPath());
		if (node != null) {
			node.addDefaultCASCMod(model, popupParent);
		}
	}

	public void addSpecificCASCMod() {
		DataSourceTreeNode<?> node = getNode(getSelectionPath());
		if (node != null) {
			node.addSpecificCASCMod(model, popupParent);
		}
	}

	public void rebuildTree() {
		Set<String> selectionSet = new HashSet<>();
		TreePath[] selectionPaths = getSelectionPaths();
		int rowCount = getRowCount();

		if (selectionPaths != null) {
			// collect selection paths
			Arrays.stream(selectionPaths).forEach(sp -> selectionSet.add(sp.toString()));
		}
		for (int i = root.getChildCount() - 1; 0 <= i; i--) {
			model.removeNodeFromParent((MutableTreeNode) root.getChildAt(i));
		}
		buildTree();

		expandPath(new TreePath(root));
		for (int i = 0; i < root.getChildCount(); i++) {
			TreePath treePath = new TreePath(model.getPathToRoot(root.getChildAt(i)));
			expandPath(treePath);
			String path = treePath.toString();
			if (selectionSet.contains(path)) {
				addSelectionPath(treePath);
			}
		}
		if (selectionPaths != null && rowCount < getRowCount()) {
			setSelectionRow(getRowCount() - 1);
		}
	}

	public void buildTree() {
		for (DataSourceDescriptor descriptor : dataSourceDescriptors) {
			DataSourceDescTreeNode<?> newChild = getNewChild(descriptor);
			model.insertNodeInto(newChild, root, root.getChildCount());
		}
		expandPath(new TreePath(root));
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void addDataSources(List<DataSourceDescriptor> descriptors) {
		dataSourceDescriptors.addAll(descriptors);
		for (DataSourceDescriptor descriptor : descriptors) {
			DataSourceDescTreeNode<?> newChild = getNewChild(descriptor);
			model.insertNodeInto(newChild, root, root.getChildCount());
			TreePath treePath = new TreePath(model.getPathToRoot(newChild));
			expandPath(treePath);
			setSelectionPath(treePath);
		}
	}
	private DataSourceDescTreeNode<?> getNewChild(DataSourceDescriptor descriptor) {
		if (descriptor instanceof CascDataSourceDescriptor cascDesc) {
			return new CascDataSourceDescTreeNode(cascDesc);
		} else {
			return new DataSourceDescTreeNode<>(descriptor);
		}
	}

	public boolean isCascSelected(TreePath selectionPath) {
		DataSourceTreeNode<?> node = getNode(selectionPath);
		return node instanceof CascDataSourceDescTreeNode || node instanceof CascDataSourceSubNode;
	}

	public boolean canMoveUp(TreePath selectionPath) {
		DataSourceTreeNode<?> node = getNode(selectionPath);
		return node != null && node.canMoveUp();
	}

	public boolean canMoveDown(TreePath selectionPath) {
		DataSourceTreeNode<?> node = getNode(selectionPath);
		return node != null && node.canMoveDown();
	}

	public DataSourceTreeNode<?> getNode(TreePath selectionPath) {
		if (selectionPath != null && selectionPath.getLastPathComponent() instanceof DataSourceTreeNode treeNode) {
			return  treeNode;
		}
		return null;
	}

	public CascDataSourceDescriptor getCascDataSourceDescriptor() {
		if (dataSourceDescriptors.size() == 1 && dataSourceDescriptors.get(0) instanceof CascDataSourceDescriptor cascDesc) {
			return  cascDesc;
		}
		return null;
	}

	public void clearAll() {
		dataSourceDescriptors.clear();
		for (int i = root.getChildCount() - 1; 0 <= i; i--) {
			model.removeNodeFromParent((MutableTreeNode) root.getChildAt(i));
		}
		expandPath(new TreePath(root));
	}

	@Override
	public TreePath[] getSelectionPaths() {
		TreePath[] selectionPaths = super.getSelectionPaths();
		return selectionPaths == null ? new TreePath[0] : selectionPaths;
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSourceDescriptors;
	}
}
