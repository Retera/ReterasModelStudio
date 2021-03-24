package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

class AnimListCellRenderer extends DefaultListCellRenderer {
	public AnimListCellRenderer() {
	}

	private static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	private static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	private static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	private static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	private static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);

	AnimShell selectedAnim;

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		super.getListCellRendererComponent(list, ((AnimShell) value).getOldName(), index, iss, chf);
//		super.getListCellRendererComponent(list, ((AnimShell) value).displName(), index, iss, chf);
		AnimShell importAnimShell = ((AnimShell) value).getImportAnimShell();
		Vec3 bg;
		Vec3 fg;
		if (importAnimShell != null && importAnimShell == selectedAnim) {
			bg = selectedOwnerBgCol;
			fg = selectedOwnerFgCol;
		} else if (importAnimShell != null) {
			bg = otherOwnerBgCol;
			fg = otherOwnerFgCol;
		} else {
			bg = noOwnerBgCol;
			fg = noOwnerFgCol;
		}
		if (iss) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}
		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());
		setIcon(ImportPanel.animIcon); // todo choose icon based on import status
		return this;
	}
}
