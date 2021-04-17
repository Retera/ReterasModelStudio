package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import javax.swing.*;
import java.awt.*;

class ObjPanelListCellRenderer extends DefaultListCellRenderer {
	public ObjPanelListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((ObjectShell)value).toString(true, false), index, iss, chf);
		setIcon(ImportPanel.redIcon);
		return this;
	}
}
