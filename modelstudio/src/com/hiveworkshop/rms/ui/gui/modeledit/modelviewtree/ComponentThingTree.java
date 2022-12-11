package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.*;

public class ComponentThingTree extends JTree {

	private static final long serialVersionUID = -4194122328392241790L;

	// Defining a new event type for the checking mechanism and preparing event-handling mechanism
	protected EventListenerList listenerList = new EventListenerList();
	private ComponentTreeNode<EditableModel> root;
	private ComponentTreeGeosetsTopNode meshes;
	private ComponentTreeIdObjectTopNode nodes;
	private ComponentTreeCamerasTopNode cameras;

	protected ModelHandler modelHandler;
	protected ModelView modelView;
	protected UndoManager undoManager;
	ModelTreeMouseAdapter mouseAdapter;

	private final Map<IdObject, ComponentTreeNode<IdObject>> nodeToTreeElement = new HashMap<>();
	private final ModelTreeExpansionListener tel;

	public ComponentThingTree() {
		super();
		// Disabling toggling by double-click
		setToggleClickCount(0);
		setOpaque(false);
		setEditable(false);

		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);
		System.out.println("KeyListeners: " + getKeyListeners().length);
		for(KeyListener a_KeyListener : getKeyListeners()){
			System.out.println(a_KeyListener);
		}
		System.out.println("MouseListeners: " + getMouseListeners().length);
		for(MouseListener MouseListener : getMouseListeners()){
			System.out.println(MouseListener);
		}
		System.out.println("MouseMotionListeners: " + getMouseMotionListeners().length);
		for(MouseMotionListener a_MouseMotionListener : getMouseMotionListeners()){
			System.out.println(a_MouseMotionListener);
		}
		System.out.println("InputMethodListeners: " + getInputMethodListeners().length);
		for(InputMethodListener a_InputMethodListener : getInputMethodListeners()){
			System.out.println(a_InputMethodListener);
		}

		tel = new ModelTreeExpansionListener();
		mouseAdapter = new ModelTreeMouseAdapter(tel::setControlDown, this);

		addTreeExpansionListener(tel);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		ModelTreeKeyAdapter keyAdapter = new ModelTreeKeyAdapter(tel::setControlDown);
		addKeyListener(keyAdapter);
		ModelStructureChangeListener.changeListener.addSelectionListener(this, this::repaint);
		ModelStructureChangeListener.changeListener.addStateChangeListener(this, this::updateNodes);
	}

	public ComponentThingTree setControlDown(boolean controlDown) {
		tel.setControlDown(controlDown);
		return this;
	}

	public ComponentThingTree setModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			this.modelView = modelHandler.getModelView();
			this.undoManager = modelHandler.getUndoManager();

		} else {
			this.modelView = null;
			this.undoManager = null;

		}
		mouseAdapter.setUndoManager(this.undoManager);
		buildBaseNodes();
//		System.out.println("ComponentThingTree#setModel: buildTreeModel");
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);

//		System.out.println("meshes: " + meshes);
//		System.out.println("meshes: " + Arrays.toString(meshes.getPath()));

