//package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;
//
//import com.hiveworkshop.rms.editor.model.IdObject;
//import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
//
//public class CheckableNodeElement extends CheckableDisplayElement<IdObject> {
//	public CheckableNodeElement(ModelView modelView, IdObject item) {
//		super(modelView, item);
//	}
//
//	@Override
//	protected void setChecked(IdObject item, ModelView modelView, boolean checked) {
//		if (checked) {
//			modelView.makeIdObjectEditable(item);
//		} else {
//			modelView.makeIdObjectNotVisible(item);
//		}
//	}
//	@Override
//	public void setEditable(boolean editable){
//		if(editable){
//			modelView.makeIdObjectEditable(item);
//		} else {
//			modelView.makeIdObjectNotEditable(item);
//		}
//	}
//	@Override
//	public void setVisible(boolean visible){
//		if(visible){
//			modelView.makeIdObjectVisible(item);
//		} else {
//			modelView.makeIdObjectNotVisible(item);
//		}
//	}
////	@Override
////	public boolean isEditable(){
////		return modelView.isEditable(item);
////	}
////	@Override
////	public boolean isVisible(){
////		return modelView.isVisible(item);
////	}
//
//	@Override
//	protected String getName(IdObject item, ModelView modelView) {
//		return item.getClass().getSimpleName() + " \"" + item.getName() + "\"";
//	}
//
//	@Override
//	public void mouseEntered() {
//		modelView.highlightNode(item);
//	}
//
//	@Override
//	public void mouseExited() {
//		modelView.unhighlightNode(item);
//	}
//}