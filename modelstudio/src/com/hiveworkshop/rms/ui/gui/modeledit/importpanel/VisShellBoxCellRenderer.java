package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import javax.swing.*;
import java.awt.*;

class VisShellBoxCellRenderer extends javax.swing.plaf.basic.BasicComboBoxRenderer {
	public VisShellBoxCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		if (value == null) {
			return getListCellRendererComponent(list, "NULL ERROR", index, iss, chf);
		}
		if (value.getClass() != String.class) {
			super.getListCellRendererComponent(list, value.toString(), index, iss, chf);
//			super.getListCellRendererComponent(list,
//					((VisibilityShell) value).model.getName() + ": " + ((VisibilityShell) value).source.getName(), index, iss, chf);
		} else {
			super.getListCellRendererComponent(list, value, index, iss, chf);
		}
		return this;
	}
}
