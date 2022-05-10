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

public abstract class BufferSubInstance {
	protected final Bitmap[] textures = new Bitmap[6];
	protected Bitmap texture;
	protected int textureSlot = 0;
	protected Vec4[] layerColors = new Vec4[6];
	protected Vec4 layerColor = new Vec4();
	protected int offset = 0;
	protected int vertCount = 0;
	protected float fresnelTeamColor = 0;
	protected Vec4 fresnelColor = new Vec4();
	protected Mat4 uvTransform = new Mat4();
	protected boolean twoSided = false;
	protected boolean renderTextures = false;
	protected final TextureThing textureThing;
	protected final EditableModel model;
	protected Layer diffuseLayer;
	protected Material material;

	public BufferSubInstance(EditableModel model, TextureThing textureThing){
		this.model = model;
		this.textureThing = textureThing;
	}

	public Vec4[] getLayerColors() {
		return layerColors;
	}

	public BufferSubInstance setLayerColors(Vec4[] layerColors) {
		this.layerColors = layerColors;
		return this;
	}

	public Vec4 getLayerColor() {
		return layerColor;
	}

	public BufferSubInstance setLayerColor(Vec4 layerColor) {
		this.layerColor.set(layerColor);
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public BufferSubInstance setOffset(int offset) {
		this.offset = offset;
		return this;
	}
	public BufferSubInstance setEnd(int endVertexCount) {
		vertCount = endVertexCount - offset;
		return this;
	}

	public int getVertCount() {
		return vertCount;
	}
	public BufferSubInstance setVertCount(int vertCount) {
		this.vertCount = vertCount;
		return this;
	}

	public float getFresnelTeamColor() {
		return fresnelTeamColor;
	}

	public Vec4 getFresnelColor() {
		return fresnelColor;
	}


	public boolean isRenderTextures() {
		return renderTextures;
	}

	public BufferSubInstance setRenderTextures(boolean renderTextures) {
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

	public BufferSubInstance setMaterial(Material material, int textureSlot, TimeEnvironmentImpl timeEnvironment) {
		this.material = material;
		this.textureSlot = textureSlot;
		this.diffuseLayer = material.getLayer(textureSlot);
		this.twoSided = diffuseLayer.getTwoSided() || (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.getTwoSided());

		fetchTextures(timeEnvironment);

		return this;
	}

	protected abstract BufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment);



	protected void setUVTransform(Layer layer, TimeEnvironmentImpl timeEnvironment) {
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
