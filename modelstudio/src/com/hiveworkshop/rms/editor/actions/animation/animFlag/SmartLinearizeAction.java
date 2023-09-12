package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class SmartLinearizeAction<T> implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final AnimFlag<T> animFlag;
	private final InterpolationType oldInterpType;
	private final AnimFlag<T> oldAnimFlag;
	private final AnimFlag<T> newAnimFlag;


	public SmartLinearizeAction(AnimFlag<T> animFlag, Float diff, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.animFlag = animFlag;
		oldInterpType = animFlag.getInterpolationType();
		oldAnimFlag = animFlag.deepCopy();
		newAnimFlag = animFlag.deepCopy();
		newAnimFlag.linearize();

		if(diff != null){
			for (Sequence sequence : newAnimFlag.getAnimMap().keySet()){
				for (int i = 0; i<=sequence.getLength(); i++) {
					T v_new = newAnimFlag.interpolateAt(sequence, i);
					T v_old = oldAnimFlag.interpolateAt(sequence, i);
					if(diff < getDiff(v_new, v_old)) {
						newAnimFlag.addEntry(i, v_old, sequence);
					}
				}
			}
		}

	}

	private float getDiff(T v_new, T v_old) {
		if(v_new instanceof Bitmap && v_old instanceof Bitmap) {
			return 0;
		} else if(v_new instanceof Integer && v_old instanceof Integer) {
			return Math.abs(((Integer)v_new) - ((Integer)v_old));
		} else if(v_new instanceof Float && v_old instanceof Float) {
			return Math.abs(((Float)v_new) - ((Float)v_old));
		} else if(v_new instanceof Vec3 && v_old instanceof Vec3) {
			return Math.abs(((Vec3)v_new).distance(((Vec3)v_old)));
		} else if(v_new instanceof Quat && v_old instanceof Quat) {
			return Math.abs(((Quat)v_new).distance(((Quat)v_old)));
		}
		return 0;
	}

	@Override
	public UndoAction undo() {
		animFlag.setSequenceMap(oldAnimFlag.getAnimMap());
		animFlag.quickSetInterpType(oldInterpType);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		animFlag.setSequenceMap(newAnimFlag.getAnimMap());
		animFlag.quickSetInterpType(InterpolationType.LINEAR);
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Linearize Animations";
	}
}
