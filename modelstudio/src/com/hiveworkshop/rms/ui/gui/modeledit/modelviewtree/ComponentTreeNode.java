
package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.selection.*;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import net.miginfocom.swing.MigLayout;

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

public class ComponentTreeNode<T extends Named> extends DefaultMutableTreeNode {
	private Color color1 = new Color(255, 255, 255, 0);
	private Color color2 = new Color(55, 200, 55, 20);
	private Color buttonBGOn = new Color(120, 120, 120, 255);
	private Color buttonBGOff = new Color(55, 55, 55, 255);
	private ModelHandler modelHandler;
	private ModelView modelView;
	private UndoManager undoManager;
	private T item;
	private boolean visible = true;
	private boolean editable = true;
	private JLabel itemLabel;
	private JButton editableButton;
	private JButton visibleButton;
	private JPanel treeRenderComponent;
	String sgCompName = "sg CompName";
	//	ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	ModelStructureChangeListener changeListener = null;
//	private JLabel editableLabel;

	public ComponentTreeNode(ModelHandler modelHandler, T item) {
		super();
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		this.item = item;
		this.editable = modelView.isInEditable(item);
		this.visible = modelView.isInVisible(item);

		makeRenderComponent(item);
	}

	private void makeRenderComponent(T item) {
		treeRenderComponent = new JPanel(new MigLayout("ins 0, gap 0", "[" + sgCompName + "][right][right]"));
//		treeRenderComponent.setOpaque(true);
		treeRenderComponent.setBackground(color1);
//		treeRenderComponent.setFocusable(true);

		itemLabel = new JLabel(item.getClass().getSimpleName() + ": " + item.getName());
		itemLabel.addMouseListener(getMouseListener());

		editableButton = new JButton("E");
		editableButton.setBackground(getButtonBGColor(editable));
		editableButton.addActionListener(e -> setEditable(e, !editable));

		visibleButton = new JButton("V");
		visibleButton.setBackground(getButtonBGColor(visible));
		visibleButton.addActionListener(e -> setVisible(e, !visible));

		treeRenderComponent.add(editableButton);
		treeRenderComponent.add(visibleButton);
		treeRenderComponent.add(itemLabel);
	}


	public ComponentTreeNode(final Object userObject) {
		super(userObject);
	}


	public boolean isVisible() {
		return visible;
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

	private UndoAction setSingleVisible(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return new ShowHideSingleAction(item, visible, modelView, changeListener);
	}

	private UndoAction setMultipleVisible(boolean visible) {
		Set<ComponentTreeNode<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (ComponentTreeNode<?> comp : comSet) {
			itemSet.add(comp.setVisible1(visible));
		}
		return new ShowHideMultipleAction(itemSet, visible, modelView, changeListener);
	}

	public T setVisible1(boolean visible) {
		this.visible = visible;
		visibleButton.setBackground(getButtonBGColor(visible));
		return item;
	}

	public boolean isEditable() {
		return editable;
	}

	public ComponentTreeNode<T> setEditable(ActionEvent e, boolean editable) {
		System.out.println("setEd1");

		if (isModUsed(e, ActionEvent.SHIFT_MASK)) {
			undoManager.pushAction(setMultipleEditable(editable).redo());
		} else {
			undoManager.pushAction(setSingleEditable(editable).redo());
		}
		return this;
	}

	private UndoAction setSingleEditable(boolean editable) {
		System.out.println("editable!");
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return new SetEditableSingleAction(item, editable, modelView, changeListener);
	}

	private UndoAction setMultipleEditable(boolean editable) {
		Set<ComponentTreeNode<?>> comSet = new HashSet<>();
		getChildComponents(comSet);
		Set<Object> itemSet = new HashSet<>();
		for (ComponentTreeNode<?> comp : comSet) {
			itemSet.add(comp.setEditable1(editable));
		}
		return new SetEditableMultipleAction(itemSet, editable, modelView, changeListener);
	}

	public T setEditable1(boolean editable) {
		this.editable = editable;
		editableButton.setBackground(getButtonBGColor(editable));
		return item;
	}

	private void getChildComponents(Set<ComponentTreeNode<?>> thingsToAffect) {
		thingsToAffect.add(this);
		for (int i = 0; i < getChildCount(); i++) {
			TreeNode childAt = getChildAt(i);
			if (childAt instanceof ComponentTreeNode) {
				((ComponentTreeNode<?>) childAt).getChildComponents(thingsToAffect);
			}
		}
	}

	private Color getButtonBGColor(boolean isOn) {
		return isOn ? buttonBGOn : buttonBGOff;
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
			newSelection = new SelectionBundle(Collections.singleton((Camera) item));
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
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView).redo());
				}
//					} else if (modifiersEx == removeSelectModifier) {
			} else if (isModUsed(e, removeSelectModifier)) {
//						SelectionMode.DESELECT;
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
					undoManager.pushAction(new RemoveSelectionUggAction(newSelection, modelView).redo());
				}
			} else {
//						SelectionMode.SELECT;
				System.out.println("normal select! ");
				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameras())) {
					System.out.println("viable selection! ");
					undoManager.pushAction(new SetSelectionUggAction(newSelection, modelView).redo());
				}
			}
//					System.out.println("newSel: idob: " + newSelection.getSelectedIdObjects().size() + ", vert: " + newSelection.getSelectedVertices().size());
		}
	}
}
