package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticle2Inst;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ParticleBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

public class ParticleBufferFiller {
	private TextureThing textureThing;
	private RenderModel renderModel;
	private final Vec2 uvHeap = new Vec2();
	private final Vec4 tang = new Vec4();

	public ParticleBufferFiller(){
	}

	public ParticleBufferFiller setModel(TextureThing textureThing, RenderModel renderModel){
		this.textureThing = textureThing;
		this.renderModel = renderModel;
		return this;
	}

	public void fillBuffer(ShaderPipeline pipeline){
		if(renderModel != null){
			pipeline.prepare();
			for(RenderParticleEmitter2 emitter2 : renderModel.getRenderParticleEmitters2()) {
				fillParticleHeap(pipeline, emitter2);
			}
		}
	}

	public void fillParticleHeap(ShaderPipeline pipeline, RenderParticleEmitter2 emitter2){
		if(renderModel != null){
			ParticleEmitter2 particleEmitter2 = emitter2.getParticleEmitter2();
			int[] blend = getBlend(particleEmitter2.getFilterMode());

			bind(particleEmitter2);
			GL11.glBlendFunc(blend[0], blend[1]);

			ParticleBufferSubInstance instance = new ParticleBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setParticle(particleEmitter2, 0, renderModel.getTimeEnvironment());
			instance.setRenderTextures(true);
			pipeline.startInstance(instance);
			tang.set(0,0,0,0);
			for(RenderParticle2Inst inst : emitter2.getAliveHeadQueue()){
				uvHeap.set(inst.getUv_iX(), inst.getUv_iY());
				if(inst.getVert(0) != null){
					pipeline.addVert(inst.getWorldLocation(), inst.getTailLocation(), tang, uvHeap, inst.getColorV(), Vec3.ZERO, inst.getUniformScale());
				}
			}
			if(emitter2.getParticleEmitter2().isTail()){
				tang.w = 1;
				for(RenderParticle2Inst inst : emitter2.getAliveTailQueue()){
					uvHeap.set(inst.getUv_iX(), inst.getUv_iY());
					if(inst.getVert(0) != null){
						tang.set(inst.getTailLocation());
						pipeline.addVert(inst.getWorldLocation(), inst.getTailLocation(), tang, uvHeap, inst.getColorV(), Vec3.ZERO, inst.getUniformScale());
					}
				}
			}
			pipeline.endInstance();
		}
	}
	public void fillParticleHeap1(ShaderPipeline pipeline, RenderParticleEmitter2 emitter2){
		// I'm not sure if separating RenderParticle2Inst into RenderParticle2HeadInst and
		// RenderParticle2TailInst is a good idea or not. Leaving this here to make it easy to revert.
		if(renderModel != null){
			ParticleEmitter2 particleEmitter2 = emitter2.getParticleEmitter2();
			int[] blend = getBlend(particleEmitter2.getFilterMode());

			bind(particleEmitter2);
			GL11.glBlendFunc(blend[0], blend[1]);

			ParticleBufferSubInstance instance = new ParticleBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setParticle(particleEmitter2, 0, renderModel.getTimeEnvironment());
			pipeline.startInstance(instance);
			tang.set(0,0,0,0);
			for(RenderParticle2Inst inst : emitter2.getAliveQueue()){
				uvHeap.set(inst.getUv_iX(), inst.getUv_iY());
				if(inst.getVert(0) != null){
					tang.set(inst.getTailLocationV4());
					pipeline.addVert(inst.getWorldLocation(), inst.getTailLocation(), tang, uvHeap, inst.getColorV(), Vec3.ZERO, inst.getUniformScale());
				}
			}
			pipeline.endInstance();
		}
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


	public void bind(ParticleEmitter2 particleEmitter2) {
		Bitmap bitmap = particleEmitter2.getTexture();
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}
}
