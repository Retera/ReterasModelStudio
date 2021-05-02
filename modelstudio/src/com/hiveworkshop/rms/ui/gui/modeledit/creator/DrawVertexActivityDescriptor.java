package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawVertexActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ViewportListener viewportListener;

	public DrawVertexActivityDescriptor(ProgramPreferences programPrefences,
	                                    ViewportListener viewportListener) {
		this.programPrefences = programPrefences;
		this.viewportListener = viewportListener;
	}

	@Override
	public ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawVertexActivity(modelHandler, programPrefences, modelEditorManager.getModelEditor(), modelEditorManager.getSelectionView(), viewportListener);
	}

}
