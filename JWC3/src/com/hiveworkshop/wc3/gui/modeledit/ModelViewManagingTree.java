package com.hiveworkshop.wc3.gui.modeledit;

import javax.swing.tree.DefaultTreeModel;

import com.etheller.collections.ArrayList;
import com.etheller.collections.Collection;
import com.etheller.collections.HashMap;
import com.etheller.collections.List;
import com.etheller.collections.Map;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.wc3.gui.modeledit.util.JCheckBoxTree;
import com.hiveworkshop.wc3.gui.modeledit.util.JCheckBoxTreeNode;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public final class ModelViewManagingTree extends JCheckBoxTree {
	private final ModelViewManager modelViewManager;

	public ModelViewManagingTree(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager) {
		super(buildTreeModel(modelViewManager));
		this.modelViewManager = modelViewManager;
		addCheckChangeEventListener(new CheckChangeEventListener() {
			@Override
			public void checkStateChanged(final CheckChangeEvent event) {
				final JCheckBoxTreeNode sourceNode = (JCheckBoxTreeNode) event.getSource();
				final List<CheckableDisplayElement<?>> components = new ArrayList<>();
				handleNodeRecursively(sourceNode, components);
				final CheckableDisplayElementToggleHandler toggleHandler = new CheckableDisplayElementToggleHandler(
						components);
				UndoAction showHideComponentAction;
				if (isSelected(sourceNode)) {
					showHideComponentAction = modelEditorManager.getSelectingEventHandler()
							.showComponent(toggleHandler);
				} else {
					final Runnable refreshGUIRunnable = new Runnable() {
						@Override
						public void run() {
							reloadFromModelView();
						}
					};
					showHideComponentAction = modelEditorManager.getSelectingEventHandler().hideComponent(components,
							toggleHandler, refreshGUIRunnable);
				}
				undoActionListener.pushAction(showHideComponentAction);
			}

			private void handleNodeRecursively(final JCheckBoxTreeNode parent,
					final List<CheckableDisplayElement<?>> components) {
				notifyModelViewManagerStateChanged(parent, components);
				for (int i = 0; i < parent.getChildCount(); i++) {
					final JCheckBoxTreeNode childAt = (JCheckBoxTreeNode) parent.getChildAt(i);
					handleNodeRecursively(childAt, components);
				}
			}

			private void notifyModelViewManagerStateChanged(final JCheckBoxTreeNode sourceNode,
					final List<CheckableDisplayElement<?>> components) {
				final Object userObject = sourceNode.getUserObject();
				final CheckableDisplayElement<?> element = (CheckableDisplayElement<?>) userObject;
				element.setChecked(isSelected(sourceNode));
				components.add(element);
			}
		});
	}

	public void reloadFromModelView() {
		setModel(buildTreeModel(modelViewManager));
	}

	private static DefaultTreeModel buildTreeModel(final ModelViewManager modelViewManager) {
		final JCheckBoxTreeNode root = new JCheckBoxTreeNode(new CheckableModelElement(modelViewManager));

		final JCheckBoxTreeNode mesh = new JCheckBoxTreeNode(new CheckableDummyElement(modelViewManager, "Mesh"));

		for (final Geoset geoset : modelViewManager.getModel().getGeosets()) {
			mesh.add(new JCheckBoxTreeNode(new CheckableGeosetElement(modelViewManager, geoset),
					modelViewManager.getEditableGeosets().contains(geoset)));
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
				List<JCheckBoxTreeNode> awaitingChildrenList = nodeToChildrenAwaitingLink.get(parent);
				if (awaitingChildrenList == null) {
					awaitingChildrenList = new ArrayList<>();
					nodeToChildrenAwaitingLink.put(parent, awaitingChildrenList);
				}
				awaitingChildrenList.add(treeNode);
			} else {
				parentTreeNode.add(treeNode);
			}
			final List<JCheckBoxTreeNode> childrenNeedingLinkToCurrentNode = nodeToChildrenAwaitingLink.get(object);
			if (childrenNeedingLinkToCurrentNode != null
					&& !Collection.Util.isEmpty(childrenNeedingLinkToCurrentNode)) {
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

		final DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);
		return defaultTreeModel;
	}

	private static abstract class CheckableDisplayElement<T> implements SelectableComponent {
		private final ModelViewManager modelViewManager;
		protected final T item;

		public CheckableDisplayElement(final ModelViewManager modelViewManager, final T item) {
			this.modelViewManager = modelViewManager;
			this.item = item;
		}

		public void setChecked(final boolean checked) {
			setChecked(item, modelViewManager, checked);
		}

		protected abstract void setChecked(T item, ModelViewManager modelViewManager, boolean checked);

		@Override
		public String toString() {
			return getName(item, modelViewManager);
		}

		protected abstract String getName(T item, ModelViewManager modelViewManager);
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
			return "Geoset " + modelViewManager.getModel().getGeosetId(item);
		}

		@Override
		public void visit(final SelectableComponentVisitor visitor) {
			visitor.accept(item);
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
