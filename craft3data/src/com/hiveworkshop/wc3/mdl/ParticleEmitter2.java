package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitter2;

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
	static final String[] knownFlagNames = { "DontInherit { Rotation }", "DontInherit { Translation }",
			"DontInherit { Scaling }", "SortPrimsFarZ", "Unshaded", "LineEmitter", "Unfogged", "ModelSpace", "XYQuad",
			"Squirt", "Additive", "Modulate2x", "Modulate", "AlphaKey", "Blend", "Tail", "Head", "Both" };
	boolean[] knownFlags = new boolean[knownFlagNames.length];

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
	Vertex alphas = new Vertex();
	Vertex particleScaling = new Vertex();
	Vertex headUVAnim = new Vertex();
	Vertex headDecayUVAnim = new Vertex();
	Vertex tailUVAnim = new Vertex();
	Vertex tailDecayUVAnim = new Vertex();
	List<String> unknownFlags = new ArrayList<>();
	Bitmap texture;

	private ParticleEmitter2() {

	}

	public ParticleEmitter2(final String name) {
		this.name = name;
	}

	public ParticleEmitter2(final MdlxParticleEmitter2 emitter) {
		if ((emitter.flags & 4096) != 4096) {
			System.err.println("MDX -> MDL error: A particle emitter '" + emitter.name
					+ "' not flagged as particle emitter in MDX!");
		}
		
		loadObject(emitter);

		int flags = emitter.flags;

		if (((flags >> 15) & 1) == 1) {
			add("Unshaded");
		}
		if (((flags >> 16) & 1) == 1) {
			add("SortPrimsFarZ");
		}
		if (((flags >> 17) & 1) == 1) {
			add("LineEmitter");
		}
		if (((flags >> 18) & 1) == 1) {
			add("Unfogged");
		}
		if (((flags >> 19) & 1) == 1) {
			add("ModelSpace");
		}
		if (((flags >> 20) & 1) == 1) {
			add("XYQuad");
		}

		setSpeed(emitter.speed);
		setVariation(emitter.variation);
		setLatitude(emitter.latitude);
		setGravity(emitter.gravity);
		setLifeSpan(emitter.lifeSpan);
		setEmissionRate(emitter.emissionRate);
		setLength(emitter.length);
		setWidth(emitter.width);

		switch (emitter.filterMode.getValue()) {
		case 0:
			add("Blend");
			break;
		case 1:
			add("Additive");
			break;
		case 2:
			add("Modulate");
			break;
		case 3:
			add("Modulate2x");
			break;
		case 4:
			add("AlphaKey");
			break;
		default:
			System.err.println("Unkown filter mode error");
			add("UnknownFilterMode");
			break;
		}

		setRows((int)emitter.rows);
		setColumns((int)emitter.columns);

		switch ((int)emitter.headOrTail) {
		case 0:
			add("Head");
			break;
		case 1:
			add("Tail");
			break;
		case 2:
			add("Both");
			break;
		default:
			System.err.println("Unkown head or tail error");
			add("UnknownHeadOrTail");
			break;
		}

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
			add("Squirt");
		}

		setPriorityPlane(emitter.priorityPlane);
		setReplaceableId((int)emitter.replaceableId);
	}

	public MdlxParticleEmitter2 toMdlx() {
		MdlxParticleEmitter2 emitter = new MdlxParticleEmitter2();
	
		objectToMdlx(emitter);

		for (final String flag : getFlags()) {
			if (flag.equals("Unshaded")) {
				emitter.flags |= 0x8000;
			} else if (flag.equals("SortPrimsFarZ")) {
				emitter.flags |= 0x10000;
			} else if (flag.equals("LineEmitter")) {
				emitter.flags |= 0x20000;
			} else if (flag.equals("Unfogged")) {
				emitter.flags |= 0x40000;
			} else if (flag.equals("ModelSpace")) {
				emitter.flags |= 0x80000;
			} else if (flag.equals("XYQuad")) {
				emitter.flags |= 0x100000;
			} else if (flag.equals("Blend")) {
				emitter.filterMode = MdlxParticleEmitter2.FilterMode.BLEND;
			} else if (flag.equals("Additive")) {
				emitter.filterMode = MdlxParticleEmitter2.FilterMode.ADDITIVE;
			} else if (flag.equals("Modulate")) {
				emitter.filterMode = MdlxParticleEmitter2.FilterMode.MODULATE;
			} else if (flag.equals("Modulate2x")) {
				emitter.filterMode = MdlxParticleEmitter2.FilterMode.MODULATE2X;
			} else if (flag.equals("AlphaKey")) {
				emitter.filterMode = MdlxParticleEmitter2.FilterMode.ALPHAKEY;
			} else if (flag.equals("Head")) {
				emitter.headOrTail = 0;
			} else if (flag.equals("Tail")) {
				emitter.headOrTail = 1;
			} else if (flag.equals("Both")) {
				emitter.headOrTail = 2;
			} else if (flag.equals("Squirt")) {
				emitter.squirt = 1;
			}
		}

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
	public IdObject copy() {
		final ParticleEmitter2 x = new ParticleEmitter2();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.speed = speed;
		x.variation = variation;
		x.latitude = latitude;
		x.gravity = gravity;
		x.emissionRate = emissionRate;
		x.width = width;
		x.length = length;
		x.lifeSpan = lifeSpan;
		x.tailLength = tailLength;
		x.time = time;
		x.rows = rows;
		x.columns = columns;
		x.textureID = textureID;
		x.replaceableId = replaceableId;
		x.priorityPlane = priorityPlane;

		x.knownFlags = knownFlags.clone();
		x.segmentColor = segmentColor.clone();
		x.alphas = new Vertex(alphas);
		x.particleScaling = new Vertex(particleScaling);
		x.headUVAnim = new Vertex(headUVAnim);
		x.headDecayUVAnim = new Vertex(headDecayUVAnim);
		x.tailUVAnim = new Vertex(tailUVAnim);
		x.tailDecayUVAnim = new Vertex(tailDecayUVAnim);
		x.addAll(getAnimFlags());
		x.unknownFlags = new ArrayList<>(unknownFlags);

		x.texture = texture;
		return x;
	}

	public boolean isDontInheritRotation() {
		return knownFlags[0];
	}

	public boolean isDontInheritTranslation() {
		return knownFlags[1];
	}

	public boolean isDontInheritScaling() {
		return knownFlags[2];
	}

	public boolean isSortPrimsFarZ() {
		return knownFlags[3];
	}

	public boolean isUnshaded() {
		return knownFlags[4];
	}

	public boolean isLineEmitter() {
		return knownFlags[5];
	}

	public boolean isUnfogged() {
		return knownFlags[6];
	}

	public boolean isModelSpace() {
		return knownFlags[7];
	}

	public boolean isXYQuad() {
		return knownFlags[8];
	}

	public boolean isSquirt() {
		return knownFlags[9];
	}

	public boolean isAdditive() {
		return knownFlags[10];
	}

	public boolean isModulate2x() {
		return knownFlags[11];
	}

	public boolean isModulate() {
		return knownFlags[12];
	}

	public boolean isAlphaKey() {
		return knownFlags[13];
	}

	public boolean isBlend() {
		return knownFlags[14];
	}

	public boolean isTail() {
		return knownFlags[15] || isBoth();
	}

	public boolean isHead() {
		return knownFlags[16] || isBoth();
	}

	public boolean isBoth() {
		return knownFlags[17];
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
		switch (getFilterModeReallyBadReallySlow()) {
		case Blend:
			return GL11.GL_SRC_ALPHA;
		case Additive:
			return GL11.GL_SRC_ALPHA;
		case AlphaKey:
			return GL11.GL_SRC_ALPHA;
		case Modulate:
			return GL11.GL_ZERO;
		case Modulate2x:
			return GL11.GL_DST_COLOR;
		}
		return GL11.GL_ONE;
	}

	@Override
	public int getBlendDst() {
		switch (getFilterModeReallyBadReallySlow()) {
		case Blend:
			return GL11.GL_ONE_MINUS_SRC_ALPHA;
		case Additive:
			return GL11.GL_ONE;
		case AlphaKey:
			return GL11.GL_ONE;
		case Modulate:
			return GL11.GL_SRC_COLOR;
		case Modulate2x:
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

	@Override
	public void add(final String flag) {
		boolean isKnownFlag = false;
		for (int i = 0; (i < knownFlagNames.length) && !isKnownFlag; i++) {
			if (knownFlagNames[i].equals(flag)) {
				knownFlags[i] = true;
				isKnownFlag = true;
			}
		}
		if (!isKnownFlag) {
			unknownFlags.add(flag);
		}
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
	public Set<String> getFlags() {
		final Set<String> flags = new HashSet<>(unknownFlags);
		for (int i = 0; i < knownFlags.length; i++) {
			if (knownFlags[i]) {
				flags.add(knownFlagNames[i]);
			}
		}
		return flags;
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

	public static enum FilterMode {
		Blend, Additive, Modulate, Modulate2x, AlphaKey;
	};

	public FilterMode getFilterModeReallyBadReallySlow() {
		FilterMode filterMode = FilterMode.Blend;
		for (final String flag : getFlags()) {
			switch (flag) {
			case "Head":
				break;
			case "Tail":
				break;
			case "Both":
				break;
			case "Blend":
				filterMode = FilterMode.Blend;
				break;
			case "Additive":
				filterMode = FilterMode.Additive;
				break;
			case "Modulate":
				filterMode = FilterMode.Modulate;
				break;
			case "Modulate2x":
				filterMode = FilterMode.Modulate2x;
				break;
			case "AlphaKey":
				filterMode = FilterMode.AlphaKey;
				break;
			case "Squirt":
				break;
			default:
				break;
			// do nothing for the other flags, there will be many
			}
		}
		return filterMode;
	}
}
