package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.CameraShell;

import javax.swing.*;
import java.awt.*;

public class CameraShellListCellRenderer extends DefaultListCellRenderer {
	public CameraShellListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((CameraShell) value).toString(true, false), index, iss, chf);
		setIcon(ImportPanel.redIcon);
		return this;
	}
}
