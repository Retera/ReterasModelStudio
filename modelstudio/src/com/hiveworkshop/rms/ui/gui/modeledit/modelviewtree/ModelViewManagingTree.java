package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTree;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTreeNode;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

public final class ModelViewManagingTree extends JCheckBoxTree {
	private ModelHandler modelHandler;
	JCheckBoxTreeNode root;
	JCheckBoxTreeNode meshes;
	JCheckBoxTreeNode nodes;
	JCheckBoxTreeNode cameras;
	Map<IdObject, JCheckBoxTreeNode> nodeToTreeElement = new HashMap<>();

	public ModelViewManagingTree(ModelHandler modelHandler, ModelEditorManager modelEditorManager) {
		super(modelHandler);
		root = new JCheckBoxTreeNode(new CheckableModelElement(modelHandler)).setChecked(true);
		meshes = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, "Mesh")).setChecked(true);
		nodes = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, "Nodes")).setChecked(false);
		cameras = new JCheckBoxTreeNode(new CheckableDummyElement(modelHandler, "Cameras"));

		setModel(buildTreeModel(modelHandler));
		BasicTreeUI basicTreeUI = (BasicTreeUI) getUI();
		basicTreeUI.setRightChildIndent(5);

		this.modelHandler = modelHandler;

		listenerList.add(CheckChangeEventListener.class, changeEventListener(modelHandler.getUndoManager(), modelEditorManager));

		final HighlightOnMouseoverListenerImpl mouseListener = new HighlightOnMouseoverListenerImpl();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
	}


	public ModelViewManagingTree reloadFromModelView() {
		System.out.println("reloadFromModelView");
		SwingUtilities.invokeLater(this::reloadFromModelView2);
		return this;
	}

	private DefaultTreeModel buildTreeModel(ModelHandler modelHandler) {
		root.removeAllChildren();
		meshes.removeAllChildren();
		nodes.removeAllChildren();
		cameras.removeAllChildren();

		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			boolean contains = modelHandler.getModelView().isEditable(geoset);
			meshes.add(new JCheckBoxTreeNode(new CheckableGeosetElement(modelHandler.getModelView(), geoset), contains));
		}

		if (meshes.getChildCount() > 0) {
			root.add(meshes);
		}

		nodeToTreeElement.clear();

		nodeToTreeElement.put(null, nodes);

		for (IdObject object : modelHandler.getModel().getIdObjects()) {
			boolean checked = modelHandler.getModelView().isEditable(object);
			JCheckBoxTreeNode treeNode = new JCheckBoxTreeNode(new CheckableNodeElement(modelHandler, object), checked);
			nodeToTreeElement.put(object, treeNode);

		}
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
		if (nodes.getChildCount() > 0) {
			root.add(nodes);
		}

		for (final Camera camera : modelHandler.getModel().getCameras()) {
			boolean checked = modelHandler.getModelView().isEditable(camera);
			cameras.add(new JCheckBoxTreeNode(new CheckableCameraElement(modelHandler, camera), checked));
		}

		if (cameras.getChildCount() > 0) {
			root.add(cameras);
		}

		return new DefaultTreeModel(root);
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

	private CheckChangeEventListener changeEventListener(UndoManager undoManager, ModelEditorManager editorManager) {
		return new CheckChangeEventListener() {
			@Override
			public void checkStateChanged(final CheckChangeEvent event) {
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
						modelHandler.getModelView().setIdObjectsVisible(nodes.isChecked());
						modelHandler.getModelView().setGeosetsVisible(meshes.isChecked());
						modelHandler.getModelView().setCamerasVisible(cameras.isChecked());
//						hideOrUnhideTemporary(baseNode, !baseNode.isChecked(), components);
					}
					handleNodeRecursively(baseNode);
				} else {
					components.add((CheckableDisplayElement<?>) sourceNode.getUserObject());
					handleNodeRecursively(sourceNode);
					modelHandler.getModelView().setCamerasVisible(cameras.isChecked());
				}

				EditabilityToggleHandler toggleHandler = new EditabilityToggleHandler(components);

				UndoAction showHideUndo;
				if (isSelected(sourceNode)) {
					Runnable refreshGUI = () -> reloadFromModelView();
					showHideUndo = toggleHandler.showComponent(modelHandler.getModelView(), refreshGUI);
//					showHideUndo = editorManager.getModelEditor().showComponent(toggleHandler);
				} else {
					Runnable refreshGUI = () -> reloadFromModelView();
					showHideUndo = toggleHandler.hideComponent(modelHandler.getModelView(), refreshGUI);
//					showHideUndo = editorManager.getModelEditor().hideComponent(components, toggleHandler, refreshGUI);
				}
				undoManager.pushAction(showHideUndo);
			}

			private void handleNodeRecursively(JCheckBoxTreeNode parent) {
				notifyModelViewManagerStateChanged(parent);
				for (int i = 0; i < parent.getChildCount(); i++) {
					final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) parent.getChildAt(i);
					handleNodeRecursively(childAt);
				}
			}

			private void notifyModelViewManagerStateChanged(JCheckBoxTreeNode sourceNode) {
				CheckableDisplayElement<?> element = (CheckableDisplayElement<?>) sourceNode.getUserObject();
				element.setChecked(sourceNode.isChecked() && !sourceNode.isTempHidden());
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
		};
	}

	private final class HighlightOnMouseoverListenerImpl implements MouseMotionListener, MouseListener {
		private CheckableDisplayElement<?> lastMouseOverNode = null;

		@Override
		public void mouseMoved(final MouseEvent mouseEvent) {
			TreePath pathForLocation = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			CheckableDisplayElement<?> element;
			if (pathForLocation == null) {
				element = null;
			} else {
				final JCheckBoxTreeNode lastPathComponent = (JCheckBoxTreeNode) pathForLocation.getLastPathComponent();
				element = (CheckableDisplayElement<?>) lastPathComponent.getUserObject();
			}
			if (element != lastMouseOverNode) {
				if (lastMouseOverNode != null) {
					lastMouseOverNode.mouseExited();
				}
				if (element != null) {
					element.mouseEntered();
				}
				lastMouseOverNode = element;
			}
		}

		@Override
		public void mouseDragged(final MouseEvent e) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
		}

		@Override
		public void mousePressed(final MouseEvent e) {
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			if (lastMouseOverNode != null) {
				lastMouseOverNode.mouseExited();
			}
		}

		@Override
		public void mouseEntered(final MouseEvent e) {
		}

		@Override
		public void mouseClicked(final MouseEvent e) {
		}
	}
}
