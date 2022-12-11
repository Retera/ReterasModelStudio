package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.colorchooser.CharIconLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

public abstract class NodeThing<T> extends DefaultMutableTreeNode {
	ModelStructureChangeListener changeListener = null;
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
	protected CharIconLabel editableButton2;
	protected CharIconLabel visibleButton2;
	protected JPanel treeRenderComponent;

	public NodeThing(ModelHandler modelHandler, T item) {
		super();
		this.modelHandler = modelHandler;
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		this.item = item;
		this.changeListener = ModelStructureChangeListener.changeListener;

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

//		editableButton2 = new JLabel("E");
//		editableButton2 = new JLabel("\uF441");
//		editableButton2 = new JLabel("\u270B");
//		editableButton2 = new JLabel("\uD83D\uDC41");
//		editableButton2 = new JLabel("\uD83D\uDD91");
		editableButton2 = new CharIconLabel("\uD83E\uDD1A", 18, Color.GRAY, 18);

//		visibleButton2 = new CharIconLabel("V", 18, Color.GRAY, 18);
		visibleButton2 = new CharIconLabel("\uD83D\uDC41", 18, Color.GRAY, 18);

		treeRenderComponent.add(visibleButton2);
		treeRenderComponent.add(editableButton2);
		treeRenderComponent.add(itemLabel);
	}

	public abstract T updateState();

	protected void updateButtons(){
		visibleButton2.toggleOn(visible);
		editableButton2.toggleOn(editable);
	}

	protected abstract JLabel getItemLabel(T item);

	public T getItem() {
		return item;
	}

	public boolean isVisible() {
		return visible;
	}

	public NodeThing<T> setVisible(ActionEvent e, boolean visible) {
		System.out.println("[NodeThing] set visible! " + visible);
		UndoAction visAction = getVisAction(visible, isModUsed(e, ActionEvent.SHIFT_MASK));

		if(visAction != null){
			undoManager.pushAction(visAction.redo());
		}
		return this;
	}

	public NodeThing<T> setVisible(boolean visible, boolean multiple) {
		System.out.println("[NodeThing] set visible! " + visible);
		UndoAction visAction = getVisAction(visible, multiple);

		if(visAction != null){
			undoManager.pushAction(visAction.redo());
		}
		return this;
	}

	private UndoAction getVisAction(boolean visible, boolean multiple) {
		return multiple ? getShowHideMultipleAction(visible) : getShowHideSingleAction(visible);
	}

	public boolean isEditable() {
		return editable;
	}

	protected abstract UndoAction getShowHideSingleAction(boolean visible);

	protected abstract UndoAction getShowHideMultipleAction(boolean visible);

	protected abstract UndoAction getSetEditableSingleAction(boolean editable);

	protected abstract UndoAction getSetEditableMultipleAction(boolean editable);

	protected UndoAction getEdAction(boolean editable, boolean multiple) {
		return multiple ? getSetEditableMultipleAction(editable) : getSetEditableSingleAction(editable);
	}

	public ModelView highlight() {
		return modelHandler.getModelView().higthlight(item);
	}

	public void unHigthlight() {
		modelHandler.getModelView().unHigthlight(item);
	}

	public T updateVisibility(boolean visible) {
		this.visible = visible;
		visibleButton2.toggleOn(visible);
		return item;
	}

	public T updateEditability(boolean editable) {
		this.editable = editable;
		editableButton2.toggleOn(editable);
		return item;
	}

	protected Set<Object> getChildrenItemSet(Set<Object> itemSet) {
		itemSet.add(this.getItem());
		for (int i = 0; i < getChildCount(); i++) {
			TreeNode childAt = getChildAt(i);
			if (childAt instanceof NodeThing) {
				((NodeThing<?>) childAt).getChildrenItemSet(itemSet);
			}
		}
		return itemSet;
	}

	protected Set<NodeThing<?>> getChildComponents(Set<NodeThing<?>> thingsToAffect) {
		thingsToAffect.add(this);
		for (int i = 0; i < getChildCount(); i++) {
			TreeNode childAt = getChildAt(i);
			if (childAt instanceof NodeThing) {
				((NodeThing<?>) childAt).getChildComponents(thingsToAffect);
			}
		}
		if(this instanceof ComponentTreeGeosetsTopNode){
//			System.out.println("GeoTop-child-comps:" + thingsToAffect.size());
		}
		return thingsToAffect;
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

	public Component getComponentAt(Point point){
		if(treeRenderComponent.getBounds().contains(point)){
			for(Component component : treeRenderComponent.getComponents()) {
				if(component.getBounds().contains(point)){
					return component;
				}
			}
			return treeRenderComponent;
		}
		return null;
	}

	public UndoAction clickAt(int x, int y, MouseEvent e){
		if(e.getID() == MouseEvent.MOUSE_DRAGGED || e.getID() == MouseEvent.MOUSE_PRESSED){
//			System.out.println("[NodeThingME] clickAt: [" + x + ", " + y + "], bounds: " + treeRenderComponent.getBounds());

			for(Component component : treeRenderComponent.getComponents()) {
//				System.out.println("\t[NodeThingME] clickAt: [" + x + ", " + y + "], "
//						+ "contains: " + component.getBounds().contains(x,y)
//						+ ", " + component);
				if(component.getBounds().contains(x,y)){

//					System.out.println("[NodeThingME] clickAt: [" + x + ", " + y + "], " + component);
					if(component == editableButton2){
						boolean multiple = isModUsed(e, InputEvent.SHIFT_DOWN_MASK) && e.getID() != MouseEvent.MOUSE_DRAGGED;
						return getEdAction(!editable, multiple);
					}
					if(component == visibleButton2){
						boolean multiple = isModUsed(e, InputEvent.SHIFT_DOWN_MASK) && e.getID() != MouseEvent.MOUSE_DRAGGED;
						return getVisAction(!visible, multiple);
					}
					if(component == itemLabel){
						System.out.println("\t "+ e);
						return getSelectAction(e);// drag throws error, fix to use getModifiersEx
					}
					return null;
				}
			}
		}
		return null;
	}

	protected UndoAction getSelectAction(MouseEvent e) {
		return null;
	}

}
