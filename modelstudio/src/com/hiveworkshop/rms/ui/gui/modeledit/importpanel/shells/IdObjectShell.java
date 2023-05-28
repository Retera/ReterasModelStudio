package com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IdObjectShell<T extends IdObject> extends AbstractShell {
	private final T idObject;
	private String name;
	private final String modelName;
	private boolean showClass;

	private IdObjectShell<?> motionSrcShell;
	private final List<IdObjectShell<?>> motionDestShells = new ArrayList<>(); // motion destinations
	private boolean shouldImport = true;
	private boolean prioritizeMotionFromSelf = false;
	private ImportType importStatus = ImportType.IMPORT;
	private final IdObject oldParent;
	private IdObjectShell<?> newParentShell;
	private IdObjectShell<?> oldParentShell;

	public IdObjectShell(T object) {
		this(object, false, "", true);
	}

	public IdObjectShell(T object, boolean isFromDonating) {
		this(object, isFromDonating, "", true);
	}

	public IdObjectShell(T object, boolean isFromDonating, String modelName) {
		this(object, isFromDonating, modelName, true);
	}

	public IdObjectShell(T object, boolean isFromDonating, String modelName, boolean showClass) {
		super(isFromDonating);
		idObject = object;
		if (object != null) {
			oldParent = idObject.getParent();
			name = idObject.getName();
		} else {
			oldParent = null;
			name = "none";
		}
		this.modelName = modelName;
		this.showClass = showClass;
	}

	public T getIdObject() {
		return idObject;
	}

	public IdObject getOldParent() {
		return oldParent;
	}


	public IdObjectShell<?> getNewParentShell() {
		return newParentShell;
	}

	public IdObjectShell<T> setNewParentShell(IdObjectShell<?> newParentShell) {
		if (newParentShell != null && newParentShell.getImportStatus() == ImportType.DONT_IMPORT) {
			newParentShell.setImportStatus(ImportType.IMPORT);
		}
		this.newParentShell = newParentShell;
		return this;
	}

	public IdObjectShell<?> getOldParentShell() {
		return oldParentShell;
	}

	public void setParent(IdObjectShell<?> parent) {
		newParentShell = parent;
	}

	public IdObjectShell<T> setParentBs(BiMap<IdObject, IdObjectShell<?>> idObjectMap) {
		this.oldParentShell = idObjectMap.get(oldParent);
		this.newParentShell = idObjectMap.get(oldParent);
		return this;
	}

	public ImportType getImportStatus() {
		return importStatus;
	}

	public IdObjectShell<T> setImportStatus(int importStatus) {
		this.importStatus = ImportType.fromInt(importStatus);

		shouldImport = this.importStatus == ImportType.IMPORT || this.importStatus == ImportType.RECEIVE_MOTION;
		return this;
	}

	public IdObjectShell<T> setImportStatus(ImportType importStatus) {
		this.importStatus = importStatus;
		shouldImport = importStatus == ImportType.IMPORT || importStatus == ImportType.RECEIVE_MOTION;
		return this;
	}

	public boolean getShouldImport() {
		return shouldImport;
	}

	public IdObjectShell<T> setShouldImport(boolean shouldImport) {
		this.shouldImport = shouldImport;
//		if (shouldImport) {
//			importStatus = ImportType.IMPORT;
//		} else {
//			importStatus = ImportType.DONT_IMPORT;
//		}
		return this;
	}

	public IdObjectShell<T> setPrioritizeMotionFromSelf(boolean prioritizeMotionFromSelf) {
		this.prioritizeMotionFromSelf = prioritizeMotionFromSelf;
		return this;
	}

	public boolean isPrioritizeMotionFromSelf() {
		return prioritizeMotionFromSelf;
	}

	public String getModelName() {
		return modelName;
	}

	public boolean getShowClass() {
		return showClass;
	}

	public IdObjectShell<T> setShowClass(boolean showClass) {
		this.showClass = showClass;
		return this;
	}

	public String getName() {
		return name;
	}

	public IdObjectShell<T> setName(String name) {
		this.name = name;
		return this;
	}

	public IdObjectShell<?> getMotionSrcShell() {
		return motionSrcShell;
	}

	public IdObjectShell<T> setMotionSrcShell(IdObjectShell<?> motionSrcShell) {
		if (this.motionSrcShell != motionSrcShell) {
			if (this.motionSrcShell != null) {
				this.motionSrcShell.removeMotionDest(this);
			}
			this.motionSrcShell = motionSrcShell;
			if(motionSrcShell != null){
				this.motionSrcShell.addMotionDest(this);
//				for (IdObjectShell<?> idObjectShell : motionDestShells) {
//					idObjectShell.setMotionSrcShell(null);
//				}
//				importStatus = ImportType.RECEIVE_MOTION;
			}
		}
		return this;
	}

	public IdObjectShell<T> addMotionDest(IdObjectShell<?> boneShell) {
		if (!motionDestShells.contains(boneShell)) {
			if(motionSrcShell != null){
				motionSrcShell.removeMotionDest(this);
			}
			motionDestShells.add(boneShell);
			boneShell.setMotionSrcShell(this);
//			importStatus = ImportType.MOTION_FROM;
//			if(motionSrcShell != null){
//				motionSrcShell.removeMotionDest(this);
//				motionSrcShell = null;
//			}
		}
		return this;
	}

	public IdObjectShell<T> removeMotionDest(IdObjectShell<?> boneShell) {
		if (motionDestShells.remove(boneShell)) {
			boneShell.setMotionSrcShell(null);
		}
		return this;
	}

	public List<IdObjectShell<?>> getMotionDestShells() {
		return motionDestShells;
	}

	@Override
	public String toString() {
		if (idObject == null) {
			return "None";
		}
		String nameString = "";
		if (showClass) {
			nameString += idObject.getClass().getSimpleName() + " \"" + idObject.getName() + "\"";
		} else {
			nameString += idObject.getName();
		}
		return nameString;
	}

	public String toString(boolean showClass, boolean showParent) {
		if (idObject == null) {
			return "None";
		}
		String stringToReturn = "";
		if (modelName != null && !modelName.equals("")) {
			stringToReturn += modelName + ": ";
		}
		if (showClass) {
			stringToReturn += "(" + getClassName() + ") ";
		}
		stringToReturn += name;
		if (showParent) {
			if (oldParentShell == null && oldParent == null) {
				stringToReturn += "; (no parent)";
			} else if (oldParentShell != null) {
				stringToReturn += "; " + oldParentShell.getName();
			} else {
				stringToReturn += "; " + oldParent.getName();
			}
		}
		return stringToReturn;
	}

	private String getClassName() {
		if (idObject != null) {
			return idObject.getClass().getSimpleName();
		}
		return "";
	}

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
}
