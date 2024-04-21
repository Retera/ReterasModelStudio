package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.OffsetSequenceAction;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.OffsetObjectKFPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OffsetKeyframes extends ActionFunction {

	public OffsetKeyframes() {
		super(TextKey.OFFSET_KEYFRAMES, OffsetKeyframes::showPanel);
	}
	public static void showPanel(ModelHandler modelHandler) {
		OffsetObjectKFPanel offsetPanel = new OffsetObjectKFPanel(modelHandler);
		offsetPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		JFrame frame = FramePopup.get(offsetPanel, ProgramGlobals.getMainPanel(), "Offset Animations [" + modelHandler.getModel().getName() + "]");
		ProgramGlobals.linkActions(offsetPanel);
		frame.setVisible(true);
	}

	private static void doStuff_(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		Sequence currentSequence = modelHandler.getRenderModel().getTimeEnvironment().getCurrentSequence();

		UndoAction offsetAction = getOffsetAction(modelView.getSelectedIdObjects(), currentSequence, 0.5f);
		modelHandler.getUndoManager().pushAction(offsetAction.redo());
	}

	private static UndoAction getOffsetAction(Collection<IdObject> nodes, Sequence sequence, float offset) {
		Set<AnimFlag<?>> objects = new HashSet<>();
		nodes.forEach(idObject -> objects.addAll(idObject.getAnimFlags()));
		return new OffsetSequenceAction(objects, sequence, offset, ModelStructureChangeListener.changeListener);
	}
}
