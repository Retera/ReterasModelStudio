package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeInterpTypeAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LinearizeAnimations extends ActionFunction {
	public LinearizeAnimations(){
		super(TextKey.LINEARIZE_ANIMATIONS, LinearizeAnimations::linearizeAnimations);
	}

	public static void linearizeAnimations(ModelHandler modelHandler) {
		int x = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"This is an irreversible process that will lose some of your model data," +
						"\nin exchange for making it a smaller storage size." +
						"\n\nContinue and simplify animations?",
				"Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
		if (x == JOptionPane.OK_OPTION) {
			List<AnimFlag<?>> allAnimFlags = ModelUtils.getAllAnimFlags(modelHandler.getModel());
			List<UndoAction> interpTypActions = new ArrayList<>();
			for (AnimFlag<?> flag : allAnimFlags) {
				interpTypActions.add(new ChangeInterpTypeAction<>(flag, InterpolationType.LINEAR, null));
//                flag.linearize();
			}

			UndoAction action = new CompoundAction("Liniarize Animations", interpTypActions, ModelStructureChangeListener.changeListener::materialsListChanged);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
}
