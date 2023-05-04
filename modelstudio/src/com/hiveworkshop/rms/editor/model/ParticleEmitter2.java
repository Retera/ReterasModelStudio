package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitter2.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.EnumSet;

/**
 * ParticleEmitter2 class, these are the things most people would think of as a
 * particle emitter, I think. Blizzard favored use of these over
 * ParticleEmitters and I do too simply because I so often recycle data and
 * there are more of these to use.
 *
 * Eric Theller 3/10/2012 3:32 PM
 *
 * ParticleEmitter2 specific animation tags
 * 	KP2S - Particle emitter 2 speed
 * 	KP2R - Particle emitter 2 variation
 * 	KP2L - Particle emitter 2 latitude
 * 	KP2G - Particle emitter 2 gravity
 * 	KP2E - Particle emitter 2 emission rate
 * 	KP2N - Particle emitter 2 length
 * 	KP2W - Particle emitter 2 width
 * 	KP2V - Particle emitter 2 visibility
 */
public class ParticleEmitter2 extends EmitterIdObject {
	private final EnumSet<HeadTailFlag> headTailFlags = EnumSet.noneOf(HeadTailFlag.class);
	private FilterMode filterMode = FilterMode.BLEND;
	private final EnumSet<P2Flag> p2Flags = EnumSet.noneOf(P2Flag.class);
	private boolean squirt = false;
	private double speed = 0;
	private double variation = 0;
	private double latitude = 0;
	private double gravity = 0;
	private double emissionRate = 0;
	private double width = 0;
	private double length = 0;
	private double lifeSpan = 0;
	private double tailLength = 0;
	private double time = 0;
	private int rows = 0;
	private int columns = 0;
	private int replaceableId = 0;
	private int priorityPlane = 0;
	private final Vec3[] segmentColor = {new Vec3(1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, 1)};
	private final Vec3 alphas = new Vec3(1, 1, 1);
	private final Vec3 particleScaling = new Vec3(1, 1, 1);
	private final Vec3 headUVAnim = new Vec3(0, 0, 1);
	private final Vec3 headDecayUVAnim = new Vec3(0, 0, 1);
	private final Vec3 tailUVAnim = new Vec3(0, 0, 1);
	private final Vec3 tailDecayUVAnim = new Vec3(0, 0, 1);
	private Bitmap texture;

	public ParticleEmitter2() {
	}

	public ParticleEmitter2(String name) {
		this.name = name;
	}

	public ParticleEmitter2(ParticleEmitter2 emitter) {
		super(emitter);

		filterMode = emitter.filterMode;
		headTailFlags.addAll(emitter.headTailFlags);
		p2Flags.addAll(emitter.p2Flags);

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
		replaceableId = emitter.replaceableId;
		priorityPlane = emitter.priorityPlane;

		segmentColor[0].set(emitter.segmentColor[0]);
		segmentColor[1].set(emitter.segmentColor[1]);
		segmentColor[2].set(emitter.segmentColor[2]);
		alphas.set(emitter.alphas);
		particleScaling.set(emitter.particleScaling);
		headUVAnim.set(emitter.headUVAnim);
		headDecayUVAnim.set(emitter.headDecayUVAnim);
		tailUVAnim.set(emitter.tailUVAnim);
		tailDecayUVAnim.set(emitter.tailDecayUVAnim);

		texture = emitter.texture;
	}

	@Override
	public ParticleEmitter2 copy() {
		return new ParticleEmitter2(this);
	}

	public boolean isFlagSet(P2Flag flag){
		return p2Flags.contains(flag);
	}
	public ParticleEmitter2 setFlag(P2Flag flag, boolean set){
		if(set){
			p2Flags.add(flag);
		} else {
			p2Flags.remove(flag);
		}
		return this;
	}
	public ParticleEmitter2 setFlags(EnumSet<P2Flag> flags){
		p2Flags.addAll(flags);
		return this;
	}

	public ParticleEmitter2 toggleFlag(P2Flag flag){
		return setFlag(flag, !p2Flags.contains(flag));
	}

	public EnumSet<P2Flag> getP2Flags() {
		return p2Flags;
	}

	public boolean getUnshaded() {
		return p2Flags.contains(P2Flag.UNSHADED);
	}

	public void setUnshaded(boolean unshaded) {
		setFlag(P2Flag.UNSHADED, unshaded);
	}

	public boolean getSortPrimsFarZ() {
		return p2Flags.contains(P2Flag.SORT_PRIMS_FAR_Z);
	}

	public void setSortPrimsFarZ(boolean sortPrimsFarZ) {
		setFlag(P2Flag.SORT_PRIMS_FAR_Z, sortPrimsFarZ);
	}

	public boolean getLineEmitter() {
		return p2Flags.contains(P2Flag.LINE_EMITTER);
	}

	public void setLineEmitter(boolean lineEmitter) {
		setFlag(P2Flag.LINE_EMITTER, lineEmitter);
	}

