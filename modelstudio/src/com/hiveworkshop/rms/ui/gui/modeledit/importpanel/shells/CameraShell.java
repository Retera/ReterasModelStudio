package com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells;

import com.hiveworkshop.rms.editor.model.Camera;

public class CameraShell extends AbstractShell {
	private String name;
	private final Camera camera;
	private String modelName;
	private boolean showClass = true;

	private boolean shouldImport = true;
	private int importStatus = 0;


	public CameraShell(final Camera c, boolean isFromDonating) {
		super(isFromDonating);
		camera = c;
		name = camera.getName();
	}


	public Camera getCamera() {
		return camera;
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

	public CameraShell setShouldImport(boolean shouldImport) {
		this.shouldImport = shouldImport;
		return this;
	}

	public String getModelName() {
		return modelName;
	}

	public CameraShell setModelName(String modelName) {
		this.modelName = modelName;
		return this;
	}

	public boolean getShowClass() {
		return showClass;
	}

	public CameraShell setShowClass(boolean showClass) {
		this.showClass = showClass;
		return this;
	}

	@Override
	public String toString() {
		if (camera == null) {
			return "None";
		}
		String nameString = "";
//		if(modelName != null){
//			nameString += modelName + ": ";
//		}
		if (showClass) {
			nameString += camera.getClass().getSimpleName() + " \"" + camera.getName() + "\"";
		} else {
			nameString += camera.getName();
		}
		return nameString;
	}

	public String toString(boolean showClass, boolean showParent) {
		if (camera == null) {
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
		return stringToReturn;
	}

	private String getClassName() {
		if (camera != null) {
			return camera.getClass().getSimpleName();
		}
		return "";
	}
}
