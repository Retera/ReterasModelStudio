package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxAnimatedObject;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class TimelineContainer implements VisibilitySource {
	public Map<String, AnimFlag<?>> animFlags = new HashMap<>();

	public void loadTimelines(MdlxAnimatedObject object) {
		for (MdlxTimeline<?> timeline : object.timelines) {
			add(AnimFlag.createFromTimeline(timeline));
		}
	}

	public void timelinesToMdlx(MdlxAnimatedObject mdlxObject) {
		for (AnimFlag<?> timeline : animFlags.values()) {
			mdlxObject.timelines.add(timeline.toMdlx(this));
		}
	}

	public void add(AnimFlag<?> timeline) {
		animFlags.put(timeline.getName(), timeline);
	}

	public void addAll(Collection<AnimFlag<?>> timelines) {
		for (AnimFlag<?> timeline : timelines) {
			add(timeline);
		}
	}

	public boolean has(String name) {
		return animFlags.containsKey(name);
	}

	public void remove(AnimFlag<?> timeline) {
		animFlags.remove(timeline.getName());
	}

	public void remove(String name) {
		AnimFlag<?> timeline = animFlags.get(name);

		if (timeline != null) {
			animFlags.remove(name);
		}
	}

	public ArrayList<AnimFlag<?>> getAnimFlags() {
		return new ArrayList<>(animFlags.values());
	}

	public void setAnimFlags(Collection<AnimFlag<?>> timelines) {
		animFlags.clear();

		for (AnimFlag<?> timeline : timelines) {
			add(timeline);
		}
	}

	public AnimFlag<?> find(String name) {
		return animFlags.get(name);
	}

	public AnimFlag<?> find(String name, Integer globalSeq) {
		AnimFlag<?> timeline = animFlags.get(name);

		if (timeline != null && (((globalSeq == null) && (timeline.getGlobalSeqLength() == null))
				|| ((globalSeq != null) && globalSeq.equals(timeline.getGlobalSeqLength())))) {
			return timeline;
		}

		return null;
	}

	public void removeAllTimelinesForGlobalSeq(Integer selectedValue) {
		for (AnimFlag<?> timeline : animFlags.values()) {
			if (selectedValue.equals(timeline.getGlobalSeqLength())) {
				remove(timeline);
			}
		}
	}

	public int getInterpolatedInteger(TimeEnvironmentImpl animatedRenderEnvironment, String tag, int defaultValue) {
		IntAnimFlag timeline = (IntAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public float getInterpolatedFloat(TimeEnvironmentImpl animatedRenderEnvironment, String tag, float defaultValue) {
		FloatAnimFlag timeline = (FloatAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public Vec3 getInterpolatedVector(TimeEnvironmentImpl animatedRenderEnvironment, String tag, Vec3 defaultValue) {
		Vec3AnimFlag timeline = (Vec3AnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public Quat getInterpolatedQuat(TimeEnvironmentImpl animatedRenderEnvironment, String tag, Quat defaultValue) {
		QuatAnimFlag timeline = (QuatAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public void flipOver(byte axis) {
		for (AnimFlag<?> timeline : animFlags.values()) {
			timeline.flipOver(axis);
		}
	}

	@Override
	public AnimFlag<Float> getVisibilityFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_VISIBILITY);

		if (timeline == null) {
			timeline = find(MdlUtils.TOKEN_ALPHA);
		}

		if(timeline instanceof FloatAnimFlag){
			return (FloatAnimFlag) timeline;
		}
		return null;
	}

	// VisibilitySource methods
	@Override
	public void setVisibilityFlag(AnimFlag<Float> flag) {
		remove(MdlUtils.TOKEN_VISIBILITY);
		remove(MdlUtils.TOKEN_ALPHA);

		if (flag != null) {
			add(flag);
		}
	}

	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, 1);
	}

	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment, float defaultValue) {
		return getInterpolatedFloat(animatedRenderEnvironment, visFlagName(), defaultValue);
	}

	@Override
	public String visFlagName() {
		return "Visibility";
	}

	public VisibilitySource getVisibilitySource() {
		if (getVisibilityFlag() != null) {
			return this;
		}
		return null;
	}
}
