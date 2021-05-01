package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

import javax.swing.*;

public abstract class ComponentPanel<T> extends JPanel {

	public abstract void setSelectedItem(T itemToSelect);

	public abstract void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener);
}
