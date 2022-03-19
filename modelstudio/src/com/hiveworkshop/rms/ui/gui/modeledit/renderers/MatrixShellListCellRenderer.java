package com.hiveworkshop.rms.ui.gui.modeledit.renderers;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.MatrixShell;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

public class MatrixShellListCellRenderer extends DefaultListCellRenderer {
	protected static final Vec3 recModelColor = new Vec3(200, 255, 255);
	protected static final Vec3 donModelColor = new Vec3(220, 180, 255);
	protected static final Vec3 selectedOwnerBgCol = new Vec3(130, 230, 170);
	protected static final Vec3 selectedOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 otherOwnerBgCol = new Vec3(160, 160, 160);
	protected static final Vec3 emptyBgCol = new Vec3(220, 160, 160);
	protected static final Vec3 otherOwnerFgCol = new Vec3(60, 60, 60);
	protected static final Vec3 noOwnerBgCol = new Vec3(255, 255, 255);
	protected static final Vec3 noOwnerFgCol = new Vec3(0, 0, 0);
	protected static final Vec3 hLAdjBgCol = new Vec3(0, 0, 50);
	boolean showParent = false;

	MatrixShell selectedMatrixShell;
//	IdObjectShell<?> selectedObject;
//	boolean showClass = false;

//	Set<IdObjectShell<Bone>> bonesInAllMatricies = new HashSet<>();
//	Set<IdObjectShell<Bone>> bonesNotInAllMatricies = new HashSet<>();


	public void setSelectedMatrixShell(MatrixShell boneShell) {
		selectedMatrixShell = boneShell;
	}


//	public void addInAllBone(IdObjectShell<Bone> boneShell) {
//		bonesInAllMatricies.add(boneShell);
//	}
//
//	public void removeInAllBone(IdObjectShell<Bone> boneShell) {
//		bonesInAllMatricies.remove(boneShell);
//	}
//
//	public void addNotInAllBone(IdObjectShell<Bone> boneShell) {
//		bonesNotInAllMatricies.add(boneShell);
//	}
//
//	public void removeNotInAllBone(IdObjectShell<Bone> boneShell) {
//		bonesNotInAllMatricies.remove(boneShell);
//	}
//
//	public void addNotInAllBone(Collection<IdObjectShell<Bone>> boneShells) {
//		bonesNotInAllMatricies.addAll(boneShells);
//	}
//
//	public void removeNotInAllBone(Collection<IdObjectShell<Bone>> boneShells) {
//		bonesNotInAllMatricies.removeAll(boneShells);
//	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSel, boolean hasFoc) {
		super.getListCellRendererComponent(list, value, index, isSel, hasFoc);

		Vec3 bg = noOwnerBgCol;
		Vec3 fg = noOwnerFgCol;

		// ToDo check if type matters for this renders
		if (value instanceof MatrixShell) { // ObjectShell
			setText(value.toString());
			if (((MatrixShell) value).getNewBones().isEmpty()) {
//				bg = otherOwnerBgCol;
//				fg = otherOwnerFgCol;
				bg = emptyBgCol;
//				new Color(220, 160, 160);
//				new Color(150, 80, 80);
			}
//			else if (bonesNotInAllMatricies.contains(value)) {
//				new Color(150, 80, 80);
//				bg = new Vec3(150, 80, 80);
//				fg = otherOwnerFgCol;
//			}
		} else {
			setText(value.toString());
		}
//		if (value instanceof IdObjectShell<?> && ((IdObjectShell<?>) value).getImportStatus() != IdObjectShell.ImportType.IMPORT) { // BoneShell
//			bg = Vec3.getProd(bg, otherOwnerBgCol).normalize().scale(160);
//			fg = Vec3.getProd(bg, otherOwnerFgCol).normalize().scale(60);
//		}

		if (isSel) {
			bg = Vec3.getSum(bg, hLAdjBgCol);
		}

		this.setBackground(bg.asIntColor());
		this.setForeground(fg.asIntColor());

		return this;
	}
}
