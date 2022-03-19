package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.tools.RigAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class RigSelection extends ActionFunction {
	public RigSelection(){
		super(TextKey.RIG_ACTION, () -> rigActionRes(), "control W");
		setMenuItemMnemonic(KeyEvent.VK_R);
	}



	public static void rigActionRes() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			doRig(modelHandler);
		}
	}

	public static void doRig(ModelHandler modelHandler) {
		ModelView modelView = modelHandler.getModelView();
		if (!modelView.getSelectedIdObjects().isEmpty() && !modelView.getSelectedVertices().isEmpty()) {
			modelHandler.getUndoManager().pushAction(rig(modelView).redo());
		} else {
			System.err.println("NOT RIGGING, NOT VALID: " + modelView.getSelectedIdObjects().size() + " idObjects and " + modelView.getSelectedVertices() + " vertices selected");
		}
	}

	public static RigAction rig(ModelView modelView) {
		List<Bone> selectedBones = new ArrayList<>();
		for (IdObject object : modelView.getSelectedIdObjects()) {
			if (object instanceof Bone) {
				selectedBones.add((Bone) object);
			}
		}
		return new RigAction(modelView.getSelectedVertices(), selectedBones);
	}
}
