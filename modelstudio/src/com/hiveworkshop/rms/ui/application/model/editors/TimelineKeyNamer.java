package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;

import java.awt.*;
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
		boolean odd = false;
		for (final Animation item : model.getAnims()) {
			AnimationMarker marker = new AnimationMarker(item, odd);
			animationMarkers.add(marker);
			animationTreeMap.put(item.getStart(), item);
			animMap.put(marker.start, marker);
			animMap.put(marker.end, marker);
			odd = !odd;
		}
	}

	public AnimationMarker getAnimationMarker(int time) {
//		return animationMarkers.stream().filter(am -> am.contains(time)).findAny().orElse(null);
		Integer ceilingKey = animMap.ceilingKey(time);
		if (ceilingKey != null && animMap.get(ceilingKey) != null && animMap.get(ceilingKey).contains(time)) {
			return animMap.get(ceilingKey);
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
		protected static final Color START_END_ODD = new Color(120, 240, 200);
		protected static final Color MID_ODD = new Color(200, 250, 230);
		protected static final Color START_END_EVEN = new Color(170, 250, 130);
		protected static final Color MID_EVEN = new Color(220, 250, 190);
		Animation animation;
		String name;
		int start;
		int end;
		boolean odd;


		AnimationMarker(Animation animation, boolean odd) {
			this.animation = animation;
			this.odd = odd;
			update();
		}

		private void update(Animation animation) {
			this.name = animation.getName();
			this.start = animation.getStart();
			this.end = animation.getEnd();
		}
		private void update() {
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

		Color getColor(int time){
			if(isEndPoint(time)){
				return odd ? START_END_ODD : START_END_EVEN;
			} else if (contains(time)){
				return odd ? MID_ODD : MID_EVEN;
			}
			return Color.LIGHT_GRAY;
		}

		@Override
		public String toString() {
			return animation.getName() + " (" + animation.getStart() + "-" + animation.getEnd() + ")";
		}
	}
}
