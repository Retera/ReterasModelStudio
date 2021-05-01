package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ViewportListener viewportListener;

	public DrawBoneActivityDescriptor(ProgramPreferences programPrefences,
	                                  ViewportListener viewportListener) {
		this.programPrefences = programPrefences;
		this.viewportListener = viewportListener;
	}

	@Override
	public ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawBoneActivity(programPrefences, modelHandler.getUndoManager(), modelEditorManager.getModelEditor(),
				modelHandler.getModelView(), modelEditorManager.getSelectionView(), viewportListener);
	}

}
