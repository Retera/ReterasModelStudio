package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

public class AnimListCellRenderer extends DefaultListCellRenderer {
	public AnimListCellRenderer() {
	}

	public AnimListCellRenderer(boolean showLength) {
		this.showLength = showLength;
	}

	private static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	private static final Vec3 timeScaleBgCol = new Vec3(150, 170, 230);
	private static final Color timeScaleBgCol1 = new Color(150, 170, 230);
	private static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	private static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	private static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	private static final Vec3 dontImpBgCol = new Vec3(240, 200, 200);
	private static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);

	boolean showLength = false;
	AnimShell selectedAnim;

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, ((AnimShell) value).getOldName() + " " + ((AnimShell) value).getAnim().getLength(), index, isSel, hasFoc);
		AnimShell animDataSrc = ((AnimShell) value).getAnimDataSrc();
		Vec3 bg;
		Vec3 fg;
		if (((AnimShell) value).getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE && animDataSrc != null && animDataSrc == selectedAnim) {
			bg = selectedOwnerBgCol;
			fg = selectedOwnerFgCol;
		} else if (((AnimShell) value).getImportType() == AnimShell.ImportType.TIMESCALE_RECEIVE && animDataSrc != null) {
			bg = otherOwnerBgCol;
			fg = otherOwnerFgCol;
		} else if (((AnimShell) value).getImportType() == AnimShell.ImportType.DONT_IMPORT) {
			bg = dontImpBgCol;
			fg = otherOwnerFgCol;
		} else if (((AnimShell) value).getImportType() == AnimShell.ImportType.TIMESCALE_INTO) {
			bg = timeScaleBgCol;
			fg = noOwnerFgCol;
		} else {
			bg = noOwnerBgCol;
			fg = noOwnerFgCol;
		}
		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}
		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());
		setIcon(ImportPanel.animIcon); // todo choose icon based on import status
		return this;
	}

	private String getAnimName(AnimShell value){
		if(showLength) {
			return value.getOldName() + " (" + value.getAnim().getLength() + ")";
		}
		return value.getOldName();
	}
}
