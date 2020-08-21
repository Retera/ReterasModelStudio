package com.hiveworkshop.wc3.mdl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.etheller.warsmash.parsers.mdlx.MdlxAnimatedObject;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxTimeline;
import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

public abstract class TimelineContainer implements VisibilitySource {
	public Map<String, AnimFlag> animFlags = new HashMap<>();

	public void loadTimelines(final MdlxAnimatedObject object) {
		for (final MdlxTimeline<?> timeline : object.timelines) {
			add(new AnimFlag(timeline));
		}
	}

	public void timelinesToMdlx(MdlxAnimatedObject object) {
		for (final AnimFlag timeline : animFlags.values()) {
			object.timelines.add(timeline.toMdlx(this));
		}
	}

	public void add(final AnimFlag timeline) {
		animFlags.put(timeline.getName(), timeline);
	}

	public void addAll(final Collection<AnimFlag> timelines) {
		for (final AnimFlag timeline : timelines) {
			add(timeline);
		}
	}

	public boolean has(final String name) {
		return animFlags.containsKey(name);
	}

	public void remove(final AnimFlag timeline) {
		animFlags.remove(timeline.getName());
	}

	public void remove(final String name) {
		AnimFlag timeline = animFlags.get(name);

		if (timeline != null) {
			animFlags.remove(name);
		}
	}

	public Collection<AnimFlag> getAnimFlags() {
		return animFlags.values();
	}

	public void setAnimFlags(final Collection<AnimFlag> timelines) {
		animFlags.clear();

		for (final AnimFlag timeline : timelines) {
			add(timeline);
		}
	}

	public AnimFlag find(final String name) {
		return animFlags.get(name);
	}

	public AnimFlag find(final String name, final Integer globalSeq) {
		AnimFlag timeline = animFlags.get(name);

		if (timeline != null && (((globalSeq == null) && (timeline.globalSeq == null))
				|| ((globalSeq != null) && globalSeq.equals(timeline.globalSeq)))) {
			return timeline;
		}

		return null;
	}

	public void removeAllTimelinesForGlobalSeq(final Integer selectedValue) {
		for (final AnimFlag timeline : animFlags.values()) {
			if (selectedValue.equals(timeline.getGlobalSeq())) {
				remove(timeline);
			}
		}
	}

	public int getInterpolatedInteger(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final int defaultValue) {
		final AnimFlag timeline = find(tag);

		if (timeline != null) {
			return ((Integer)timeline.interpolateAt(animatedRenderEnvironment)).intValue();
		}
		
		return defaultValue;
	}

	public float getInterpolatedFloat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final float defaultValue) {
		final AnimFlag timeline = find(tag);

		if (timeline != null) {
			return ((Double)timeline.interpolateAt(animatedRenderEnvironment)).floatValue();
		}
		
		return defaultValue;
	}

	public Vertex getInterpolatedVector(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final Vertex defaultValue) {
		final AnimFlag timeline = find(tag);

		if (timeline != null) {
			return (Vertex)timeline.interpolateAt(animatedRenderEnvironment);
		}
		
		return defaultValue;
	}

	public QuaternionRotation getInterpolatedQuat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final QuaternionRotation defaultValue) {
		final AnimFlag timeline = find(tag);

		if (timeline != null) {
			return (QuaternionRotation)timeline.interpolateAt(animatedRenderEnvironment);
		}
		
		return defaultValue;
	}

	public void flipOver(final byte axis) {
		for (final AnimFlag timeline : animFlags.values()) {
			timeline.flipOver(axis);
		}
	}

	// VisibilitySource methods
	public void setVisibilityFlag(final AnimFlag flag) {
		remove(MdlUtils.TOKEN_VISIBILITY);
		remove(MdlUtils.TOKEN_ALPHA);

		if (flag != null) {
			add(flag);
		}
	}

	public AnimFlag getVisibilityFlag() {
		AnimFlag timeline = find(MdlUtils.TOKEN_VISIBILITY);
		
		if (timeline == null) {
			timeline = find(MdlUtils.TOKEN_ALPHA);
		}

		return timeline;
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
}
