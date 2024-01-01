package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
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

	private static <Q> AddFlagEntryAction<Q> getAddEntryAction1(AnimFlag<Q> timeline, Sequence sequence, int trackTime) {
		Entry<Q> entry = getEntry(timeline, getValueInstanceFrom(timeline, sequence, trackTime), trackTime, sequence);
		return new AddFlagEntryAction<>(timeline, entry, sequence, null);
	}

	private static <Q> SetFlagEntryAction<Q> getAddEntryAction(AnimFlag<Q> timeline, Sequence sequence, int trackTime) {
//		Entry<Q> entry = getEntry(timeline, getValueInstanceFrom(timeline, sequence, trackTime), trackTime, sequence);
		List<Entry<Q>> entries = getEntries(timeline, getValueInstanceFrom(timeline, sequence, trackTime), trackTime, sequence);
		return new SetFlagEntryAction<>(timeline, entries, sequence, null);
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

			float[] tcbFactor = timeline.getTcbFactor(0, 0, 0.5f);
			timeline.calcNewTans(tcbFactor, entryOut, entryIn, entry, animationLength);
		}
		return entry;
	}


	public static <T> List<Entry<T>> getEntries(AnimFlag<T> timeline, T value, int time, Sequence sequence) {
		List<Entry<T>> list = new ArrayList<>();
		Entry<T> entry = new Entry<>(time, value);
		list.add(entry);
		if (sequence != null && timeline.tans()) {
			Entry<T> prevValue = timeline.getFloorEntry(time-1, sequence).deepCopy();
			Entry<T> nextValue = timeline.getCeilEntry(time+1, sequence).deepCopy();
			float timeBetweenFrames = nextValue.getTime() - prevValue.getTime();

			float timeFraction = (time-prevValue.getTime()) / timeBetweenFrames;

			T pValue = AnimFlagUtils.getDiffValue(value, timeline.interpolateAt(sequence, time - 1));
			T nValue = AnimFlagUtils.getDiffValue(timeline.interpolateAt(sequence, time + 1), value);

			float inTanAdj = time - prevValue.getTime();
			float outTanAdj = nextValue.getTime() - time;

			entry.setInTan(AnimFlagUtils.getScaledValue(pValue, inTanAdj)).setOutTan(AnimFlagUtils.getScaledValue(nValue, outTanAdj));
			timeline.addEntry(entry, sequence);

			prevValue.setOutTan(AnimFlagUtils.getScaledValue(prevValue.getOutTan(), timeFraction));
			nextValue.setInTan(AnimFlagUtils.getScaledValue(nextValue.getInTan(), (1-timeFraction)));

			list.add(prevValue);
			list.add(nextValue);
		}
		return list;
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
