package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
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
	float initVelocity = 0;
	float emissionRate = 0;
	float lifeSpan = 0;
	String path = "";
	String animVisibilityGuide = "";
	State always = State.off;
	Map<Animation, State> animationVisStateMap = new HashMap<>();

	public ParticleEmitterPopcorn(String name) {
		this.name = name;
	}

	public ParticleEmitterPopcorn() {
	}

	public ParticleEmitterPopcorn(ParticleEmitterPopcorn emitter) {
		super(emitter);

		replaceableId = emitter.replaceableId;
		alpha = emitter.alpha;
		color = new Vec3(emitter.color);
		initVelocity = emitter.initVelocity;
		emissionRate = emitter.emissionRate;
		lifeSpan = emitter.lifeSpan;
		path = emitter.path;
		animVisibilityGuide = emitter.animVisibilityGuide;
	}

	@Override
	public ParticleEmitterPopcorn copy() {
		return new ParticleEmitterPopcorn(this);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setAnimVisibilityGuide(String flagString) {
		animVisibilityGuide = flagString;
	}

	public String getAnimVisibilityGuide() {
		if (!animationVisStateMap.isEmpty()) {
			TreeMap<String, State> stringStateMap = new TreeMap<>();
			List<String> visStrings = new ArrayList<>();
			if (always != State.none) {
				visStrings.add("Always=" + always.name());
//				System.out.println("added: " + "Always=" + always.name());
			}
			animationVisStateMap.keySet()
//					.filter(s -> !animationVisStateMap.get(s).equals(State.none))
					.forEach(s -> stringStateMap.put(s.getName(), animationVisStateMap.get(s)));
//					.forEach(s -> visStrings.add(s.getName() + "=" + animationVisStateMap.get(s).name()));


			TreeMap<String, Set<State>> rootMap = new TreeMap<>();
			for (String s : stringStateMap.keySet()) {
				String[] nameParts = s.split(" ");
				String rootName = "";
				for (String np : nameParts) {
					rootName += np;
					if (rootMap.containsKey(rootName)) {
						rootMap.get(rootName).add(stringStateMap.get(s));
					} else {
						Set<State> stateSet = new HashSet<>();
						stateSet.add(stringStateMap.get(s));
						rootMap.put(rootName, stateSet);
					}
					rootName += " ";
				}
			}

//			System.out.println(name);
//			for (String s : rootMap.keySet()){
//				if(rootMap.get(s).size() == 1 && !rootMap.get(s).contains(State.none)){
//					System.out.println("root: " + s + " "+ rootMap.get(s).toArray(new State[0])[0]);
//				}
//			}

			State lastState = State.none;
			String rootName = ""; //recursive solution?
			String orgRootName = "";
			String lastAddedRootName = " ";
			for (String s : rootMap.keySet()) {
				if (rootMap.get(s).size() == 1) {
					State currState = rootMap.get(s).toArray(new State[0])[0];
					if (currState != lastState || !s.startsWith(rootName)) {
						if (rootName.length() > 0 && lastState != State.none) {
//							System.out.println("added: " + rootName + "=" + lastState.name());
							visStrings.add(rootName + "=" + lastState.name());
							lastAddedRootName = rootName;
						}
						rootName = s;
//					orgRootName = s;
//					rootName = s.split(" ")[0];
						lastState = currState;
					}
				}

			}

			if (!rootName.startsWith(lastAddedRootName) && lastState != State.none) {
//				System.out.println("added: " + rootName + "=" + lastState.name());
				visStrings.add(rootName + "=" + lastState.name());
			}


//			State lastState = State.none;
//			String rootName = ""; //recursive solution?
//			String orgRootName = "";
//			String lastAddedRootName = "";
//			for (String s : stringStateMap.keySet()){
//				State currState = stringStateMap.get(s);
//				if(currState != lastState || !s.startsWith(rootName)){
//					if(rootName.length() > 0 && lastState != State.none){
//						System.out.println("added: " + rootName + "=" + lastState.name());
//						visStrings.add(rootName + "=" + lastState.name());
//					}
//					lastAddedRootName = rootName;
//					rootName = s;
////					orgRootName = s;
////					rootName = s.split(" ")[0];
//					lastState = currState;
//				}
//
//			}
//
//			if(!rootName.startsWith(lastAddedRootName)){
//				System.out.println("added: " + rootName + "=" + lastState.name());
//				visStrings.add(rootName + "=" + lastState.name());
//			}


			return String.join(", ", visStrings);
		}
		return animVisibilityGuide;
	}

	@Override
	public double getClickRadius(CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / coordinateSystem.getZoom();
	}

	public double getRenderEmissionRate(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedFloat(animatedRenderEnvironment, "EmissionRate", 0);
	}

	public Vec3 getColor() {
		return color;
	}

	public void setColor(Vec3 color) {
		this.color = color;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getEmissionRate() {
		return emissionRate;
	}

	public void setEmissionRate(float emissionRate) {
		this.emissionRate = emissionRate;
	}

	public float getLifeSpan() {
		return lifeSpan;
	}

	public void setLifeSpan(float lifeSpan) {
		this.lifeSpan = lifeSpan;
	}

	public float getInitVelocity() {
		return initVelocity;
	}

	public void setInitVelocity(float initVelocity) {
		this.initVelocity = initVelocity;
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public void setReplaceableId(int replaceableId) {
		this.replaceableId = replaceableId;
	}

	public ParticleEmitterPopcorn initAnimsVisStates(List<Animation> anims) {
//		System.out.println(name);
		Map<String, String> visGuid = new HashMap<>();
		for (String vg : animVisibilityGuide.split(",")) {
			String[] anSt = vg.toLowerCase().split("=");
			if (anSt.length == 2) {
//				System.out.println(vg + ", splitted: " + Arrays.toString(anSt));
				visGuid.put(anSt[0].strip(), anSt[1].strip());
			}
		}
		if (visGuid.containsKey("always")) {
			always = State.valueOf(visGuid.get("always"));
		}
		for (Animation animation : anims) {
			State state = State.none;
//			System.out.println(animation.getName());
			String[] animName = animation.getName().toLowerCase().split(" ");
			String namePart = "";
			for (String s : animName) {
				namePart += s;
				if (visGuid.containsKey(namePart)) {
//					System.out.println("name matched!");
					state = State.valueOf(visGuid.get(namePart));
				}
				namePart += " ";

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
