package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeyframeActionHelpers {

	public static CompoundAction getRotSetupAction(Collection<IdObject> selection, Collection<CameraNode> camSelection, Sequence sequence, int trackTime) {
		List<UndoAction> actions = new ArrayList<>();
		if (sequence != null) {
			for (IdObject node : selection) {
				UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_ROTATION, new Quat());
				if(setupAction != null){
					actions.add(setupAction);
				}
			}
			for (CameraNode node : camSelection) {
				if (node instanceof CameraNode.SourceNode){
					UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_ROTATION, 0f);
					if(setupAction != null){
						actions.add(setupAction);
					}
				}
			}
		}

		return new CompoundAction("setup", actions, ModelStructureChangeListener.changeListener::keyframesUpdated);
	}

	public static CompoundAction getTranslSetupAction(Collection<IdObject> selection, Collection<CameraNode> camSelection, Sequence sequence, int trackTime) {
		List<UndoAction> actions = new ArrayList<>();
		if (sequence != null) {
			for (IdObject node : selection) {
				UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_TRANSLATION, new Vec3());
				if(setupAction != null){
					actions.add(setupAction);
				}
			}
			for (CameraNode node : camSelection) {
				if (node instanceof CameraNode.SourceNode){
					UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_TRANSLATION, new Vec3());
					if(setupAction != null){
						actions.add(setupAction);
					}
				}
			}
		}

		return new CompoundAction("setup", actions, ModelStructureChangeListener.changeListener::keyframesUpdated);
	}

	public static CompoundAction getScaleSetupAction(Collection<IdObject> selection, Collection<CameraNode> camSelection, Sequence sequence, int trackTime) {
		List<UndoAction> actions = new ArrayList<>();
		if (sequence != null) {
			for (IdObject node : selection) {
				UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_TRANSLATION, new Vec3(1,1,1));
				if(setupAction != null){
					actions.add(setupAction);
				}
			}
//			for (CameraNode node : camSelection) {
//				if (node instanceof CameraNode.SourceNode){
//					UndoAction setupAction = getSetupAction(sequence, trackTime, node, MdlUtils.TOKEN_TRANSLATION, new Vec3());
//					if(setupAction != null){
//						actions.add(setupAction);
//					}
//				}
//			}
		}

		return new CompoundAction("setup", actions, ModelStructureChangeListener.changeListener::keyframesUpdated);
	}


	public static <Q> UndoAction getSetupAction(Sequence sequence, int trackTime, AnimatedNode node, String name, Q defaultValue) {
		AnimFlag<?> timeline = node.find(name);
		if (timeline == null) {
			return getNewPreparedAnimFlag(sequence, trackTime, node, name, defaultValue);
		}
		if (!timeline.hasEntryAt(sequence, trackTime) && isValidSequence(sequence, timeline)){
			return getAddEntryAction(timeline, sequence, trackTime);
		}
		return null;
	}
	private static <Q> UndoAction getNewPreparedAnimFlag(Sequence sequence, int trackTime, AnimatedNode node, String name, Q defaultValue) {
		AnimFlag<Q> timeline = AnimFlagUtils.createNewAnimFlag(defaultValue, name);
		if(timeline != null){
			Entry<Q> entry = getEntry(timeline, getValueInstanceFrom(timeline, sequence, trackTime), trackTime, sequence);
			timeline.addEntry(entry, sequence);
			return new AddTimelineAction<>(node, timeline);
		}
		return null;
	}

	private static <Q> AddFlagEntryAction<Q> getAddEntryAction(AnimFlag<Q> timeline, Sequence sequence, int trackTime) {
		Entry<Q> entry = getEntry(timeline, getValueInstanceFrom(timeline, sequence, trackTime), trackTime, sequence);
		return new AddFlagEntryAction<>(timeline, entry, sequence, null);
	}

	private static boolean isValidSequence(Sequence sequence, AnimFlag<?> rotationTimeline) {
		return !rotationTimeline.hasGlobalSeq() && sequence instanceof Animation
				|| rotationTimeline.getAnimMap().isEmpty()
				|| rotationTimeline.getGlobalSeq() == sequence;
	}

	public static <T> Entry<T> getEntry(AnimFlag<T> timeline, T value, int trackTime, Sequence sequence) {
		Entry<T> entry = new Entry<>(trackTime, value);
		if (sequence != null && timeline.tans()) {
			Entry<T> entryIn = timeline.getFloorEntry(trackTime, sequence);
			Entry<T> entryOut = timeline.getCeilEntry(trackTime, sequence);

			int animationLength = sequence.getLength();

			float[] tbcFactor = timeline.getTbcFactor(0, 0.5f, 0);
			timeline.calcNewTans(tbcFactor, entryOut, entryIn, entry, animationLength);
		}
		return entry;
	}





	public static <Q> void getNewOrCopiedTimelineWithKFs(AnimatedNode node, Collection<Sequence> sequences, String name, Q defaultValue, GlobalSeq globalSeq) {
		AnimFlag<?> timeline = getNewOrCopiedTimeline(node, name, defaultValue, globalSeq);
		if(timeline != null){
			ensureSequenceKFs(sequences, timeline);
		}
	}

	public static  <Q> AnimFlag<?> getNewOrCopiedTimeline(AnimatedNode node, String name, Q defaultValue, GlobalSeq globalSeq) {
		AnimFlag<?> timeline = node.find(name);
		if (timeline == null) {
			AnimFlag<Q> newAnimFlag = AnimFlagUtils.createNewAnimFlag(defaultValue, name);
			if(newAnimFlag != null){
				newAnimFlag.setGlobSeq(globalSeq);
			}
			return newAnimFlag;
		} else {
			return timeline.deepCopy();
		}
	}

	public static <T> void ensureSequenceKFs(Collection<Sequence> sequences, AnimFlag<T> timeline) {
		for (Sequence sequence : sequences){
			if(timeline.size(sequence) == 0
					&& (!timeline.hasGlobalSeq() && sequence instanceof Animation
					|| timeline.getGlobalSeq() == sequence)){
				Entry<T> entry = getEntry(timeline, getIdentityValue(timeline), 0, sequence);
				timeline.addEntry(entry, sequence);
			}
		}
	}

	public static <T> T getIdentityValue(AnimFlag<T> timeline) {
		T value = timeline.interpolateAt(null);
		if (value instanceof Vec3) {
			return (T) new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return (T) new Quat((Quat) value);
		}
		return value;
	}

	public static <T> T getValueInstanceFrom(AnimFlag<T> timeline, Sequence sequence, int trackTime) {
		T value = timeline.interpolateAt(sequence, trackTime);
		if (value instanceof Vec3) {
			return (T) new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return (T) new Quat((Quat) value);
		}
		return value;
	}
}
