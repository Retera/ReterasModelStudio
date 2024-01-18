package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BoneShellMotionListCellRenderer extends AbstractObject2DThumbnailListCellRenderer<IdObjectShell<?>> {
	private boolean showParent = false;
	private final Set<IdObjectShell<?>> selectedBones = new HashSet<>();
	private IdObjectShell<?> selectedBone;
	private boolean showClass = false;
	private boolean destList = false;
	private boolean markDontImp;

	public BoneShellMotionListCellRenderer(EditableModel model, EditableModel other) {
		super(model, other);
	}

	public BoneShellMotionListCellRenderer setMarkDontImp(boolean markDontImp) {
		this.markDontImp = markDontImp;
		return this;
	}

	public BoneShellMotionListCellRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public BoneShellMotionListCellRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public BoneShellMotionListCellRenderer setDestList(boolean destList) {
		this.destList = destList;
		return this;
	}

	public BoneShellMotionListCellRenderer setSelectedBoneShell(IdObjectShell<?> boneShell) {
		selectedBones.clear();
		selectedBone = boneShell;
//		selectedBones.add(boneShell);
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

		bg.set(noOwnerBgCol);
		fg.set(noOwnerFgCol);

		if(selectedBones.isEmpty() && value instanceof IdObjectShell<?>){
			IdObjectShell<?> idObjectShell = (IdObjectShell<?>) value;
			setText(idObjectShell.toString(showClass, showParent));
			IdObjectShell<?> motionSrcShell = idObjectShell.getMotionSrcShell();

			if(destList && selectedBone != null){
				if (motionSrcShell == selectedBone) {
					bg.set(selectedOwnerBgCol);
					fg.set(selectedOwnerFgCol);
				} else if (motionSrcShell != null) {
					bg.set(otherOwnerBgCol);
					fg.set(otherOwnerFgCol);
				}
			} else if (selectedBone != null){
				if (selectedBone.getMotionSrcShell() == idObjectShell) {
					bg.set(selectedOwnerBgCol);
					fg.set(selectedOwnerFgCol);
				}
			}
		} else if (value instanceof IdObjectShell<?>){
			IdObjectShell<?> idObjectShell = (IdObjectShell<?>) value;
			setText(idObjectShell.toString(showClass, showParent));
			IdObjectShell<?> motionSrcShell = idObjectShell.getMotionSrcShell();

			if(destList){
				if (!idObjectShell.getShouldImport() && markDontImp) {
					fg.set(bg).multiply(otherOwnerFgCol).normalize().scale(60);
					bg.multiply(otherOwnerBgCol).normalize().scale(160);
				} else if (motionSrcShell == null) {
					bg.set(noOwnerBgCol);
					fg.set(noOwnerFgCol);
				} else if (selectedBones.contains(motionSrcShell)) {
					bg.set(selectedOwnerBgCol);
					fg.set(selectedOwnerFgCol);
				} else {
					bg.set(otherOwnerBgCol);
					fg.set(otherOwnerFgCol);
				}
			} else {
				fg.set(noOwnerFgCol);
				long count = selectedBones.stream().filter(o -> o.getMotionSrcShell() == idObjectShell).count();
				if (count == 0) {
					bg.set(noOwnerBgCol);
				} else if (count == selectedBones.size()){
					bg.set(selectedOwnerBgCol);
				} else {
					bg.set(selectedOwnerBgCol).add(noOwnerBgCol).scale(0.5f);
				}
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
