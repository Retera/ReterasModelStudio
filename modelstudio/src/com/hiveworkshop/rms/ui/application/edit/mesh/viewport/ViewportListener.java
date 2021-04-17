package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

public class ViewportListener {
	private Viewport viewport;

	public void viewportChanged(final Viewport viewport) {
		this.viewport = viewport;

	}

	public Viewport getViewport() {
		return viewport;
	}
}
