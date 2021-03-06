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

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		setBackground(new Color(220, 180, 255));
		super.getListCellRendererComponent(list, value, index, iss, chf);
		setText(value.toString());
		if (value instanceof BoneShell) {
			BoneShell importBoneShell = ((BoneShell) value).getImportBoneShell();
			if (selectedBones.contains(importBoneShell)) {
				this.setBackground(new Color(130, 230, 170));
			} else if (importBoneShell != null) {
				this.setBackground(new Color(160, 160, 160));
				setForeground(new Color(60, 60, 60));
			} else {
				this.setBackground(new Color(255, 255, 255));
				setForeground(new Color(0, 0, 0));
			}
		}


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
