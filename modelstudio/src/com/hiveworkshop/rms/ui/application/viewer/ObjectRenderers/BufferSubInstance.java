package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public abstract class BufferSubInstance {
	protected int offset = 0;
	protected int vertCount = 0;
	protected final Bitmap[] textures = new Bitmap[6];
	protected final Bitmap[] colorTexs = new Bitmap[6];
	protected final Color[] colors = new Color[6];
	{
		colorTexs[0] = new Bitmap("RMS_Placeholders\\Diffuse");
		colorTexs[1] = new Bitmap("RMS_Placeholders\\Normal");
		colorTexs[2] = new Bitmap("RMS_Placeholders\\ORM");
		colorTexs[3] = new Bitmap("RMS_Placeholders\\Emissive");
		colorTexs[4] = new Bitmap("RMS_Placeholders\\Team Color");
		colorTexs[5] = new Bitmap("RMS_Placeholders\\Reflections");

		colors[0] = Color.GRAY;
		colors[1] = new Color(.5f, .5f, 0, 1);
		colors[2] = new Color(.9f, .2f, .8f, 0);
		colors[3] = Color.BLACK;
		colors[4] = Color.RED;
		colors[5] = new Color(.56f, .89f, .92f, 1);
	}
	protected Bitmap texture;
	protected final Vec2 flipBookSize = new Vec2(1,1);
	protected int textureSlot = 0;
	protected Vec4 layerColor = new Vec4();
	protected float fresnelTeamColor = 0;
	protected Vec4 fresnelColor = new Vec4();
	protected Mat4 uvTransform = new Mat4();
	protected boolean twoSided = false;
	protected boolean renderTextures = false;
	protected final TextureThing textureThing;
	protected final EditableModel model;
	protected Layer diffuseLayer;
	protected Material material;
	protected ParticleEmitter2 particleEmitter2;
	protected boolean shouldDraw = true;

	public BufferSubInstance(EditableModel model, TextureThing textureThing){
		this.model = model;
		this.textureThing = textureThing;
	}

	public boolean shouldDraw(){
		return shouldDraw;
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

	public BufferSubInstance setFlipBookSize(Vec2 flipBookSize) {
		this.flipBookSize.set(flipBookSize);
		return this;
	}

	public Vec2 getFlipBookSize() {
		return flipBookSize;
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
				if(textures[i] != null){
					textureThing.loadAndBindTexture(model, textures[i], i);
				}
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

	public BufferSubInstance setParticle(ParticleEmitter2 particleEmitter2, int textureSlot, TimeEnvironmentImpl timeEnvironment) {
		this.particleEmitter2 = particleEmitter2;
		this.textureSlot = textureSlot;
//		System.out.println("cols: " + (particleEmitter2.getCols()+1) + ", rows: " + (particleEmitter2.getRows()+1));
		flipBookSize.set(particleEmitter2.getCols(), particleEmitter2.getRows());
		texture = particleEmitter2.getTexture();


		return this;
	}

	protected abstract BufferSubInstance fetchTextures(TimeEnvironmentImpl timeEnvironment);


	Vec3 uvCenter = new Vec3(.5,.5,.5);
	protected void setUVTransform(Layer layer, TimeEnvironmentImpl timeEnvironment) {
		uvTransform.setIdentity();
		if(layer.getTextureAnim() != null){
			uvTransform.fromRotationTranslationScaleOrigin(
					layer.getTextureAnim().getInterpolatedQuat(timeEnvironment, MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_SCALING, Vec3.ONE),
					uvCenter
			);
		} else {
			uvTransform.fromRotationTranslationScale(Quat.IDENTITY, Vec3.ZERO, Vec3.ONE);
		}
	}

	public boolean isTwoSided() {
		return twoSided;
	}


}
