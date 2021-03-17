package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;

public class BoneShell {
	private final Bone bone;
	private BoneShell importBoneShell;
	private String modelName;
	private boolean showClass = false;
	private String name = "";
	private boolean shouldImportBone = true;
	private int importStatus = 0;
	private IdObject oldParent;
	private BoneShell oldParentBs;
	private IdObject newParent;
	private BoneShell newParentBs;
	boolean isFromDonating;

	public BoneShell(final Bone b) {
		bone = b;
		if (b != null) {
			name = b.getName();
			oldParent = bone.getParent();
		}
	}

	public BoneShell(final Bone b, boolean isFromDonating) {
		bone = b;
		if (b != null) {
			name = b.getName();
			oldParent = bone.getParent();
		}
		this.isFromDonating = isFromDonating;
	}

	public Bone getImportBone() {
		if (importBoneShell == null) {
			return null;
		}
		return importBoneShell.getBone();
//		return importBone;
	}

	public Bone getBone() {
		return bone;
	}

	public IdObject getOldParent() {
		return oldParent;
	}

	public IdObject getNewParent() {
		return newParent;
	}

	public BoneShell setNewParent(IdObject newParent) {
		this.newParent = newParent;
		return this;
	}

	public BoneShell getNewParentBs() {
		return newParentBs;
	}

	public BoneShell setNewParentBs(BoneShell newParentBs) {
		this.newParentBs = newParentBs;
		return this;
	}

	public BoneShell getOldParentBs() {
		return oldParentBs;
	}

	public BoneShell setParentBs(BiMap<IdObject, BoneShell> idObjectMap) {
		this.oldParentBs = idObjectMap.get(oldParent);
		return this;
	}

	public int getImportStatus() {
		return importStatus;
	}

	public BoneShell setImportStatus(int importStatus) {
		this.importStatus = importStatus;
		if (importStatus == 0) {
			shouldImportBone = true;
		} else if (importStatus == 2) {
			shouldImportBone = false;
		}
		return this;
	}

	public boolean isShouldImportBone() {
		return shouldImportBone;
	}

	public BoneShell setShouldImportBone(boolean shouldImportBone) {
		this.shouldImportBone = shouldImportBone;
		return this;
	}

	public String getModelName() {
		return modelName;
	}

	public BoneShell setModelName(String modelName) {
		this.modelName = modelName;
		return this;
	}

	public boolean isShowClass() {
		return showClass;
	}

	public BoneShell setShowClass(boolean showClass) {
		this.showClass = showClass;
		return this;
	}

	@Override
	public String toString() {
		if (bone == null) {
			return "None";
		}
		if (showClass) {
			if (modelName == null) {
				return bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
			} else {
				return modelName + ": " + bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
			}
		} else {
			if (modelName == null) {
				return bone.getName();
			} else {
				return modelName + ": " + bone.getName();
			}
		}
	}
	public String toString(boolean showClass, boolean showParent) {
		if (bone == null) {
			return "None";
		}
		String stringToReturn = "";
		if (modelName != null) {
			stringToReturn += modelName + ": ";
		}
		if (showClass) {
			stringToReturn += "(" + bone.getClass().getSimpleName() + ") ";
		}
		stringToReturn += name;
		if (showParent){
			if (oldParentBs == null && oldParent == null) {
				stringToReturn += "; (no parent)";
			} else if (oldParentBs != null) {
				stringToReturn += "; " + oldParentBs.getName();
			} else {
				stringToReturn += "; " + oldParent.getName();
			}
		}
		return stringToReturn;
	}



	public String getName() {
		return name;
	}

	public BoneShell setName(String name) {
		this.name = name;
		return this;
	}

	public BoneShell getImportBoneShell() {
		return importBoneShell;
	}

	public BoneShell setImportBoneShell(BoneShell importBoneShell) {
		this.importBoneShell = importBoneShell;
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}
}