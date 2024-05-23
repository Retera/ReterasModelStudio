package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.OldRenderer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;

public class ParticleRenderer {
	private TextureThing textureThing;
	private RenderModel renderModel;

	public ParticleRenderer(){
	}
	public ParticleRenderer setModel(RenderModel renderModel, TextureThing textureThing){
		this.textureThing = textureThing;
		this.renderModel = renderModel;
		return this;
	}


	public void render(RenderParticleEmitter2 emitter) {
		renderParticles(emitter);
	}

	public void render(RenderRibbonEmitter emitter) {
		renderRibbon(emitter);
	}

	private void renderParticles(RenderParticleEmitter2 emitter) {
		ParticleEmitter2 particleEmitter2 = emitter.getParticleEmitter2();
		int[] blend = getBlend(particleEmitter2.getFilterMode());

		bind(particleEmitter2);
		GL11.glBlendFunc(blend[0], blend[1]);
		GL11.glBegin(GL11.GL_TRIANGLES);

		for (RenderParticle2Inst inst : emitter.getAliveHeadQueue()) {
			Vec4 color = inst.getColorV();
			if(inst.getVert(0) != null){
				GL11.glColor4f(color.x, color.y, color.z, color.w);
				for (int j = 0; j<6; j++){
					GL11.glTexCoord2f(inst.getUv_u(j), inst.getUv_v(j));

					Vec3 vert = inst.getVert(j);
					GL11.glVertex3f(vert.x, vert.y, vert.z);
				}
			}
		}
		for (RenderParticle2Inst inst : emitter.getAliveTailQueue()) {
			Vec4 color = inst.getColorV();
			if(inst.getVert(0) != null){
				GL11.glColor4f(color.x, color.y, color.z, color.w);
				for (int j = 0; j<6; j++){
					GL11.glTexCoord2f(inst.getUv_u(j), inst.getUv_v(j));

					Vec3 vert = inst.getVert(j);
					GL11.glVertex3f(vert.x, vert.y, vert.z);
				}
			}
		}
		GL11.glEnd();
	}
	private void renderParticles1(RenderParticleEmitter2 emitter) {
		ParticleEmitter2 particleEmitter2 = emitter.getParticleEmitter2();
		int[] blend = getBlend(particleEmitter2.getFilterMode());

		bind(particleEmitter2);
		GL11.glBlendFunc(blend[0], blend[1]);
		GL11.glBegin(GL11.GL_TRIANGLES);

		for (RenderParticle2Inst inst : emitter.getAliveQueue()) {
			Vec4 color = inst.getColorV();
			if(inst.getVert(0) != null){
				GL11.glColor4f(color.x, color.y, color.z, color.w);
				for (int j = 0; j<6; j++){
					GL11.glTexCoord2f(inst.getUv_u(j), inst.getUv_v(j));

					Vec3 vert = inst.getVert(j);
					GL11.glVertex3f(vert.x, vert.y, vert.z);
				}
			}
		}
		GL11.glEnd();
	}
	private void renderRibbon(RenderRibbonEmitter emitter) {
		RibbonEmitter particleEmitter2 = emitter.getRibbon();
//		int[] blend = getBlend(MdlxParticleEmitter2.FilterMode.BLEND);

//		bind(particleEmitter2);
//		GL11.glBlendFunc(blend[0], blend[1]);
//		GL11.glColor4f(255, 0,0, 255);
//		GL11.glDisable(GL11.GL_BLEND);
//		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

//		GL11.glDisable(GL11.GL_BLEND);
//		GL11.glBegin(GL_TRIANGLES);

		GL11.glColor4f(255, 255,255, 255);
		for (Layer layer : particleEmitter2.getMaterial().getLayers()) {
			bind(layer);

			Vec4 lastColor = null;
			Vec3 lastAbove = null;
			Vec2 lastUvAbove = null;
			Vec3 lastBelow = null;
			Vec2 lastUvBelow = null;
			GL11.glBegin(GL_TRIANGLES);
			for (RenderParticleRibbonInst inst : emitter.getAliveQueue()) {
				Vec4 color = inst.getColorV();
//				GL11.glColor4f(color.x, color.y, color.z, 255);
				GL11.glColor4f(color.x, color.y, color.z, color.w);
//			System.out.println("color: " + color);
//			GL11.glColor4f(color.x, color.y, color.z, 255);
//			Vec4 color = inst.getColorV();
				Vec3 above = inst.getWorldAbove();
				Vec2 uvAbove = inst.getUvAbove();
				Vec3 below = inst.getWorldBelow();
				Vec2 uvBelow = inst.getUvBelow();

				if (lastAbove != null && 0 < inst.health) {
//					GL11.glColor4f(lastColor.x, lastColor.y, lastColor.z, lastColor.w);
					GL11.glTexCoord2f(lastUvAbove.x, lastUvAbove.y);
					GL11.glVertex3f(lastAbove.x, lastAbove.y, lastAbove.z);
					GL11.glTexCoord2f(lastUvBelow.x, lastUvBelow.y);
					GL11.glVertex3f(lastBelow.x, lastBelow.y, lastBelow.z);
//					GL11.glColor4f(color.x, color.y, color.z, color.w);
					GL11.glTexCoord2f(uvAbove.x, uvAbove.y);
					GL11.glVertex3f(above.x, above.y, above.z);

//					GL11.glColor4f(lastColor.x, lastColor.y, lastColor.z, lastColor.w);
					GL11.glTexCoord2f(lastUvBelow.x, lastUvBelow.y);
					GL11.glVertex3f(lastBelow.x, lastBelow.y, lastBelow.z);
//					GL11.glColor4f(color.x, color.y, color.z, color.w);
					GL11.glTexCoord2f(uvBelow.x, uvBelow.y);
					GL11.glVertex3f(below.x, below.y, below.z);
					GL11.glTexCoord2f(uvAbove.x, uvAbove.y);
					GL11.glVertex3f(above.x, above.y, above.z);

//				    Vec2 uvAbove = inst.getUvAbove();
//				    GL11.glTexCoord2f(uvAbove.x, uvAbove.y);
//				    Vec3 below = inst.getWorldBelow();
//				    GL11.glVertex3f(below.x, below.y, below.z);
//				    Vec2 uvBelow = inst.getUvBelow();
//				    pipeline.addVert(lastAbove, Vec3.X_AXIS, tang, lastUvAbove, lastColor, fresnelColorHeap, 2);
//				    pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
//				    pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);
//
//				    pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
//				    pipeline.addVert(below, Vec3.X_AXIS, tang, uvBelow, color, fresnelColorHeap, 2);
//				    pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);
				}
				lastColor = color;
				lastAbove = above;
				lastUvAbove = uvAbove;
				lastBelow = below;
				lastUvBelow = uvBelow;
			}
			GL11.glEnd();
		}

//		RibbonEmitter particleEmitter2 = emitter.getRibbon();
//		int[] blend = getBlend(MdlxParticleEmitter2.FilterMode.BLEND);
//
//		bind(particleEmitter2);
//		GL11.glBlendFunc(blend[0], blend[1]);
//		GL11.glColor4f(255, 0,0, 255);
//		GL11.glDisable(GL11.GL_BLEND);
//		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
//
//		for (RenderParticleRibbonInst inst : emitter.getAliveQueue()) {
//			Vec4 color = inst.getColorV();
//			GL11.glColor4f(color.x, color.y, color.z, color.w);
//			Vec3 above = inst.getWorldAbove();
//			GL11.glVertex3f(above.x, above.y, above.z);
//			Vec2 uvAbove = inst.getUvAbove();
//			GL11.glTexCoord2f(uvAbove.x, uvAbove.y);
//			Vec3 below = inst.getWorldBelow();
//			GL11.glVertex3f(below.x, below.y, below.z);
//			Vec2 uvBelow = inst.getUvBelow();
//			GL11.glTexCoord2f(uvBelow.x, uvBelow.y);
//
//		}
//		GL11.glEnd();
	}
	private void renderRibbon1(RenderRibbonEmitter emitter) {
		RibbonEmitter particleEmitter2 = emitter.getRibbon();
		int[] blend = getBlend(MdlxParticleEmitter2.FilterMode.BLEND);

		bind(particleEmitter2);
		GL11.glBlendFunc(blend[0], blend[1]);
		GL11.glColor4f(255, 0,0, 255);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

		for (RenderParticleRibbonInst inst : emitter.getAliveQueue()) {
			Vec4 color = inst.getColorV();
			GL11.glColor4f(color.x, color.y, color.z, color.w);
			Vec3 above = inst.getWorldAbove();
			GL11.glVertex3f(above.x, above.y, above.z);
			Vec2 uvAbove = inst.getUvAbove();
			GL11.glTexCoord2f(uvAbove.x, uvAbove.y);
			Vec3 below = inst.getWorldBelow();
			GL11.glVertex3f(below.x, below.y, below.z);
			Vec2 uvBelow = inst.getUvBelow();
			GL11.glTexCoord2f(uvBelow.x, uvBelow.y);

		}
		GL11.glEnd();
	}

