package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SimplifyKeyframesFlagAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimplifyKeyframesAction implements UndoAction {
	private final List<SimplifyKeyframesFlagAction<?>> actions;

	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, List<Sequence> sequences, Float trans, Float rot, boolean allowRemovePeaks) {
		this(animFlags, sequences, trans, rot, null, null, allowRemovePeaks);
	}

	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, List<Sequence> sequences, Float valueDiff, boolean allowRemovePeaks) {
		this(animFlags, sequences, valueDiff, valueDiff, valueDiff, valueDiff, allowRemovePeaks);
	}

	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, List<Sequence> sequences,
	                               Float trans, Float scale, Float rot, Float valueDiff,
	                               boolean allowRemovePeaks) {
		actions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			switch (animFlag.getName()){
				case MdlUtils.TOKEN_TRANSLATION -> {
					if (trans != null && 0 <= trans) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, trans, allowRemovePeaks));
				}
				case MdlUtils.TOKEN_SCALING -> {
					if (scale != null && 0 <= scale) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, scale, allowRemovePeaks));
				}
				case MdlUtils.TOKEN_ROTATION -> {
					if (rot != null && 0 <= rot) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, rot, allowRemovePeaks));
				}
				default -> {
					if (valueDiff != null && 0 <= valueDiff) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, valueDiff, allowRemovePeaks));
				}
			}
		}
	}
	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, List<Sequence> sequences,
	                               float trans, float scale, float rot, float valueDiff,
	                               boolean allowRemovePeaks, boolean ugg) {
		actions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			switch (animFlag.getName()){
				case MdlUtils.TOKEN_TRANSLATION -> {
					if (0 <= trans) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, trans, allowRemovePeaks));
				}
				case MdlUtils.TOKEN_SCALING -> {
					if (0 <= scale) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, scale, allowRemovePeaks));
				}
				case MdlUtils.TOKEN_ROTATION -> {
					if (0 <= rot) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, rot, allowRemovePeaks));
				}
				default -> {
					if (0 <= valueDiff) actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, valueDiff, allowRemovePeaks));
				}
			}
		}
	}

	public int getNumberOfEntriesToRemove() {
		int found = 0;
		for (SimplifyKeyframesFlagAction<?> action : actions) {
			found += action.getNumberOfEntriesToRemove();
		}
		return found;
	}

	@Override
	public SimplifyKeyframesAction undo() {
		for (SimplifyKeyframesFlagAction<?> action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public SimplifyKeyframesAction redo() {
		for (SimplifyKeyframesFlagAction<?> action : actions) {
			action.redo();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Keyframes";
	}
}
