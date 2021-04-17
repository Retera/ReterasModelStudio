package com.hiveworkshop.rms.editor.render3d;

public interface ParticleEmitterShader {

	void renderParticles(int blendSrc, int blendDst, int rows, int cols,
	                     InternalResource texture,
	                     RenderSharedGeometryEmitter.RenderData[] bufferData,
	                     boolean isRibbonEmitter, int numItemsToRender);

	void use();
}