//		System.out.println("ComponentThingTree#setModel: setTreeModel");
		setModel(treeModel);

		setCellRenderer(this::getCellComp);
		setCellEditor(new ModelTreeButtonCellEditor());

		expandMeshNode();

		revalidate();
		repaint();
		return this;
	}
	Component getCellComp(JTree tree, Object value,
	                      boolean selected, boolean expanded,
	                      boolean leaf, int row, boolean hasFocus) {
		if (value instanceof NodeThing) {
//			System.out.println("value: " +  value);
//			JPanel treeRenderComponent = ((NodeThing<?>) value).getTreeRenderComponent();
//			System.out.println("treeRenderComponent: " +  treeRenderComponent);
			return ((NodeThing<?>) value).getTreeRenderComponent();
		}
		return new JLabel("null");
	}

	public void expandMeshNode() {
		TreePath path = new TreePath(getModel().getRoot());
		path.pathByAddingChild(meshes);
		expandPath(path);
	}

	private void buildBaseNodes() {
		root = new ComponentTreeNode<>(modelHandler, modelHandler.getModel());
//		root.setVisible1(true);
//		root.setEditable1(true);
		root.updateEditability(true);
		root.updateVisibility(true);

		meshes = new ComponentTreeGeosetsTopNode(modelHandler);
		nodes = new ComponentTreeIdObjectTopNode(modelHandler);
		cameras = new ComponentTreeCamerasTopNode(modelHandler);
	}

	public ComponentThingTree reloadTree() {
		TreePath rootPath = new TreePath(getModel().getRoot());
		Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
//		System.out.println("ComponentThingTree#setModel: setTreeModel");
		setModel(treeModel);

		expandTree(expandedDescendants);
		return this;
	}

	private void expandTree(Enumeration<TreePath> expandedDescendants) {
		TreePath newRootPath = new TreePath(getModel().getRoot());
		List<TreePath> pathsToExpand = new ArrayList<>();

		while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
			TreePath oldExpandedPath = expandedDescendants.nextElement();

			TreePath newPathToExpand = newRootPath;
			NodeThing<?> rootNode = (NodeThing<?>) getModel().getRoot();
			newPathToExpand = getTreePath(oldExpandedPath, newPathToExpand, rootNode);

			pathsToExpand.add(newPathToExpand);
		}
		for (final TreePath path : pathsToExpand) {
			expandPath(path);
		}
	}

	private TreePath getTreePath(TreePath oldExpandedPath, TreePath newPathToExpand, NodeThing<?> rootNode) {
		NodeThing<?> currentNode = rootNode;
		for (int i = 1; i < oldExpandedPath.getPathCount(); i++) {
			boolean foundMatchingChild = false;

			NodeThing<?> pathComponent = (NodeThing<?>) oldExpandedPath.getPathComponent(i);
			Object oldPathItem = pathComponent.getItem();

			for (int j = 0; j < currentNode.getChildCount(); j++) {
				NodeThing<?> childAt = (NodeThing<?>) currentNode.getChildAt(j);
				Object newPathItem = childAt.getItem();
				if (newPathItem == oldPathItem
						|| oldPathItem instanceof String && newPathItem.equals(oldPathItem)) {
					currentNode = childAt;
					newPathToExpand = newPathToExpand.pathByAddingChild(childAt);
					foundMatchingChild = true;
					break;
				}
			}

			if (!foundMatchingChild) {
				// stop searching if no matching child was found at this depth
				return newPathToExpand;
			}
		}
		return newPathToExpand;
	}


	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {
		root.removeAllChildren();
		meshes.removeAllChildren();
		nodes.removeAllChildren();
		cameras.removeAllChildren();

		if (modelHandler != null) {
			buildGeosetTree(modelHandler);
			if (meshes.getChildCount() > 0) {
				root.add(meshes);
			}

			buildNodeTree(modelHandler);
			if (nodes.getChildCount() > 0) {
				root.add(nodes);
			}

			buildCameraTree(modelHandler);
			if (cameras.getChildCount() > 0) {
				root.add(cameras);
			}
		}

		return new DefaultTreeModel(root);
	}


	private void buildNodeTree(ModelHandler modelHandler) {
		updateNodeToElement(modelHandler);

		for (IdObject object : modelHandler.getModel().getIdObjects()) {
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			ComponentTreeNode<IdObject> parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode != null) {
				parentTreeNode.add(nodeToTreeElement.get(object));
			} else {
				nodes.add(nodeToTreeElement.get(object));
			}
		}
	}

	private void buildGeosetTree(ModelHandler modelHandler) {
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			meshes.add(new ComponentTreeNode<>(modelHandler, geoset));
		}
	}

	private void updateNodeToElement(ModelHandler modelHandler) {
		nodeToTreeElement.clear();

		EditableModel model = modelHandler.getModel();
		for (IdObject object : model.getIdObjects()) {
			nodeToTreeElement.put(object, new ComponentTreeNode<>(modelHandler, object));

		}
	}

	private void buildCameraTree(ModelHandler modelHandler) {
		for (Camera camera : modelHandler.getModel().getCameras()) {
			cameras.add(new ComponentTreeNode<>(modelHandler, camera));
		}
	}

	public void updateNodes(){
//		meshes.updateState();
//		nodes.updateState();
//		cameras.updateState();
		meshes.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
		nodes.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
		cameras.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
	}


	public void expandAllChildren(TreeNode node, TreePath path, boolean expand) {
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = node.getChildAt(i);
			expandAllChildren(child, path.pathByAddingChild(child), expand);
		}
		if (expand) {
			expandPath(path);
		} else {
			collapsePath(path);
		}
	}
}