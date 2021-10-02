package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.VisibilitySource;

public class VisibilityShell {
	private final Named source;
	private final VisibilitySource visibilitySource;
	private final EditableModel model;
	private boolean isFromDonating;
	private boolean favorOld = true;
	private VisibilityShell newVisSource;
	private VisibilityShell oldVisSource;

	private boolean alwaysVisible = false;
	private boolean neverVisible = false;
	private boolean multipleSelected = false;

	public VisibilityShell(VisibilitySource vs, EditableModel whichModel, boolean isFromDonating) {
		source = (Named) vs;
		visibilitySource = vs;
		model = whichModel;
		this.isFromDonating = isFromDonating;
		if (visibilitySource instanceof TimelineContainer && visibilitySource.getVisibilityFlag() != null) {
			if (isFromDonating) {
				newVisSource = this;
			} else {
				oldVisSource = this;
			}
		}

	}

	public VisibilityShell(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
		this.neverVisible = !alwaysVisible;
		visibilitySource = null;
		source = null;
		model = null;
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

	public VisibilitySource getVisibilitySource() {
		return visibilitySource;
	}

	public EditableModel getModel() {
		return model;
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

	public VisibilityShell setFromDonating(boolean fromDonating) {
		isFromDonating = fromDonating;
		return this;
	}

	public VisibilityShell getNewVisSource() {
		return newVisSource;
	}

	public VisibilityShell setNewVisSource(VisibilityShell newVisSource) {
		this.newVisSource = newVisSource;
		return this;
	}

	public VisibilityShell getOldVisSource() {
		return oldVisSource;
	}

	public VisibilityShell setOldVisSource(VisibilityShell oldVisSource) {
		this.oldVisSource = oldVisSource;
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
			if(name.length() > 50){
				name = name.substring(0,50);
			}
			return model.getName() + ": " + name;
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
