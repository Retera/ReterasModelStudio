package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.BiMap;

import java.util.ArrayList;
import java.util.List;

public class ObjectShell {
	private IdObject idObject;
	private String name;
	private Camera camera;
	private IdObject importBone;
	private String modelName;
	private boolean showClass = true;
	private boolean isFromDonating;

	private boolean shouldImport = true;
	private int importStatus = 0;
	private IdObject oldParent;
	private IdObject newParent;
	private BoneShell newParentBs;
	private BoneShell oldParentBs;

	public ObjectShell(final IdObject b) {
		idObject = b;
		if (b != null) {
			oldParent = idObject.getParent();
			name = idObject.getName();
		}
	}

	public ObjectShell(final IdObject b, boolean isFromDonating) {
		idObject = b;
		if (b != null) {
			oldParent = idObject.getParent();
			name = idObject.getName();
		}
		this.isFromDonating = isFromDonating;
	}

	public ObjectShell(final Camera c) {
		camera = c;
	}

	public ObjectShell(final Camera c, boolean isFromDonating) {
		camera = c;
		this.isFromDonating = isFromDonating;
		name = camera.getName();
	}

	public static List<IdObject> toBonesList(final List<BoneShell> boneShells) {
		final List<IdObject> bones = new ArrayList<>();
		for (final BoneShell bs : boneShells) {
			bones.add(bs.getBone());
		}
		return bones;
	}

	public IdObject getImportBone() {
		return importBone;
	}

	public void setImportBone(final IdObject b) {
		importBone = b;
	}

	public IdObject getIdObject() {
		return importBone;
	}

	public Camera getCamera() {
		return camera;
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

	public ObjectShell setModelName(String modelName) {
		this.modelName = modelName;
		return this;
	}

	public boolean getShowClass() {
		return showClass;
	}

	public ObjectShell setShowClass(boolean showClass) {
		this.showClass = showClass;
		return this;
	}

	@Override
	public String toString() {
		if (idObject == null && camera == null) {
			return "None";
		}
		String nameString = "";
//		if(modelName != null){
//			nameString += modelName + ": ";
//		}
		if (idObject != null) {
			if (showClass) {
				nameString += idObject.getClass().getSimpleName() + " \"" + idObject.getName() + "\"";
			} else {
				nameString += idObject.getName();
			}
			return nameString;
		}
		if (showClass) {
			nameString += camera.getClass().getSimpleName() + " \"" + camera.getName() + "\"";
		} else {
			nameString += camera.getName();
		}
		return nameString;
	}
	public String toString(boolean showClass, boolean showParent) {
		if (idObject == null && camera == null) {
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
		} else if (camera != null){
			return camera.getClass().getSimpleName();
		}
		return "";
	}
}
