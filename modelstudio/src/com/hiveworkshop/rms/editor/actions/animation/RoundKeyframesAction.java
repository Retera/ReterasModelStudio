package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RoundFlagValuesAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoundKeyframesAction implements UndoAction {
	private final ModelStructureChangeListener changeListener;
	private final List<RoundFlagValuesAction<?>> actions;


	public RoundKeyframesAction(Collection<AnimFlag<?>> animFlags,
	                            Float transClamp, Integer transMagnitude,
	                            Float scaleClamp, Integer scaleMagnitude,
	                            Float rotClamp, Integer rotMagnitude,
	                            Float otherClamp, Integer otherMagnitude,
	                            ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		actions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			switch (animFlag.getName()){
				case MdlUtils.TOKEN_TRANSLATION -> {
					if (transClamp != null || transMagnitude != null) actions.add(new RoundFlagValuesAction<>(animFlag, transMagnitude, transClamp, null));
				}
				case MdlUtils.TOKEN_SCALING -> {
					if (scaleClamp != null || scaleMagnitude != null) actions.add(new RoundFlagValuesAction<>(animFlag, scaleMagnitude, scaleClamp, null));
				}
				case MdlUtils.TOKEN_ROTATION -> {
					if (rotClamp != null || rotMagnitude != null) actions.add(new RoundFlagValuesAction<>(animFlag, rotMagnitude, rotClamp, null));
				}
				default -> {
					if (otherClamp != null || otherMagnitude != null) actions.add(new RoundFlagValuesAction<>(animFlag, otherMagnitude, otherClamp, null));
				}
			}
		}
	}

	@Override
	public RoundKeyframesAction undo() {
		for (RoundFlagValuesAction<?> action : actions) {
			action.undo();
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public RoundKeyframesAction redo() {
		for (RoundFlagValuesAction<?> action : actions) {
			action.redo();
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Round Keyframes";
	}
}
