package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.util.AbstractObject2DThumbnailListCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoneShellListCellRenderer extends AbstractObject2DThumbnailListCellRenderer<IdObjectShell<?>> {
	private boolean showParent = false;
	private final Set<IdObjectShell<?>> selectedBones = new HashSet<>();
	private IdObjectShell<?> selectedBone;
	private IdObjectShell<?> selectedObject;
	private boolean showClass = false;

	public BoneShellListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
	}

	public BoneShellListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public BoneShellListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public BoneShellListCellRenderer setSelectedBoneShell(IdObjectShell<?> boneShell) {
		selectedBones.clear();
		selectedBone = boneShell;
		return this;
	}

	public BoneShellListCellRenderer setSelectedBones(Collection<IdObjectShell<?>> selected) {
		selectedBones.clear();
		selectedBones.addAll(selected);
		selectedBone = null;
		selectedObject = null;
		return this;
	}

	public BoneShellListCellRenderer setSelectedObjectShell(IdObjectShell<?> objectShell) {
		selectedBones.clear();
		selectedObject = objectShell;
		return this;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		bg.set(noOwnerBgCol);
		fg.set(noOwnerFgCol);

		if (value instanceof IdObjectShell) {
			setText(((IdObjectShell<?>) value).toString(showClass, showParent));
			if (selectedBone != null && selectedBone.getNewParentShell() == value
					|| selectedObject != null && selectedObject.getNewParentShell() == value) {
				bg.set(selectedOwnerBgCol);
				fg.set(selectedOwnerFgCol);
			} else if (!selectedBones.isEmpty()) {
				long count = selectedBones.stream().filter(o -> o.getNewParentShell() == value).count();
				fg.set(noOwnerFgCol);

				if (count == 0) {
					bg.set(noOwnerBgCol);
				} else if (count == selectedBones.size()){
					bg.set(selectedOwnerBgCol);
				} else {
					bg.set(selectedOwnerBgCol).add(noOwnerBgCol).scale(0.5f);
				}
			}
			if (!((IdObjectShell<?>) value).getShouldImport()) {
				bg.multiply(otherOwnerBgCol).normalize().scale(160);
				fg.set(bg).multiply(otherOwnerFgCol).normalize().scale(60);
			}
		} else {
			setText(value.toString());
		}

		if (isSel) {
			bg.add(hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}
}
