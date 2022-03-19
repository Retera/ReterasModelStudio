package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MatrixEditListRenderer extends AbstractSnapshottingListCellRenderer2D<IdObjectShell<Bone>> {
	boolean showParent = false;

	IdObjectShell<Bone> selectedBone;
	IdObjectShell<?> selectedObject;
	boolean showClass = false;

	Set<IdObjectShell<Bone>> bonesInAllMatricies = new HashSet<>();
	Set<IdObjectShell<Bone>> bonesNotInAllMatricies = new HashSet<>();

	public MatrixEditListRenderer(EditableModel recModel, EditableModel donModel) {
		super(recModel, donModel);
	}

	public MatrixEditListRenderer setShowClass(boolean b) {
		showClass = b;
		return this;
	}

	public MatrixEditListRenderer setShowParent(boolean b) {
		showParent = b;
		return this;
	}

	public void setSelectedBoneShell(IdObjectShell<Bone> boneShell) {
		selectedBone = boneShell;
	}

	public void setSelectedObjectShell(IdObjectShell<Bone> objectShell) {
		selectedObject = objectShell;
	}

	@Override
	protected boolean isFromDonating(IdObjectShell<Bone> value) {
		if (value != null) {
			return value.isFromDonating();
		}
		return false;
	}

	@Override
	protected boolean isFromReceiving(IdObjectShell<Bone> value) {
		if (value != null) {
			return !value.isFromDonating();
		}
		return false;
	}

	@Override
	protected IdObjectShell<Bone> valueToType(final Object value) {
		return (IdObjectShell<Bone>) value;
	}

	@Override
	protected boolean contains(EditableModel model, final IdObjectShell<Bone> object) {
		if (model != null) {
			return model.contains(object.getIdObject());
		}
		return false;
	}

	@Override
	protected Vec3 getRenderVertex(final IdObjectShell<Bone> value) {
		return value.getIdObject().getPivotPoint();
	}


	public void addInAllBone(IdObjectShell<Bone> boneShell) {
		bonesInAllMatricies.add(boneShell);
	}

	public void removeInAllBone(IdObjectShell<Bone> boneShell) {
		bonesInAllMatricies.remove(boneShell);
	}

	public void addNotInAllBone(IdObjectShell<Bone> boneShell) {
		bonesNotInAllMatricies.add(boneShell);
	}

	public void removeNotInAllBone(IdObjectShell<Bone> boneShell) {
		bonesNotInAllMatricies.remove(boneShell);
	}

	public void addNotInAllBone(Collection<IdObjectShell<Bone>> boneShells) {
		bonesNotInAllMatricies.addAll(boneShells);
	}

	public void removeNotInAllBone(Collection<IdObjectShell<Bone>> boneShells) {
		bonesNotInAllMatricies.removeAll(boneShells);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		// ToDo check if type matters for this renders
		if (value instanceof IdObjectShell<?>) { // ObjectShell
			setText(((IdObjectShell<?>) value).toString(showClass, showParent));
			if (selectedBone != null && selectedBone.getNewParentShell() == value
					|| selectedObject != null && selectedObject.getNewParentShell() == value) {
				bg = selectedOwnerBgCol;
				fg = selectedOwnerFgCol;
			} else if (bonesNotInAllMatricies.contains(value)) {
				new Color(150, 80, 80);
				bg = new Vec3(150, 80, 80);
				fg = otherOwnerFgCol;
			}
		} else {
			setText(value.toString());
		}
		if (value instanceof IdObjectShell<?> && ((IdObjectShell<?>) value).getImportStatus() != IdObjectShell.ImportType.IMPORT) { // BoneShell
			bg = Vec3.getProd(bg, otherOwnerBgCol).normalize().scale(160);
			fg = Vec3.getProd(bg, otherOwnerFgCol).normalize().scale(60);
		}

		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}
}
