package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxParticleEmitterPopcorn;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * Popcorn FX is what I am calling the CORN chunk, somebody said that's probably
 * what they represent. 2020-08: Changing the name to ParticleEmitterPopcorn to
 * match leaked Blizzard MDL. (one of the builds of the game included an MDL by
 * mistake or something)
 */
public class ParticleEmitterPopcorn extends IdObject {
	List<AnimFlag> animFlags = new ArrayList<>();
	private int replaceableId;
	private float alpha;
	private Vertex color;
	private float speed;
	private float emissionRate;
	private float lifeSpan;
	String path = null;
	String animVisibilityGuide = null;
	List<String> flags = new ArrayList<>();

	private ParticleEmitterPopcorn() {

	}

	public ParticleEmitterPopcorn(final String name) {
		this.name = name;
	}

	public ParticleEmitterPopcorn(final MdlxParticleEmitterPopcorn emitter) {
		loadObject(emitter);

		lifeSpan = emitter.lifeSpan;
		emissionRate = emitter.emissionRate;
		speed = emitter.speed;
		color = new Vertex(MdlxUtils.flipRGBtoBGR(emitter.color));
		alpha = emitter.alpha;
		replaceableId = emitter.replaceableId;
		path = emitter.path;
		animVisibilityGuide = emitter.animationVisiblityGuide;
	}

	public MdlxParticleEmitterPopcorn toMdlx() {
		MdlxParticleEmitterPopcorn emitter = new MdlxParticleEmitterPopcorn();

		objectToMdlx(emitter);

		emitter.lifeSpan = lifeSpan;
		emitter.emissionRate = emissionRate;
		emitter.speed = speed;
		emitter.color = MdlxUtils.flipRGBtoBGR(color.toFloatArray());
		emitter.alpha = alpha;
		emitter.replaceableId = replaceableId;
		emitter.path = path;
		emitter.animationVisiblityGuide = animVisibilityGuide;

		return emitter;
	}

	@Override
	public IdObject copy() {
		final ParticleEmitterPopcorn x = new ParticleEmitterPopcorn();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());

		x.path = path;
		x.animVisibilityGuide = animVisibilityGuide;
		x.replaceableId = replaceableId;
		x.alpha = alpha;
		if (color != null) {
			x.color = new Vertex(color);
		} else {
			x.color = null;
		}
		x.speed = speed;
		x.emissionRate = emissionRate;
		x.lifeSpan = lifeSpan;

		for (final AnimFlag af : animFlags) {
			x.animFlags.add(new AnimFlag(af));
		}
		return x;
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public void setAnimVisibilityGuide(final String flagString) {
		this.animVisibilityGuide = flagString;
	}

	public String getAnimVisibilityGuide() {
		return animVisibilityGuide;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.popcornFxEmitter(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	public double getRenderEmissionRate(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "EmissionRate", 0);
	}

	public Vertex getColor() {
		return color;
	}

	public void setColor(final Vertex color) {
		this.color = color;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(final float alpha) {
		this.alpha = alpha;
	}

	public float getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(final float emissionRate) {
		this.emissionRate = emissionRate;
	}

	public float getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(final float lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(final float speed) {
		this.speed = speed;
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public void setReplaceableId(final int replaceableId) {
		this.replaceableId = replaceableId;
	}
}
