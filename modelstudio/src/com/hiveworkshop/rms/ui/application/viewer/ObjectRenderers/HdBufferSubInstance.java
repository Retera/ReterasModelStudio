package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

public class HdBufferSubInstance extends BufferSubInstance {
	public HdBufferSubInstance(EditableModel model, TextureThing textureThing){
		super(model, textureThing);
	}

	long time;
	public void setUpInstance(ShaderPipeline pipeline){


		if(renderTextures){
			pipeline.prepareToBindTexture();

//			if(time < System.currentTimeMillis()){
//				System.out.println("binding \"" + (textures[0] == null ? "null" : textures[0].getName()) + "\" to slot " + 0);
//				time = System.currentTimeMillis() + 1000;
//			}

			for (int i = 0; i < 6; i++){
				textureThing.loadAndBindTexture(model, textures[i], i);
			}
			textureThing.bindLayer(pipeline, diffuseLayer);
		} else {
			for (int i = 0; i < 6; i++){
				textureThing.loadAndBindTexture(model, textures[i], i);
			}
		}


		if (twoSided) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		pipeline.glFresnelTeamColor1f(fresnelTeamColor);
		pipeline.glFresnelOpacity1f(fresnelOpacity);
	}

	protected HdBufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment){
		for(int i = 0; i<material.getLayers().size() && i < 6; i++){
			textures[i] = material.getLayer(i).getRenderTexture(timeEnvironment, model);
		}

		fresnelColor.set(diffuseLayer.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ZERO));
		fresnelTeamColor = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0);
		fresnelOpacity = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_OPACITY, 0.0f);

		setUVTransform(diffuseLayer, timeEnvironment);

		return this;
	}

}
