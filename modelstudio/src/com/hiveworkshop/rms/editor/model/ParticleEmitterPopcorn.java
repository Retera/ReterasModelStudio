package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitterPopcorn;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Vertex;

/**
 * Popcorn FX is what I am calling the CORN chunk, somebody said that's probably
 * what they represent. 2020-08: Changing the name to ParticleEmitterPopcorn to
 * match leaked Blizzard MDL. (one of the builds of the game included an MDL by
 * mistake or something)
 */
public class ParticleEmitterPopcorn extends IdObject {
	int replaceableId = 0;
	float alpha = 0;
	Vertex color = new Vertex();
	float speed = 0;
	float emissionRate = 0;
	float lifeSpan = 0;
	String path = "";
	String animVisibilityGuide = "";

	public ParticleEmitterPopcorn(final String name) {
		this.name = name;
	}

	public ParticleEmitterPopcorn(final ParticleEmitterPopcorn emitter) {
		copyObject(emitter);

		replaceableId = emitter.replaceableId;
		alpha = emitter.alpha;
		color = new Vertex(emitter.color);
		speed = emitter.speed;
		emissionRate = emitter.emissionRate;
		lifeSpan = emitter.lifeSpan;
		path = emitter.path;
		animVisibilityGuide = emitter.animVisibilityGuide;
	}

	public ParticleEmitterPopcorn(final MdlxParticleEmitterPopcorn emitter) {
		loadObject(emitter);

		lifeSpan = emitter.lifeSpan;
		emissionRate = emitter.emissionRate;
		speed = emitter.speed;
		color = new Vertex(ModelUtils.flipRGBtoBGR(emitter.color));
		alpha = emitter.alpha;
		replaceableId = emitter.replaceableId;
		path = emitter.path;
		animVisibilityGuide = emitter.animationVisiblityGuide;
	}

	public MdlxParticleEmitterPopcorn toMdlx() {
		final MdlxParticleEmitterPopcorn emitter = new MdlxParticleEmitterPopcorn();

		objectToMdlx(emitter);

		emitter.lifeSpan = lifeSpan;
		emitter.emissionRate = emissionRate;
		emitter.speed = speed;
		emitter.color = ModelUtils.flipRGBtoBGR(color.toFloatArray());
		emitter.alpha = alpha;
		emitter.replaceableId = replaceableId;
		emitter.path = path;
		emitter.animationVisiblityGuide = animVisibilityGuide;

		return emitter;
	}

	@Override
	public ParticleEmitterPopcorn copy() {
		return new ParticleEmitterPopcorn(this);
	}

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public void setAnimVisibilityGuide(final String flagString) {
		animVisibilityGuide = flagString;
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
