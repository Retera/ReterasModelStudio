package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

public class ShaderManager {
	private ShaderPipeline hdPipeline;
	private ShaderPipeline sdPipeline;
	private ShaderPipeline particlePipeline;
	private ShaderPipeline bonePipeline;
	private ShaderPipeline vertPipeline;
	private ShaderPipeline normPipeline;
	private ShaderPipeline gridPipeline;
	private ShaderPipeline selectionPipeline;
	private ShaderPipeline customHDShaderPipeline;
	private ShaderPipeline customBonePipeline;
	private ShaderPipeline customGridPipeline;
	private Runnable customShaderMaker;
	private Runnable customBoneShaderMaker;
	private Runnable customGridShaderMaker;

	private Exception lastExeption;

	public ShaderPipeline getCustomHDShaderPipeline() {
		return customHDShaderPipeline;
	}
	public Exception getCustomShaderException() {
		return lastExeption;
	}

	public ShaderManager clearLastException(){
		lastExeption = null;
		return this;
	}

	public void createCustomShader(String vertexShader, String fragmentShader, boolean isHD){
		customShaderMaker = () -> makeCustomShader(vertexShader, fragmentShader, isHD);
	}
	public void makeCustomShader(String vertexShader, String fragmentShader, boolean isHD){
		try {
			ShaderPipeline newCustomPipeline;
			if (isHD) {
				newCustomPipeline = new HDDiffuseShaderPipeline(vertexShader, fragmentShader);
			} else {
				newCustomPipeline = new SimpleDiffuseShaderPipeline(vertexShader, fragmentShader);
			}
			if(customHDShaderPipeline != null){
				customHDShaderPipeline.discard();
			}
			customHDShaderPipeline = newCustomPipeline;

			customHDShaderPipeline.onGlobalPipelineSet();
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

	public void createCustomBoneShader(String vertexShader, String fragmentShader, String geometryShader){
		customBoneShaderMaker = () -> makeCustomBoneShader(vertexShader, fragmentShader, geometryShader);
	}

	public void makeCustomBoneShader(String vertexShader, String fragmentShader, String geometryShader){
		try {
			ShaderPipeline newCustomPipeline = new BoneMarkerShaderPipeline(vertexShader, fragmentShader, geometryShader);

			if(customBonePipeline != null){
				customBonePipeline.discard();
			}
			customBonePipeline = newCustomPipeline;

		} catch (Exception e){
			e.printStackTrace();
			System.out.println("adding Exception!");
			lastExeption = e;
		}
		customBoneShaderMaker = null;
	}

	public ShaderManager removeCustomBoneShader(){
		customBoneShaderMaker = this::doRemoveCustomBoneShader;
		return this;
	}

	private void doRemoveCustomBoneShader(){
		if(customBonePipeline != null){
			customBonePipeline.discard();
			customBonePipeline = null;
			customBoneShaderMaker = null;
		}
	}



	public void createCustomGridShader(String vertexShader, String fragmentShader, String geometryShader){
		customGridShaderMaker = () -> makeCustomGridShader(vertexShader, fragmentShader, geometryShader);
	}


	public void makeCustomGridShader(String vertexShader, String fragmentShader, String geometryShader){
		try {
			ShaderPipeline newCustomPipeline = new GridShaderPipeline(vertexShader, fragmentShader, geometryShader);

			if(customGridPipeline != null){
				customGridPipeline.discard();
			}
			customGridPipeline = newCustomPipeline;

		} catch (Exception e){
			e.printStackTrace();
			System.out.println("adding Exception!");
			lastExeption = e;
		}
		customGridShaderMaker = null;
	}

	public ShaderManager removeCustomGridShader(){
		customGridShaderMaker = this::doRemoveCustomGridShader;
		return this;
	}

	private void doRemoveCustomGridShader(){
		if(customGridPipeline != null){
			customGridPipeline.discard();
			customGridPipeline = null;
			customGridShaderMaker = null;
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
			hdPipeline.onGlobalPipelineSet();
//			pipeline = new NormalLinesShaderPipeline();
//			pipeline = new VertMarkerShaderPipeline();
		}
		return hdPipeline;
	}

	public ShaderPipeline getOrCreatePipeline(boolean hd) {
		if (customShaderMaker != null) {
			customShaderMaker.run();
		}
		if(customHDShaderPipeline != null){
			return customHDShaderPipeline;
		} else if (hd) {
			return getOrCreateHdPipeline();
		} else {
			return getOrCreateSdPipeline();
		}
	}

	public ShaderPipeline getOrCreateHdPipeline() {
		if (hdPipeline == null) {
			hdPipeline = new HDDiffuseShaderPipeline();
			hdPipeline.onGlobalPipelineSet();
		}
		return hdPipeline;
	}
	public ShaderPipeline getOrCreateSdPipeline() {
		if (sdPipeline == null) {
			sdPipeline = new SimpleDiffuseShaderPipeline();
			sdPipeline.onGlobalPipelineSet();
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
		if (customGridShaderMaker != null) {
			customGridShaderMaker.run();
		}
		if(customGridPipeline != null){
			return customGridPipeline;
		}

		if (gridPipeline == null) {
			gridPipeline = new GridShaderPipeline();
		}
		return gridPipeline;
	}

	public ShaderPipeline getOrCreateBoneMarkerShaderPipeline() {
		if (customBoneShaderMaker != null) {
			customBoneShaderMaker.run();
		}
		if(customBonePipeline != null){
			return customBonePipeline;
		}

		if (bonePipeline == null) {
			bonePipeline = new BoneMarkerShaderPipeline();
		}
		return bonePipeline;
	}

	public ShaderPipeline getOrCreateParticleShaderPipeline() {
		if (particlePipeline == null) {
			particlePipeline = new ParticleShaderPipeline();
		}
		return particlePipeline;
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
		if (particlePipeline  != null) particlePipeline.discard();


		hdPipeline          = null;
		sdPipeline          = null;
		bonePipeline        = null;
		vertPipeline        = null;
		normPipeline        = null;
		selectionPipeline   = null;
		gridPipeline        = null;
		particlePipeline    = null;

		if (customHDShaderPipeline != null) customHDShaderPipeline.discard();
		customHDShaderPipeline = null;

		if (customBonePipeline != null) customBonePipeline.discard();
		customBonePipeline = null;

		if (customGridPipeline != null) customGridPipeline.discard();
		customGridPipeline = null;

		return this;
	}
}
