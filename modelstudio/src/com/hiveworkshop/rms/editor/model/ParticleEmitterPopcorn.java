package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParticleEmitterPopcorn;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * Popcorn FX is what I am calling the CORN chunk, somebody said that's probably
 * what they represent. 2020-08: Changing the name to ParticleEmitterPopcorn to
 * match leaked Blizzard MDL. (one of the builds of the game included an MDL by
 * mistake or something)
 */
public class ParticleEmitterPopcorn extends IdObject {
	int replaceableId = 0;
	float alpha = 0;
	Vec3 color = new Vec3();
	float speed = 0;
	float emissionRate = 0;
	float lifeSpan = 0;
	String path = "";
	String animVisibilityGuide = "";
	Map<Animation, State> animationVisStateMap = new HashMap<>();

	public ParticleEmitterPopcorn(final String name) {
		this.name = name;
	}

	public ParticleEmitterPopcorn(final ParticleEmitterPopcorn emitter) {
		copyObject(emitter);

		replaceableId = emitter.replaceableId;
		alpha = emitter.alpha;
		color = new Vec3(emitter.color);
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
		System.out.println("emitter color: " + Arrays.toString(emitter.color));
		color = new Vec3(emitter.color);
//		color = new Vec3(ModelUtils.flipRGBtoBGR(emitter.color));
		alpha = emitter.alpha;
		replaceableId = emitter.replaceableId;
		path = emitter.path;
		animVisibilityGuide = emitter.animationVisiblityGuide;
		System.out.println(emitter.animationVisiblityGuide);
	}

	public MdlxParticleEmitterPopcorn toMdlx(EditableModel model) {
		final MdlxParticleEmitterPopcorn emitter = new MdlxParticleEmitterPopcorn();

		objectToMdlx(emitter, model);

		emitter.lifeSpan = lifeSpan;
		emitter.emissionRate = emissionRate;
		emitter.speed = speed;
		emitter.color = color.toFloatArray();
//		emitter.color = ModelUtils.flipRGBtoBGR(color.toFloatArray());
		emitter.alpha = alpha;
		emitter.replaceableId = replaceableId;
		emitter.path = path;
//		emitter.animationVisiblityGuide = animVisibilityGuide;
		emitter.animationVisiblityGuide = getAnimVisibilityGuide();

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
		if (!animationVisStateMap.isEmpty()) {
			List<String> visStrings = new ArrayList<>();
			animationVisStateMap.keySet().stream()
					.filter(s -> !animationVisStateMap.get(s).equals(State.none))
					.forEach(s -> visStrings.add(s.getName() + "=" + animationVisStateMap.get(s).name()));
			return String.join(", ", visStrings);
		}
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

	public Vec3 getColor() {
		return color;
	}

	public void setColor(final Vec3 color) {
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

	public ParticleEmitterPopcorn initAnimsVisStates(List<Animation> anims) {
		Map<String, String> visGuid = new HashMap<>();
		for (String vg : animVisibilityGuide.split(",")) {
			String[] anSt = vg.toLowerCase().split("=");
			if (anSt.length == 2) {
				System.out.println(vg);
				visGuid.put(anSt[0].strip(), anSt[1].strip());
			}
		}
		for (Animation animation : anims) {
			State state = State.none;
			if (visGuid.containsKey(animation.getName())) {
				state = State.valueOf(visGuid.get(animation.getName()));
			}
			animationVisStateMap.put(animation, state);
		}

		return this;
	}

	public ParticleEmitterPopcorn updateAnimsVisMap(List<Animation> anims) {
		Set<Animation> existingAnimationSet = new HashSet<>(anims);
		for (Animation animation : anims) {
			if (!animationVisStateMap.containsKey(animation)) {
				animationVisStateMap.put(animation, State.none);
			}
		}
		animationVisStateMap.forEach((a, s) -> {
			if (!existingAnimationSet.contains(a)) animationVisStateMap.remove(a);
		});
		return this;
	}

	public ParticleEmitterPopcorn setAnimVisState(Animation animation, State state) {
		animationVisStateMap.put(animation, state);
		return this;
	}

	public State getAnimVisState(Animation animation) {
		return animationVisStateMap.get(animation);
	}

	public void setStaticVis(float vis) {
		System.out.println(vis);
	}

	;

	public enum State {
		on, off, none;

		static State fromInt(int i) {
			if (0 <= i && i < 3) {
				return values()[i];
			}
			return none;
		}
	}
}
