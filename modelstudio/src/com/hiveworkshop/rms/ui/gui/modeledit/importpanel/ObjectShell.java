package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;


public class ObjectShell {
	private final IdObject idObject;
	private String name;
	private final String modelName;
	private boolean showClass;
	private final boolean isFromDonating;

	private boolean shouldImport = true;
	private int importStatus = 0;
	private final IdObject oldParent;
	private IdObject newParent;
	private BoneShell newParentBs;
	private BoneShell oldParentBs;

	public ObjectShell(IdObject object) {
		this(object, false, "", true);
	}

	public ObjectShell(IdObject object, boolean isFromDonating) {
		this(object, isFromDonating, "", true);
	}
	public ObjectShell(IdObject object, boolean isFromDonating, String modelName) {
		this(object, isFromDonating, modelName, true);
	}
	public ObjectShell(IdObject object, boolean isFromDonating, String modelName, boolean showClass) {
		idObject = object;
		if (object != null) {
			oldParent = idObject.getParent();
			name = idObject.getName();
		} else {
			oldParent = null;
		}
		this.isFromDonating = isFromDonating;
		this.modelName = modelName;
		this.showClass = showClass;
	}

	public IdObject getIdObject() {
		return idObject;
	}

	public IdObject getOldParent() {
		return oldParent;
	}

	public IdObject getNewParent() {
		return newParent;
	}

	public void setParentBone(IdObject parent) {
		newParent = parent;
	}

	public BoneShell getNewParentBs() {
		return newParentBs;
	}

	public ObjectShell setNewParentBs(BoneShell newParentBs) {
		this.newParentBs = newParentBs;
		if (newParentBs != null) {
			newParent = newParentBs.getBone();
		} else {
			newParent = null;
		}
		return this;
	}

	public BoneShell getOldParentBs() {
		return oldParentBs;
	}

	public void setParent(BoneShell parent) {
		newParent = parent.getBone();
		newParentBs = parent;
	}

	public ObjectShell setParentBs(BiMap<IdObject, BoneShell> idObjectMap) {
		this.oldParentBs = idObjectMap.get(oldParent);
		this.newParentBs = idObjectMap.get(oldParent);
		return this;
	}

	public int getImportStatus() {
		return importStatus;
	}

	public void setImportStatus(int importStatus) {
		this.importStatus = importStatus;
		if (importStatus == 0) {
			shouldImport = true;
		} else if (importStatus == 2) {
			shouldImport = false;
		}
	}

	public boolean getShouldImport() {
		return shouldImport;
	}

	public ObjectShell setShouldImport(boolean shouldImport) {
		this.shouldImport = shouldImport;
		return this;
	}

	public String getModelName() {
		return modelName;
	}

	public boolean getShowClass() {
		return showClass;
	}

	public ObjectShell setShowClass(boolean showClass) {
		this.showClass = showClass;
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
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
		if (modelName != null) {
			stringToReturn += modelName + ": ";
		}
		if (showClass) {
			stringToReturn += "(" + getClassName() + ") ";
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

	private String getClassName(){
		if (idObject != null){
			return idObject.getClass().getSimpleName();
		}
		return "";
	}
}
