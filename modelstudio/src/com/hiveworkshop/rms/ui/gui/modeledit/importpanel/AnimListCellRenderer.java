package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import javax.swing.*;
import java.awt.*;

class AnimListCellRenderer extends DefaultListCellRenderer {
	public AnimListCellRenderer() {
	}

	AnimShell selectedAnim;
	public void setSelectedAnim(AnimShell animShell){
		selectedAnim = animShell;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((AnimShell) value).getOldName(), index, iss, chf);
		AnimShell importAnimShell = ((AnimShell) value).getImportAnimShell();
		if(importAnimShell == selectedAnim){
			this.setBackground(new Color(130, 230, 170));
		} else if (importAnimShell != null) {
			this.setBackground(new Color(160, 160, 160));
			setForeground(new Color(60, 60, 60));
		} else {
			this.setBackground(new Color(255, 255, 255));
			setForeground(new Color(0, 0, 0));
		}
		setIcon(ImportPanel.animIcon);
		return this;
	}
}
