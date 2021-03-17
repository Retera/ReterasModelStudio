package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class BoneShellMotionListCellRenderer extends AbstractSnapshottingListCellRenderer2D<BoneShell> {
	Set<BoneShell> selectedBones = new HashSet<>();

	public BoneShellMotionListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	@Override
	protected ResettableVertexFilter<BoneShell> createFilter() {
		return new BoneShellFilter();
	}

	@Override
	protected BoneShell valueToType(final Object value) {
		return (BoneShell) value;
	}

	@Override
	protected boolean contains(final ModelView modelDisp, final BoneShell object) {
		return modelDisp.getModel().contains(object.getBone());
	}

	@Override
	protected Vec3 getRenderVertex(final BoneShell value) {
		return value.getBone().getPivotPoint();
	}

	public void setSelectedBoneShell(BoneShell boneShell) {
		selectedBones.clear();
		selectedBones.add(boneShell);
	}

	public void setSelectedBoneShell(ArrayList<BoneShell> boneShells) {
		selectedBones.clear();
		selectedBones.addAll(boneShells);
	}

	boolean showClass = false;
	boolean showParent = false;

	public BoneShellMotionListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public BoneShellMotionListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean isSelected, final boolean chf) {
		super.getListCellRendererComponent(list, value, index, isSelected, chf);

		setText(value.toString());

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
		}

		if (isSelected) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}

	private static final class BoneShellFilter implements ResettableVertexFilter<BoneShell> {
		private BoneShell boneShell;

		@Override
		public boolean isAccepted(final GeosetVertex vertex) {
			return vertex.getBoneAttachments().contains(boneShell.getBone());
		}

		@Override
		public ResettableVertexFilter<BoneShell> reset(final BoneShell matrix) {
			boneShell = matrix;
			return this;
		}

	}
}
