package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticleRibbonInst;
import com.hiveworkshop.rms.editor.render3d.RenderRibbonEmitter;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.HdBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public class RibbonBufferFiller {
	private TextureThing textureThing;
	private RenderModel renderModel;
	private final Vec2 uvHeap = new Vec2();
	private final Vec4 tang = new Vec4();

	public RibbonBufferFiller(){
	}

	public RibbonBufferFiller setModel(TextureThing textureThing, RenderModel renderModel){
		this.textureThing = textureThing;
		this.renderModel = renderModel;
		return this;
	}

	public void fillBuffer(ShaderPipeline pipeline){
		if (renderModel != null) {
			pipeline.prepare();
			for (RenderRibbonEmitter emitter2 : renderModel.getRenderParticleEmitters2Rib()) {
				fillParticleHeap(pipeline, emitter2);
			}
		}
	}

	private final Vec3 fresnelColorHeap = new Vec3();
	public void fillParticleHeap(ShaderPipeline pipeline, RenderRibbonEmitter emitter2){
		if(renderModel != null){
			RibbonEmitter particleEmitter2 = emitter2.getRibbon();

			HdBufferSubInstance instance = new HdBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setMaterial(particleEmitter2.getMaterial(), 0, renderModel.getTimeEnvironment());
			instance.setRenderTextures(true);
//			instance.setLayerColor(new Vec4(1,1,0,.5));
			instance.setLayerColor(emitter2.getColorHeap());
			pipeline.startInstance(instance);
			tang.set(0,0,1,1);
			Vec4 lastColor = null;
			Vec3 lastAbove = null;
			Vec2 lastUvAbove = null;
			Vec3 lastBelow = null;
			Vec2 lastUvBelow = null;

			for (RenderParticleRibbonInst inst : emitter2.getAliveQueue()) {
				Vec4 color = inst.getColorV();
				Vec3 above = inst.getWorldAbove();
				Vec2 uvAbove = inst.getUvAbove();
				Vec3 below = inst.getWorldBelow();
				Vec2 uvBelow = inst.getUvBelow();

				if (lastAbove != null && 0 < inst.health) {
					pipeline.addVert(lastAbove, Vec3.X_AXIS, tang, lastUvAbove, lastColor, fresnelColorHeap, 2);
					pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
					pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);

					pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
					pipeline.addVert(below, Vec3.X_AXIS, tang, uvBelow, color, fresnelColorHeap, 2);
					pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);
				}
				lastColor = color;
				lastAbove = above;
				lastUvAbove = uvAbove;
				lastBelow = below;
				lastUvBelow = uvBelow;
			}

			pipeline.endInstance();
		}
	}
	public void fillParticleHeap_TRI_STRIP(ShaderPipeline pipeline, RenderRibbonEmitter emitter2){
		if (renderModel != null) {
			RibbonEmitter particleEmitter2 = emitter2.getRibbon();

			HdBufferSubInstance instance = new HdBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setMaterial(particleEmitter2.getMaterial(), 0, renderModel.getTimeEnvironment());
			pipeline.startInstance(instance);
			tang.set(0,0,1,1);

			Vec3 lastAbove = null;
			Vec2 lastUvAbove = null;
			Vec3 lastBelow = null;
			Vec2 lastUvBelow = null;

			for (RenderParticleRibbonInst inst : emitter2.getAliveQueue()) {
				Vec4 color = inst.getColorV();
				Vec3 above = inst.getWorldAbove();
				Vec2 uvAbove = inst.getUvAbove();
				Vec3 below = inst.getWorldBelow();
				Vec2 uvBelow = inst.getUvBelow();

				pipeline.addVert(above, Vec3.Z_AXIS, tang, uvAbove, color, fresnelColorHeap, 0);
				pipeline.addVert(below, Vec3.Z_AXIS, tang, uvBelow, color, fresnelColorHeap, 0);
			}
			pipeline.endInstance();
		}
	}

	public void bind(ParticleEmitter2 particleEmitter2) {
		Bitmap bitmap = particleEmitter2.getTexture();
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}
}