	public int getBlendSrc(MdlxParticleEmitter2.FilterMode filterMode) {
		return switch (filterMode) {
			case BLEND -> GL11.GL_SRC_ALPHA;
			case ADDITIVE -> GL11.GL_SRC_ALPHA;
			case ALPHAKEY -> GL11.GL_SRC_ALPHA;
			case MODULATE -> GL11.GL_ZERO;
			case MODULATE2X -> GL11.GL_DST_COLOR;
		};
	}
	public int[] getBlend(MdlxParticleEmitter2.FilterMode filterMode) {
		return switch (filterMode) {
			case BLEND -> new int[]{
				GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA
			};
			case ADDITIVE, ALPHAKEY -> new int[]{
				GL11.GL_SRC_ALPHA, GL11.GL_ONE
			};
			case MODULATE -> new int[]{
				GL11.GL_ZERO, GL11.GL_SRC_COLOR
			};
			case MODULATE2X -> new int[]{
				GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR
			};
		};
	}

	public int getBlendDst(MdlxParticleEmitter2.FilterMode filterMode) {
		return switch (filterMode) {
			case BLEND -> GL11.GL_ONE_MINUS_SRC_ALPHA;
			case ADDITIVE -> GL11.GL_ONE;
			case ALPHAKEY -> GL11.GL_ONE;
			case MODULATE -> GL11.GL_SRC_COLOR;
			case MODULATE2X -> GL11.GL_SRC_COLOR;
		};
	}

