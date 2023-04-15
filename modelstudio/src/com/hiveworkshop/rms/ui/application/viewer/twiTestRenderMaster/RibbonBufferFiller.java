package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticle2Inst;
import com.hiveworkshop.rms.editor.render3d.RenderParticleRibbonInst;
import com.hiveworkshop.rms.editor.render3d.RenderRibbonEmitter;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.HdBufferSubInstance;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

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

	private final Vec3 fresnelColorHeap = new Vec3();
	public void fillParticleHeap(ShaderPipeline pipeline, RenderRibbonEmitter emitter2){
		if(renderModel != null){
//			System.out.println("Ribbon!");
			RibbonEmitter particleEmitter2 = emitter2.getRibbon();
//			System.out.println("alive p: " + emitter2.getAliveQueue().size());

			HdBufferSubInstance instance = new HdBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setMaterial(particleEmitter2.getMaterial(), 0, renderModel.getTimeEnvironment());
//			instance.setLayerColor(new Vec4(1,1,0,.5));
			instance.setLayerColor(new Vec4(1,1,1,1));
			pipeline.startInstance(instance);
			tang.set(0,0,1,1);
			Vec4 lastColor = null;
			Vec3 lastAbove = null;
			Vec2 lastUvAbove = null;
			Vec3 lastBelow = null;
			Vec2 lastUvBelow = null;

			for(RenderParticleRibbonInst inst : emitter2.getAliveQueue()){
				Vec4 color = inst.getColorV();
				Vec3 above = inst.getWorldAbove();
				Vec2 uvAbove = inst.getUvAbove();
				Vec3 below = inst.getWorldBelow();
				Vec2 uvBelow = inst.getUvBelow();

				if(lastAbove != null){
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




//			Vec4 lastColor = new Vec4(1,0,0,1);
//			Vec3 lastAbove = new  Vec3(50,-100, 100);
//			Vec2 lastUvAbove = new  Vec2();
//			Vec3 lastBelow = new  Vec3(50,-100, 1);
//			Vec2 lastUvBelow = new  Vec2(0,1);
//			Vec4 color = new Vec4(1,0,0,1);
//			Vec3 above = new Vec3(-50,100, 100);
//			Vec2 uvAbove = new Vec2(1,0);
//			Vec3 below = new Vec3(-50,100, 1);
//			Vec2 uvBelow = new Vec2(1,1);
//			pipeline.addVert(lastAbove, Vec3.X_AXIS, tang, lastUvAbove, lastColor, fresnelColorHeap, 2);
//			pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
//			pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);
//
//			pipeline.addVert(lastBelow, Vec3.X_AXIS, tang, lastUvBelow, lastColor, fresnelColorHeap, 2);
//			pipeline.addVert(below, Vec3.X_AXIS, tang, uvBelow, color, fresnelColorHeap, 2);
//			pipeline.addVert(above, Vec3.X_AXIS, tang, uvAbove, color, fresnelColorHeap, 2);

			pipeline.endInstance();
		}
	}
	public void fillParticleHeap_TRI_STRIP(ShaderPipeline pipeline, RenderRibbonEmitter emitter2){
		if(renderModel != null){
			RibbonEmitter particleEmitter2 = emitter2.getRibbon();
//			System.out.println("alive p: " + emitter2.getAliveQueue().size());

			HdBufferSubInstance instance = new HdBufferSubInstance(renderModel.getModel(), textureThing);
			instance.setMaterial(particleEmitter2.getMaterial(), 0, renderModel.getTimeEnvironment());
			pipeline.startInstance(instance);
			tang.set(0,0,1,1);

			Vec3 lastAbove = null;
			Vec2 lastUvAbove = null;
			Vec3 lastBelow = null;
			Vec2 lastUvBelow = null;

			for(RenderParticleRibbonInst inst : emitter2.getAliveQueue()){
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
