package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetLayerFlagAction implements UndoAction {
	private final Layer layer;
	private final String flag;
	private final ModelStructureChangeListener changeListener;

	public SetLayerFlagAction(Layer layer, String flag, ModelStructureChangeListener changeListener) {
		this.layer = layer;
		this.flag = flag;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		toggleFlag();
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		toggleFlag();
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	private void toggleFlag() {
		switch (flag) {
			case "Unshaded" -> layer.setUnshaded(!layer.getUnshaded());
			case "SphereEnvMap" -> layer.setSphereEnvMap(!layer.getSphereEnvMap());
			case "TwoSided" -> layer.setTwoSided(!layer.getTwoSided());
			case "Unfogged" -> layer.setUnfogged(!layer.getUnfogged());
			case "NoDepthTest" -> layer.setNoDepthTest(!layer.getNoDepthTest());
			case "NoDepthSet" -> layer.setNoDepthSet(!layer.getNoDepthSet());
			case "Unlit" -> layer.setUnlit(!layer.getUnlit());
		}
	}

	@Override
	public String actionName() {
		return "Toggled Layer " + flag;
	}
}