	private void drawTestTriangle() {
		glBegin(GL_TRIANGLES);
		GL11.glNormal3f(0, 0, 0);

		GL11.glColor4f(1, 0,0,1);
		GL11.glVertex3f(0.0f, 0.0f, 2.0f);
		GL11.glVertex3f(2.0f, 2.0f, 0.0f);
		GL11.glVertex3f(-2.0f, -2.0f, 0.0f);
		GL11.glColor4f(0, 1,0,1);
		GL11.glVertex3f(0.0f, 0.0f, 2.0f);
		GL11.glVertex3f(-2.0f, -2.0f, 0.0f);
		GL11.glVertex3f(2.0f, 2.0f, 0.0f);
		GL11.glEnd();
	}


	public void bind(ParticleEmitter2 particleEmitter2) {
		Bitmap bitmap = particleEmitter2.getTexture();
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}

	public void bind(RibbonEmitter particleEmitter2) {
		Material material = particleEmitter2.getMaterial();
		Layer layer = material.getLayer(0);
		Bitmap bitmap = layer.getTexture(0);
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
//		textureThing.bindLayerTexture(layer, layer.getTexture(0), 800, material);
	}

	public void bind(Layer layer) {
		Bitmap bitmap = layer.getRenderTexture(renderModel.getTimeEnvironment());
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		bindLayer(layer);
//		textureThing.bindLayerTexture(layer, layer.getTexture(0), 800, material);
	}


	public void bindLayer(Layer layer) {
		boolean depthMask = false;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01f);
		switch (layer.getFilterMode()) {
			case BLEND -> {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			case ADDITIVE, ADDALPHA -> {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			case MODULATE -> {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			}
			case MODULATE2X -> {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			}
			case NONE -> {
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
			case TRANSPARENT -> {
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.75f);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
		}

		if (layer.getNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		if (layer.getNoDepthSet()) {
			GL11.glDepthMask(false);
		} else {
			GL11.glDepthMask(depthMask);
		}
	}
}
