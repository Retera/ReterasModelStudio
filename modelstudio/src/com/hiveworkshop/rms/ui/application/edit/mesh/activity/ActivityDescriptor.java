package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

public interface ActivityDescriptor {
	ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler);
}
