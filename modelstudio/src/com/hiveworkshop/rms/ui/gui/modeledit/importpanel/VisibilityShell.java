package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.VisibilitySource;

class VisibilityShell {
	Named source;
	VisibilitySource visibilitySource;
	EditableModel model;
	boolean isFromDonating;
	private boolean favorOld = true;
	private VisibilityShell newVisSource;
	private VisibilityShell oldVisSource;

	private boolean alwaysVisible = false;
	private boolean neverVisible = false;

	public VisibilityShell(final Named n, final EditableModel whichModel) {
		source = n;
		model = whichModel;
	}

	public VisibilityShell(final VisibilitySource vs, final EditableModel whichModel, boolean isFromDonating) {
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
//		this.isFromDonating = isFromDonating;
	}

	public VisibilityShell(boolean alwaysVisible, boolean isFromDonating) {
		this.alwaysVisible = alwaysVisible;
		this.neverVisible = !alwaysVisible;
		this.isFromDonating = isFromDonating;
	}

	public Named getSource() {
		return source;
	}

	public VisibilityShell setSource(Named source) {
		this.source = source;
		return this;
	}

	public VisibilitySource getVisibilitySource() {
		return visibilitySource;
	}

	public VisibilityShell setVisibilitySource(VisibilitySource visibilitySource) {
		this.visibilitySource = visibilitySource;
		return this;
	}

	public EditableModel getModel() {
		return model;
	}

	public VisibilityShell setModel(EditableModel model) {
		this.model = model;
		return this;
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
			return model.getName() + ": " + source.getName();
		} else if (alwaysVisible && !neverVisible) {
			return "Always visible";
		} else if (neverVisible && !alwaysVisible) {
			return "Not visible";
		}
		return "Null";
	}
}
