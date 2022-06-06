package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoneShellMotionListCellRenderer extends AbstractSnapshottingListCellRenderer2D<IdObjectShell<?>> {
	boolean showParent = false;
	Set<IdObjectShell<?>> selectedBones = new HashSet<>();
	IdObjectShell<?> selectedBone;
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

	public BoneShellMotionListCellRenderer setSelectedBoneShell(IdObjectShell<?> boneShell) {
		selectedBones.clear();
		selectedBone = boneShell;
		selectedBones.add(boneShell);
		return this;
	}

	public BoneShellMotionListCellRenderer setSelectedBoneShell(Collection<IdObjectShell<?>> boneShells) {
		selectedBones.clear();
		selectedBone = null;
		selectedBones.addAll(boneShells);
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		if (value instanceof IdObjectShell<?>) {
			IdObjectShell<?> idObjectShell = (IdObjectShell<?>) value;
			setText(idObjectShell.toString(showClass, showParent));
			IdObjectShell<?> motionSrcShell = idObjectShell.getMotionSrcShell();
			if (selectedBones.contains(motionSrcShell)
					|| selectedBone != null && selectedBone.getMotionSrcShell() == idObjectShell) {
				bg = selectedOwnerBgCol;
				fg = selectedOwnerFgCol;
			} else if (motionSrcShell != null) {
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
	protected boolean isFromDonating(IdObjectShell<?> value) {
		if (value != null) {
			return value.isFromDonating();
		}
		return false;
	}

	@Override
	protected boolean isFromReceiving(IdObjectShell<?> value) {
		if (value != null) {
			return !value.isFromDonating();
		}
		return false;
	}

	@Override
	protected IdObjectShell<?> valueToTyped(Object value) {
		return (IdObjectShell<?>) value;
	}

	@Override
	protected boolean contains(EditableModel model, IdObjectShell<?> object) {
		if (model != null) {
			return model.contains(object.getIdObject());
		}
		return false;
	}

	@Override
	protected Vec3 getRenderVertex(IdObjectShell<?> value) {
		return value.getIdObject().getPivotPoint();
	}
}