	public boolean getUnfogged() {
		return p2Flags.contains(P2Flag.UNFOGGED);
	}

	public void setUnfogged(boolean unfogged) {
		setFlag(P2Flag.UNFOGGED, unfogged);
	}

	public boolean getModelSpace() {
		return p2Flags.contains(P2Flag.MODEL_SPACE);
	}

	public void setModelSpace(boolean modelSpace) {
		setFlag(P2Flag.MODEL_SPACE, modelSpace);
	}

	public boolean getXYQuad() {
		return p2Flags.contains(P2Flag.XY_QUAD);
	}

	public void setXYQuad(boolean xYQuad) {
		setFlag(P2Flag.XY_QUAD, xYQuad);
	}

	public boolean getSquirt() {
		return squirt;
	}

	public void setSquirt(boolean squirt) {
		this.squirt = squirt;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public boolean isTail() {
		return headTailFlags.contains(HeadTailFlag.EMIT_TAIL);
	}

	public boolean isHead() {
		return headTailFlags.isEmpty() || headTailFlags.contains(HeadTailFlag.EMIT_HEAD);
	}

	public ParticleEmitter2 setHead(boolean b) {
		return setFlag(HeadTailFlag.EMIT_HEAD, b);
	}

	public ParticleEmitter2 setTail(boolean b) {
		return setFlag(HeadTailFlag.EMIT_TAIL, b);
	}

	public boolean isFlagSet(HeadTailFlag flag){
		return headTailFlags.contains(flag);
	}

	public ParticleEmitter2 setFlag(HeadTailFlag flag, boolean set){
		if(set){
			headTailFlags.add(flag);
		} else {
			headTailFlags.remove(flag);
		}
		return this;
	}

	public ParticleEmitter2 toggleFlag(HeadTailFlag flag){
		return setFlag(flag, !headTailFlags.contains(flag));
	}

	public EnumSet<HeadTailFlag> getHeadTailFlags() {
		return headTailFlags;
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
		this.particleScaling.set(particleScaling);
	}

	public Vec3 getHeadUVAnim() {
		return headUVAnim;
	}

	public void setHeadUVAnim(Vec3 headUVAnim) {
		this.headUVAnim.set(headUVAnim);
	}

	public Vec3 getHeadDecayUVAnim() {
		return headDecayUVAnim;
	}

	public void setHeadDecayUVAnim(Vec3 headDecayUVAnim) {
		this.headDecayUVAnim.set(headDecayUVAnim);
	}

	public Vec3 getTailUVAnim() {
		return tailUVAnim;
	}

	public void setTailUVAnim(Vec3 tailUVAnim) {
		this.tailUVAnim.set(tailUVAnim);
	}

	public Vec3 getTailDecayUVAnim() {
		return tailDecayUVAnim;
	}

	public void setTailDecayUVAnim(Vec3 tailDecayUVAnim) {
		this.tailDecayUVAnim.set(tailDecayUVAnim);
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
		return ProgramGlobals.getPrefs().getNodeBoxSize();
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

	public enum HeadTailFlag {
		EMIT_HEAD(MdlUtils.TOKEN_HEAD, 0x1),
		EMIT_TAIL(MdlUtils.TOKEN_TAIL, 0x2);
		final String name;
		final int flagBit;
		HeadTailFlag(String name, int flagBit){
			this.name = name;
			this.flagBit = flagBit;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public int getFlagBit() {
			return flagBit;
		}
		public static EnumSet<HeadTailFlag> fromBits(int bits){
			EnumSet<HeadTailFlag> flagSet = EnumSet.noneOf(HeadTailFlag.class);
			for (HeadTailFlag f : HeadTailFlag.values()){
				if ((f.flagBit & bits) == f.flagBit){
					flagSet.add(f);
				}
			}
			return flagSet;
		}
	}

	public enum P2Flag {
		UNSHADED(MdlUtils.TOKEN_UNSHADED, 0x8000),
		SORT_PRIMS_FAR_Z(MdlUtils.TOKEN_SORT_PRIMS_FAR_Z, 0x10000),
		LINE_EMITTER(MdlUtils.TOKEN_LINE_EMITTER, 0x20000),
		UNFOGGED(MdlUtils.TOKEN_UNFOGGED, 0x40000),
		MODEL_SPACE(MdlUtils.TOKEN_MODEL_SPACE, 0x80000),
		XY_QUAD(MdlUtils.TOKEN_XY_QUAD, 0x100000);
		final String name;
		final int flagBit;
		P2Flag(String name, int flagBit){
			this.name = name;
			this.flagBit = flagBit;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}

		public int getFlagBit() {
			return flagBit;
		}
		public static EnumSet<P2Flag> fromBits(int bits){
			EnumSet<P2Flag> flagSet = EnumSet.noneOf(P2Flag.class);
			for (P2Flag f : P2Flag.values()){
				if ((f.flagBit & bits) == f.flagBit){
					flagSet.add(f);
				}
			}
			return flagSet;
		}
	}
}