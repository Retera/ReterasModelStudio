package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.TreeMap;

public class RoundFlagValuesAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final AnimFlag<T> animFlag;
	private final Float zeroFloor;
	private final ArrayList<SetFlagEntryMapAction<T>> actions = new ArrayList<>();
	private final MathContext mathContext;

	public RoundFlagValuesAction(AnimFlag<T> animFlag, Integer scale, Float zeroFloor, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		mathContext = scale == null ? null : new MathContext(scale, RoundingMode.HALF_UP);
		this.zeroFloor = zeroFloor;
		findFramesToRemove();
	}

	private void findFramesToRemove() {
		for (Sequence sequence : animFlag.getAnimMap().keySet()) {
			TreeMap<Integer, Entry<T>> entryMapCopy = animFlag.getSequenceEntryMapCopy(sequence);
			if(entryMapCopy != null && !entryMapCopy.isEmpty()){
				for (Entry<T> entry : entryMapCopy.values()){
					if(entry.getValue() instanceof Float){
						fixFloat((Entry<Float>) entry);
					} else if(entry.getValue() instanceof Vec3){
						fixVec3((Entry<Vec3>) entry);
					} else if(entry.getValue() instanceof Quat){
						fixQuat((Entry<Quat>) entry);
					}
				}
				actions.add(new SetFlagEntryMapAction<>(animFlag, sequence, entryMapCopy, null));
			}
		}
	}

	private void fixFloat(Entry<Float> entry){
		entry.setValue(round(entry.getValue()));
		entry.setInTan(round(entry.getInTan()));
		entry.setOutTan(round(entry.getOutTan()));
	}
	private void fixVec3(Entry<Vec3> entry){
		entry.setValue(round(entry.getValue()));
		entry.setInTan(round(entry.getInTan()));
		entry.setOutTan(round(entry.getOutTan()));
	}
	private void fixQuat(Entry<Quat> entry){
		entry.setValue(round(entry.getValue()));
		entry.setInTan(round(entry.getInTan()));
		entry.setOutTan(round(entry.getOutTan()));
	}

	private Float round(Float value){
		if(value != null){
			if(mathContext != null){
				value = new BigDecimal(value, mathContext).floatValue();
			}

			if(zeroFloor != null && Math.abs(value) <= zeroFloor){
				value = 0f;
			}
		}
		return value;
	}

	private Vec3 round(Vec3 value){
		if(value != null){
			value.x = round(value.x);
			value.y = round(value.y);
			value.z = round(value.z);
		}
		return value;
	}

	private Quat round(Quat value){
		if(value != null){
			value.x = round(value.x);
			value.y = round(value.y);
			value.z = round(value.z);
			value.w = round(value.w);
		}
		return value;
	}

	@Override
	public RoundFlagValuesAction<T> undo() {
		for(UndoAction action : actions){
			action.undo();
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public RoundFlagValuesAction<T> redo() {
		for(UndoAction action : actions){
			action.redo();
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Round Keyframe Values";
	}
}
