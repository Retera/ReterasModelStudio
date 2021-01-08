package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

class BoneShellListCellRenderer extends AbstractSnapshottingListCellRenderer2D<BoneShell> {
	public BoneShellListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
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
		return modelDisp.getModel().contains(object.bone);
	}

	@Override
	protected Vec3 getRenderVertex(final BoneShell value) {
		return value.bone.getPivotPoint();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		setBackground(new Color(220, 180, 255));
		super.getListCellRendererComponent(list, value, index, iss, chf);
		setText(value.toString()/*
		 * ((BoneShell) value).bone.getClass().getSimpleName() + " \"" +
		 * ((BoneShell) value).bone.getName() + "\""
		 */);
		// setIcon(new
		// ImageIcon(Material.mergeImageScaled(ImportPanel.boneIcon.getImage(),
		// ((ImageIcon) getIcon()).getImage(), 64, 64, 64, 64)));
		return this;
	}

	private static final class BoneShellFilter implements ResettableVertexFilter<BoneShell> {
		private BoneShell boneShell;

		@Override
		public boolean isAccepted(final GeosetVertex vertex) {
			return vertex.getBoneAttachments().contains(boneShell.bone);
		}

		@Override
		public ResettableVertexFilter<BoneShell> reset(final BoneShell matrix) {
			boneShell = matrix;
			return this;
		}

	}
}
