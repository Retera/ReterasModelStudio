package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SimplifyKeyframesFlagAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SimplifyKeyframesAction implements UndoAction {
	private final List<SimplifyKeyframesFlagAction<?>> actions;

	public SimplifyKeyframesAction(ModelView modelView, float trans, float scale, float rot) {
		List<Sequence> allSequences = modelView.getModel().getAllSequences();
		actions = new ArrayList<>();
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			if (trans >= 0 && idObject.has(MdlUtils.TOKEN_TRANSLATION)) {
				actions.add(new SimplifyKeyframesFlagAction<>(idObject.find(MdlUtils.TOKEN_TRANSLATION), allSequences, trans));
			}
			if (scale >= 0 && idObject.has(MdlUtils.TOKEN_SCALING)) {
				actions.add(new SimplifyKeyframesFlagAction<>(idObject.find(MdlUtils.TOKEN_SCALING), allSequences, scale));
			}
			if (rot >= 0 && idObject.has(MdlUtils.TOKEN_ROTATION)) {
				actions.add(new SimplifyKeyframesFlagAction<>(idObject.find(MdlUtils.TOKEN_ROTATION), allSequences, rot));
			}
		}

	}

	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, List<Sequence> sequences, float valueDiff) {
		actions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			actions.add(new SimplifyKeyframesFlagAction<>(animFlag, sequences, valueDiff));
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
	public UndoAction undo() {
		for (SimplifyKeyframesFlagAction<?> action : actions) {
			action.undo();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (SimplifyKeyframesFlagAction<?> action : actions) {
			action.redo();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "simplify keyframes";
	}
}
