package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.VisibilityShell;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;
import java.awt.*;

public class VisPaneListCellRenderer extends DefaultListCellRenderer {
	public VisPaneListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value.toString(), index, isSel, hasFoc);
		if (((VisibilityShell) value).isFromDonating()) {
			setIcon(RMSIcons.orangeIcon);
		} else {
			setIcon(RMSIcons.greenIcon);
		}
		return this;
	}
}
