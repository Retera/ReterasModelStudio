package com.hiveworkshop.rms.ui.application.edit.uv.activity;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;

public interface TVertexEditorActivityDescriptor {
	TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelView modelView,
                                                 UndoActionListener undoActionListener);
}
