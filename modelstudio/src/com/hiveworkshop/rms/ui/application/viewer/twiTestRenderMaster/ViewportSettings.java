package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

public class ViewportSettings {
	private boolean renderTextures = true;
	private boolean renderParticles = true;
	private boolean wireFrame = false;
	private boolean showNormals = false;
	private boolean show3dVerts = false;



	public ViewportSettings setRenderTextures(boolean renderTextures) {
		this.renderTextures = renderTextures;
		return this;
	}

	public ViewportSettings setRenderParticles(boolean renderParticles) {
		this.renderParticles = renderParticles;
		return this;
	}

	public ViewportSettings setWireFrame(boolean wireFrame) {
		this.wireFrame = wireFrame;
		return this;
	}

	public ViewportSettings setShowNormals(boolean showNormals) {
		this.showNormals = showNormals;
		return this;
	}

	public ViewportSettings setShow3dVerts(boolean show3dVerts) {
		this.show3dVerts = show3dVerts;
		return this;
	}

	public boolean isRenderTextures() {
		return renderTextures;
	}
	public boolean isRenderParticles() {
		return renderParticles;
	}

	public boolean isWireFrame() {
		return wireFrame;
	}

	public boolean isShowNormals() {
		return showNormals;
	}

	public boolean isShow3dVerts() {
		return show3dVerts;
	}
}
