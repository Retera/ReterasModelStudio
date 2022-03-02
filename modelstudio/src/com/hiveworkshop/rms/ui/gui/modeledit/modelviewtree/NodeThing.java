package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class NodeThing<T> extends DefaultMutableTreeNode {
	protected Color color1 = new Color(255, 255, 255, 0);
	protected Color color2 = new Color(55, 200, 55, 20);
	protected Color buttonBGOn = new Color(120, 120, 120, 255);
	protected Color buttonBGOff = new Color(55, 55, 55, 255);
	protected ModelHandler modelHandler;
	protected ModelView modelView;
	protected UndoManager undoManager;
	protected T item;
	protected boolean visible = true;
	protected boolean editable = true;
	protected JLabel itemLabel;
	protected JButton editableButton;
	protected JButton visibleButton;
	protected JPanel treeRenderComponent;

	public NodeThing(ModelHandler modelHandler, T item) {
		super();
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		this.item = item;

		makeRenderComponent(item);
	}

	protected abstract void makeRenderComponent(T item);

	public T getItem() {
		return item;
	}


	public boolean isVisible() {
		return visible;
	}

	public NodeThing<T> setVisible(ActionEvent e, boolean visible) {
		System.out.println("set visible! " + visible);
		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			undoManager.pushAction(setMultipleVisible(visible).redo());
		} else {
			undoManager.pushAction(setSingleVisible(visible).redo());
		}
		return this;
	}

	protected abstract UndoAction setSingleVisible(boolean visible);

	protected abstract UndoAction setMultipleVisible(boolean visible);

	public T setVisible1(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return item;
	}

	public boolean isEditable() {
		return editable;
	}

	public NodeThing<T> setEditable(ActionEvent e, boolean editable) {
		System.out.println("setEd1");

		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			undoManager.pushAction(setMultipleEditable(editable).redo());
		} else {
			undoManager.pushAction(setSingleEditable(editable).redo());
		}
		return this;
	}

	protected abstract UndoAction setSingleEditable(boolean editable);

	protected abstract UndoAction setMultipleEditable(boolean editable);

	public T setEditable1(boolean editable) {
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return item;
	}

	protected void getChildComponents(Set<NodeThing<?>> thingsToAffect) {
		thingsToAffect.add(this);
		for (int i = 0; i < getChildCount(); i++) {
			TreeNode childAt = getChildAt(i);
			if (childAt instanceof NodeThing) {
				((NodeThing<?>) childAt).getChildComponents(thingsToAffect);
			}
		}
	}

	protected Color getButtonBGColor(boolean isOn) {
		return isOn ? buttonBGOn : buttonBGOff;
	}

	public abstract JPanel getTreeRenderComponent();

	private boolean isModUsed(ActionEvent e, int mask) {
		return ((e.getModifiers() & mask) == mask);
	}

	private boolean isModUsed(MouseEvent e, int mask) {
		return ((e.getModifiersEx() & mask) == mask);
	}


	private MouseListener getMouseListener() {
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

		SelectionBundle newSelection = null;
		System.out.println("selecting? " + (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton));
		if (item instanceof IdObject) {
			System.out.println("IdObject!");
			newSelection = new SelectionBundle(Collections.singleton((IdObject) item));
		} else if (item instanceof Geoset) {
			System.out.println("Geoset!");
			newSelection = new SelectionBundle(((Geoset) item).getVertices());
		} else if (item instanceof Camera) {
//			newSelection = new SelectionBundle(Collections.singleton((Camera) item));
			Set<CameraNode> cameraNodes = new HashSet<>();
			cameraNodes.add(((Camera) item).getSourceNode());
			cameraNodes.add(((Camera) item).getTargetNode());
			newSelection = new SelectionBundle(cameraNodes);
		} else {
			System.out.println("not viable item :O");
		}

		if (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton && newSelection != null) {
			Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
			Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();

			if (isModUsed(e, addSelectModifier)) {
//						SelectionMode.ADD;
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
				}
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
