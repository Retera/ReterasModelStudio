package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;

import java.util.ArrayList;
import java.util.List;


public class TimelineKeyNamer {

	List<AnimationMarker> animationMarkers;

	public TimelineKeyNamer(EditableModel model) {
		animationMarkers = new ArrayList<>();
		for (final Animation item : model.getAnims()) {
			animationMarkers.add(new AnimationMarker(item.getStart(), item.getEnd(), item.getName()));
//			System.out.println(item.getEnd());
//			System.out.println(item.getStart());
//			System.out.println(item.getName());
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
		return animationMarkers.stream().filter(am -> am.contains(time)).findAny().orElse(null);
	}

	class AnimationMarker {
		int start;
		int end;
		String name;

		AnimationMarker(int start, int end, String name) {
			this.start = start;
			this.end = end;
			this.name = name;
		}

		boolean contains(int time) {
			return start <= time && time <= end;
		}

		boolean isEndPoint(int time) {
			return start == time || time == end;
		}

	}
}
