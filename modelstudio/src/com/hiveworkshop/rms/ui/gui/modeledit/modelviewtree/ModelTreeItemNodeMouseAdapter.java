package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.selection.AddSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.RemoveSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.preferences.Nav3DMouseAction;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ModelTreeItemNodeMouseAdapter<T> extends MouseAdapter {
	protected ModelHandler modelHandler;
	protected ModelView modelView;
	protected UndoManager undoManager;
	protected T item;
	protected NodeThing<T> nodeThing;

	@Override
	public void mouseClicked(final MouseEvent e) {
		System.out.println("[CompTreeNode] mouseClicked");
		super.mouseClicked(e);
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		System.out.println("[CompTreeNode] mouseEntered");
		nodeThing.highlight();
		super.mouseEntered(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		System.out.println("[CompTreeNode] mouseExited");
		nodeThing.unHigthlight();
		super.mouseExited(e);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		System.out.println("[CompTreeNode] mousePressed: " + e);
		doSelection(e);
		super.mousePressed(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		System.out.println("[CompTreeNode] mouseReleased: " + e);
//				doSelection(e);
		super.mouseReleased(e);
	}
	public void mouseWheelMoved(MouseWheelEvent e){
		System.out.println("[CompTreeNode] mouseWheelMoved: " + e);
		super.mouseWheelMoved(e);
	}
	public void mouseDragged(MouseEvent e){
		System.out.println("[CompTreeNode] mouseDragged: " + e);
		super.mouseDragged(e);
	}
//			public void mouseMoved(MouseEvent e){
//				System.out.println("mouseMoved: " + e);
//				super.mouseMoved(e);
//			}




	private void doSelection(MouseEvent e) {
		Integer selectMouseButton = ProgramGlobals.getPrefs().getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.SELECT);
//		System.out.println("mouse released: " + item.getName() + ", " + selectMouseButton + ", " + MouseEvent.getMaskForButton(e.getButton()) +  ", " + e.getModifiersEx() + ", " + (selectMouseButton & e.getModifiersEx()) + ", sameButton: " + (e.getButton() == selectMouseButton) + ", " + e);

		SelectionBundle newSelection = getSelectionBundle(e, selectMouseButton);

		if (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton && newSelection != null) {
			System.out.println("should be selecting! ");
			Integer addSelectModifier = ProgramGlobals.getPrefs().getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.ADD_SELECT_MODIFIER);
			Integer removeSelectModifier = ProgramGlobals.getPrefs().getNav3DMousePrefs().getKeyStroke(Nav3DMouseAction.REMOVE_SELECT_MODIFIER);

//					if (modifiersEx == addSelectModifier) {
			if (isModUsed(e, addSelectModifier)) {
//						SelectionMode.ADD;
				if (!modelView.allSelected(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
				}
//					} else if (modifiersEx == removeSelectModifier) {
			} else if (isModUsed(e, removeSelectModifier)) {
//						SelectionMode.DESELECT;
				if (modelView.anySelected(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					undoManager.pushAction(new RemoveSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
				}
			} else {
//						SelectionMode.SELECT;
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					undoManager.pushAction(new SetSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
				}
			}
		}
	}

	private SelectionBundle getSelectionBundle(MouseEvent e, Integer selectMouseButton) {
		SelectionBundle newSelection = null;
		System.out.println("selecting? " + (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton));
		if (item instanceof IdObject) {
			System.out.println("IdObject!");
			newSelection = new SelectionBundle(Collections.singleton((IdObject) item));
		} else if (item instanceof Geoset) {
			System.out.println("Geoset!");
			newSelection = new SelectionBundle(((Geoset) item).getVertices());
		} else if (item instanceof Camera) {
			Set<CameraNode> cameraNodes = new HashSet<>();
			cameraNodes.add(((Camera) item).getSourceNode());
			cameraNodes.add(((Camera) item).getTargetNode());
			newSelection = new SelectionBundle(cameraNodes);
		} else {
			System.out.println("not viable item :O");
		}
		return newSelection;
	}

	protected boolean isModUsed(MouseEvent e, int mask) {
		return ((e.getModifiersEx() & mask) == mask);
	}
}
