package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2.HeadOrTail;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * ParticleEmitter2 class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 */
public class ParticleEmitter2 extends EmitterIdObject {
	FilterMode filterMode = FilterMode.BLEND;
	HeadOrTail headOrTail = HeadOrTail.HEAD;
	boolean unshaded = false;
	boolean sortPrimsFarZ = false;
	boolean lineEmitter = false;
	boolean unfogged = false;
	boolean modelSpace = false;
	boolean xYQuad = false;
	boolean squirt = false;
	double speed = 0;
	double variation = 0;
	double latitude = 0;
	double gravity = 0;
	double emissionRate = 0;
	double width = 0;
	double length = 0;
	double lifeSpan = 0;
	double tailLength = 0;
	double time = 0;
	int rows = 0;
	int columns = 0;
	int textureID = 0;
	int replaceableId = 0;
	int priorityPlane = 0;
	Vec3[] segmentColor = {new Vec3(1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, 1)};
	Vec3 alphas = new Vec3(1, 1, 1);
	Vec3 particleScaling = new Vec3(1, 1, 1);
	Vec3 headUVAnim = new Vec3(0, 0, 1);
	Vec3 headDecayUVAnim = new Vec3(0, 0, 1);
	Vec3 tailUVAnim = new Vec3(0, 0, 1);
	Vec3 tailDecayUVAnim = new Vec3(0, 0, 1);
	Bitmap texture;

	public ParticleEmitter2() {
	}

	public ParticleEmitter2(String name) {
		this.name = name;
	}

	public ParticleEmitter2(ParticleEmitter2 emitter) {
		super(emitter);

		filterMode = emitter.filterMode;
		headOrTail = emitter.headOrTail;
		unshaded = emitter.unshaded;
		sortPrimsFarZ = emitter.sortPrimsFarZ;
		lineEmitter = emitter.lineEmitter;
		unfogged = emitter.unfogged;
		modelSpace = emitter.modelSpace;
		xYQuad = emitter.xYQuad;
		squirt = emitter.squirt;

		speed = emitter.speed;
		variation = emitter.variation;
		latitude = emitter.latitude;
		gravity = emitter.gravity;
		emissionRate = emitter.emissionRate;
		width = emitter.width;
		length = emitter.length;
		lifeSpan = emitter.lifeSpan;
		tailLength = emitter.tailLength;
		time = emitter.time;
		rows = emitter.rows;
		columns = emitter.columns;
		textureID = emitter.textureID;
		replaceableId = emitter.replaceableId;
		priorityPlane = emitter.priorityPlane;

//		segmentColor = emitter.segmentColor.clone(); //todo clone for real
		segmentColor[0].set(emitter.segmentColor[0]);
		segmentColor[1].set(emitter.segmentColor[1]);
		segmentColor[2].set(emitter.segmentColor[2]);
		alphas = new Vec3(emitter.alphas);
		particleScaling = new Vec3(emitter.particleScaling);
		headUVAnim = new Vec3(emitter.headUVAnim);
		headDecayUVAnim = new Vec3(emitter.headDecayUVAnim);
		tailUVAnim = new Vec3(emitter.tailUVAnim);
		tailDecayUVAnim = new Vec3(emitter.tailDecayUVAnim);

		texture = emitter.texture;
	}

	@Override
	public ParticleEmitter2 copy() {
		return new ParticleEmitter2(this);
	}

	public boolean getUnshaded() {
		return unshaded;
	}

	public void setUnshaded(boolean unshaded) {
		this.unshaded = unshaded;
	}

	public boolean getSortPrimsFarZ() {
		return sortPrimsFarZ;
	}

	public void setSortPrimsFarZ(boolean sortPrimsFarZ) {
		this.sortPrimsFarZ = sortPrimsFarZ;
	}

	public boolean getLineEmitter() {
		return lineEmitter;
	}

	public void setLineEmitter(boolean lineEmitter) {
		this.lineEmitter = lineEmitter;
	}

	public boolean getUnfogged() {
		return unfogged;
	}

	public void setUnfogged(boolean unfogged) {
		this.unfogged = unfogged;
	}

	public boolean getModelSpace() {
		return modelSpace;
	}

	public void setModelSpace(boolean modelSpace) {
		this.modelSpace = modelSpace;
	}

	public boolean getXYQuad() {
		return xYQuad;
	}

	public void setXYQuad(boolean xYQuad) {
		this.xYQuad = xYQuad;
	}

	public boolean getSquirt() {
		return squirt;
	}

	public void setSquirt(boolean squirt) {
		this.squirt = squirt;
	}

	public boolean isAdditive() {
		return filterMode == FilterMode.ADDITIVE;
	}

	public boolean isModulate2x() {
		return filterMode == FilterMode.MODULATE2X;
	}

	public boolean isModulate() {
		return filterMode == FilterMode.MODULATE;
	}

	public boolean isAlphaKey() {
		return filterMode == FilterMode.ALPHAKEY;
	}

	public boolean isBlend() {
		return filterMode == FilterMode.BLEND;
	}

