package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.opengl.GL11;

import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.pkb.PkbEffectSummary;

/**
 * Preview of a Reforged popcorn emitter: one billboard particle system per
 * billboard renderer of the baked effect, drawn with that renderer's texture
 * and blend mode. The motion is an approximation (the real behaviour is
 * compiled PopcornFX bytecode; see roadmap W12) but the emitter's position,
 * textures, blending and the MDX multipliers are honoured.
 */
public class RenderPopcornEmitter extends RenderSharedGeometryEmitter<ParticleEmitterPopcorn, RenderPopcornEmitterView> {
	private final PkbEffectSummary.Renderer renderer;
	private final int rendererIndex;
	private final int rendererCount;

	public RenderPopcornEmitter(final ParticleEmitterPopcorn emitter, final PkbEffectSummary.Renderer renderer,
			final int rendererIndex, final int rendererCount, final InternalResource textureResource) {
		super(emitter, 30, textureResource);
		this.renderer = renderer;
		this.rendererIndex = rendererIndex;
		this.rendererCount = rendererCount;
	}

	public PkbEffectSummary.Renderer getRenderer() {
		return renderer;
	}

	/** Index of this renderer among the effect's billboard renderers; varies the look per layer. */
	public int getRendererIndex() {
		return rendererIndex;
	}

	public int getRendererCount() {
		return rendererCount;
	}

	public ParticleEmitterPopcorn getEmitter() {
		return modelObject;
	}

	@Override
	protected void emit(final RenderPopcornEmitterView emitterView) {
		emitObject(emitterView, true);
	}

	@Override
	protected EmittedObject<RenderPopcornEmitterView> createObject() {
		return new RenderPopcornParticle(this);
	}

	public int getBlendSrc() {
		switch (renderer.blendMode) {
		case ADDITIVE_NO_ALPHA:
		case PREMULTIPLIED:
			return GL11.GL_ONE;
		default:
			return GL11.GL_SRC_ALPHA;
		}
	}

	public int getBlendDst() {
		switch (renderer.blendMode) {
		case ADDITIVE:
		case ADDITIVE_NO_ALPHA:
			return GL11.GL_ONE;
		default:
			return GL11.GL_ONE_MINUS_SRC_ALPHA;
		}
	}

	@Override
	public void render(final RenderModel modelView, final ParticleEmitterShader shader) {
		if ((internalResource != null) && (alive > 0)) {
			shader.renderParticles(getBlendSrc(), getBlendDst(), Math.max(1, renderer.atlasSubDivY),
					Math.max(1, renderer.atlasSubDivX), internalResource, data, false, alive * 6);
		}
	}
}
