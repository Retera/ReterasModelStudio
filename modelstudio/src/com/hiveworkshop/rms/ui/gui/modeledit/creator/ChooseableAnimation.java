package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

public class ChooseableAnimation implements CreatorModelingPanel.ChooseableTimeRange {
	private final Animation animation;

	public ChooseableAnimation(Animation animation) {
		this.animation = animation;
	}

	@Override
	public void applyTo(TimeEnvironmentImpl timeEnvironment) {
		timeEnvironment.setBounds(animation);
	}

	@Override
	public String toString() {
		return animation.getName();
	}

	@Override
	public Object getThing() {
		return animation;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + (animation == null ? 0 : animation.hashCode());
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
		ChooseableAnimation other = (ChooseableAnimation) obj;
		if (animation == null) {
			return other.animation == null;
		} else {
			return animation.equals(other.animation);
		}
	}
}
