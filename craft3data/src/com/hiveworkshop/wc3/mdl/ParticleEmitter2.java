package com.hiveworkshop.wc3.mdl;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter2;
import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter2.FilterMode;
import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter2.HeadOrTail;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.render3d.EmitterIdObject;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

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
	Vertex[] segmentColor = new Vertex[3];
	Vertex alphas = new Vertex(1, 1, 1);
	Vertex particleScaling = new Vertex(1, 1, 1);
	Vertex headUVAnim = new Vertex(0, 0, 1);
	Vertex headDecayUVAnim = new Vertex(0, 0, 1);
	Vertex tailUVAnim = new Vertex(0, 0, 1);
	Vertex tailDecayUVAnim = new Vertex(0, 0, 1);
	Bitmap texture;

	public ParticleEmitter2() {

	}

	public ParticleEmitter2(final String name) {
		this.name = name;
	}

	public ParticleEmitter2(final ParticleEmitter2 emitter) {
		copyObject(emitter);
		
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
	
		segmentColor = emitter.segmentColor.clone();
		alphas = new Vertex(emitter.alphas);
		particleScaling = new Vertex(emitter.particleScaling);
		headUVAnim = new Vertex(emitter.headUVAnim);
		headDecayUVAnim = new Vertex(emitter.headDecayUVAnim);
		tailUVAnim = new Vertex(emitter.tailUVAnim);
		tailDecayUVAnim = new Vertex(emitter.tailDecayUVAnim);

		texture = emitter.texture;
	}

	public ParticleEmitter2(final MdlxParticleEmitter2 emitter) {
		if ((emitter.flags & 4096) != 4096) {
			System.err.println("MDX -> MDL error: A particle emitter '" + emitter.name
					+ "' not flagged as particle emitter in MDX!");
		}
		
		loadObject(emitter);

		if ((emitter.flags & 0x8000) != 0) {
			unshaded = true;
		}
		if ((emitter.flags & 0x10000) != 0) {
			sortPrimsFarZ = true;
		}
		if ((emitter.flags & 0x20000) != 0) {
			lineEmitter = true;
		}
		if ((emitter.flags & 0x40000) != 0) {
			unfogged = true;
		}
		if ((emitter.flags & 0x80000) != 0) {
			modelSpace = true;
		}
		if ((emitter.flags & 0x100000) != 0) {
			xYQuad = true;
		}

		setSpeed(emitter.speed);
		setVariation(emitter.variation);
		setLatitude(emitter.latitude);
		setGravity(emitter.gravity);
		setLifeSpan(emitter.lifeSpan);
		setEmissionRate(emitter.emissionRate);
		setLength(emitter.length);
		setWidth(emitter.width);
		filterMode = emitter.filterMode;

		setRows((int)emitter.rows);
		setColumns((int)emitter.columns);

		headOrTail = emitter.headOrTail;

		setTailLength(emitter.tailLength);
		setTime(emitter.timeMiddle);

		float[][] colors = emitter.segmentColors;
		short[] alphas = emitter.segmentAlphas;
		
		// SegmentColor - Inverse order for MDL!
		for (int i = 0; i < 3; i++) {
			setSegmentColor(i, new Vertex(colors[i]));
		}

		setAlpha(new Vertex(alphas[0], alphas[1], alphas[2]));
		setParticleScaling(new Vertex(emitter.segmentScaling));

		long[][] head = emitter.headIntervals;
		long[][] tail = emitter.tailIntervals;

		setHeadUVAnim(new Vertex(head[0][0], head[0][1], head[0][2]));
		setHeadDecayUVAnim(new Vertex(head[1][0], head[1][1], head[1][2]));
		setTailUVAnim(new Vertex(tail[0][0], tail[0][1], tail[0][2]));
		setTailDecayUVAnim(new Vertex(tail[1][0], tail[1][1], tail[1][2]));

		setTextureID(emitter.textureId);

		if (emitter.squirt == 1) {
			squirt = true;
		}

		setPriorityPlane(emitter.priorityPlane);
		setReplaceableId((int)emitter.replaceableId);
	}

	public MdlxParticleEmitter2 toMdlx() {
		MdlxParticleEmitter2 emitter = new MdlxParticleEmitter2();
	
		objectToMdlx(emitter);

		if (unshaded) {
			emitter.flags |= 0x8000;
		}

		if (sortPrimsFarZ) {
			emitter.flags |= 0x10000;
		}

		if (lineEmitter) {
			emitter.flags |= 0x20000;
		}

		if (unfogged) {
			emitter.flags |= 0x40000;
		}

		if (modelSpace) {
			emitter.flags |= 0x80000;
		}

		if (xYQuad) {
			emitter.flags |= 0x100000;
		}

		if (squirt) {
			emitter.squirt = 1;
		}

		emitter.filterMode = filterMode;
		emitter.headOrTail = headOrTail;

		emitter.speed = (float)getSpeed();
		emitter.variation = (float)getVariation();
		emitter.latitude = (float)getLatitude();
		emitter.gravity = (float)getGravity();
		emitter.lifeSpan = (float)getLifeSpan();
		emitter.emissionRate = (float)getEmissionRate();
		emitter.length = (float)getLength();
		emitter.width = (float)getWidth();
		emitter.rows = getRows();
		emitter.columns = getCols();
		emitter.tailLength = (float)getTailLength();
		emitter.timeMiddle = (float)getTime();

		emitter.segmentColors[0] = getSegmentColor(0).toFloatArray();
		emitter.segmentColors[1] = getSegmentColor(1).toFloatArray();
		emitter.segmentColors[2] = getSegmentColor(2).toFloatArray();
		
		emitter.segmentAlphas = getAlpha().toShortArray();
		emitter.segmentScaling = getParticleScaling().toFloatArray();

		emitter.headIntervals[0] = (long[])getHeadUVAnim().toLongArray();
		emitter.headIntervals[1] = (long[])getHeadDecayUVAnim().toLongArray();
		emitter.tailIntervals[0] = (long[])getTailUVAnim().toLongArray();
		emitter.tailIntervals[1] = (long[])getTailDecayUVAnim().toLongArray();

		emitter.textureId = getTextureID();
		emitter.priorityPlane = getPriorityPlane();
		emitter.replaceableId = getReplaceableId();

		return emitter;
	}

	@Override
	public ParticleEmitter2 copy() {
		return new ParticleEmitter2(this);
	}

	public boolean getUnshaded() {
		return unshaded;
	}

	public void setUnshaded(final boolean unshaded) {
		this.unshaded = unshaded;
	}

	public boolean getSortPrimsFarZ() {
		return sortPrimsFarZ;
	}

	public void setSortPrimsFarZ(final boolean sortPrimsFarZ) {
		this.sortPrimsFarZ = sortPrimsFarZ;
	}

	public boolean getLineEmitter() {
		return lineEmitter;
	}

	public void setLineEmitter(final boolean lineEmitter) {
		this.lineEmitter = lineEmitter;
	}

	public boolean getUnfogged() {
		return unfogged;
	}

	public void setUnfogged(final boolean unfogged) {
		this.unfogged = unfogged;
	}

	public boolean getModelSpace() {
		return modelSpace;
	}

	public void setModelSpace(final boolean modelSpace) {
		this.modelSpace = modelSpace;
	}

	public boolean getXYQuad() {
		return xYQuad;
	}

	public void setXYQuad(final boolean xYQuad) {
		this.xYQuad = xYQuad;
	}

	public boolean getSquirt() {
		return squirt;
	}

	public void setSquirt(final boolean squirt) {
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

	public void setHeadOrTail(final HeadOrTail headOrTail) {
		this.headOrTail = headOrTail;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setFilterMode(final FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public void updateTextureRef(final List<Bitmap> textures) {
		texture = textures.get(getTextureId());
	}

	public int getTextureId() {
		return textureID;
	}

	public void setTextureId(final int textureId) {
		this.textureID = textureId;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getVariation() {
		return variation;
	}

	public void setVariation(final double variation) {
		this.variation = variation;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public double getGravity() {
		return gravity;
	}

	public void setGravity(final double gravity) {
		this.gravity = gravity;
	}

	public double getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(final double emissionRate) {
		this.emissionRate = emissionRate;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(final double width) {
		this.width = width;
	}

	public double getLength() {
		return length;
	}

	public void setLength(final double length) {
		this.length = length;
	}

	public double getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(final double lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public double getTailLength() {
		return tailLength;
	}

	public void setTailLength(final double tailLength) {
		this.tailLength = tailLength;
	}

	public double getTime() {
		return time;
	}

	public void setTime(final double time) {
		this.time = time;
	}

	@Override
	public int getBlendSrc() {
		switch (filterMode) {
		case BLEND:
			return GL11.GL_SRC_ALPHA;
		case ADDITIVE:
			return GL11.GL_SRC_ALPHA;
		case ALPHAKEY:
			return GL11.GL_SRC_ALPHA;
		case MODULATE:
			return GL11.GL_ZERO;
		case MODULATE2X:
			return GL11.GL_DST_COLOR;
		}

		return GL11.GL_ONE;
	}

	@Override
	public int getBlendDst() {
		switch (filterMode) {
		case BLEND:
			return GL11.GL_ONE_MINUS_SRC_ALPHA;
		case ADDITIVE:
			return GL11.GL_ONE;
		case ALPHAKEY:
			return GL11.GL_ONE;
		case MODULATE:
			return GL11.GL_SRC_COLOR;
		case MODULATE2X:
			return GL11.GL_SRC_COLOR;
		}

		return GL11.GL_ONE;
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

	public void setRows(final int rows) {
		this.rows = rows;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(final int columns) {
		this.columns = columns;
	}

	public int getTextureID() {
		return textureID;
	}

	public void setTextureID(final int textureID) {
		this.textureID = textureID;
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public boolean isTeamColored() {
		return getReplaceableId() != 0;
	}

	public void setReplaceableId(final int replaceableId) {
		this.replaceableId = replaceableId;
	}

	public int getPriorityPlane() {
		return priorityPlane;
	}

	public void setPriorityPlane(final int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	public Vertex getAlpha() {
		return alphas;
	}

	public void setAlpha(final Vertex alphas) {
		this.alphas = alphas;
	}

	public Vertex getParticleScaling() {
		return particleScaling;
	}

	public void setParticleScaling(final Vertex particleScaling) {
		this.particleScaling = particleScaling;
	}

	public Vertex getHeadUVAnim() {
		return headUVAnim;
	}

	public void setHeadUVAnim(final Vertex headUVAnim) {
		this.headUVAnim = headUVAnim;
	}

	public Vertex getHeadDecayUVAnim() {
		return headDecayUVAnim;
	}

	public void setHeadDecayUVAnim(final Vertex headDecayUVAnim) {
		this.headDecayUVAnim = headDecayUVAnim;
	}

	public Vertex getTailUVAnim() {
		return tailUVAnim;
	}

	public void setTailUVAnim(final Vertex tailUVAnim) {
		this.tailUVAnim = tailUVAnim;
	}

	public Vertex getTailDecayUVAnim() {
		return tailDecayUVAnim;
	}

	public void setTailDecayUVAnim(final Vertex tailDecayUVAnim) {
		this.tailDecayUVAnim = tailDecayUVAnim;
	}

	public void setSegmentColor(final int index, final Vertex color) {
		segmentColor[index] = color;
	}

	public Vertex getSegmentColor(final int index) {
		return segmentColor[index];
	}

	public int getSegmentColorCount() {
		return segmentColor.length;
	}

	public Vertex[] getSegmentColors() {
		return segmentColor;
	}

	public void setTexture(final Bitmap texture) {
		this.texture = texture;
	}

	public Bitmap getTexture() {
		return texture;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.particleEmitter2(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	public double getRenderWidth(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Width", (float)getWidth());
	}

	public double getRenderLength(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Length", (float)getLength());
	}

	public double getRenderLatitude(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Latitude", (float)getLatitude());
	}

	public double getRenderVariation(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Variation", (float)getVariation());
	}

	public double getRenderSpeed(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Speed", (float)getSpeed());
	}

	public double getRenderGravity(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "Gravity", (float)getGravity());
	}

	public double getRenderEmissionRate(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "EmissionRate", (float)getEmissionRate());
	}
}
