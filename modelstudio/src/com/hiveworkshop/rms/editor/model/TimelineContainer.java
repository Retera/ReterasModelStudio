package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxAnimatedObject;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public abstract class TimelineContainer implements VisibilitySource {
	protected Map<String, AnimFlag<?>> animFlags = new HashMap<>();

	public void loadTimelines(MdlxAnimatedObject object, EditableModel model) {
		for (MdlxTimeline<?> timeline : object.timelines) {
			add(AnimFlag.createFromTimeline(timeline, model));
		}
	}

	public void timelinesToMdlx(MdlxAnimatedObject mdlxObject, EditableModel model) {
		for (AnimFlag<?> timeline : animFlags.values()) {
			if (!timeline.getAnimMap().isEmpty()) {
				mdlxObject.timelines.add(timeline.toMdlx(this, model));
			}
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

	public void copyTimelines(TimelineContainer other) {
		for (AnimFlag<?> timeline : other.getAnimFlags()) {
			add(timeline.deepCopy());
		}
	}

	public boolean has(String name) {
		return animFlags.containsKey(name);
	}
	public boolean owns(AnimFlag<?> animFlag) {
		return animFlags.containsValue(animFlag);
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

	public AnimFlag<?> find(String name, GlobalSeq globalSeq) {
		AnimFlag<?> timeline = animFlags.get(name);

		if (timeline != null &&
				(globalSeq == null && timeline.getGlobalSeq() == null
						|| globalSeq == timeline.getGlobalSeq())) {
			return timeline;
		}

		return null;
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
		return MdlUtils.TOKEN_VISIBILITY;
	}

	public VisibilitySource getVisibilitySource() {
		if (getVisibilityFlag() != null) {
			return this;
		}
		return null;
	}

	public AnimFlag<Vec3> getTranslationFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_TRANSLATION, globalSeq);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getScalingFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_SCALING, globalSeq);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Quat> getRotationFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_ROTATION, globalSeq);

		if (timeline instanceof QuatAnimFlag) {
			return (QuatAnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getTranslationFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_TRANSLATION);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getScalingFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_SCALING);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Quat> getRotationFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_ROTATION);

		if (timeline instanceof QuatAnimFlag) {
			return (QuatAnimFlag) timeline;
		}
		return null;
	}

	public Set<String> getFlagNameSet() {
		return animFlags.keySet();
	}
}
