package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

public class ActiveViewportWatcher implements ViewportListener {
	private Viewport viewport;

	@Override
	public void viewportChanged(final Viewport viewport) {
		this.viewport = viewport;

	}

	public Viewport getViewport() {
		return viewport;
	}
}
