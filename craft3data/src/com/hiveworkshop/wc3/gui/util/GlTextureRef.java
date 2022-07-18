package com.hiveworkshop.wc3.gui.util;

public final class GlTextureRef {
	public final Integer textureId;
	public final int pipelineId;

	public GlTextureRef(final Integer id, final int pipelineId) {
		this.textureId = id;
		this.pipelineId = pipelineId;
	}
}