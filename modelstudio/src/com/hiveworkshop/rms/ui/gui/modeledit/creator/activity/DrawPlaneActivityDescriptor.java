package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawPlaneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private int numberOfWidthSegments;
	private final ViewportListener viewportListener;
	private int numberOfHeightSegments;

	public DrawPlaneActivityDescriptor(ViewportListener viewportListener) {
		this.programPreferences = ProgramGlobals.getPrefs();
		this.viewportListener = viewportListener;
		numberOfWidthSegments = 1;
		numberOfHeightSegments = 1;
	}

	@Override
	public ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawPlaneActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
	}

}
