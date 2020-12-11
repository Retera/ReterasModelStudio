package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTree;
import com.hiveworkshop.rms.ui.gui.modeledit.util.JCheckBoxTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

public final class ModelViewManagingTree extends JCheckBoxTree {
	private final ModelViewManager modelViewManager;

	public ModelViewManagingTree(
			final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager) {
		super(buildTreeModel(modelViewManager));

		this.modelViewManager = modelViewManager;

		addCheckChangeEventListener(changeEventListener(undoActionListener, modelEditorManager));
		final HighlightOnMouseoverListenerImpl mouseListener = new HighlightOnMouseoverListenerImpl();
		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
	}

	public void reloadFromModelView() {
		SwingUtilities.invokeLater(this::reloadFromModelView2);
	}

	private void reloadFromModelView2() {
		final TreePath rootPath = new TreePath(getModel().getRoot());
		final Enumeration<TreePath> expandedDescendants = getExpandedDescendants(rootPath);
		setModel(buildTreeModel(modelViewManager));
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

	private TreePath getTreePath(TreePath nextPathToExpand, TreePath newPathWithNewObjects, JCheckBoxTreeNode currentNode) {
		for (int i = 1; i < nextPathToExpand.getPathCount(); i++) {
			final JCheckBoxTreeNode pathComponent = (JCheckBoxTreeNode) nextPathToExpand
					.getPathComponent(i);
			boolean foundMatchingChild = false;
			for (int j = 0; (j < currentNode.getChildCount()) && !foundMatchingChild; j++) {
				final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) currentNode.getChildAt(j);
				if (asElement(childAt.getUserObject())
						.hasSameItem(asElement(pathComponent.getUserObject()))) {
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

	private CheckChangeEventListener changeEventListener(UndoActionListener undoActionListener,
	                                                     ModelEditorManager modelEditorManager) {
		return new CheckChangeEventListener() {
			@Override
			public void checkStateChanged(final CheckChangeEvent event) {
				final JCheckBoxTreeNode sourceNode = (JCheckBoxTreeNode) event.getSource();
				final List<CheckableDisplayElement<?>> components = new ArrayList<>();
				handleNodeRecursively(sourceNode, components);
				final CheckableDisplayElementToggleHandler toggleHandler = new CheckableDisplayElementToggleHandler(
						components);
				final UndoAction showHideComponentAction;
				if (isSelected(sourceNode)) {
					showHideComponentAction = modelEditorManager.getModelEditor().showComponent(toggleHandler);
				} else {
					final Runnable refreshGUIRunnable = () -> reloadFromModelView();
					showHideComponentAction = modelEditorManager.getModelEditor().hideComponent(components,
							toggleHandler, refreshGUIRunnable);
				}
				undoActionListener.pushAction(showHideComponentAction);
			}

			private void handleNodeRecursively(final JCheckBoxTreeNode parent,
			                                   final List<CheckableDisplayElement<?>> components) {
				notifyModelViewManagerStateChanged(parent, components);
				if (!parent.isHasPersonalState()) {
					for (int i = 0; i < parent.getChildCount(); i++) {
						final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) parent.getChildAt(i);
						handleNodeRecursively(childAt, components);
					}
				}
			}

			private void notifyModelViewManagerStateChanged(final JCheckBoxTreeNode sourceNode,
			                                                final List<CheckableDisplayElement<?>> components) {
				final Object userObject = sourceNode.getUserObject();
				final CheckableDisplayElement<?> element = (CheckableDisplayElement<?>) userObject;
				element.setChecked(isSelected(sourceNode));
				components.add(element);
			}
		};
	}

	private CheckableDisplayElement<?> asElement(final Object userObject) {
		return (CheckableDisplayElement<?>) userObject;
	}

	private static DefaultTreeModel buildTreeModel(final ModelViewManager modelViewManager) {
		final JCheckBoxTreeNode root = new JCheckBoxTreeNode(new CheckableModelElement(modelViewManager));

		final JCheckBoxTreeNode mesh = new JCheckBoxTreeNode(new CheckableDummyElement(modelViewManager, "Mesh"));

		for (final Geoset geoset : modelViewManager.getModel().getGeosets()) {
			final boolean contains = modelViewManager.getEditableGeosets().contains(geoset);
			mesh.add(new JCheckBoxTreeNode(new CheckableGeosetElement(modelViewManager, geoset), contains));
		}

		if (mesh.getChildCount() > 0) {
			root.add(mesh);
		}

		final Map<IdObject, JCheckBoxTreeNode> nodeToTreeElement = new HashMap<>();
		final Map<IdObject, List<JCheckBoxTreeNode>> nodeToChildrenAwaitingLink = new HashMap<>();
		final JCheckBoxTreeNode nodes = new JCheckBoxTreeNode(new CheckableDummyElement(modelViewManager, "Nodes"));
		nodeToTreeElement.put(null, nodes);
		for (final IdObject object : modelViewManager.getModel().getIdObjects()) {
			final JCheckBoxTreeNode treeNode = new JCheckBoxTreeNode(new CheckableNodeElement(modelViewManager, object),
					modelViewManager.getEditableIdObjects().contains(object));
			nodeToTreeElement.put(object, treeNode);
			IdObject parent = object.getParent();
			if (parent == object) {
				parent = null;
			}
			final JCheckBoxTreeNode parentTreeNode = nodeToTreeElement.get(parent);
			if (parentTreeNode == null) {
				List<JCheckBoxTreeNode> awaitingChildrenList = nodeToChildrenAwaitingLink.computeIfAbsent(parent, k -> new ArrayList<>());
				awaitingChildrenList.add(treeNode);
			} else {
				parentTreeNode.add(treeNode);
			}
			final List<JCheckBoxTreeNode> childrenNeedingLinkToCurrentNode = nodeToChildrenAwaitingLink.get(object);
			if ((childrenNeedingLinkToCurrentNode != null)
					&& childrenNeedingLinkToCurrentNode.size() > 0) {
				for (final JCheckBoxTreeNode child : childrenNeedingLinkToCurrentNode) {
					treeNode.add(child);
				}
			}

		}
		if (nodes.getChildCount() > 0) {
			root.add(nodes);
		}

		final JCheckBoxTreeNode cameras = new JCheckBoxTreeNode(new CheckableDummyElement(modelViewManager, "Cameras"));
		for (final Camera camera : modelViewManager.getModel().getCameras()) {
			cameras.add(new JCheckBoxTreeNode(new CheckableCameraElement(modelViewManager, camera),
					modelViewManager.getEditableCameras().contains(camera)));
		}
		if (cameras.getChildCount() > 0) {
			root.add(cameras);
		}

		return new DefaultTreeModel(root);
	}

	private final class HighlightOnMouseoverListenerImpl implements MouseMotionListener, MouseListener {
		private CheckableDisplayElement<?> lastMouseOverNode = null;

		@Override
		public void mouseMoved(final MouseEvent mouseEvent) {
			final TreePath pathForLocation = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			final CheckableDisplayElement<?> element;
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

	private static abstract class CheckableDisplayElement<T> implements SelectableComponent {
		protected final ModelViewManager modelViewManager;
		protected final T item;

		public CheckableDisplayElement(final ModelViewManager modelViewManager, final T item) {
			this.modelViewManager = modelViewManager;
			this.item = item;
		}

		public void setChecked(final boolean checked) {
			setChecked(item, modelViewManager, checked);
		}

		public abstract void mouseEntered();

		public abstract void mouseExited();

		protected abstract void setChecked(T item, ModelViewManager modelViewManager, boolean checked);

		@Override
		public String toString() {
			return getName(item, modelViewManager);
		}

		protected abstract String getName(T item, ModelViewManager modelViewManager);

		public boolean hasSameItem(final CheckableDisplayElement<?> other) {
			return Objects.equals(item, other.item);
		}
	}

	private static final class CheckableGeosetElement extends CheckableDisplayElement<Geoset> {
		public CheckableGeosetElement(final ModelViewManager modelViewManager, final Geoset item) {
			super(modelViewManager, item);
		}

		@Override
		protected void setChecked(final Geoset item, final ModelViewManager modelViewManager, final boolean checked) {
			if (checked) {
				modelViewManager.makeGeosetEditable(item);
			} else {
				modelViewManager.makeGeosetNotEditable(item);
			}
		}

		@Override
		protected String getName(final Geoset item, final ModelViewManager modelViewManager) {
			if ((item.getLevelOfDetailName() != null) && (item.getLevelOfDetailName().length() > 0)) {
				return item.getLevelOfDetailName();
			}
			return "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		public void mouseEntered() {
			modelViewManager.highlightGeoset(item);
		}

		@Override
		public void mouseExited() {
			modelViewManager.unhighlightGeoset(item);
		}

	}

	private static final class CheckableNodeElement extends CheckableDisplayElement<IdObject> {
		public CheckableNodeElement(final ModelViewManager modelViewManager, final IdObject item) {
			super(modelViewManager, item);
		}

		@Override
		protected void setChecked(final IdObject item, final ModelViewManager modelViewManager, final boolean checked) {
			if (checked) {
				modelViewManager.makeIdObjectVisible(item);
			} else {
				modelViewManager.makeIdObjectNotVisible(item);
			}
		}

		@Override
		protected String getName(final IdObject item, final ModelViewManager modelViewManager) {
			return item.getClass().getSimpleName() + " \"" + item.getName() + "\"";
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		public void mouseEntered() {
			modelViewManager.highlightNode(item);
		}

		@Override
		public void mouseExited() {
			modelViewManager.unhighlightNode(item);
		}
	}

	private static final class CheckableCameraElement extends CheckableDisplayElement<Camera> {
		public CheckableCameraElement(final ModelViewManager modelViewManager, final Camera item) {
			super(modelViewManager, item);
		}

		@Override
		protected void setChecked(final Camera item, final ModelViewManager modelViewManager, final boolean checked) {
			if (checked) {
				modelViewManager.makeCameraVisible(item);
			} else {
				modelViewManager.makeCameraNotVisible(item);
			}
		}

		@Override
		protected String getName(final Camera item, final ModelViewManager modelViewManager) {
			return item.getName();
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
		}

		@Override
		public void mouseEntered() {

		}

		@Override
		public void mouseExited() {

		}
	}

	private static final class CheckableModelElement extends CheckableDisplayElement<Void> {
		public CheckableModelElement(final ModelViewManager modelViewManager) {
			super(modelViewManager, null);
		}

		@Override
		protected void setChecked(final Void item, final ModelViewManager modelViewManager, final boolean checked) {

		}

		@Override
		protected String getName(final Void item, final ModelViewManager modelViewManager) {
			return modelViewManager.getModel().getHeaderName();
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
		}

		@Override
		public void mouseEntered() {

		}

		@Override
		public void mouseExited() {

		}
	}

	private static final class CheckableDummyElement extends CheckableDisplayElement<String> {
		public CheckableDummyElement(final ModelViewManager modelViewManager, final String name) {
			super(modelViewManager, name);
		}

		@Override
		protected void setChecked(final String item, final ModelViewManager modelViewManager, final boolean checked) {

		}

		@Override
		protected String getName(final String item, final ModelViewManager modelViewManager) {
			return item;
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
		}

		@Override
		public void mouseEntered() {

		}

		@Override
		public void mouseExited() {

		}
	}

	private static final class CheckableDisplayElementToggleHandler implements EditabilityToggleHandler {
		private final Collection<CheckableDisplayElement<?>> elements;

		public CheckableDisplayElementToggleHandler(final Collection<CheckableDisplayElement<?>> elements) {
			this.elements = elements;
		}

		@Override
		public void makeEditable() {
			for (final CheckableDisplayElement<?> element : elements) {
				element.setChecked(true);
			}
		}

		@Override
		public void makeNotEditable() {
			for (final CheckableDisplayElement<?> element : elements) {
				element.setChecked(false);
			}
		}

	}
}
