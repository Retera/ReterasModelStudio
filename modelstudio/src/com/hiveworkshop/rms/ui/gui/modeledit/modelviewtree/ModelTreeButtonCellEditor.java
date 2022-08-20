package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import javax.swing.*;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.util.EventObject;

public class ModelTreeButtonCellEditor extends AbstractCellEditor implements TreeCellEditor {
	private JPanel lastComp;
	private NodeThing<?> lastObj;
	public ModelTreeButtonCellEditor() {
	}

	@Override
	public Object getCellEditorValue() {
		System.out.println("[ButtonCellEditor] getCellEditorValue");
		return "";
//			return value.toString();
	}

	@Override
	public boolean isCellEditable(EventObject anEvent) {
		boolean cellEditable = super.isCellEditable(anEvent);
		System.out.println("[ButtonCellEditor] isCellEditable: " + cellEditable + " event: " + anEvent);
		return cellEditable;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		boolean b = super.shouldSelectCell(anEvent);
		System.out.println("[ButtonCellEditor] shouldSelectCell: " + b + ", event: " + anEvent);
//		if(lastComp != null && b && anEvent instanceof MouseEvent){
//			System.out.println("correct and stuff!");
//
////			lastComp.dispatchEvent((MouseEvent) anEvent);
//			lastComp = null;
//
////			MouseEvent mouseEvent = new MouseEvent(lastObj.editableButton, ((MouseEvent) anEvent).getID(), System.currentTimeMillis(), ((MouseEvent) anEvent).getModifiersEx(), ((MouseEvent) anEvent).getX(), ((MouseEvent) anEvent).getY(), ((MouseEvent) anEvent).getClickCount(), ((MouseEvent) anEvent).isPopupTrigger());
////			lastObj.editableButton.dispatchEvent(mouseEvent);
//
////			ActionEvent e = new ActionEvent(lastObj.editableButton, ((MouseEvent) anEvent).getID(), "", ((MouseEvent) anEvent).getModifiersEx());
////			int mods = ((MouseEvent) anEvent).getModifiersEx()&(InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
//			int mods = 0;
////			if((((MouseEvent) anEvent).getModifiersEx()&InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK){
//			if(((MouseEvent) anEvent).getButton() == MouseEvent.BUTTON1){
//				System.out.println(mods + ", Button1!!");
//				mods = mods | 16;
//			}
//			if (((MouseEvent) anEvent).isShiftDown()){
//				mods |= 1;
//			}
//			if (((MouseEvent) anEvent).isControlDown()){
//				mods |= 2;
//			}
//			System.out.println("ModifiersEx: " + ((MouseEvent) anEvent).getModifiersEx() + ", mods: " + mods + ", ctrl: " + InputEvent.CTRL_DOWN_MASK + ", shift: " + InputEvent.SHIFT_DOWN_MASK + ", but1: " + InputEvent.BUTTON1_DOWN_MASK);
//			ActionEvent e = new ActionEvent(lastObj.editableButton,
//					ActionEvent.ACTION_PERFORMED, "ugg",
//					System.currentTimeMillis(),
//					mods);
//			lastObj.editableButton.getActionListeners()[0].actionPerformed(e);
//			lastObj = null;
//		}
		return b;
	}

	@Override
	public boolean stopCellEditing() {
		boolean b = super.stopCellEditing();
		System.out.println("[ButtonCellEditor] stopCellEditing: " + b);
//				fireEditingStopped();
		return b;
	}

	@Override
	public void cancelCellEditing() {
		System.out.println("[ButtonCellEditor] cancelCellEditing");
		super.cancelCellEditing();
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		if (value instanceof NodeThing) {
			System.out.println("[ButtonCellEditor] getTreeCellEditorComponent" + " - " + "returning renderComp");
//			System.out.println("returning renderComp");

			lastObj = (NodeThing<?>) value;
			lastComp = ((NodeThing<?>) value).getTreeRenderComponent();
			return lastComp;
		}
		return null;
	}
}
