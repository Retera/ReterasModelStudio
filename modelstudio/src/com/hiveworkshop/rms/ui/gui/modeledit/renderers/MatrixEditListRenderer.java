package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.IdObjectShell;
import com.hiveworkshop.rms.ui.util.AbstractSnapshottingListCellRenderer2D;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MatrixEditListRenderer extends AbstractSnapshottingListCellRenderer2D<Bone> {
	boolean showParent = false;

	Bone selectedBone;
	IdObject selectedObject;
	boolean showClass = false;

	Set<Bone> bonesInAllMatricies = new HashSet<>();
	Set<Bone> bonesNotInAllMatricies = new HashSet<>();

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

	public void setSelectedBoneShell(Bone bone) {
		selectedBone = bone;
	}

	public void setSelectedObjectShell(Bone object) {
		selectedObject = object;
	}


	@Override
	protected boolean isFromDonating(Bone value) {
		if (value != null && other != null) {
			return other.contains(value);
		} else if (value != null && model != null){
			return !model.contains(value);
		}
		return false;
	}

	@Override
	protected boolean isFromReceiving(Bone value) {
		if (value != null && model != null) {
			return model.contains(value);
		} else if (value != null && other != null){
			return !other.contains(value);
		}
		return false;
	}

	@Override
	protected Bone valueToTyped(final Object value) {
		return (Bone) value;
	}

	@Override
	protected boolean contains(EditableModel model, final Bone object) {
		if (model != null) {
			return model.contains(object);
		}
		return false;
	}

	@Override
	protected Vec3 getRenderVertex(final Bone value) {
		return value.getPivotPoint();
	}


	public void addInAllBone(Bone boneShell) {
		bonesInAllMatricies.add(boneShell);
	}

	public void removeInAllBone(Bone boneShell) {
		bonesInAllMatricies.remove(boneShell);
	}

	public void addNotInAllBone(Bone boneShell) {
		bonesNotInAllMatricies.add(boneShell);
	}

	public void removeNotInAllBone(Bone boneShell) {
		bonesNotInAllMatricies.remove(boneShell);
	}

	public void addNotInAllBone(Collection<Bone> boneShells) {
		bonesNotInAllMatricies.addAll(boneShells);
	}

	public void removeNotInAllBone(Collection<Bone> boneShells) {
		bonesNotInAllMatricies.removeAll(boneShells);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		// ToDo check if type matters for this renders
		if (value instanceof IdObject) { // ObjectShell
			setText(getObjectString((IdObject) value, showClass, showParent));
			if (selectedBone != null && selectedBone.getParent() == value
					|| selectedObject != null && selectedObject.getParent() == value) {
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

	private String getObjectString(IdObject idObject, boolean showClass, boolean showParent) {
		if (idObject == null) {
			return "None";
		}
		String stringToReturn = "";
		if (showClass) {
			stringToReturn += "(" + getClassName(idObject) + ") ";
		}
		stringToReturn += idObject.getName();
		if (showParent) {
			if (idObject.getParent() == null) {
				stringToReturn += "; (no parent)";
			} else {
				stringToReturn += "; " + idObject.getParent().getName();
			}
		}
		return stringToReturn;
	}

	private String getClassName(IdObject idObject) {
		if (idObject != null) {
			return idObject.getClass().getSimpleName();
		}
		return "";
	}
}
