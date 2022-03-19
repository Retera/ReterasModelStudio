package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.util.IterableListModel;

import java.util.ArrayList;

public class MatrixShell {
	private final Matrix matrix;
	private final ArrayList<IdObjectShell<?>> orgBones;
	private final IterableListModel<IdObjectShell<?>> newBones;
	private final boolean isFromDonating;
	private final boolean isHd;

	public MatrixShell(final Matrix m) {
		this(m, new ArrayList<>(), false, false);
	}


	public MatrixShell(final Matrix m, ArrayList<IdObjectShell<?>> orgBones) {
		this(m, orgBones, false, false);
	}

	public MatrixShell(final Matrix m, ArrayList<IdObjectShell<?>> orgBones, boolean isFromDonating) {
		this(m, orgBones, isFromDonating, false);
	}

	public MatrixShell(final Matrix m, ArrayList<IdObjectShell<?>> orgBones, boolean isFromDonating, boolean isHd) {
		matrix = m;
		this.orgBones = orgBones;
		newBones = new IterableListModel<>(orgBones);
		this.isFromDonating = isFromDonating;
		this.isHd = isHd;
	}

	public void resetMatrix() {
		newBones.clear();
		newBones.addAll(orgBones);
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public IterableListModel<IdObjectShell<?>> getNewBones() {
		return newBones;
	}

//	public MatrixShell setNewBones(IterableListModel<IdObjectShell<?>> newBones) {
//		this.newBones = newBones;
//		return this;
//	}

	public IdObjectShell<?> getHdBoneToUse() {
		if (!newBones.isEmpty() && newBones.get(0) != null && newBones.get(0).getShouldImport()) {
			return newBones.get(0);
		} else if (!orgBones.isEmpty() && orgBones.get(0) != null && orgBones.get(0).getShouldImport()) {
			return orgBones.get(0);
		} else if (!newBones.isEmpty() && newBones.get(0) != null) {
			System.out.println("should import new bone: " + newBones.get(0).getShouldImport());
		} else if (!orgBones.isEmpty() && orgBones.get(0) != null) {
			System.out.println("should import org bone: " + orgBones.get(0).getShouldImport());
		}
		return null;
	}

	public IdObjectShell<?> getHdBoneToMapFrom() {
		if (!orgBones.isEmpty()) {
			return orgBones.get(0);
		}
		return null;
	}

	public boolean isHd() {
		return isHd;
	}

	@Override
	public String toString() {
		return matrix.getName();
	}


	public void addNewBone(IdObjectShell<?> boneShell) {
		if (boneShell.getImportStatus() == IdObjectShell.ImportType.DONT_IMPORT) {
			boneShell.setImportStatus(IdObjectShell.ImportType.IMPORT);
		}
		newBones.addElement(boneShell);
	}

	public void removeNewBone(IdObjectShell<?> boneShell) {
		newBones.remove(boneShell);
	}

	public ArrayList<IdObjectShell<?>> getOrgBones() {
		return orgBones;
	}

	public void clearNewBones() {
		newBones.clear();
	}

	public int moveBone(IdObjectShell<?> boneShell, int step) {
		int index = newBones.indexOf(boneShell);
		if (index != -1) {
			int newIndex = Math.max(0, Math.min((index + step), (newBones.size() - 1)));
			newBones.remove(index);
			newBones.add(newIndex, boneShell);
			System.out.println("bone index: " + index + " -> " + newIndex + " (i+st: " + (index + step) + ", nbz-1: " + (newBones.size() - 1) + ") (" + boneShell.toString() + ")");
			return (newIndex);
		}
		return 0;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}
}