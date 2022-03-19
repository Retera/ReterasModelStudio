package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

public abstract class ChoosableTimeRange {
	private final String text;

	public ChoosableTimeRange(String text) {
		this.text = text;
	}

	public void applyTo(TimeEnvironmentImpl timeEnvironment) {
	}
	public String toString() {
		return text;
	}

	public Object getThing() {
		return text;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + (text == null ? 0 : text.hashCode());
		return result;
	}

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
		ChoosableTimeRange other = (ChoosableTimeRange) obj;
		if (text == null) {
			return other.text == null;
		} else {
			return text.equals(other.text);
		}
	}
}
