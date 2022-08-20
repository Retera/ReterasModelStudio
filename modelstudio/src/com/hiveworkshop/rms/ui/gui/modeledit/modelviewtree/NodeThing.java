package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public abstract class NodeThing<T> extends DefaultMutableTreeNode {
	protected static final String sgCompName = "sg CompName";
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

	@Override
	public String toString() {
		if(item instanceof Named){
			return ((Named) item).getName();
		}
		return item.toString();
	}

	protected void makeRenderComponent(T item) {
		treeRenderComponent = new JPanel(new MigLayout("ins 0, gap 0", "[" + sgCompName + "][right][right]"));
		treeRenderComponent.setBackground(color1);

		itemLabel = getItemLabel(item);

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

	protected abstract JLabel getItemLabel(T item);
//	protected abstract void makeRenderComponent(T item);

	public T getItem() {
		return item;
	}


	public boolean isVisible() {
		return visible;
	}

	public NodeThing<T> setVisible(ActionEvent e, boolean visible) {
		System.out.println("set visible! " + visible);
		UndoAction visAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleVisible(visible) : setSingleVisible(visible);

		if(visAction != null){
			undoManager.pushAction(visAction.redo());
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
		UndoAction edAction = isModUsed(e, ActionEvent.SHIFT_MASK) ? setMultipleEditable(editable) : setSingleEditable(editable);

		if(edAction != null){
			undoManager.pushAction(edAction.redo());
		}
		return this;
	}

	protected abstract UndoAction setSingleEditable(boolean editable);

	protected abstract UndoAction setMultipleEditable(boolean editable);

	public ModelView highlight() {
		return modelHandler.getModelView().higthlight(item);
	}

	public void unHigthlight() {
		modelHandler.getModelView().unHigthlight(item);
	}

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

	protected boolean isModUsed(ActionEvent e, int mask) {
		return ((e.getModifiers() & mask) == mask);
	}

	protected boolean isModUsed(MouseEvent e, int mask) {
		return ((e.getModifiersEx() & mask) == mask);
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
//				doSelection(e);
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

//	private void doSelection(MouseEvent e) {
//		Integer selectMouseButton = ProgramGlobals.getPrefs().getSelectMouseButton();
////		System.out.println("mouse released: " + item.getName() + ", " + selectMouseButton + ", " + MouseEvent.getMaskForButton(e.getButton()) +  ", " + e.getModifiersEx() + ", " + (selectMouseButton & e.getModifiersEx()) + ", sameButton: " + (e.getButton() == selectMouseButton) + ", " + e);
//
//		SelectionBundle newSelection = null;
//		System.out.println("selecting? " + (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton));
//		if (item instanceof IdObject) {
//			System.out.println("IdObject!");
//			newSelection = new SelectionBundle(Collections.singleton((IdObject) item));
//		} else if (item instanceof Geoset) {
//			System.out.println("Geoset!");
//			newSelection = new SelectionBundle(((Geoset) item).getVertices());
//		} else if (item instanceof Camera) {
////			newSelection = new SelectionBundle(Collections.singleton((Camera) item));
//			Set<CameraNode> cameraNodes = new HashSet<>();
//			cameraNodes.add(((Camera) item).getSourceNode());
//			cameraNodes.add(((Camera) item).getTargetNode());
//			newSelection = new SelectionBundle(cameraNodes);
//		} else {
//			System.out.println("not viable item :O");
//		}
//
//		if (MouseEvent.getMaskForButton(e.getButton()) == selectMouseButton && newSelection != null) {
//			Integer addSelectModifier = ProgramGlobals.getPrefs().getAddSelectModifier();
//			Integer removeSelectModifier = ProgramGlobals.getPrefs().getRemoveSelectModifier();
//
//			if (isModUsed(e, addSelectModifier)) {
////						SelectionMode.ADD;
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
//					undoManager.pushAction(new AddSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
//				}
//			} else if (isModUsed(e, removeSelectModifier)) {
////						SelectionMode.DESELECT;
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
//					undoManager.pushAction(new RemoveSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
//				}
//			} else {
////						SelectionMode.SELECT;
//				if (!modelView.sameSelection(newSelection.getSelectedVertices(), newSelection.getSelectedIdObjects(), newSelection.getSelectedCameraNodes())) {
//					undoManager.pushAction(new SetSelectionUggAction(newSelection, modelView, ModelStructureChangeListener.changeListener).redo());
//				}
//			}
//		}
//	}
}
