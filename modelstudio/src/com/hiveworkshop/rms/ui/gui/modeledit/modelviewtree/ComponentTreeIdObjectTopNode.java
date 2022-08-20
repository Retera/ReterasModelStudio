
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
import com.hiveworkshop.rms.editor.actions.selection.SetIdObjectsEdibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetIdObjectsVisibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowHideMultipleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeIdObjectTopNode extends NodeThing<String> {
	public ComponentTreeIdObjectTopNode(ModelHandler modelHandler) {
		super(modelHandler, "Nodes");
		setEditable1(modelView.isIdObjectsEditable());
		setVisible1(modelView.isIdObjectsVisible());
	}

	protected JLabel getItemLabel(String title) {
		JLabel itemLabel = new JLabel(title);
		itemLabel.addMouseListener(getMouseListener());
		return itemLabel;
	}

	public ComponentTreeIdObjectTopNode setVisible(ActionEvent e, boolean visible) {
		UndoAction visAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleVisible(visible) : setSingleVisible(visible);

		if(visAction != null){
			undoManager.pushAction(visAction.redo());
		}
		return this;
	}

	protected UndoAction setSingleVisible(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return new SetIdObjectsVisibilityAction(visible, modelView, null);
	}

	protected UndoAction setMultipleVisible(boolean visible) {
		Set<NodeThing<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (NodeThing<?> comp : comSet) {
			itemSet.add(comp.setVisible1(visible));
		}
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return new CompoundAction("Set nodes editability", null,
				new SetIdObjectsVisibilityAction(visible, modelView, null),
				new ShowHideMultipleAction(itemSet, visible, modelView, null));
	}

	public ComponentTreeIdObjectTopNode setEditable(ActionEvent e, boolean editable) {
		System.out.println("setEd1");
		UndoAction edAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleEditable(editable) : setSingleEditable(editable);

		if(edAction != null){
			undoManager.pushAction(edAction.redo());
		}
		return this;
	}

	protected UndoAction setSingleEditable(boolean editable) {
		System.out.println("editable!");
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return new SetIdObjectsEdibilityAction(editable, modelView, null);
	}

	protected UndoAction setMultipleEditable(boolean editable) {
		Set<NodeThing<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (NodeThing<?> comp : comSet) {
			itemSet.add(comp.setEditable1(editable));
		}
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return new CompoundAction("Set nodes editability", null,
				new SetIdObjectsEdibilityAction(editable, modelView, null),
				new SetEditableMultipleAction(itemSet, editable, modelView, null));
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);

		itemLabel.setText("Nodes");
		treeRenderComponent.setBackground(color1);
		return treeRenderComponent;
	}


	protected MouseAdapter getMouseListener() {
		// Calling checking mechanism on mouse click
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				System.out.println("mouseClicked");
				super.mouseClicked(e);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				System.out.println("mouseEntered");
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("mouseExited");
				super.mouseExited(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				System.out.println("mousePressed: " + e);
				doSelection(e);
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				System.out.println("mouseReleased: " + e);
//				doSelection(e);
				super.mouseReleased(e);
			}
		};
	}

	private void doSelection(MouseEvent e) {
		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
//		System.out.println("mouse released: " + item.getName() + ", " + selectMouseButton + ", " + MouseEvent.getMaskForButton(e.getButton()) +  ", " + e.getModifiersEx() + ", " + (selectMouseButton & e.getModifiersEx()) + ", sameButton: " + (e.getButton() == selectMouseButton) + ", " + e);

//		SelectionBundle newSelection = null;
//		System.out.println("selecting? " + (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton));
//		if(item instanceof IdObject){
//			System.out.println("IdObject!");
//			newSelection = new SelectionBundle(Collections.singleton((IdObject) item));
//		} else if(item instanceof Geoset){
//			System.out.println("Geoset!");
//			newSelection = new SelectionBundle(((Geoset) item).getVertices());
//		} else if(item instanceof Camera){
//			newSelection = new SelectionBundle(Collections.singleton((Camera) item));
//		} else {
//			System.out.println("not viable item :O");
//		}
//
//		if (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton && newSelection != null) {
//			System.out.println("should be selecting! ");
//			Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
//			Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();
//
////					if (modifiersEx == addSelectModifier) {
//			if (isModUsed(e, addSelectModifier)) {
////						SelectionMode.ADD;
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
//					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView).redo());
//				}
////					} else if (modifiersEx == removeSelectModifier) {
//			} else if (isModUsed(e, removeSelectModifier)) {
////						SelectionMode.DESELECT;
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
//					undoManager.pushAction(new RemoveSelectionUggAction(newSelection, modelView).redo());
//				}
//			} else {
////						SelectionMode.SELECT;
//				System.out.println("normal select! ");
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
//					System.out.println("viable selection! ");
//					undoManager.pushAction(new SetSelectionUggAction(newSelection, modelView).redo());
//				}
//			}
////					System.out.println("newSel: idob: " + newSelection.getSelectedIdObjects().size() + ", vert: " + newSelection.getSelectedVertices().size());
//		}
	}
}
