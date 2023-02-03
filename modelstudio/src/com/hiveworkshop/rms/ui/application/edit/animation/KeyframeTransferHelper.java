package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;

import java.util.*;

public class KeyframeTransferHelper {
	private final List<KeyFrameWrapper<?>> copiedKeyframes = new ArrayList<>();
	private final Map<TimelineContainer, Map<AnimFlag<?>, KeyFrameWrapper<?>>> objectToKeyframes = new HashMap<>();
	private final Map<AnimFlag<?>, KeyFrameWrapper<?>> flagToKeyframes = new HashMap<>();
	boolean useAllCopiedKeyframes;

	public KeyframeTransferHelper clear() {
		copiedKeyframes.clear();
		objectToKeyframes.clear();
		flagToKeyframes.clear();
		return this;
	}

	public KeyframeTransferHelper setUseAll(boolean useAll) {
		this.useAllCopiedKeyframes = useAll;
		return this;
	}

	public boolean isUseAll() {
		return useAllCopiedKeyframes;
	}

	public KeyFrameWrapper<?> getKFW(AnimFlag<?> flag){
		return flagToKeyframes.get(flag);
	}

	public Map<AnimFlag<?>, KeyFrameWrapper<?>> getFlagToKeyframes() {
		return flagToKeyframes;
	}

	public Collection<KeyFrameWrapper<?>> getWrappedKeyframes() {
		return flagToKeyframes.values();
	}

	public KeyframeTransferHelper collectKFs(Collection<? extends TimelineContainer> objects, Sequence sequence, int time){

		for (TimelineContainer object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				collectKF(sequence, time, object, flag);
			}
		}
		return this;
	}

	public KeyframeTransferHelper collectKFs(Sequence sequence, int time, TimelineContainer... objects){
		for (TimelineContainer object : objects) {
			for (AnimFlag<?> flag : object.getAnimFlags()) {
				collectKF(sequence, time, object, flag);
			}
		}
		return this;
	}

	public KeyframeTransferHelper collectKF(Sequence sequence, int time, TimelineContainer object, AnimFlag<?> flag) {
		if (isCorrectSeq(flag, sequence) && flag.hasEntryAt(sequence, time)) {
			KeyFrameWrapper<?> wrappedKeyframe = getWrappedKeyframe(object, flag, sequence, time);
			copiedKeyframes.add(wrappedKeyframe);
			objectToKeyframes.computeIfAbsent(object, k-> new HashMap<>()).put(flag, wrappedKeyframe);
			flagToKeyframes.put(flag, wrappedKeyframe);
		}
		return this;
	}

	private <Q> KeyFrameWrapper<Q> getWrappedKeyframe(TimelineContainer object, AnimFlag<Q> flag, Sequence sequence, int trackTime) {
		if (flag.hasEntryAt(sequence, trackTime)) {
			return new KeyFrameWrapper<>(object, flag, flag.getEntryAt(sequence, trackTime).deepCopy(), true);
		} else {
			Entry<Q> entry = new Entry<>(trackTime, flag.interpolateAt(sequence, trackTime));

			if (flag.tans()) {
				Entry<Q> entryIn = flag.getFloorEntry(trackTime, sequence);
				Entry<Q> entryOut = flag.getCeilEntry(trackTime, sequence);
				int animationLength = sequence.getLength();
//				float factor = getTimeFactor(trackTime, animationLength, entryIn.time, entryOut.time);
				float[] tcb = AnimFlagUtils.calculateTCB(flag, sequence, trackTime);
				if(tcb == null){
					tcb = new float[]{0, .5f, 0};
				}
				float[] tbcFactor = flag.getTbcFactor(tcb[2], tcb[0], tcb[1]);
				flag.calcNewTans(tbcFactor, entryOut, entryIn, entry, animationLength);
				System.out.println("calc tans! " + entryIn + entryOut + entry);
			}
			return new KeyFrameWrapper<>(object, flag, entry, false);
		}
	}


	private boolean isCorrectSeq(AnimFlag<?> flag, Sequence sequence) {
		return Objects.equals(flag.getGlobalSeq(), sequence) || flag.getGlobalSeq() == null && !(sequence instanceof GlobalSeq);
	}

}
