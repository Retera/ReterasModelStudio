package com.hiveworkshop.rms.ui.gui.modeledit.components;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.editor.model.EditableModel;

public interface ComponentPanel {
	void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener);
}
