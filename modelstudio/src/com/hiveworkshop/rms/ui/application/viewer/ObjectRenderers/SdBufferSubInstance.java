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

public class SdBufferSubInstance {
//	private ArrayList<Bitmap> textures = new ArrayList<>();
//	private ArrayList<Boolean> twoSided = new ArrayList<>();
	private Bitmap texture;
	private Boolean twoSided = false;
	int textureSlot = 0;
	private Vec4[] layerColor = new Vec4[6];
	private int offset = 0;
	private int vertCount = 0;
	private float fresnelTeamColor = 0;
	private float fresnelOpacity = 0;
	private Vec3 fresnelColor = new Vec3();
	private Mat4 uvTransform = new Mat4();
	private boolean renderTextures = false;
	private final TextureThing textureThing;
	private final EditableModel model;
	private ShaderPipeline pipeline;
	private Layer diffuseLayer;
	private Material material;

	public SdBufferSubInstance(EditableModel model, TextureThing textureThing){
		this.model = model;
		this.textureThing = textureThing;
	}

	public Vec4[] getLayerColor() {
		return layerColor;
	}

	public SdBufferSubInstance setLayerColor(Vec4[] layerColor) {
		this.layerColor = layerColor;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public SdBufferSubInstance setOffset(int offset) {
		this.offset = offset;
		return this;
	}
	public SdBufferSubInstance setEnd(int endVertexCount) {
		vertCount = endVertexCount - offset;
		return this;
	}

	public int getVertCount() {
		return vertCount;
	}
	public SdBufferSubInstance setVertCount(int vertCount) {
		this.vertCount = vertCount;
		return this;
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

	public SdBufferSubInstance setRenderTextures(boolean renderTextures) {
		this.renderTextures = renderTextures;
		return this;
	}

	public SdBufferSubInstance setTexture(Bitmap texture, int i) {
		this.texture = texture;
		return this;
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
			textureThing.bindLayer(pipeline, diffuseLayer);
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

	public SdBufferSubInstance setPipeline(ShaderPipeline pipeline) {
		this.pipeline = pipeline;
		return this;
	}

	public Mat4 getUvTransform() {
		return uvTransform;
	}

	public int getTextureSlot() {
		return textureSlot;
	}

	public Material getMaterial() {
		return material;
	}

	public SdBufferSubInstance setMaterial(Material material) {
		this.material = material;
		this.diffuseLayer = material.getLayer(0);
		twoSided = diffuseLayer.getTwoSided() || (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.getTwoSided());

		return this;
	}

	public SdBufferSubInstance setMaterial(Material material, TimeEnvironmentImpl timeEnvironment) {
		this.material = material;
		this.diffuseLayer = material.getLayer(0);
		twoSided = diffuseLayer.getTwoSided() || (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.getTwoSided());

		fetchTextures(timeEnvironment);

		return this;
	}

	public SdBufferSubInstance setLayer(int textureSlot, Layer layer, TimeEnvironmentImpl timeEnvironment) {
		this.textureSlot = textureSlot;
		this.diffuseLayer = layer;
		twoSided = diffuseLayer.getTwoSided();

		fetchTextures(timeEnvironment);

		return this;
	}

	public SdBufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment){
//		for(int i = 0; i<material.getLayers().size() && i < 6; i++){
//			textures[i] = material.getLayer(i).getRenderTexture(timeEnvironment, model);
//		}
		texture = diffuseLayer.getRenderTexture(timeEnvironment, model);

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
