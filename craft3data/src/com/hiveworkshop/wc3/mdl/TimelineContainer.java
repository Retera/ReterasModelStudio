package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxAnimatedObject;
import com.etheller.warsmash.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

public abstract class TimelineContainer implements VisibilitySource {
	public List<AnimFlag> animFlags = new ArrayList<>();
	protected List<String> flags = new ArrayList<>();

	public void loadTimelines(final MdlxAnimatedObject object) {
		for (final MdlxTimeline<?> timeline : object.timelines) {
			add(new AnimFlag(timeline));
		}
	}

	public void timelinesToMdlx(MdlxAnimatedObject object) {
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

	public void setAnimFlags(final List<AnimFlag> timelines) {
		this.animFlags = timelines;
	}

	public AnimFlag find(final String name, final Integer globalSeq) {
		// TODO make flags be a map and remove this method, this is 2018
		// not 2012 anymore, and I learned basic software dev
		for (final AnimFlag timeline : animFlags) {
			if (timeline.getName().equals(name) && (((globalSeq == null) && (timeline.globalSeq == null))
					|| ((globalSeq != null) && globalSeq.equals(timeline.globalSeq)))) {
				return timeline;
			}
		}
		return null;
	}

	public AnimFlag find(final String name) {
		// TODO make flags be a map and remove this method, this is 2018
		// not 2012 anymore, and I learned basic software dev
		for (final AnimFlag timeline : animFlags) {
			if (timeline.getName().equals(name)) {
				return timeline;
			}
		}
		return null;
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

	public void setFlags(final List<String> newFlags) {
		flags = newFlags;
	}
}
