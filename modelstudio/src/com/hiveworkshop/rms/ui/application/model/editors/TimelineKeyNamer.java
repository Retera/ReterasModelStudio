package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class TimelineKeyNamer {

	TreeMap<Integer, Animation> animationTreeMap = new TreeMap<>();
	TreeMap<Integer, AnimationMarker> animMap = new TreeMap<>();
	List<AnimationMarker> animationMarkers = new ArrayList<>();
	EditableModel model;

	public TimelineKeyNamer(EditableModel model) {
		this.model = model;
		update();
	}

	public void update() {
		animationMarkers.clear();
		for (final Animation item : model.getAnims()) {
			AnimationMarker marker = new AnimationMarker(item);
			animationMarkers.add(marker);
			animationTreeMap.put(item.getStart(), item);
			animMap.put(marker.start, marker);
			animMap.put(marker.end, marker);
		}
	}

	public String getAnimationName(int time) {
		AnimationMarker animationMarker = animationMarkers.stream().filter(am -> am.contains(time)).findAny().orElse(null);
		if (animationMarker != null) {
			return animationMarker.name;
		}
		return "";
	}

	public AnimationMarker getAnimationMarker(int time) {
//		return animationMarkers.stream().filter(am -> am.contains(time)).findAny().orElse(null);
		if (animMap.get(time) == null || animMap.get(time).contains(time)) {
			return animMap.get(time);
		}
		return null;
//		return animationMarkers.stream().filter(am -> am.contains(time)).findAny().orElse(null);
	}

	public Animation getAnimation(int time) {
		Integer floorKey = animationTreeMap.floorKey(time);
		if (floorKey != null) {
			Animation animation = animationTreeMap.get(floorKey);
			if (animation.getStart() <= time && time <= animation.getEnd()) {
				return animation;
			}
		}
		Integer ceilingKey = animationTreeMap.ceilingKey(time);
		if (ceilingKey != null) {
			Animation animation = animationTreeMap.get(ceilingKey);
			if (animation.getStart() <= time && time <= animation.getEnd()) {
				return animation;
			}
		}
		return null;
	}

	static class AnimationMarker {
		Animation animation;
		String name;
		int start;
		int end;


		AnimationMarker(Animation animation) {
			this.animation = animation;
			update(animation);
		}

		private void update(Animation animation) {
			this.name = animation.getName();
			this.start = animation.getStart();
			this.end = animation.getEnd();
		}

		boolean contains(int time) {
			return animation.getStart() <= time && time <= animation.getEnd();
		}

		boolean isEndPoint(int time) {
			return animation.getStart() == time || time == animation.getEnd();
		}

		@Override
		public String toString() {
			return animation.getName() + " (" + animation.getStart() + "-" + animation.getEnd() + ")";
		}
	}
}
