package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;

import javax.swing.*;
import java.awt.*;

class BoneListCellRenderer extends DefaultListCellRenderer {
	public BoneListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list,
				((Bone) value).getClass().getSimpleName() + " \"" + ((Bone) value).getName() + "\"", index, iss, chf);
		setIcon(ImportPanel.boneIcon);
		return this;
	}
}
