package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawPlaneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private int numberOfWidthSegments;
	private final ViewportListener viewportListener;
	private int numberOfHeightSegments;

	public DrawPlaneActivityDescriptor(ProgramPreferences programPreferences,
	                                   ViewportListener viewportListener) {
		this.programPreferences = programPreferences;
		this.viewportListener = viewportListener;
		numberOfWidthSegments = 1;
		numberOfHeightSegments = 1;
	}

	@Override
	public ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawPlaneActivity(modelHandler, programPreferences, modelEditorManager.getModelEditor(),
				modelEditorManager.getSelectionView(), viewportListener, 1, 1, 1);
	}

}
