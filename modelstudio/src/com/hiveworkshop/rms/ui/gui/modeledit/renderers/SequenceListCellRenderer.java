package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

public class SequenceListCellRenderer extends DefaultListCellRenderer {
	public SequenceListCellRenderer() {
	}

	public SequenceListCellRenderer(boolean showLength) {
		this.showLength = showLength;
	}

	private static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	private static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	private static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	private static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	private static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	private static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);

	boolean showLength = false;

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		String name = "";
		if (value instanceof Animation) {
			name = ((Animation) value).getName();
		} else if (value instanceof GlobalSeq) {
			name = "GlobalSeq " + ((GlobalSeq) value).getLength();
		} else {
			name = "unanimated";
		}
		super.getListCellRendererComponent(list, name, index, isSel, hasFoc);
		Vec3 bg = noOwnerBgCol;

		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}
		this.setBackground(bg.asIntColor());
		this.setForeground(noOwnerFgCol.asIntColor());
		return this;
	}
}
