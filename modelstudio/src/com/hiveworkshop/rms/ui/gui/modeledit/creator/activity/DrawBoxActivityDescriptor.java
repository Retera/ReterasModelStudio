package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoxActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private final ViewportListener viewportListener;

	public DrawBoxActivityDescriptor(ViewportListener viewportListener) {
		this.programPreferences = ProgramGlobals.getPrefs();
		this.viewportListener = viewportListener;
	}

	@Override
	public ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return new DrawBoxActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
	}

}
