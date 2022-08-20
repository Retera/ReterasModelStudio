
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.*;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ComponentTreeNode<T extends Named> extends NodeThing<T> {
	ModelStructureChangeListener changeListener = null;

	public ComponentTreeNode(ModelHandler modelHandler, T item) {
		super(modelHandler, item);
		setEditable1(modelView.isInEditable(item));
		setVisible1(modelView.isInVisible(item));
	}

	protected JLabel getItemLabel(T item) {
		JLabel itemLabel = new JLabel(item.getClass().getSimpleName() + ": " + item.getName());
		MouseAdapter mouseListener = getMouseListener();
		itemLabel.addMouseListener(mouseListener);
		itemLabel.addMouseMotionListener(mouseListener);
		return itemLabel;
	}

	public ComponentTreeNode<T> setVisible(ActionEvent e, boolean visible) {
		System.out.println("set visible! " + visible);
		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			undoManager.pushAction(setMultipleVisible(visible).redo());
		} else {
			undoManager.pushAction(setSingleVisible(visible).redo());
		}
		return this;
	}

	protected UndoAction setSingleVisible(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return new ShowHideSingleAction(item, visible, modelView, changeListener);
	}

	protected UndoAction setMultipleVisible(boolean visible) {
		Set<NodeThing<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (NodeThing<?> comp : comSet) {
			if (comp instanceof ComponentTreeNode) {
				itemSet.add(comp.setVisible1(visible));
			}
		}
		return new ShowHideMultipleAction(itemSet, visible, modelView, changeListener);
	}

	public ComponentTreeNode<T> setEditable(ActionEvent e, boolean editable) {
		System.out.println("setEd1, mods: " + e.getModifiers());

		UndoAction visAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleVisible(visible) : setSingleVisible(visible);

		if(visAction != null){
			undoManager.pushAction(visAction.redo());
		}
		return this;
	}

	protected UndoAction setSingleEditable(boolean editable) {
		System.out.println("editable!");
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return new SetEditableSingleAction(item, editable, modelView, changeListener);
	}

	protected UndoAction setMultipleEditable(boolean editable) {
		Set<NodeThing<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (NodeThing<?> comp : comSet) {
			if (comp instanceof ComponentTreeNode) {
				itemSet.add(comp.setEditable1(editable));
			}
		}
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


	protected MouseAdapter getMouseListener() {
		// Calling checking mechanism on mouse click
		return new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				System.out.println("[CompTreeNode] mouseClicked");
				super.mouseClicked(e);
			}

			@Override
			public void mouseEntered(final MouseEvent e) {
				System.out.println("[CompTreeNode] mouseEntered");
				highlight();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				System.out.println("[CompTreeNode] mouseExited");
				unHigthlight();
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
		};
	}

	private void doSelection(MouseEvent e) {
		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
//		System.out.println("mouse released: " + item.getName() + ", " + selectMouseButton + ", " + MouseEvent.getMaskForButton(e.getButton()) +  ", " + e.getModifiersEx() + ", " + (selectMouseButton & e.getModifiersEx()) + ", sameButton: " + (e.getButton() == selectMouseButton) + ", " + e);

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

		if (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton && newSelection != null) {
			System.out.println("should be selecting! ");
			Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
			Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();

//					if (modifiersEx == addSelectModifier) {
			if (isModUsed(e, addSelectModifier)) {
//						SelectionMode.ADD;
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
				}
//					} else if (modifiersEx == removeSelectModifier) {
			} else if (isModUsed(e, removeSelectModifier)) {
//						SelectionMode.DESELECT;
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
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
}
