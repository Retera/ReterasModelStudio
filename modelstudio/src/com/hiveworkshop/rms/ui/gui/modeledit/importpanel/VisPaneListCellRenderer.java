package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;

import javax.swing.*;
import java.awt.*;

class VisPaneListCellRenderer extends DefaultListCellRenderer {
	EditableModel current;

	public VisPaneListCellRenderer(final EditableModel whichModel) {
		current = whichModel;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((VisibilityPanel) value).sourceShell.model.getName() + ": "
				+ ((VisibilityPanel) value).sourceShell.source.getName(), index, iss, chf);
		if (current == ((VisibilityPanel) value).sourceShell.model) {
			setIcon(ImportPanel.greenIcon);
		} else {
			setIcon(ImportPanel.orangeIcon);
		}
		return this;
	}
}
