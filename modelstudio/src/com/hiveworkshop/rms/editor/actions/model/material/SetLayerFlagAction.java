package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
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
			case MdlUtils.TOKEN_UNSHADED -> layer.setUnshaded(!layer.getUnshaded());
			case MdlUtils.TOKEN_SPHERE_ENV_MAP -> layer.setSphereEnvMap(!layer.getSphereEnvMap());
			case MdlUtils.TOKEN_TWO_SIDED -> layer.setTwoSided(!layer.getTwoSided());
			case MdlUtils.TOKEN_UNFOGGED -> layer.setUnfogged(!layer.getUnfogged());
			case MdlUtils.TOKEN_NO_DEPTH_TEST -> layer.setNoDepthTest(!layer.getNoDepthTest());
			case MdlUtils.TOKEN_NO_DEPTH_SET -> layer.setNoDepthSet(!layer.getNoDepthSet());
			case MdlUtils.TOKEN_UNLIT -> layer.setUnlit(!layer.getUnlit());
		}
	}

	@Override
	public String actionName() {
		return "Toggled Layer " + flag;
	}
}
