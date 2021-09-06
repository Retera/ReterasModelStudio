package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;

import javax.swing.*;
import java.awt.*;

public class BoneListCellRenderer extends DefaultListCellRenderer {
	public BoneListCellRenderer() {
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index,
	                                              boolean isSel, boolean chf) {
		super.getListCellRendererComponent(list,
				((Bone) value).getClass().getSimpleName() + " \"" + ((Bone) value).getName() + "\"", index, isSel, chf);
		setIcon(ImportPanel.boneIcon);
		return this;
	}
}
