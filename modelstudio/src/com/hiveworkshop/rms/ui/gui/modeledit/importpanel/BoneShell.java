package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;

import java.util.Arrays;

public class BoneShell {
	private final Bone bone;
	private BoneShell importBoneShell;
	private String modelName;
	private boolean showClass = false;
	private String name = "";
	private ImportType importStatus = ImportType.IMPORT;
	private IdObject oldParent;
	private BoneShell oldParentBs;
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

	public BoneShell getNewParentBs() {
		return newParentBs;
	}

	public BoneShell setNewParentBs(BoneShell newParentBs) {
		if (newParentBs.getImportStatus() == ImportType.DONTIMPORT) {
			newParentBs.setImportStatus(ImportType.IMPORT);
		}
		this.newParentBs = newParentBs;
		return this;
	}

	public BoneShell getOldParentBs() {
		return oldParentBs;
	}

	public BoneShell setParentBs(BiMap<IdObject, BoneShell> idObjectMap) {
		this.oldParentBs = idObjectMap.get(oldParent);
		this.newParentBs = idObjectMap.get(oldParent);
		return this;
	}

	public ImportType getImportStatus() {
		return importStatus;
	}

	public BoneShell setImportStatus(ImportType importStatus) {
		this.importStatus = importStatus;
		return this;
	}

	public BoneShell setImportStatus(int importStatus) {
		this.importStatus = ImportType.fromInt(importStatus);
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

//	static final String IMPORT = "Import this bone";
//	static final String MOTIONFROM = "Import motion to pre-existing:";
//	static final String LEAVE = "Do not import";

	public enum ImportType {
		DONTIMPORT("Do Not Import"), IMPORT("Import this bone"), MOTIONFROM("Import motion to pre-existing:");
		String dispText;

		ImportType(String s) {
			dispText = s;
		}

		public static String[] getDispList() {
			return Arrays.stream(values()).map(ImportType::getDispText).toArray(String[]::new);
		}

		public static ImportType fromInt(int i) {
			return values()[i];
		}

		public String getDispText() {
			return dispText;
		}
	}
}