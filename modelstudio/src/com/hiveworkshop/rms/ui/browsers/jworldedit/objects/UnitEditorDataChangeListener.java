package com.hiveworkshop.rms.ui.browsers.jworldedit.objects;

import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.TreeNodeLinkerFromModel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.sorting.general.TopLevelCategoryFolder;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class UnitEditorDataChangeListener {
	private final MutableObjectData unitData;
	private final UnitEditorPanel unitEditorPanel;

	UnitEditorDataChangeListener(UnitEditorPanel unitEditorPanel, MutableObjectData unitData) {
		this.unitData = unitData;
		this.unitEditorPanel = unitEditorPanel;
	}

	public void textChanged(final War3ID changedObject) {
		UnitEditorTreeModel treeModel = unitEditorPanel.getTreeModel();
		MutableTreeNode changedNode = treeModel.getNodeById(changedObject);
		if (changedNode != null) {
			treeModel.nodeChanged(changedNode);
		}
	}

	public void modelChanged(final War3ID changedObject) {
	}

	public void iconsChanged(final War3ID changedObject) {
		UnitEditorTreeModel treeModel = unitEditorPanel.getTreeModel();
		MutableTreeNode changedNode = treeModel.getNodeById(changedObject);
		if (changedNode != null) {
			treeModel.nodeChanged(changedNode);
		}
	}

	public void fieldsChanged(final War3ID changedObject) {
	}

	public void categoriesChanged(final War3ID changedObject) {
		System.out.println("categoriesChanged(" + changedObject + ")");
		UnitEditorTreeModel treeModel = unitEditorPanel.getTreeModel();
		MutableTreeNode changedNode = treeModel.getNodeById(changedObject);
		if (changedNode != null) {
			treeModel.removeNodeFromParent(changedNode);
			TreeNodeLinkerFromModel linker = new TreeNodeLinkerFromModel(treeModel);
			TopLevelCategoryFolder root = unitEditorPanel.getRoot();

			DefaultMutableTreeNode newObjectNode = root.insertObjectInto(unitData.get(changedObject), linker);
			unitEditorPanel.selectTreeNode(newObjectNode);
		} else {
			System.out.println("Changed node was not found");
		}
	}

	public void objectCreated(War3ID newObject) {
		MutableGameObject mutableGameObject = unitData.get(newObject);
		UnitEditorTreeModel model = unitEditorPanel.getTreeModel();
		TopLevelCategoryFolder root = unitEditorPanel.getRoot();
		TreeNodeLinkerFromModel linker = new TreeNodeLinkerFromModel(model);
		DefaultMutableTreeNode newTreeNode = root.insertObjectInto(mutableGameObject, linker);
		TreeNode node = newTreeNode.getParent();

		while (node != null) {
			model.nodeChanged(node);
			node = node.getParent();
		}
		unitEditorPanel.selectTreeNode(newTreeNode);
	}

	public void objectsCreated(War3ID[] newObjects) {
		unitEditorPanel.setTreeSelectionPath(null);
		for (War3ID newObjectId : newObjects) {
			MutableGameObject mutableGameObject = unitData.get(newObjectId);
			UnitEditorTreeModel model = unitEditorPanel.getTreeModel();
			TreeNodeLinkerFromModel linker = new TreeNodeLinkerFromModel(model);
			TopLevelCategoryFolder root = unitEditorPanel.getRoot();
			DefaultMutableTreeNode newTreeNode = root.insertObjectInto(mutableGameObject, linker);
			TreeNode node = newTreeNode.getParent();
			while (node != null) {
				model.nodeChanged(node);
				node = node.getParent();
			}
			unitEditorPanel.addSelectedTreeNode(newTreeNode);
		}
	}

	public void objectRemoved(War3ID removedId) {
		UnitEditorTreeModel treeModel = unitEditorPanel.getTreeModel();
		MutableTreeNode changedNode = treeModel.getNodeById(removedId);
		if (changedNode != null) {
			treeModel.removeNodeFromParent(changedNode);
		}
	}

	public void objectsRemoved(War3ID[] removedIds) {
		UnitEditorTreeModel treeModel = unitEditorPanel.getTreeModel();
		for (War3ID removedId : removedIds) {
			MutableTreeNode changedNode = treeModel.getNodeById(removedId);
			if (changedNode != null) {
				treeModel.removeNodeFromParent(changedNode);
			}
		}
	}
}
