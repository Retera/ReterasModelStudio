package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

public class ShaderManager {
	private ShaderPipeline hdPipeline;
	private ShaderPipeline sdPipeline;
	private ShaderPipeline bonePipeline;
	private ShaderPipeline vertPipeline;
	private ShaderPipeline normPipeline;
	private ShaderPipeline gridPipeline;
	private ShaderPipeline selectionPipeline;
	private ShaderPipeline customHDShaderPipeline;
	private Runnable customShaderMaker;

	private Exception lastExeption;

	public ShaderPipeline getCustomHDShaderPipeline() {
		return customHDShaderPipeline;
	}
	public Exception getCustomHDShaderException() {
		return lastExeption;
	}

	public ShaderManager clearLastException(){
		lastExeption = null;
		return this;
	}

	public void createCustomShader(String vertexShader, String fragmentShader){
		customShaderMaker = () -> makeCustomShader(vertexShader, fragmentShader);
	}
	public void makeCustomShader(String vertexShader, String fragmentShader){
		try {
			ShaderPipeline newCustomPipeline = new CustomHDShaderPipeline(vertexShader, fragmentShader);
			if(customHDShaderPipeline != null){
				customHDShaderPipeline.discard();
			}
			customHDShaderPipeline = newCustomPipeline;
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("adding Exception!");
			lastExeption = e;
		}
		customShaderMaker = null;
	}

	public ShaderManager removeCustomShader(){
		customShaderMaker = this::doRemoveCustomShader;
		return this;
	}

	private void doRemoveCustomShader(){
		if(customHDShaderPipeline != null){
			customHDShaderPipeline.discard();
			customHDShaderPipeline = null;
			customShaderMaker = null;
		}
	}


	public ShaderPipeline getOrCreatePipeline() {
		if (customShaderMaker != null) {
			customShaderMaker.run();
		}
		if(customHDShaderPipeline != null){
			return customHDShaderPipeline;
		} else if (hdPipeline == null) {
//			if (model != null && ModelUtils.isShaderStringSupported(model.getFormatVersion())
//					&& !model.getGeosets().isEmpty() && model.getGeoset(0).isHD()) {
//				pipeline = new HDDiffuseShaderPipeline();
//			}
//			else {
//				pipeline = new SimpleDiffuseShaderPipeline();
//			}
//			pipeline = new SimpleDiffuseShaderPipeline();
//			hdPipeline = new SimpleDiffuseShaderPipeline();
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
		System.out.println("discarding pipelines");
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

		if (customHDShaderPipeline != null) customHDShaderPipeline.discard();
		customHDShaderPipeline = null;

		return this;
	}
}
