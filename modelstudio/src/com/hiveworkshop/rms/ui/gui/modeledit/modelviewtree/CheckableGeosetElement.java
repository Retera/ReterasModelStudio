package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;

public class CheckableGeosetElement extends CheckableDisplayElement<Geoset> {
	public CheckableGeosetElement(ModelViewManager modelViewManager, Geoset item) {
		super(modelViewManager, item);
	}

	@Override
	protected void setChecked(Geoset item, ModelViewManager modelViewManager, boolean checked) {
		if (checked) {
			modelViewManager.makeGeosetEditable(item);
		} else {
			modelViewManager.makeGeosetNotEditable(item);
		}
	}

	@Override
	protected String getName(Geoset item, ModelViewManager modelViewManager) {
		if ((item.getLevelOfDetailName() != null) && (item.getLevelOfDetailName().length() > 0)) {
			return item.getLevelOfDetailName();
		}
//		return "Geoset " + (modelViewManager.getModel().getGeosetId(item) + 1);
//		return item.getName().substring(0, Math.max(100, item.getName().length()));
		return item.getName();
	}

	@Override
	public void visit(final SelectableComponentVisitor visitor) {
		visitor.accept(item);
	}

	@Override
	public void mouseEntered() {
		modelViewManager.highlightGeoset(item);
	}

	@Override
	public void mouseExited() {
		modelViewManager.unhighlightGeoset(item);
	}

}