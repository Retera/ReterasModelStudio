package com.hiveworkshop.rms.ui.application.edit.uv.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.viewport.TVertexEditorManager;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public interface TVertexEditorActivityDescriptor {
	TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelView modelView,
                                                 UndoActionListener undoActionListener);
}
