package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.AnimatedObject;
import com.etheller.warsmash.parsers.mdlx.timeline.Timeline;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

public abstract class TimelineContainer implements VisibilitySource {
	public List<AnimFlag> animFlags = new ArrayList<>();
	protected List<String> flags = new ArrayList<>();

	public void loadTimelines(final AnimatedObject object) {
		for (final Timeline<?> timeline : object.timelines) {
			add(new AnimFlag(timeline));
		}
	}

	public void timelinesToMdlx(AnimatedObject object) {
		for (final AnimFlag timeline : animFlags) {
			object.timelines.add(timeline.toMdlx());
		}
	}

	public void add(final AnimFlag timeline) {
		animFlags.add(timeline);
	}

	public void remove(final AnimFlag timeline) {
		animFlags.remove(timeline);
	}

	public AnimFlag get(final int i) {
		return animFlags.get(i);
	}

	public List<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> timelines) {
		this.animFlags = timelines;
	}

	public int getInterpolatedInteger(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final int defaultValue) {
		final AnimFlag timeline = AnimFlag.find(animFlags, tag);

		if (timeline != null) {
			//return (int)timeline.interpolateAt(animatedRenderEnvironment);
			return ((Integer)timeline.interpolateAt(animatedRenderEnvironment)).intValue();
		}
		
		return defaultValue;
	}

	public float getInterpolatedFloat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final float defaultValue) {
		final AnimFlag timeline = AnimFlag.find(animFlags, tag);

		if (timeline != null) {
			return ((Double)timeline.interpolateAt(animatedRenderEnvironment)).floatValue();
		}
		
		return defaultValue;
	}

	public Vertex getInterpolatedVector(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final Vertex defaultValue) {
		final AnimFlag timeline = AnimFlag.find(animFlags, tag);

		if (timeline != null) {
			return (Vertex)timeline.interpolateAt(animatedRenderEnvironment);
		}
		
		return defaultValue;
	}

	public QuaternionRotation getInterpolatedQuat(final AnimatedRenderEnvironment animatedRenderEnvironment, final String tag, final QuaternionRotation defaultValue) {
		final AnimFlag timeline = AnimFlag.find(animFlags, tag);

		if (timeline != null) {
			return (QuaternionRotation)timeline.interpolateAt(animatedRenderEnvironment);
		}
		
		return defaultValue;
	}

	public void flipOver(final byte axis) {
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag flag = animFlags.get(i);
			flag.flipOver(axis);
		}
	}

	// VisibilitySource methods
	@Override
	public void setVisibilityFlag(final AnimFlag flag) {
		for (final AnimFlag timeline : animFlags) {
			String name = timeline.getName();

			if (name.equals("Visibility") || name.equals("Alpha")) {
				animFlags.remove(timeline);
			}
		}

		if (flag != null) {
			animFlags.add(flag);
		}
	}

	@Override
	public AnimFlag getVisibilityFlag() {
		for (final AnimFlag timeline : animFlags) {
			String name = timeline.getName();

			if (name.equals("Visibility") || name.equals("Alpha")) {
				return timeline;
			}
		}

		return null;
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

	public void add(final String flag) {
		flags.add(flag);
	}

	public List<String> getFlags() {
		return flags;
	}

	public void setFlags(final ArrayList<String> newFlags) {
		flags = newFlags;
	}
}
