package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.*;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.util.TwiTreeStuff.TwiTreeMouseAdapter;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ModelTreeMouseAdapter extends TwiTreeMouseAdapter {
	private final ComponentThingTree tree;
	private NodeThing<?> lastNode;
	private final Set<NodeThing<?>> affectedNodes = new HashSet<>();
	private final Set<UndoAction> undoActions = new LinkedHashSet<>();
	private UndoAction lastValidAction;
	private UndoManager undoManager;

	public ModelTreeMouseAdapter(Consumer<Boolean> expansionPropagationKeyDown, ComponentThingTree tree) {
		super(expansionPropagationKeyDown);
		this.tree = tree;

		setMouseEnteredConsumer(e -> highlight(e));
		setMouseExitedConsumer(e -> unHighLight());
		setMousePressedConsumer(e -> onMousePressed(e));
		setMouseReleasedConsumer(e -> onMouseReleased(e));
		setMouseMovedConsumer(e -> highlight(e));
		setMouseDraggedConsumer(e -> highlight(e));

	}

	public ModelTreeMouseAdapter setUndoManager(UndoManager undoManager) {
		this.undoManager = undoManager;
		return this;
	}

	private void highlight(MouseEvent e) {
		TreePath pathForLocation = tree.getPathForLocation(e.getX(), e.getY());
		if (pathForLocation != null && pathForLocation.getLastPathComponent() instanceof NodeThing) {
			Rectangle pathBounds = tree.getPathBounds(pathForLocation);
			lastNode = (NodeThing<?>) pathForLocation.getLastPathComponent();
			if (!affectedNodes.contains(lastNode)) {

				UndoAction affected = lastNode.clickAt((int) (e.getX() - pathBounds.getX()), (int) (e.getY() - pathBounds.getY()), e);
				if (affected != null && (lastValidAction == null || affected.getClass() == lastValidAction.getClass())) {
					affectedNodes.add(lastNode);
					lastValidAction = affected;
					undoActions.add(affected.redo());
				}
			}
			lastNode.highlight();
		} else {
			unHighLight();
		}
	}
	private void unHighLight() {
		if (lastNode != null) {
			lastNode.unHigthlight();
			lastNode = null;
		}
	}

	private void onMousePressed(MouseEvent e) {
		affectedNodes.clear();
		lastValidAction = null;
		highlight(e);
	}

	private void onMouseReleased(MouseEvent e) {
		affectedNodes.clear();
		doUndoActions();
		lastValidAction = null;
		highlight(e);
	}

	private void doUndoActions() {
		if (undoManager != null && !undoActions.isEmpty()) {
			String name;
			if (undoActions.size() == 1) {
				UndoAction action = undoActions.stream().findFirst().get();
				name = action.actionName();
			} else {
				if (lastValidAction instanceof SetEditableMultipleAction) {
					name = "Set Edibility";
				} else if (lastValidAction instanceof AddSelectionUggAction) {
					name = "Add Selection";
				} else if (lastValidAction instanceof SetSelectionUggAction) {
					name = "Select";
				} else if (lastValidAction instanceof RemoveSelectionUggAction) {
					name = "Remove Selection";
				} else if (lastValidAction instanceof ShowHideMultipleAction) {
					name = "Set Visibility";
				} else if (lastValidAction instanceof SetGeosetsVisibilityAction) {
					name = "Set Geosets Visibility";
				} else if (lastValidAction instanceof SetIdObjectsVisibilityAction) {
					name = "Set Nodes Visibility";
				} else if (lastValidAction instanceof SetCamerasVisibilityAction) {
					name = "Set Cameras Visibility";
				} else if (lastValidAction instanceof SetGeosetsEdibilityAction) {
					name = "Set Geosets Edibility";
				} else if (lastValidAction instanceof SetIdObjectsEdibilityAction) {
					name = "Set Nodes Edibility";
				} else if (lastValidAction instanceof SetCamerasEdibilityAction) {
					name = "Set Cameras Edibility";
				} else {
					name = "Stuff";
				}
			}
			undoManager.pushAction(new CompoundAction(name, new ArrayList<>(undoActions), ModelStructureChangeListener.changeListener::nodesUpdated));
			ModelStructureChangeListener.changeListener.nodesUpdated();
			undoActions.clear();
		}
	}


	private NodeThing<?> getNode(int x, int y) {
		TreePath pathForLocation = tree.getPathForLocation(x, y);
		if (pathForLocation != null && pathForLocation.getLastPathComponent() instanceof NodeThing) {
			return  (NodeThing<?>) pathForLocation.getLastPathComponent();
		}
		return null;
	}
}
