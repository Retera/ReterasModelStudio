package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ChangeInterpTypeAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final AnimFlag<T> animFlag;
	private final InterpolationType newInterpType;
	private final InterpolationType oldInterpType;
	private final Map<Sequence, TreeMap<Integer, Pair<T, T>>> oldTanMap;


	public ChangeInterpTypeAction(AnimFlag<T> animFlag, InterpolationType newInterpType, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		this.newInterpType = newInterpType;
		oldInterpType = animFlag.getInterpolationType();
		oldTanMap = new HashMap<>();
		if (oldInterpType.tangential()) {
			for (Sequence sequence : animFlag.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
				if (entryMap != null) {
					TreeMap<Integer, Pair<T, T>> tanMap = oldTanMap.computeIfAbsent(sequence, k -> new TreeMap<>());
					for (Integer time : entryMap.keySet()) {
						Entry<T> entry = entryMap.get(time);
						tanMap.put(time, new Pair<>(entry.inTan, entry.outTan));
					}
				}
			}
		}
	}

	@Override
	public ChangeInterpTypeAction<T> undo() {
		animFlag.setInterpType(oldInterpType);
		if (oldInterpType.tangential()) {
			for (Sequence sequence : oldTanMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
				TreeMap<Integer, Pair<T, T>> tanMap = oldTanMap.get(sequence);
				for (Integer time : tanMap.keySet()) {
					Pair<T, T> ttPair = tanMap.get(time);
					entryMap.get(time).setInTan(ttPair.getFirst()).setOutTan(ttPair.getSecond());
				}
			}
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public ChangeInterpTypeAction<T> redo() {
		animFlag.setInterpType(newInterpType);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Interpolation Type";
	}
}
