package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxAnimatedObject;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class TimelineContainer implements VisibilitySource {
	public Map<String, AnimFlag<?>> animFlags = new HashMap<>();

	public void loadTimelines(final MdlxAnimatedObject object) {
		for (final MdlxTimeline<?> timeline : object.timelines) {
			add(AnimFlag.createFromTimeline(timeline));
		}
	}

	public void timelinesToMdlx(final MdlxAnimatedObject object) {
		for (final AnimFlag<?> timeline : animFlags.values()) {
			object.timelines.add(timeline.toMdlx(this));
		}
	}

	public void add(final AnimFlag<?> timeline) {
		animFlags.put(timeline.getName(), timeline);
	}

	public void addAll(final Collection<AnimFlag<?>> timelines) {
		for (final AnimFlag<?> timeline : timelines) {
			add(timeline);
		}
	}

	public boolean has(final String name) {
		return animFlags.containsKey(name);
	}

	public void remove(final AnimFlag<?> timeline) {
		animFlags.remove(timeline.getName());
	}

	public void remove(final String name) {
		final AnimFlag<?> timeline = animFlags.get(name);

		if (timeline != null) {
			animFlags.remove(name);
		}
	}

	public ArrayList<AnimFlag<?>> getAnimFlags() {
		return new ArrayList<>(animFlags.values());
	}

	public void setAnimFlags(final Collection<AnimFlag<?>> timelines) {
		animFlags.clear();

		for (final AnimFlag<?> timeline : timelines) {
			add(timeline);
		}
	}

	public AnimFlag<?> find(final String name) {
		return animFlags.get(name);
	}

	public AnimFlag<?> find(final String name, final Integer globalSeq) {
		final AnimFlag<?> timeline = animFlags.get(name);

		if (timeline != null && (((globalSeq == null) && (timeline.getGlobalSeq() == null))
				|| ((globalSeq != null) && globalSeq.equals(timeline.getGlobalSeq())))) {
			return timeline;
		}

		return null;
	}

	public void removeAllTimelinesForGlobalSeq(final Integer selectedValue) {
		for (final AnimFlag<?> timeline : animFlags.values()) {
			if (selectedValue.equals(timeline.getGlobalSeq())) {
				remove(timeline);
			}
		}
	}

	public int getInterpolatedInteger(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final int defaultValue) {
		final IntAnimFlag timeline = (IntAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public float getInterpolatedFloat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final float defaultValue) {
		final FloatAnimFlag timeline = (FloatAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public Vec3 getInterpolatedVector(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final Vec3 defaultValue) {
		final Vec3AnimFlag timeline = (Vec3AnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public Quat getInterpolatedQuat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final Quat defaultValue) {
		final QuatAnimFlag timeline = (QuatAnimFlag) find(tag);

		if (timeline != null) {
			return timeline.interpolateAt(animatedRenderEnvironment);
		}

		return defaultValue;
	}

	public void flipOver(final byte axis) {
		for (final AnimFlag<?> timeline : animFlags.values()) {
			timeline.flipOver(axis);
		}
	}

	@Override
	public AnimFlag<?> getVisibilityFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_VISIBILITY);

		if (timeline == null) {
			timeline = find(MdlUtils.TOKEN_ALPHA);
		}

		return timeline;
	}

	// VisibilitySource methods
	@Override
	public void setVisibilityFlag(final AnimFlag<?> flag) {
		remove(MdlUtils.TOKEN_VISIBILITY);
		remove(MdlUtils.TOKEN_ALPHA);

		if (flag != null) {
			add(flag);
		}
	}

	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, 1);
	}

	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment, final float defaultValue) {
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
