package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.TreeMap;

public class ReplaceSequenceTransformations<T> implements UndoAction {
	//	private final Sequence sourceSequence;
	private final Sequence targetSequence;
	private final TreeMap<Integer, Entry<T>> newEntryMap;
	private final TreeMap<Integer, Entry<T>> oldEntryMap;
	private final AnimFlag<T> targetAnimFlag;
	private final ModelStructureChangeListener changeListener;

	public ReplaceSequenceTransformations(AnimFlag<T> sourceAnimFlag, AnimFlag<T> targetAnimFlag, Sequence sourceSequence, Sequence targetSequence, ModelStructureChangeListener changeListener) {
		this.targetAnimFlag = targetAnimFlag;
//		this.sourceSequence = sourceSequence;
		this.targetSequence = targetSequence;
		this.changeListener = changeListener;

		oldEntryMap = targetAnimFlag.getEntryMap(targetSequence);
		if (sourceAnimFlag.getEntryMap(sourceSequence) != null) {
			newEntryMap = getEntryMap(sourceAnimFlag, sourceSequence, targetAnimFlag.tans());
			if (sourceSequence.getLength() != targetSequence.getLength()) {
				double ratio = ((double) targetSequence.getLength()) / ((double) sourceSequence.getLength());
				scaleMapEntries(ratio, newEntryMap);
			}
		} else {
			newEntryMap = null;
		}

	}

	private TreeMap<Integer, Entry<T>> getEntryMap(AnimFlag<T> sourceAnimFlag, Sequence sequence, boolean targetIsTang) {
		if (!targetIsTang && sourceAnimFlag.tans()) {
			TreeMap<Integer, Entry<T>> newEntryMap = sourceAnimFlag.getSequenceEntryMapCopy(sequence);
			for (Entry<T> entry : newEntryMap.values()) {
				entry.linearize();
			}
			return newEntryMap;
		} else if (targetIsTang && !sourceAnimFlag.tans()) {
			AnimFlag<T> tempAnimFlag = sourceAnimFlag.deepCopy();
			tempAnimFlag.unLinearize2();
			return tempAnimFlag.getEntryMap(sequence);
		}
		return sourceAnimFlag.getSequenceEntryMapCopy(sequence);
	}

//	private void checkAndFixTangential(AnimFlag<T> sourceAnimFlag, AnimFlag<T> targetAnimFlag) {
//		if (!targetAnimFlag.tans() && sourceAnimFlag.tans()) {
//			for(Entry<T> entry : newEntryMap.values()){
//				entry.linearize();
//			}
//		} else if (targetAnimFlag.tans() && !sourceAnimFlag.tans()) {
//			for(Entry<T> entry : newEntryMap.values()){
//				entry.unLinearize();
//			}
//		}
//	}

	private void scaleMapEntries(double ratio, TreeMap<Integer, Entry<T>> entryMap) {
		TreeMap<Integer, Entry<T>> scaledMap = new TreeMap<>();
		for (Integer time : entryMap.keySet()) {
			int newTime = (int) (time * ratio);
			scaledMap.put(newTime, entryMap.get(time).setTime(newTime));
		}
		entryMap.clear();
		entryMap.putAll(scaledMap);
	}

	@Override
	public UndoAction undo() {
		if (oldEntryMap == null) {
			targetAnimFlag.deleteAnim(targetSequence);
		} else {
			targetAnimFlag.setEntryMap(targetSequence, oldEntryMap);
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if (newEntryMap == null) {
			targetAnimFlag.deleteAnim(targetSequence);
		} else {
			targetAnimFlag.setEntryMap(targetSequence, newEntryMap);
		}
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "";
	}
}
