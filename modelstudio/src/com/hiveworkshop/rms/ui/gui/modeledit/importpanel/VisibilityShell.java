package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;

public class VisibilityShell {
	private final Named source;
	private final TimelineContainer visibilitySource;
	private final String modelName;

	private final boolean isFromDonating;
	private boolean favorOld = true;
	private VisibilityShell recModAnimsVisSource;
	private VisibilityShell donModAnimsVisSource;
//	private VisibilityShell newVisSource;
//	private VisibilityShell oldVisSource;

	private boolean alwaysVisible = false;
	private boolean neverVisible = false;
	private boolean multipleSelected = false;

	public VisibilityShell(TimelineContainer vs, String modelName, boolean isFromDonating) {
		source = (Named) vs;
		visibilitySource = vs;
		this.modelName = modelName;
		this.isFromDonating = isFromDonating;
		if (visibilitySource != null && visibilitySource.getVisibilityFlag() != null) {
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
		visibilitySource = null;
		source = null;
		modelName = null;
		this.isFromDonating = false;
	}

	public VisibilityShell setMultiple(){
		alwaysVisible = false;
		neverVisible = false;
		multipleSelected = true;
		return this;
	}

	public Named getSource() {
		return source;
	}

	public TimelineContainer getVisibilitySource() {
		return visibilitySource;
	}

	public boolean isFavorOld() {
		return favorOld;
	}

	public VisibilityShell setFavorOld(boolean favorOld) {
		this.favorOld = favorOld;
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}

	public VisibilityShell getRecModAnimsVisSource() {
		return recModAnimsVisSource;
	}

	public VisibilityShell setRecModAnimsVisSource(VisibilityShell recModAnimsVisSource) {
		this.recModAnimsVisSource = recModAnimsVisSource;
		return this;
	}

	public VisibilityShell getDonModAnimsVisSource() {
		return donModAnimsVisSource;
	}

	public VisibilityShell setDonModAnimsVisSource(VisibilityShell donModAnimsVisSource) {
		this.donModAnimsVisSource = donModAnimsVisSource;
		return this;
	}

	public boolean isAlwaysVisible() {
		return alwaysVisible;
	}

	public VisibilityShell setAlwaysVisible(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
		this.neverVisible = !alwaysVisible;
		return this;
	}

	public boolean isNeverVisible() {
		return neverVisible;
	}

	public VisibilityShell setNeverVisible(boolean neverVisible) {
		this.neverVisible = neverVisible;
		this.alwaysVisible = !neverVisible;
		return this;
	}

	@Override
	public String toString() {
		if (source != null) {
//			return source.getName();
			String name = source.getName();
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
