package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;

import java.util.Arrays;

public class BoneShell extends IdObjectShell<Bone> {
//	private final Bone bone;
//	private final IdObject oldParent;
//	private BoneShell motionSrcShell;
//	private List<BoneShell> motionDestShells = new ArrayList<>(); // motion destinations
//	private final String modelName;
//	private boolean showClass;
//	private String name;
//	private ImportType importStatus = ImportType.IMPORT;
//	private BoneShell oldParentShell;
//	private BoneShell newParentShell;
//	private final boolean isFromDonating;

	public BoneShell(final Bone b) {
		this(b, false, "", false);
	}

	public BoneShell(final Bone b, boolean isFromDonating) {
		this(b, isFromDonating, "", false);
	}

	public BoneShell(final Bone b, boolean isFromDonating, String modelName) {
		this(b, isFromDonating, modelName, false);
	}

	public BoneShell(final Bone b, boolean isFromDonating, String modelName, boolean showClass) {
		super(b, isFromDonating, modelName, showClass);
//		bone = b;
//		if (b != null) {
//			name = b.getName();
//			oldParent = bone.getParent();
//		} else {
//			name = "";
//			oldParent = null;
//		}
//		this.isFromDonating = isFromDonating;
//		this.modelName = modelName;
//		this.showClass = showClass;
	}

//	public Bone getMotionSrcBone() {
//		if (motionSrcShell == null) {
//			return null;
//		}
//		return motionSrcShell.getBone();
////		return importBone;
//	}
//
//	public List<BoneShell> getMotionDestShells() {
//		return motionDestShells;
//	}
//
//	public BoneShell addMotionDest(BoneShell boneShell){
//		if(!motionDestShells.contains(boneShell)){
//			motionDestShells.add(boneShell);
//			boneShell.setMotionSrcShell(this);
//			importStatus = ImportType.MOTION_FROM;
//		}
//		return this;
//	}
//
//	public BoneShell removeMotionDest(BoneShell boneShell){
//		if(motionDestShells.remove(boneShell)){
//			boneShell.setMotionSrcShell(null);
//		}
//		return this;
//	}

//	public Bone getIdObject() {
//		return bone;
//	}

//	public IdObject getOldParent() {
//		return oldParent;
//	}

//	public BoneShell getNewParentShell() {
//		return newParentShell;
//	}

//	public BoneShell setNewParentShell(BoneShell newParentShell) {
//		if (newParentShell.getImportStatus() == ImportType.DONT_IMPORT) {
//			newParentShell.setImportStatus(ImportType.IMPORT);
//		}
//		this.newParentShell = newParentShell;
//		return this;
//	}

//	public BoneShell getOldParentShell() {
//		return oldParentShell;
//	}

//	public BoneShell setParentBs(BiMap<IdObject, BoneShell> idObjectMap) {
//		this.oldParentShell = idObjectMap.get(oldParent);
//		this.newParentShell = idObjectMap.get(oldParent);
//		return this;
//	}

//	public ImportType getImportStatus() {
//		return importStatus;
//	}

//	public BoneShell setImportStatus(ImportType importStatus) {
//		this.importStatus = importStatus;
//		return this;
//	}
//
//	public BoneShell setImportStatus(int importStatus) {
//		this.importStatus = ImportType.fromInt(importStatus);
//		return this;
//	}

//	public String getModelName() {
//		return modelName;
//	}

//	public boolean isShowClass() {
//		return showClass;
//	}
//
//	public BoneShell setShowClass(boolean showClass) {
//		this.showClass = showClass;
//		return this;
//	}

//	@Override
//	public String toString() {
//		if (bone == null) {
//			return "None";
//		}
//		String stringToReturn = "";
//		if (modelName != null && !modelName.equals("")) {
//			stringToReturn += modelName + ": ";
//		}
//		if (showClass) {
//			stringToReturn += getClassName() + " ";
//		}
//		stringToReturn += "\"" + bone.getName() + "\"";
//		return stringToReturn;
////		if (showClass) {
////			if (modelName == null) {
////				return bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
////			} else {
////				return modelName + ": " + bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"";
////			}
////		} else {
////			if (modelName == null) {
////				return bone.getName();
////			} else {
////				return modelName + ": " + bone.getName();
////			}
////		}
//	}
//	public String toString(boolean showClass, boolean showParent) {
//		if (bone == null) {
//			return "None";
//		}
//		String stringToReturn = "";
//		if (modelName != null) {
//			stringToReturn += modelName + ": ";
//		}
//		if (showClass) {
//			stringToReturn += "(" + bone.getClass().getSimpleName() + ") ";
//		}
//		stringToReturn += name;
//		if (showParent){
//			if (oldParentShell == null && oldParent == null) {
//				stringToReturn += "; (no parent)";
//			} else if (oldParentShell != null) {
//				stringToReturn += "; " + oldParentShell.getName();
//			} else {
//				stringToReturn += "; " + oldParent.getName();
//			}
//		}
//		return stringToReturn;
//	}


//	public String getName() {
//		return name;
//	}
//
//	public BoneShell setName(String name) {
//		this.name = name;
//		return this;
//	}

//	public BoneShell getMotionSrcShell() {
//		return motionSrcShell;
//	}
//
//	public BoneShell setMotionSrcShell(BoneShell motionSrcShell) {
//		if(this.motionSrcShell != motionSrcShell){
//			this.motionSrcShell = motionSrcShell;
//			motionSrcShell.addMotionDest(this);
//		}
//		return this;
//	}

//	public boolean isFromDonating() {
//		return isFromDonating;
//	}

//	static final String IMPORT = "Import this bone";
//	static final String MOTIONFROM = "Import motion to pre-existing:";
//	static final String LEAVE = "Do not import";

	public enum ImportType {
		DONT_IMPORT("Do Not Import"),
		IMPORT("Import this bone"),
		//		MOTION_FROM("Import motion to pre-existing:"),
		MOTION_FROM("Import motion into:"),
		RECEIVE_MOTION("Receive motion from:");
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

//	private String getClassName(){
//		if (bone != null){
//			return bone.getClass().getSimpleName();
//		}
//		return "";
//	}
}