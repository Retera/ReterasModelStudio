package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

import java.util.List;

public class ModelComponentTreeListener {
	private final ComponentsPanel panel;

	public ModelComponentTreeListener(ComponentsPanel panel) {

		this.panel = panel;
	}

	public void uggPanel() {
		System.out.println("make the panel!");
	}

	public void selectedBlank() {
		panel.selectedBlank();
	}

	public void selectedHeaderComment(List<String> header) {
		panel.selectedHeaderComment(header);
	}

	public void selectedHeaderData(EditableModel item, ModelViewManager modelViewManager, UndoActionListener undoListener, ModelStructureChangeListener modelStructureChangeListener) {
		panel.selectedHeaderData(item, modelViewManager, undoListener, modelStructureChangeListener);
	}

	public void selected(EditableModel item) {
		panel.selected(item);
	}
}
