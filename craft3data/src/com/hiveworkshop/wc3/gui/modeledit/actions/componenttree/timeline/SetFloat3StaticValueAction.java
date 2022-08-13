package com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.timeline;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.Callback;

public class SetFloat3StaticValueAction implements UndoAction {
	private final String valueTypeName;
	private final Vertex oldValue;
	private final Vertex newValue;
	private final Callback<Vertex> setter;

	public SetFloat3StaticValueAction(final String valueTypeName, final Vertex oldValue, final Vertex newValue,
			final Callback<Vertex> setter) {
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
