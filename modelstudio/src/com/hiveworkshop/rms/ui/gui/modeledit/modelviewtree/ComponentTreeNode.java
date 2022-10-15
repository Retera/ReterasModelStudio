
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.*;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeNode<T extends Named> extends NodeThing<T> {

	public ComponentTreeNode(ModelHandler modelHandler, T item) {
		super(modelHandler, item);
		updateState();
	}

	protected JLabel getItemLabel(T item) {
		return new JLabel(item.getClass().getSimpleName() + ": " + item.getName());
	}
	public T updateState() {
		this.visible = modelView.isInVisible(item);
		this.editable = modelView.isInEditable(item);
		updateButtons();
		return item;
	}

	protected UndoAction getShowHideSingleAction(boolean visible) {
		return new ShowHideMultipleAction(Collections.singleton(item), visible, modelView, changeListener);
	}

	protected UndoAction getShowHideMultipleAction(boolean visible) {
		System.out.println("[CompTreeNode] getShowHideMultipleAction: " + visible);
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new ShowHideMultipleAction(itemSet, visible, modelView, changeListener);
	}

	protected UndoAction getSetEditableSingleAction(boolean editable) {
		return new SetEditableMultipleAction(Collections.singleton(item), editable, modelView, changeListener);
	}

	protected UndoAction getSetEditableMultipleAction(boolean editable) {
		Set<Object> itemSet = getChildrenItemSet(new HashSet<>());
		return new SetEditableMultipleAction(itemSet, editable, modelView, changeListener);
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);
		itemLabel.setText(item.getClass().getSimpleName() + ": " + item.getName());
		if (item instanceof IdObject && modelView.isSelected((IdObject) item)
				|| item instanceof Camera && modelView.isSelected((Camera) item)) {
			treeRenderComponent.setBackground(color2);
		} else {
			treeRenderComponent.setBackground(color1);
		}
		return treeRenderComponent;
	}

	private SelectionBundle getSelectionBundle(MouseEvent e) {
		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
		SelectionBundle newSelection = null;
//		if(MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton){
		if((e.getModifiersEx() & selectMouseButton) == selectMouseButton){
//			System.out.println("selecting? " + (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton));
			System.out.println("selecting? " + ((e.getModifiersEx() & selectMouseButton) == selectMouseButton));
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
		}
		return newSelection;
	}

	protected UndoAction getSelectAction(MouseEvent e) {
		SelectionBundle newSelection = getSelectionBundle(e);
		if(newSelection != null){
			System.out.println("should be selecting! ");
			Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
			Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();
			if (isModUsed(e, addSelectModifier)
					|| e.getID() == MouseEvent.MOUSE_DRAGGED
					&& !isModUsed(e, removeSelectModifier)
					&& ProgramGlobals.getSelectionMode() != SelectionMode.DESELECT) {
//						SelectionMode.ADD;
				System.out.println("\tADD");
				if (!modelView.allSelected(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					return new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener);
				}
			} else if (isModUsed(e, removeSelectModifier)) {
//						SelectionMode.DESELECT;
				System.out.println("\tDESELECT");
				if (modelView.anySelected(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					return new RemoveSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener);

				}
			} else {
//						SelectionMode.SELECT;
				System.out.println("\tSELECT");
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					return new SetSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener);

				}
			}
		}
		return null;
	}
}
