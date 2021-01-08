package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import javax.swing.*;
import java.awt.*;

class AnimListCellRenderer extends DefaultListCellRenderer {
	public AnimListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((AnimShell) value).anim.getName(), index, iss, chf);
		setIcon(ImportPanel.animIcon);
		return this;
	}
}
