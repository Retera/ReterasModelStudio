package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;

public class VisibilityShell<T extends TimelineContainer & Named> {
	private final T item;
	private final String modelName;

	private final boolean isFromDonating;
	private boolean favorOld = true;
	private VisibilityShell<?> visSource;
	private VisibilityShell<?> recModAnimsVisSource;
	private VisibilityShell<?> donModAnimsVisSource;

	private boolean alwaysVisible = false;
	private boolean neverVisible = false;
	private boolean multipleSelected = false;

	public VisibilityShell(T vs, String modelName, boolean isFromDonating) {
		item = vs;
		this.modelName = modelName;
		this.isFromDonating = isFromDonating;
		if (item != null && item.getVisibilityFlag() != null) {
			if (isFromDonating) {
				recModAnimsVisSource = this;
			} else {
				donModAnimsVisSource = this;
			}
		}
	}

	public VisibilityShell(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
		this.neverVisible = !alwaysVisible;
		item = null;
		modelName = null;
		this.isFromDonating = false;
	}

	public VisibilityShell<T> setMultiple(){
		alwaysVisible = false;
		neverVisible = false;
		multipleSelected = true;
		return this;
	}

	public T getSource() {
		return item;
	}
	public Named getNameSource() {
		return item;
	}

	public boolean isFavorOld() {
		return favorOld;
	}

	public VisibilityShell<T> setFavorOld(boolean favorOld) {
		this.favorOld = favorOld;
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}

	public VisibilityShell<?> getVisSource() {
		return visSource;
	}

	public VisibilityShell<T> setVisSource(VisibilityShell<?> visSource) {
		this.visSource = visSource;
		return this;
	}

	public VisibilityShell<?> getRecModAnimsVisSource() {
		return recModAnimsVisSource;
	}

	public VisibilityShell<T> setRecModAnimsVisSource(VisibilityShell<?> recModAnimsVisSource) {
		this.recModAnimsVisSource = recModAnimsVisSource;
		return this;
	}

	public VisibilityShell<?> getDonModAnimsVisSource() {
		return donModAnimsVisSource;
	}

	public VisibilityShell<T> setDonModAnimsVisSource(VisibilityShell<?> donModAnimsVisSource) {
		this.donModAnimsVisSource = donModAnimsVisSource;
		return this;
	}

	public boolean isAlwaysVisible() {
		return alwaysVisible;
	}

	public VisibilityShell<T> setAlwaysVisible(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
		this.neverVisible = !alwaysVisible;
		return this;
	}

	public boolean isNeverVisible() {
		return neverVisible;
	}

	public VisibilityShell<T> setNeverVisible(boolean neverVisible) {
		this.neverVisible = neverVisible;
		this.alwaysVisible = !neverVisible;
		return this;
	}

	@Override
	public String toString() {
		if (item != null) {
//			return item.getName();
			String name = item.getName();
			if (name.length() > 50) {
				name = name.substring(0, 50);
			}
			return modelName + ": " + name;
		} else if (alwaysVisible && !neverVisible) {
			return "Always visible";
		} else if (neverVisible && !alwaysVisible) {
			return "Not visible";
		} else if (multipleSelected) {
			return "Multiple selected";
		}
		return "Null";
	}
}
