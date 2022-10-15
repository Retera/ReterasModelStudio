package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.*;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ModelTreeMouseAdapter extends MouseAdapter {
	private final Consumer<Boolean> controlDown;
	private final ComponentThingTree tree;
	private NodeThing<?> lastNode;
	private Set<NodeThing<?>> affectedNodes = new HashSet<>();
	private Set<UndoAction> undoActions = new HashSet<>();
	private UndoAction lastValidAction;
	private UndoManager undoManager;

	public ModelTreeMouseAdapter(Consumer<Boolean> controlDown, ComponentThingTree tree){
		this.controlDown = controlDown;
		this.tree = tree;
	}

	public ModelTreeMouseAdapter setUndoManager(UndoManager undoManager) {
		this.undoManager = undoManager;
		return this;
	}

	private void highlight(MouseEvent e){
		controlDown.accept(e.isControlDown());
		TreePath pathForLocation = tree.getPathForLocation(e.getX(), e.getY());
		if (pathForLocation != null && pathForLocation.getLastPathComponent() instanceof NodeThing) {
			if(lastNode != pathForLocation.getLastPathComponent()){
				NodeThing<?> lastN2 = (NodeThing<?>) pathForLocation.getLastPathComponent();
				JPanel rComp = lastN2.getTreeRenderComponent();
				Rectangle pathBounds = tree.getPathBounds(pathForLocation);
//				System.out.println("[ModelTreeMouseAdapter] HL, node: " + lastN2 + ", mouse: [" + e.getX() + "," + e.getY() +"], "
//						+ pathBounds + "/" + rComp.getBounds()
//						+ ", width: " + rComp.getWidth() + ", x: " + rComp.getX());
//				System.out.println("mouseInComp: " + (e.getX()-pathBounds.getX()) + ", " + (e.getY()-pathBounds.getY()));
//				System.out.println(", MousePos: " + rComp.getMousePosition(true));
////				System.out.println(", screenLoc: " + rComp.getLocationOnScreen());
			}
			Rectangle pathBounds = tree.getPathBounds(pathForLocation);
			lastNode = (NodeThing<?>) pathForLocation.getLastPathComponent();
			if(!affectedNodes.contains(lastNode)){

				UndoAction affected = lastNode.clickAt((int) (e.getX() - pathBounds.getX()), (int) (e.getY() - pathBounds.getY()), e);
				if(affected != null && (lastValidAction == null || affected.getClass() == lastValidAction.getClass())){
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
	private void unHighLight(){
		if(lastNode != null){
			lastNode.unHigthlight();
			lastNode = null;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
//		System.out.println("[ModelTreeMouseAdapter] mouseClicked");
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
//		System.out.println("[ModelTreeMouseAdapter] mouseEntered");
		highlight(e);
		super.mouseEntered(e);
//		System.out.println(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
//		System.out.println("[ModelTreeMouseAdapter] mouseExited");
		super.mouseExited(e);
		unHighLight();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		affectedNodes.clear();
		lastValidAction = null;
		highlight(e);
		super.mousePressed(e);
//		System.out.println("[ModelTreeMouseAdapter] mousePressed: " + e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		affectedNodes.clear();
		if(undoManager != null && !undoActions.isEmpty()){
			if(undoActions.size() == 1){
				UndoAction action = undoActions.stream().findFirst().get();
				undoManager.pushAction(action);
			} else {
				String name;
				if(lastValidAction instanceof SetEditableMultipleAction){
					name = "Set Edibility";
				} else if (lastValidAction instanceof AddSelectionUggAction){
					name = "Add Selection";
				} else if (lastValidAction instanceof SetSelectionUggAction){
					name = "Select";
				} else if (lastValidAction instanceof RemoveSelectionUggAction){
					name = "Remove Selection";
				} else if (lastValidAction instanceof ShowHideMultipleAction){
					name = "Set Visibility";
				} else if (lastValidAction instanceof SetGeosetsVisibilityAction){
					name = "Set Geosets Visibility";
				} else if (lastValidAction instanceof SetIdObjectsVisibilityAction){
					name = "Set Nodes Visibility";
				} else if (lastValidAction instanceof SetCamerasVisibilityAction){
					name = "Set Cameras Visibility";
				} else if (lastValidAction instanceof SetGeosetsEdibilityAction){
					name = "Set Geosets Edibility";
				} else if (lastValidAction instanceof SetIdObjectsEdibilityAction){
					name = "Set Nodes Edibility";
				} else if (lastValidAction instanceof SetCamerasEdibilityAction){
					name = "Set Cameras Edibility";
				} else {
					name = "Stuff";
				}
				undoManager.pushAction(new CompoundAction(name, new ArrayList<>(undoActions)));
			}
			undoActions.clear();
		}
		lastValidAction = null;
		super.mouseReleased(e);
		highlight(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
//		tree.startEditingAtPath(tree.getPathForLocation(e.getX(), e.getY()));
//		controlDown.accept(e.isControlDown());
		highlight(e);
	}
	public void mouseWheelMoved(MouseWheelEvent e){
//		System.out.println("[ModelTreeMouseAdapter] mouseWheelMoved: " + e);
		super.mouseWheelMoved(e);
	}
	public void mouseDragged(MouseEvent e){
//		System.out.println("[ModelTreeMouseAdapter] mouseDragged: " + e.toString());
		super.mouseDragged(e);
		highlight(e);
	}

	private NodeThing<?> getNode(int x, int y){
		TreePath pathForLocation = tree.getPathForLocation(x, y);
		if (pathForLocation != null && pathForLocation.getLastPathComponent() instanceof NodeThing) {
			return  (NodeThing<?>) pathForLocation.getLastPathComponent();
		}
		return null;
	}
}
