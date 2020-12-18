package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

public interface ComponentPanel<T> {

	void setSelectedItem(T itemToSelect);

	void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener);
}
