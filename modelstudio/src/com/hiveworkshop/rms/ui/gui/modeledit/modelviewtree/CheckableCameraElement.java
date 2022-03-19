package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public class CheckableCameraElement extends CheckableDisplayElement<Camera> {
	public CheckableCameraElement(ModelView modelView, Camera item) {
		super(modelView, item);
	}

	@Override
	protected void setChecked(Camera item, ModelView modelView, boolean checked) {
		if (checked) {
			modelView.makeCameraEditable(item);
		} else {
			modelView.makeCameraNotVisible(item);
		}
	}
	@Override
	public void setEditable(boolean editable){
		if(editable){
			modelView.makeCameraEditable(item);
		} else {
			modelView.makeCameraNotEditable(item);
		}
	}
	@Override
	public void setVisible(boolean visible){
		if(visible){
			modelView.makeCameraVisible(item);
		} else {
			modelView.makeCameraNotVisible(item);
		}
	}
//	@Override
//	public boolean isEditable(){
//		return modelView.isEditable(item);
//	}
//	@Override
//	public boolean isVisible(){
//		return modelView.isVisible(item);
//	}

	@Override
	protected String getName(Camera item, ModelView modelView) {
		return item.getName();
	}
}