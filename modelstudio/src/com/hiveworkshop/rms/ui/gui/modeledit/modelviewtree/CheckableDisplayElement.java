//package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;
//
//import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
//
//import java.util.Objects;
//
//public abstract class CheckableDisplayElement<T> {
//	protected final ModelView modelView;
//	protected final T item;
//
//	public CheckableDisplayElement(ModelView modelView, T item) {
//		this.modelView = modelView;
//		this.item = item;
//	}
//
//	public void setChecked(boolean checked) {
//		setChecked(item, modelView, checked);
//	}
//
//	public void mouseEntered() {
//	}
//
//	public void mouseExited() {
//	}
//
//	protected abstract void setChecked(T item, ModelView modelView, boolean checked);
//
//	public abstract void setEditable(boolean editable);
//	public abstract void setVisible(boolean visible);
////	public abstract boolean isEditable();
////	public abstract boolean isVisible();
////	public void setEditable(ModelView modelView, boolean editable){
//////		modelView.makeEditable(item);
////	}
////	public void setVisible(ModelView modelView, boolean visible){
////
//////		modelView.makeVisible(item);
////	}
//	public boolean isEditable(){
//		return modelView.isInEditable(item);
////		return modelView.isEditable(item);
////		return false;
//	}
//	public boolean isVisible(){
//		modelView.isInVisible(item);
////		return modelView.isVisible(item);
//		return false;
//	}
//
//	@Override
//	public String toString() {
//		return getName(item, modelView);
//	}
//
//	protected abstract String getName(T item, ModelView modelView);
//
//	public boolean hasSameItem(CheckableDisplayElement<?> other) {
//		return Objects.equals(item, other.item);
//	}
//
//	public T getItem() {
//		return item;
//	}
//}