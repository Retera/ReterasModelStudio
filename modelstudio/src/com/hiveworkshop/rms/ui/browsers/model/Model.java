package com.hiveworkshop.rms.ui.browsers.model;

// ToDo should be renamed to modelFile
public class Model implements Comparable<Model> {
	private String cachedIcon;
	private String displayName;
	private String filepath;

	public Model() {

	}

	public Model(String cachedIcon, String displayName, String filepath) {
		this.cachedIcon = cachedIcon;
		this.displayName = displayName;
		this.filepath = filepath;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public String getCachedIcon() {
		return cachedIcon;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFilepath() {
		return filepath;
	}

	public Model setCachedIcon(String cachedIcon) {
		this.cachedIcon = cachedIcon;
		return this;
	}

	public Model setDisplayName(String displayName) {
		this.displayName = displayName;
		return this;
	}

	public Model setFilepath(String filepath) {
		this.filepath = filepath;
		return this;
	}

	@Override
	public int compareTo(Model otherModel) {
		return displayName.compareToIgnoreCase(otherModel.getDisplayName());
	}
}
