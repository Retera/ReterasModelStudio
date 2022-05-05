package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import org.lwjgl.opengl.GL11;

public class HdBufferSubInstance {
	private final Bitmap[] textures = new Bitmap[6];
	private Vec4[] layerColor = new Vec4[6];
	private int offset = 0;
	private int vertCount = 0;
	private float fresnelTeamColor = 0;
	private float fresnelOpacity = 0;
	private Vec3 fresnelColor = new Vec3();
	private Mat4 uvTransform = new Mat4();
	private boolean twoSided = false;
	private boolean renderTextures = false;
	private final TextureThing textureThing;
	private final EditableModel model;
	private ShaderPipeline pipeline;
	private Layer diffuseLayer;
	private Material material;

	public HdBufferSubInstance(EditableModel model, TextureThing textureThing){
		this.model = model;
		this.textureThing = textureThing;
	}

	public Vec4[] getLayerColor() {
		return layerColor;
	}

	public HdBufferSubInstance setLayerColor(Vec4[] layerColor) {
		this.layerColor = layerColor;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public HdBufferSubInstance setOffset(int offset) {
		this.offset = offset;
		return this;
	}
	public HdBufferSubInstance setEnd(int endVertexCount) {
		vertCount = endVertexCount - offset;
		return this;
	}

	public int getVertCount() {
		return vertCount;
	}

	public float getFresnelTeamColor() {
		return fresnelTeamColor;
	}

	public float getFresnelOpacity() {
		return fresnelOpacity;
	}

	public Vec3 getFresnelColor() {
		return fresnelColor;
	}


	public boolean isRenderTextures() {
		return renderTextures;
	}

	public HdBufferSubInstance setRenderTextures(boolean renderTextures) {
		this.renderTextures = renderTextures;
		return this;
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

	public ShaderPipeline getPipeline() {
		return pipeline;
	}

	public HdBufferSubInstance setPipeline(ShaderPipeline pipeline) {
		this.pipeline = pipeline;
		return this;
	}

	public Mat4 getUvTransform() {
		return uvTransform;
	}

	public Material getMaterial() {
		return material;
	}

	public HdBufferSubInstance setMaterial(Material material) {
		this.material = material;
		this.diffuseLayer = material.getLayer(0);
		twoSided = diffuseLayer.getTwoSided() || (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.getTwoSided());

		return this;
	}

	public HdBufferSubInstance setMaterial(Material material, TimeEnvironmentImpl timeEnvironment) {
		this.material = material;
		this.diffuseLayer = material.getLayer(0);
		twoSided = diffuseLayer.getTwoSided() || (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.getTwoSided());

		fetchTextures(timeEnvironment);

		return this;
	}

	public HdBufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment){
		for(int i = 0; i<material.getLayers().size() && i < 6; i++){
			textures[i] = material.getLayer(i).getRenderTexture(timeEnvironment, model);
		}

		fresnelColor.set(diffuseLayer.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_FRESNEL_COLOR, Vec3.ZERO));
		fresnelTeamColor = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, 0);
		fresnelOpacity = diffuseLayer.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_FRESNEL_OPACITY, 0.0f);

		setUVTransform(diffuseLayer, timeEnvironment);

		return this;
	}



	private void setUVTransform(Layer layer, TimeEnvironmentImpl timeEnvironment) {
		uvTransform.setIdentity();
		if(layer.getTextureAnim() != null){

			uvTransform.fromRotationTranslationScale(
					layer.getTextureAnim().getInterpolatedQuat(timeEnvironment, MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_SCALING, Vec3.ONE)
			);
		}
//		return uvTransform;
//		return null;
	}



	public boolean isTwoSided() {
		return twoSided;
	}


}
