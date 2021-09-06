package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTree;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTreeNode;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public final class ModelViewManagingTree extends JCheckBoxTree {
	JCheckBoxTreeNode root;
	JCheckBoxTreeNode meshes;
	JCheckBoxTreeNode nodes;
	JCheckBoxTreeNode cameras;

	private static final String MESH = "Mesh";
	private static final String NODES = "Nodes";
	private static final String CAMERAS = "Cameras";

	Map<IdObject, JCheckBoxTreeNode> nodeToTreeElement = new HashMap<>();

	public ModelViewManagingTree() {
		super();
		System.out.println("ModelViewManagingTree");
		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);

		MouseAdapter mouseListener = getMouseAdapter();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
	}

	public ModelViewManagingTree setModel(ModelHandler modelHandler) {
		System.out.println("ModelViewManagingTree#setModel: setModel");
		super.setModel(modelHandler);
		System.out.println("ModelViewManagingTree#setModel: creating checkboxNodes");
		root = new JCheckBoxTreeNode(new CheckableModelElement(modelHandler)).setChecked(true);
//		nodes = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, "Nodes")).setChecked(false);
		meshes = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, MESH)).setChecked(true);
		nodes = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, NODES)).setChecked(true);
		cameras = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, CAMERAS));

		System.out.println("ModelViewManagingTree#setModel: buildTreeModel");
		DefaultTreeModel treeModel = buildTreeModel(modelHandler);
		System.out.println("ModelViewManagingTree#setModel: setTreeModel");
		setModel(treeModel);

		System.out.println("ModelViewManagingTree#setModel: addListListener");
		listenerList.add(CheckChangeEventListener.class, e -> updateCheckedState(e));
		return this;
	}

	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {
		root.removeAllChildren();
		meshes.removeAllChildren();
		cameras.removeAllChildren();
		nodes.removeAllChildren();

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

	private void buildGeosetTree(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			boolean contains = modelView.isEditable(geoset);
			meshes.add(new JCheckBoxTreeNode(new CheckableGeosetElement(modelView, geoset), contains));
		}
	}

	private void buildCameraTree(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		for (final Camera camera : modelHandler.getModel().getCameras()) {
			boolean checked = modelView.isEditable(camera);
			cameras.add(new JCheckBoxTreeNode(new CheckableCameraElement(modelView, camera), checked));
		}
	}

	private void buildNodeTree(ModelHandler modelHandler) {
		updateNodeToElement(modelHandler);

		for (IdObject object : modelHandler.getModel().getIdObjects()) {
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			JCheckBoxTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode != null) {
				parentTreeNode.add(nodeToTreeElement.get(object));
			}
		}
	}

	private void updateNodeToElement(ModelHandler modelHandler) {
		nodeToTreeElement.clear();

		nodeToTreeElement.put(null, nodes);

		ModelView modelView = modelHandler.getModelView();
		EditableModel model = modelHandler.getModel();
		for (IdObject object : model.getIdObjects()) {
			boolean checked = modelView.isEditable(object);
			JCheckBoxTreeNode treeNode = new JCheckBoxTreeNode(new CheckableNodeElement(modelView, object), checked);
			nodeToTreeElement.put(object, treeNode);

		}
	}

	private TreePath getTreePath(TreePath nextPathToExpand, TreePath newPathWithNewObjects, JCheckBoxTreeNode currentNode) {
		for (int i = 1; i < nextPathToExpand.getPathCount(); i++) {
			final JCheckBoxTreeNode pathComponent = (JCheckBoxTreeNode) nextPathToExpand.getPathComponent(i);
			boolean foundMatchingChild = false;
			for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
				final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) currentNode.getChildAt(j);
				if (asElement(childAt.getUserObject()).hasSameItem(asElement(pathComponent.getUserObject()))) {
					currentNode = childAt;
					newPathWithNewObjects = newPathWithNewObjects.pathByAddingChild(childAt);
					foundMatchingChild = true;
				}
			}
			if (!foundMatchingChild) {
				break;
			}
		}
		return newPathWithNewObjects;
	}


	public ModelViewManagingTree reloadFromModelView() {
		System.out.println("reloadFromModelView");
		SwingUtilities.invokeLater(this::reloadFromModelView2);
		return this;
	}

	private void reloadFromModelView2() {
		final TreePath rootPath = new TreePath(getModel().getRoot());
		final Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);

		updateModel(buildTreeModel(modelHandler));

		final TreePath newRootPath = new TreePath(getModel().getRoot());
		final List<TreePath> pathsToExpand = new ArrayList<>();

		while ((expandedDescendants != null) && expandedDescendants.hasMoreElements()) {
			final TreePath nextPathToExpand = expandedDescendants.nextElement();

			TreePath newPathWithNewObjects = newRootPath;
			JCheckBoxTreeNode currentNode = (JCheckBoxTreeNode) getModel().getRoot();
			newPathWithNewObjects = getTreePath(nextPathToExpand, newPathWithNewObjects, currentNode);

			pathsToExpand.add(newPathWithNewObjects);
		}
		for (final TreePath path : pathsToExpand) {
			expandPath(path);
		}
	}

	private CheckableDisplayElement<?> asElement(final Object userObject) {
		return (CheckableDisplayElement<?>) userObject;
	}

	private void updateCheckedState(CheckChangeEvent event) {
		JCheckBoxTreeNode sourceNode = (JCheckBoxTreeNode) event.getSource();
		JCheckBoxTreeNode baseNode = null;
		List<CheckableDisplayElement<?>> components = new ArrayList<>();


		if (sourceNode == meshes || meshes.isNodeDescendant(sourceNode)) {
			baseNode = meshes;
		} else if (sourceNode == nodes || nodes.isNodeDescendant(sourceNode)) {
			baseNode = nodes;
		}

		if (baseNode != null) {
			if (sourceNode == meshes || sourceNode == nodes || !baseNode.isChecked()) {
				baseNode.setChecked(sourceNode.isChecked());
				modelView.setIdObjectsVisible(nodes.isChecked());
				modelView.setGeosetsVisible(meshes.isChecked());
				modelView.setCamerasVisible(cameras.isChecked());
//						hideOrUnhideTemporary(baseNode, !baseNode.isChecked(), components);
			}
			handleNodeRecursively(baseNode);
		} else {
			components.add((CheckableDisplayElement<?>) sourceNode.getUserObject());
			handleNodeRecursively(sourceNode);
			modelView.setCamerasVisible(cameras.isChecked());
		}

		EditabilityToggleHandler toggleHandler = new EditabilityToggleHandler(components);

		undoManager.pushAction(getShowHideUndo(sourceNode, toggleHandler));
	}

	private void handleNodeRecursively(JCheckBoxTreeNode parent) {
//				notifyModelViewManagerStateChanged(parent);
		CheckableDisplayElement<?> element = (CheckableDisplayElement<?>) parent.getUserObject();
		element.setChecked(parent.isChecked() && !parent.isTempHidden());
		for (int i = 0; i < parent.getChildCount(); i++) {
			final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) parent.getChildAt(i);
			handleNodeRecursively(childAt);
		}
	}

	private void hideOrUnhideTemporary(JCheckBoxTreeNode parent, boolean hidden, List<CheckableDisplayElement<?>> elements) {
		if (parent.isChecked() && !hidden || !parent.isChecked() && hidden) {
			elements.add((CheckableDisplayElement<?>) parent.getUserObject());
		}
		for (int i = 0; i < parent.getChildCount(); i++) {
			final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) parent.getChildAt(i);
			hideOrUnhideTemporary(childAt, hidden, elements);
			childAt.setTempHidden(childAt.isChecked() && hidden);
		}
	}

	private UndoAction getShowHideUndo(JCheckBoxTreeNode sourceNode, EditabilityToggleHandler toggleHandler) {
		if (isSelected(sourceNode)) {
			return toggleHandler.showComponent(modelView, this::reloadFromModelView);
		} else {
			return toggleHandler.hideComponent(modelView, this::reloadFromModelView);
		}
	}

	private MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("mouseExited");
				if (modelView != null) {
					modelView.higthlight(null);
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (modelView != null) {
					TreePath pathForLocation = getPathForLocation(e.getX(), e.getY());
					if (pathForLocation == null) {
						modelView.higthlight(null);
					} else {
						JCheckBoxTreeNode lastPathComponent = (JCheckBoxTreeNode) pathForLocation.getLastPathComponent();
						CheckableDisplayElement<?> element = (CheckableDisplayElement<?>) lastPathComponent.getUserObject();
						if (element != null) {
							modelView.higthlight(element.item);
						}
					}
				}
			}
		};
	}
}
