package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

public class ShaderManager {
	private ShaderPipeline hdPipeline;
	private ShaderPipeline sdPipeline;
	private ShaderPipeline bonePipeline;
	private ShaderPipeline vertPipeline;
	private ShaderPipeline normPipeline;
	private ShaderPipeline gridPipeline;
	private ShaderPipeline selectionPipeline;



	public ShaderPipeline getOrCreatePipeline() {
		if (hdPipeline == null) {
//			if (model != null && ModelUtils.isShaderStringSupported(model.getFormatVersion())
//					&& !model.getGeosets().isEmpty() && model.getGeoset(0).isHD()) {
//				pipeline = new HDDiffuseShaderPipeline();
//			}
//			else {
//				pipeline = new SimpleDiffuseShaderPipeline();
//			}
//			pipeline = new SimpleDiffuseShaderPipeline();
			hdPipeline = new HDDiffuseShaderPipeline();
//			pipeline = new NormalLinesShaderPipeline();
//			pipeline = new VertMarkerShaderPipeline();
		}
		return hdPipeline;
	}
	public ShaderPipeline getOrCreateHdPipeline() {
		if (hdPipeline == null) {
			hdPipeline = new HDDiffuseShaderPipeline();
		}
		return hdPipeline;
	}
	public ShaderPipeline getOrCreateSdPipeline() {
		if (sdPipeline == null) {
			sdPipeline = new SimpleDiffuseShaderPipeline();
		}
		return sdPipeline;
	}

	public ShaderPipeline getOrCreateVertPipeline() {
		if (vertPipeline == null) {
			vertPipeline = new VertMarkerShaderPipeline();
		}
		return vertPipeline;
	}

	public ShaderPipeline getOrCreateNormPipeline() {
		if (normPipeline == null) {
			normPipeline = new NormalLinesShaderPipeline();
		}
		return normPipeline;
	}

	public ShaderPipeline getOrCreateSelectionPipeline() {
		if (selectionPipeline == null) {
			selectionPipeline = new SelectionBoxShaderPipeline();
		}
		return selectionPipeline;
	}

	public ShaderPipeline getOrCreateGridPipeline() {
		if (gridPipeline == null) {
			gridPipeline = new GridShaderPipeline();
		}
		return gridPipeline;
	}

	public ShaderPipeline getOrCreateBoneMarkerShaderPipeline() {
		if (bonePipeline == null) {
			bonePipeline = new BoneMarkerShaderPipeline();
		}
		return bonePipeline;
	}

	public ShaderManager discardPipelines(){
		if (hdPipeline        != null) hdPipeline.discard();
		if (sdPipeline        != null) sdPipeline.discard();
		if (bonePipeline      != null) bonePipeline.discard();
		if (vertPipeline      != null) vertPipeline.discard();
		if (normPipeline      != null) normPipeline.discard();
		if (selectionPipeline != null) selectionPipeline.discard();
		if (gridPipeline      != null) gridPipeline.discard();

		hdPipeline          = null;
		sdPipeline          = null;
		bonePipeline        = null;
		vertPipeline        = null;
		normPipeline        = null;
		selectionPipeline   = null;
		gridPipeline        = null;

		return this;
	}
}
