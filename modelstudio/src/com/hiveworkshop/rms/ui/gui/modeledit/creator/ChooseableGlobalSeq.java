package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

public class ChooseableGlobalSeq implements CreatorModelingPanel.ChooseableTimeRange<GlobalSeq> {
	private final GlobalSeq globalSeq;

	public ChooseableGlobalSeq(GlobalSeq globalSeq) {
		this.globalSeq = globalSeq;
	}

	@Override
	public void applyTo(TimeEnvironmentImpl timeEnvironment) {
		if (timeEnvironment != null) {
			timeEnvironment.setGlobalSeq(globalSeq);
		}
	}

	@Override
	public String toString() {
		return globalSeq.toString();
	}

	@Override
	public GlobalSeq getThing() {
		return globalSeq;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + (globalSeq == null ? 0 : globalSeq.hashCode());
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
		ChooseableGlobalSeq other = (ChooseableGlobalSeq) obj;
		if (globalSeq == null) {
			return other.globalSeq == null;
		} else {
			return globalSeq.equals(other.globalSeq);
		}
	}
}
