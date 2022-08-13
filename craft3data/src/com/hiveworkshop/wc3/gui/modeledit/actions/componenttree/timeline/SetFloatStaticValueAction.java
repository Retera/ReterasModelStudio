package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.timeline;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.util.Callback;

public class SetFloatStaticValueAction implements UndoAction {
	private final String valueTypeName;
	private final float oldValue;
	private final float newValue;
	private final Callback<Float> setter;

	public SetFloatStaticValueAction(final String valueTypeName, final float oldValue, final float newValue,
			final Callback<Float> setter) {
		this.valueTypeName = valueTypeName;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.setter = setter;
	}

	@Override
	public void undo() {
		setter.run(oldValue);
	}

	@Override
	public void redo() {
		setter.run(newValue);
	}

	@Override
	public String actionName() {
		return "set " + valueTypeName;
	}
}
