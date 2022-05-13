package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;

public class ParticleBufferSubInstance extends BufferSubInstance {
	public ParticleBufferSubInstance(EditableModel model, TextureThing textureThing){
		super(model, textureThing);
	}

	long time;
	public void setUpInstance(ShaderPipeline pipeline){


		if(renderTextures){
			pipeline.prepareToBindTexture();

//			if(time < System.currentTimeMillis()){
//				System.out.println("binding \"" + (texture == null ? "null" : texture.getName()) + "\" to slot " + textureSlot);
//				time = System.currentTimeMillis() + 2000;
//			}

			textureThing.loadAndBindTexture(model, texture, textureSlot);
			textureThing.bindParticleTexture(particleEmitter2, texture);
		}


//		if (twoSided) {
//			GL11.glDisable(GL11.GL_CULL_FACE);
//		} else {
//			GL11.glEnable(GL11.GL_CULL_FACE);
//		}
//		pipeline.glFresnelTeamColor1f(fresnelTeamColor);
	}

	protected ParticleBufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment){
////		for(int i = 0; i<material.getLayers().size() && i < 6; i++){
////			textures[i] = material.getLayer(i).getRenderTexture(timeEnvironment, model);
////		}
//		texture = diffuseLayer.getRenderTexture(timeEnvironment, model);
//
//		float fresnelOpacity = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_OPACITY, 0.0f);
//		fresnelColor.set(diffuseLayer.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ZERO), fresnelOpacity);
//		fresnelTeamColor = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0);
//
//		setUVTransform(diffuseLayer, timeEnvironment);

		return this;
	}
}
