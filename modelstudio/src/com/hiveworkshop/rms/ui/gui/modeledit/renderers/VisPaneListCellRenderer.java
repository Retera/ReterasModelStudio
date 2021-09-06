package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.VisibilityShell;

import javax.swing.*;
import java.awt.*;

public class VisPaneListCellRenderer extends DefaultListCellRenderer {
	private final EditableModel model;

	public VisPaneListCellRenderer(final EditableModel whichModel) {
		model = whichModel;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value.toString(), index, isSel, hasFoc);
		if (model == ((VisibilityShell) value).getModel()) {
			setIcon(ImportPanel.greenIcon);
		} else {
			setIcon(ImportPanel.orangeIcon);
		}
		return this;
	}
}
