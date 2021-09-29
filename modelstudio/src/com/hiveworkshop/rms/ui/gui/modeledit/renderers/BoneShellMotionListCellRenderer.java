package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoneShellMotionListCellRenderer extends AbstractSnapshottingListCellRenderer2D<BoneShell> {
	boolean showParent = false;
	Set<BoneShell> selectedBones = new HashSet<>();
	boolean showClass = false;

	public BoneShellMotionListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
	}

	public BoneShellMotionListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public BoneShellMotionListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public BoneShellMotionListCellRenderer setSelectedBoneShell(BoneShell boneShell) {
		selectedBones.clear();
		selectedBones.add(boneShell);
		return this;
	}

	public BoneShellMotionListCellRenderer setSelectedBoneShell(Collection<BoneShell> boneShells) {
		selectedBones.clear();
		selectedBones.addAll(boneShells);
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		if (value instanceof BoneShell) {
			setText(((BoneShell) value).toString(showClass, showParent));
			BoneShell importBoneShell = ((BoneShell) value).getImportBoneShell();
			if (selectedBones.contains(importBoneShell)) {
				bg = selectedOwnerBgCol;
				fg = selectedOwnerFgCol;
			} else if (importBoneShell != null) {
				bg = otherOwnerBgCol;
				fg = otherOwnerFgCol;
			}
		} else {
			setText(value.toString());
		}

		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}

	@Override
	protected boolean isFromDonating(BoneShell value) {
		if (value != null) {
			return value.isFromDonating();
		}
		return false;
	}

	@Override
	protected boolean isFromReceiving(BoneShell value) {
		if (value != null) {
			return !value.isFromDonating();
		}
		return false;
	}

	@Override
	protected BoneShell valueToType(Object value) {
		return (BoneShell) value;
	}

	@Override
	protected boolean contains(EditableModel model, BoneShell object) {
		if (model != null) {
			return model.contains(object.getBone());
		}
		return false;
	}

	@Override
	protected Vec3 getRenderVertex(BoneShell value) {
		return value.getBone().getPivotPoint();
	}
}