	public boolean isTail() {
		return headOrTail == HeadOrTail.TAIL;
	}

	public boolean isHead() {
		return headOrTail == HeadOrTail.HEAD;
	}

	public boolean isBoth() {
		return headOrTail == HeadOrTail.BOTH;
	}

	public HeadOrTail getHeadOrTail() {
		return headOrTail;
	}

	public void setHeadOrTail(HeadOrTail headOrTail) {
		this.headOrTail = headOrTail;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public void updateTextureRef(List<Bitmap> textures) {
		if (0 <= textureID && textureID < textures.size()) {
			texture = textures.get(getTextureId());
		}
	}

	public int getTextureId() {
		return textureID;
	}

	public void setTextureId(int textureId) {
		if (textureId != -1) {
			textureID = textureId;
		} else {
			textureID = 0;
		}
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getVariation() {
		return variation;
	}

	public void setVariation(double variation) {
		this.variation = variation;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getGravity() {
		return gravity;
	}

	public void setGravity(double gravity) {
		this.gravity = gravity;
	}

	public double getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(double emissionRate) {
		this.emissionRate = emissionRate;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(double lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public double getTailLength() {
		return tailLength;
	}

	public void setTailLength(double tailLength) {
		this.tailLength = tailLength;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	@Override
	public int getBlendSrc() {
		return switch (filterMode) {
			case BLEND -> GL11.GL_SRC_ALPHA;
			case ADDITIVE -> GL11.GL_SRC_ALPHA;
			case ALPHAKEY -> GL11.GL_SRC_ALPHA;
			case MODULATE -> GL11.GL_ZERO;
			case MODULATE2X -> GL11.GL_DST_COLOR;
		};

	}

	@Override
	public int getBlendDst() {
		return switch (filterMode) {
			case BLEND -> GL11.GL_ONE_MINUS_SRC_ALPHA;
			case ADDITIVE -> GL11.GL_ONE;
			case ALPHAKEY -> GL11.GL_ONE;
			case MODULATE -> GL11.GL_SRC_COLOR;
			case MODULATE2X -> GL11.GL_SRC_COLOR;
		};

	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public int getCols() {
		return columns;
	}

	@Override
	public boolean isRibbonEmitter() {
		return false;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public int getTextureID() {
		return textureID;
	}

	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public boolean isTeamColored() {
		return getReplaceableId() != 0;
	}

	public void setReplaceableId(int replaceableId) {
		this.replaceableId = replaceableId;
	}

	public int getPriorityPlane() {
		return priorityPlane;
	}

	public void setPriorityPlane(int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	public Vec3 getAlpha() {
		return alphas;
	}

	public void setAlpha(Vec3 alphas) {
		this.alphas.set(alphas);
	}

	public Vec3 getParticleScaling() {
		return particleScaling;
	}

	public void setParticleScaling(Vec3 particleScaling) {
		this.particleScaling = particleScaling;
	}

	public Vec3 getHeadUVAnim() {
		return headUVAnim;
	}

	public void setHeadUVAnim(Vec3 headUVAnim) {
		this.headUVAnim = headUVAnim;
	}

	public Vec3 getHeadDecayUVAnim() {
		return headDecayUVAnim;
	}

	public void setHeadDecayUVAnim(Vec3 headDecayUVAnim) {
		this.headDecayUVAnim = headDecayUVAnim;
	}

	public Vec3 getTailUVAnim() {
		return tailUVAnim;
	}

	public void setTailUVAnim(Vec3 tailUVAnim) {
		this.tailUVAnim = tailUVAnim;
	}

	public Vec3 getTailDecayUVAnim() {
		return tailDecayUVAnim;
	}

	public void setTailDecayUVAnim(Vec3 tailDecayUVAnim) {
		this.tailDecayUVAnim = tailDecayUVAnim;
	}

	public void setSegmentColor(int index, Vec3 color) {
		segmentColor[index].set(color);
	}

	public void setSegmentColor(int index, float[] color) {
		segmentColor[index].set(color);
	}

	public Vec3 getSegmentColor(int index) {
		return segmentColor[index];
	}

	public int getSegmentColorCount() {
		return segmentColor.length;
	}

	public Vec3[] getSegmentColors() {
		return segmentColor;
	}

	public void setTexture(Bitmap texture) {
		this.texture = texture;
	}

	public Bitmap getTexture() {
		return texture;
	}

	@Override
	public double getClickRadius() {
		return DEFAULT_CLICK_RADIUS;
	}

	public double getRenderWidth(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_WIDTH, (float) getWidth());
	}

	public double getRenderLength(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_LENGTH, (float) getLength());
	}

	public double getRenderLatitude(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_LATITUDE, (float) getLatitude());
	}

	public double getRenderVariation(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_VARIATION, (float) getVariation());
	}

	public double getRenderSpeed(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_SPEED, (float) getSpeed());
	}

	public double getRenderGravity(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_GRAVITY, (float) getGravity());
	}

	public double getRenderEmissionRate(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, MdlUtils.TOKEN_EMISSION_RATE, (float) getEmissionRate());
	}
}
