package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;

public class ModelComponentTreeListener {
	private final ComponentsPanel panel;

	public ModelComponentTreeListener(ComponentsPanel panel) {

		this.panel = panel;
	}

	public void uggPanel(Object selectedItem) {
		System.out.println("make the panel!");
		panel.setSelectedPanel(selectedItem);
	}

	public void selectedBlank() {
		panel.selectedBlank();
	}

//	public void selectedHeaderData(EditableModel item, ModelViewManager modelViewManager, UndoActionListener undoListener, ModelStructureChangeListener modelStructureChangeListener) {
//		panel.selectedHeaderData(item, modelViewManager, undoListener, modelStructureChangeListener);
//	}

//	public void selected(EditableModel item) {
//		panel.selected(item);
//	}
}
