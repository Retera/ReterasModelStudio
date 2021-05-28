package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ViewportListener viewportListener;

	public DrawBoneActivityDescriptor(ViewportListener viewportListener) {
		this.programPrefences = ProgramGlobals.getPrefs();
		this.viewportListener = viewportListener;
	}

	@Override
	public ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawBoneActivity(modelHandler, modelEditorManager, viewportListener);
	}

}
