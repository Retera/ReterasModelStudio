package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

class BonePanelListCellRenderer extends AbstractSnapshottingListCellRenderer2D<Bone> {
	public BonePanelListCellRenderer(final ModelView modelDisplay, final ModelView otherDisplay) {
		super(modelDisplay, otherDisplay);
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean isSelected, final boolean chf) {
		setBackground(new Color(200, 255, 255));
		super.getListCellRendererComponent(list, ((BoneShell) value).getBone(), index, isSelected, chf);
		setText(((BoneShell) value).getBone().getClass().getSimpleName() + " \"" + ((BoneShell) value).getName() + "\"");
		// setIcon(ImportPanel.cyanIcon);
		// setIcon(new ImageIcon(Material.mergeImageScaled(ImportPanel.cyanIcon.getImage(),
		// ((ImageIcon) getIcon()).getImage(), 64, 64, 64, 64)));
		return this;
	}

	@Override
	protected ResettableVertexFilter<Bone> createFilter() {
		return new ResettableVertexFilter<>() {
			private Bone bone;

			@Override
			public boolean isAccepted(final GeosetVertex vertex) {
				return vertex.getBoneAttachments().contains(bone);
			}

			@Override
			public ResettableVertexFilter<Bone> reset(final Bone bone) {
				this.bone = bone;
				return this;
			}
		};
	}

	@Override
	protected Bone valueToType(final Object value) {
		return (Bone) value;
	}

	@Override
	protected Vec3 getRenderVertex(final Bone value) {
		return value.getPivotPoint();
	}

	@Override
	protected boolean contains(final ModelView modelDisp, final Bone object) {
		return modelDisp.getModel().contains(object);
	}
}
