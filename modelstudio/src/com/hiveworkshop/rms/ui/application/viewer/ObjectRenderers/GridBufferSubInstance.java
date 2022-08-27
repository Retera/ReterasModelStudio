package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;

public class GridBufferSubInstance extends BufferSubInstance {
	public GridBufferSubInstance(EditableModel model, TextureThing textureThing) {
		super(model, textureThing);
	}

	@Override
	protected BufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment) {
		return null;
	}
	@Override
	public void setUpInstance(ShaderPipeline pipeline){
//		pipeline.setColor(layerColor);
	}
}
