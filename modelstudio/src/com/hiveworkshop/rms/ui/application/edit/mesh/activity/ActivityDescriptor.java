package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;

public interface ActivityDescriptor {
	ModelEditorViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelView modelView,
											   UndoActionListener undoActionListener);
}
