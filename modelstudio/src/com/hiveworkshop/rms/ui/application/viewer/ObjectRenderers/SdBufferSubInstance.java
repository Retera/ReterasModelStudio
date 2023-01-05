package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.parsers.blp.RMS_PHT;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class SdBufferSubInstance extends BufferSubInstance {
	public SdBufferSubInstance(EditableModel model, TextureThing textureThing){
		super(model, textureThing);
	}

	long time;
	public void setUpInstance(ShaderPipeline pipeline){


		pipeline.prepareToBindTexture();
		for (int i = 0; i < 6; i++){
			textureThing.loadAndBindTexture(model, textures[i], i);
		}
		if(renderTextures){

//			if(time < System.currentTimeMillis()){
//				System.out.println("binding \"" + (texture == null ? "null" : texture.getName()) + "\" to slot " + textureSlot);
//				time = System.currentTimeMillis() + 2000;
//			}
			textureThing.bindLayer(pipeline, diffuseLayer);
		}


		if (twoSided) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		pipeline.glFresnelTeamColor1f(fresnelTeamColor);
	}

	protected SdBufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment){
		List<Layer.Texture> textureSlots = diffuseLayer.getTextureSlots();
		for (int i = 0; i < 6; i++) {
			Layer.Texture texture1 = null;
			if(i < textureSlots.size()){
				texture1 = textureSlots.get(i);
			}
			if(texture1 != null){
				textures[i] = texture1.getFlipbookTexture(timeEnvironment);
			} else {
				textures[i] = RMS_PHT.values()[i].getBitmap();
			}
		}

		float fresnelOpacity = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_OPACITY, 0.0f);
		fresnelColor.set(diffuseLayer.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ZERO), fresnelOpacity);
		fresnelTeamColor = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0);

		setUVTransform(diffuseLayer, timeEnvironment);

		return this;
	}
}
