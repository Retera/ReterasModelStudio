package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;

public class CheckableCameraElement extends CheckableDisplayElement<Camera> {
	public CheckableCameraElement(ModelView modelViewManager, Camera item) {
		super(modelViewManager, item);
	}

	@Override
	protected void setChecked(Camera item, ModelView modelViewManager, boolean checked) {
		if (checked) {
			modelViewManager.makeCameraVisible(item);
		} else {
			modelViewManager.makeCameraNotVisible(item);
		}
	}

	@Override
	protected String getName(Camera item, ModelView modelViewManager) {
		return item.getName();
	}

	@Override
	public void visit(SelectableComponentVisitor visitor) {
		visitor.accept(item);
	}
}