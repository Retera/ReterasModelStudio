package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;

import java.util.ArrayList;
import java.util.List;

public class BoneShell {
	public final Bone bone;
	public Bone importBone;
	public BoneShell importBoneShell;
	public String modelName;
	//	public BonePanel panel;
	public boolean showClass = false;
	private String name = "";
	private boolean shouldImportBone = true;
	private int importStatus = 0;
	private IdObject oldParent;
	private IdObject newParent;
	private BoneShell parentBs;

	public BoneShell(final Bone b) {
		bone = b;
		if (b != null) {
			name = b.getName();
			oldParent = bone.getParent();
			newParent = bone.getParent();
		}
	}

	public static List<Bone> toBonesList(final List<BoneShell> boneShells) {
		final List<Bone> bones = new ArrayList<>();
		for (final BoneShell bs : boneShells) {
			bones.add(bs.bone);
		}
		return bones;
	}

	public Bone getImportBone() {
		return importBone;
	}

	public void setImportBone(final Bone b) {
		importBone = b;
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

	public BoneShell getParentBs() {
		return parentBs;
	}

	public BoneShell setParentBs(BoneShell parentBs) {
		this.parentBs = parentBs;
		return this;
	}

	public BoneShell setParentBs(BiMap<IdObject, BoneShell> idObjectMap) {
		this.parentBs = idObjectMap.get(newParent);
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
		this.importBone = importBoneShell.getBone();
		return this;
	}
}