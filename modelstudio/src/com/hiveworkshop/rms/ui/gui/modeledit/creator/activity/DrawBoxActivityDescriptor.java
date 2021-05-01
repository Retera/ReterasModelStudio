package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoxActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private final ViewportListener viewportListener;

	public DrawBoxActivityDescriptor(ProgramPreferences programPreferences,
	                                 ViewportListener viewportListener) {
		this.programPreferences = programPreferences;
		this.viewportListener = viewportListener;
	}

	@Override
	public ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager,
	                                                  ModelHandler modelHandler) {
		return new DrawBoxActivity(programPreferences, modelHandler.getUndoManager(), modelEditorManager.getModelEditor(),
				modelHandler.getModelView(), modelEditorManager.getSelectionView(), viewportListener, 1, 1, 1);
	}

}
