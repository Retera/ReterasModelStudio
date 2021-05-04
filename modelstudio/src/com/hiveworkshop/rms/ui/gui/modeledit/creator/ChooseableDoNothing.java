package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

public class ChooseableDoNothing implements CreatorModelingPanel.ChooseableTimeRange {
	private final String text;

	public ChooseableDoNothing(String text) {
		this.text = text;
	}

	@Override
	public void applyTo(TimeEnvironmentImpl timeEnvironment) {
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public Object getThing() {
		return text;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + (text == null ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ChooseableDoNothing other = (ChooseableDoNothing) obj;
		if (text == null) {
			return other.text == null;
		} else {
			return text.equals(other.text);
		}
	}
}
