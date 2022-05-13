package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticle2Inst;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
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

	public void fillParticleHeap(ShaderPipeline pipeline, RenderParticleEmitter2 emitter2){
		if(renderModel != null){
			ParticleEmitter2 particleEmitter2 = emitter2.getParticleEmitter2();
			int blendSrc = particleEmitter2.getBlendSrc();
			int blendDst = particleEmitter2.getBlendDst();

			bind(particleEmitter2);
			GL11.glBlendFunc(blendSrc, blendDst);

//			System.out.println("alive p: " + emitter2.getAliveQueue().size());
			ParticleBufferSubInstance instance = new ParticleBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setParticle(particleEmitter2, 0, renderModel.getTimeEnvironment());
			pipeline.startInstance(instance);
			for(RenderParticle2Inst inst : emitter2.getAliveQueue()){
//				uvHeap.set(inst.getUv_u(0), inst.getUv_v(0));
				uvHeap.set(inst.getUv_iX(), inst.getUv_iY());
				if(inst.getVert(0) != null){
//					System.out.println("world loc: " + inst.getWorldLocation() + "loc: " + inst.getLocation());
					pipeline.addVert(inst.getWorldLocation(), Vec3.Z_AXIS, tang, uvHeap, inst.getColorV(), Vec3.ZERO, inst.getUniformScale());
//					addParticle(pipeline, inst);
				}
			}
			pipeline.endInstance();
		}
	}

	public void addParticle(ShaderPipeline pipeline, RenderParticle2Inst inst){
		for (int i = 0; i<6; i++){
			uvHeap.set(inst.getUv_u(i), inst.getUv_v(i));

			Vec3 pos = inst.getVert(i);

			pipeline.addVert(pos, Vec3.Z_AXIS, tang, uvHeap, inst.getColorV(), Vec3.ZERO, 0);
		}
	}


	public void bind(ParticleEmitter2 particleEmitter2) {
		Bitmap bitmap = particleEmitter2.getTexture();
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}
}
