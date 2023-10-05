package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiTreeStuff.TwiTreeExpansionListener;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	private final TwiTreeExpansionListener expansionListener;

	public ComponentThingTree() {
		super();
		// Disabling toggling by double-click
		setToggleClickCount(0);
		setOpaque(false);
		setEditable(false);

		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);

		expansionListener = new TwiTreeExpansionListener();
		mouseAdapter = new ModelTreeMouseAdapter(expansionListener::setExpansionPropagateKeyDown, this);

		addTreeExpansionListener(expansionListener);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		ModelTreeKeyAdapter keyAdapter = new ModelTreeKeyAdapter(expansionListener::setExpansionPropagateKeyDown);
		addKeyListener(keyAdapter);
		ModelStructureChangeListener.changeListener.addSelectionListener(this, this::repaint);
		ModelStructureChangeListener.changeListener.addStateChangeListener(this, this::updateNodes);
	}

	public ComponentThingTree setControlDown(boolean controlDown) {
		expansionListener.setExpansionPropagateKeyDown(controlDown);
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
		expansionListener.clear();
		mouseAdapter.setUndoManager(this.undoManager);

		buildBaseNodes();
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
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
		root.updateEditability(true);
		root.updateVisibility(true);

		meshes = new ComponentTreeGeosetsTopNode(modelHandler);
		nodes = new ComponentTreeIdObjectTopNode(modelHandler);
		cameras = new ComponentTreeCamerasTopNode(modelHandler);
	}

	public ComponentThingTree reloadTree() {
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
//		System.out.println("ComponentThingTree#setModel: setTreeModel");
		setModel(treeModel);

		expansionListener.openTree(this);
		return this;
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

	public void updateNodes() {
		meshes.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
		nodes.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
		cameras.getChildComponents(new HashSet<>()).forEach(NodeThing::updateState);
	}
}