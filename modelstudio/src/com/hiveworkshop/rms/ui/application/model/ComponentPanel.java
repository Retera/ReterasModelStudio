package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;

public abstract class ComponentPanel<T> extends JPanel {
	protected final ModelHandler modelHandler;
	protected final ModelStructureChangeListener changeListener;

	public ComponentPanel(ModelHandler modelHandler, ModelStructureChangeListener changeListener) {
		this.modelHandler = modelHandler;
		this.changeListener = changeListener;
	}

	public abstract void setSelectedItem(T itemToSelect);

	public abstract void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener);
}
