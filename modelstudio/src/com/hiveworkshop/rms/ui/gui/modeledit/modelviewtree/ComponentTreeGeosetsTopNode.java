
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.SetEditableMultipleAction;
import com.hiveworkshop.rms.editor.actions.selection.SetGeosetsEdibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.SetGeosetsVisibilityAction;
import com.hiveworkshop.rms.editor.actions.selection.ShowHideMultipleAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeGeosetsTopNode extends NodeThing<String> {

	public ComponentTreeGeosetsTopNode(ModelHandler modelHandler) {
		super(modelHandler, "Geosets");
		setEditable1(modelView.isGeosetsEditable());
		setVisible1(modelView.isGeosetsVisible());
	}

	protected JLabel getItemLabel(String title) {
		JLabel itemLabel = new JLabel(title);
		MouseListener mouseListener = getMouseListener();
		itemLabel.addMouseListener(mouseListener);
		return itemLabel;
	}

	public ComponentTreeGeosetsTopNode setVisible(ActionEvent e, boolean visible) {
//		UndoAction visAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleVisible(visible) : setSingleVisible(visible);
//
//		if(visAction != null){
//			undoManager.pushAction(visAction.redo());
//		}
		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			System.out.println("[geosetTopNode] setMultipleVisible, " + e);
			undoManager.pushAction(setMultipleVisible(visible).redo());
		} else {
			System.out.println("[geosetTopNode] setVisible, " + e);
			undoManager.pushAction(setSingleVisible(visible).redo());
		}
		return this;
	}

	protected UndoAction setSingleVisible(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return new SetGeosetsVisibilityAction(visible, modelView, null);
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
		return new CompoundAction("Set geosets editability", null,
				new SetGeosetsVisibilityAction(visible, modelView, null),
				new ShowHideMultipleAction(itemSet, visible, modelView, null));
	}

	public ComponentTreeGeosetsTopNode setEditable(ActionEvent e, boolean editable) {
//		UndoAction edAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleEditable(visible) : setSingleEditable(visible);
//
//		if(edAction != null){
//			undoManager.pushAction(edAction.redo());
//		}

		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			System.out.println("[geosetTopNode] setMultipleEditable, " + e);
			System.out.println(e.getModifiers());
			undoManager.pushAction(setMultipleEditable(editable).redo());
		} else {
			System.out.println("[geosetTopNode] setEditable, " + e);
			System.out.println(e.getModifiers());
			undoManager.pushAction(setSingleEditable(editable).redo());
		}
		return this;
	}

	protected UndoAction setSingleEditable(boolean editable) {
		System.out.println("editable!");
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return new SetGeosetsEdibilityAction(editable, modelView, null);
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
		return new CompoundAction("Set geosets editability", null,
				new SetGeosetsEdibilityAction(editable, modelView, null),
				new SetEditableMultipleAction(itemSet, editable, modelView, null));
	}

	public JPanel getTreeRenderComponent() {
		treeRenderComponent.setOpaque(true);
		treeRenderComponent.revalidate();

		itemLabel.setText("Geosets");
		treeRenderComponent.setBackground(color1);

		return treeRenderComponent;
	}


	protected MouseAdapter getMouseListener() {
		// Calling checking mechanism on mouse click
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				System.out.println("[GeosetComp] mouseClicked");
				super.mouseClicked(e);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				System.out.println("[GeosetComp] mouseEntered");
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("[GeosetComp] mouseExited");
				super.mouseExited(e);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				System.out.println("[GeosetComp] mousePressed: " + e);
				doSelection(e);
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				System.out.println("[GeosetComp] mouseReleased: " + e);
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
