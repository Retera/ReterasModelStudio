package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public class CheckableGeosetElement extends CheckableDisplayElement<Geoset> {
	public CheckableGeosetElement(ModelView modelView, Geoset item) {
		super(modelView, item);
	}

	@Override
	protected void setChecked(Geoset item, ModelView modelView, boolean checked) {
		if (checked) {
			modelView.makeGeosetEditable(item);
		} else {
			modelView.makeGeosetNotEditable(item);
		}
	}
	@Override
	public void setEditable(boolean editable){
		if(editable){
			modelView.makeGeosetEditable(item);
		} else {
			modelView.makeGeosetNotEditable(item);
		}
	}
	@Override
	public void setVisible(boolean visible){
		if(visible){
			modelView.makeGeosetVisible(item);
		} else {
			modelView.makeGeosetNotVisible(item);
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
	protected String getName(Geoset item, ModelView modelView) {
		if ((item.getLevelOfDetailName() != null) && (item.getLevelOfDetailName().length() > 0)) {
			return item.getLevelOfDetailName();
		}
//		return "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
//		return item.getName().substring(0, Math.max(100, item.getName().length()));
		return item.getName();
	}

	@Override
	public void mouseEntered() {
		modelView.highlightGeoset(item);
	}

	@Override
	public void mouseExited() {
		modelView.unhighlightGeoset(item);
	}

}