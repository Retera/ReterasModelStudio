package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;

public class CheckableNodeElement extends CheckableDisplayElement<IdObject> {
	public CheckableNodeElement(ModelViewManager modelViewManager, IdObject item) {
		super(modelViewManager, item);
	}

	@Override
	protected void setChecked(IdObject item, ModelViewManager modelViewManager, boolean checked) {
		if (checked) {
			modelViewManager.makeIdObjectVisible(item);
		} else {
			modelViewManager.makeIdObjectNotVisible(item);
		}
	}

	@Override
	protected String getName(IdObject item, ModelViewManager modelViewManager) {
		return item.getClass().getSimpleName() + " \"" + item.getName() + "\"";
	}

	@Override
	public void visit(SelectableComponentVisitor visitor) {
		visitor.accept(item);
	}

	@Override
	public void mouseEntered() {
		modelViewManager.highlightNode(item);
	}

	@Override
	public void mouseExited() {
		modelViewManager.unhighlightNode(item);
	}
}