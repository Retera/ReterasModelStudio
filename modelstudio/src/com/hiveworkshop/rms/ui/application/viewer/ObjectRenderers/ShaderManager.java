package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import java.util.EnumMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ShaderManager {
	private final EnumMap<PipelineType, PipelineTracker> pipelines = new EnumMap<>(PipelineType.class);

	public ShaderManager(){
		for(PipelineType pipelineType : PipelineType.values()){
			pipelines.put(pipelineType, new PipelineTracker(pipelineType));
		}
	}

	public ShaderPipeline getPipeline(PipelineType pipelineType){
		return pipelines.get(pipelineType).getOrCreatePipeline();
	}

	public Exception getCustomShaderException(PipelineType pipelineType) {
		return pipelines.get(pipelineType).getCustomShaderException();
	}

	public ShaderManager clearLastException(PipelineType pipelineType){
		pipelines.get(pipelineType).clearLastException();
		return this;
	}

	public void createCustomShader(PipelineType pipelineType, String vertexShader, String fragmentShader, String geometryShader){
		pipelines.get(pipelineType).createCustomShader(vertexShader, fragmentShader, geometryShader);
	}

	public ShaderManager removeCustomShader(PipelineType pipelineType){
		pipelines.get(pipelineType).removeCustomShader();
		return this;
	}

	public ShaderManager discardPipelines(){
		System.out.println("discarding pipelines");

		for(PipelineType pipelineType : PipelineType.values()){
			PipelineTracker pipelineTracker = pipelines.get(pipelineType);
			if(pipelineTracker != null){
				pipelineTracker.discard();
			}
		}
		return this;
	}






	public static class PipelineTracker {
		PipelineType pipelineType;
		Supplier<ShaderPipeline> defaultSupplier;
		TriFunction<String, String, String, ShaderPipeline> customFunction;
		private ShaderPipeline pipeline;
		private ShaderPipeline customPipeline;
		private Runnable customShaderMaker;


		private Exception lastException;

		PipelineTracker(Supplier<ShaderPipeline> defaultSupplier, TriFunction<String, String, String, ShaderPipeline> customFunction){
			this.defaultSupplier = defaultSupplier;
			this.customFunction = customFunction;
		}
		PipelineTracker(PipelineType pipelineType){
			this.pipelineType = pipelineType;
			this.defaultSupplier = pipelineType.getDefaultSupplier();
			this.customFunction = pipelineType.getCustomFunction();
		}

		public Exception getCustomShaderException() {
			return lastException;
		}

		public PipelineTracker clearLastException(){
			lastException = null;
			return this;
		}

		public void createCustomShader(String vertexShader, String fragmentShader, String geometryShader){
			System.out.println("setting custom shader for: " + pipelineType);
			customShaderMaker = () -> makeCustomShader(vertexShader, fragmentShader, geometryShader);
		}
		public void makeCustomShader(String vertexShader, String fragmentShader, String geometryShader){
			try {
				ShaderPipeline newCustomPipeline = customFunction.apply(vertexShader, fragmentShader, geometryShader);

				if(customPipeline != null){
					customPipeline.discard();
				}
				customPipeline = newCustomPipeline;

				customPipeline.onGlobalPipelineSet();
			} catch (Exception e){
				e.printStackTrace();
				System.out.println("adding Exception!");
				lastException = e;
			}
			customShaderMaker = null;
		}

		public PipelineTracker removeCustomShader(){
			customShaderMaker = this::doRemoveCustomShader;
			return this;
		}

		private void doRemoveCustomShader(){
			if(customPipeline != null){
				customPipeline.discard();
				customPipeline = null;
				customShaderMaker = null;
			}
		}
		public ShaderPipeline getOrCreatePipeline() {
			if (customShaderMaker != null) {
				customShaderMaker.run();
			}
			if(customPipeline != null){
				return customPipeline;
			} else if (pipeline == null) {
				pipeline = defaultSupplier.get();
//				pipeline.onGlobalPipelineSet();
			}
			return pipeline;
		}

		public PipelineTracker discard(){
			if (pipeline != null) pipeline.discard();
			pipeline= null;

			if (customPipeline != null) customPipeline.discard();
			customPipeline = null;
			return this;
		}
	}

	@FunctionalInterface
	interface TriFunction<T,U,V,R> {

		R apply(T t, U u, V v);

		default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
//			Objects.requireNonNull(after);
			return (T t, U u, V v) -> after.apply(apply(t, u, v));
		}
	}

	public enum PipelineType {
		SELECTION(3, ()-> new SelectionBoxShaderPipeline(), (v, f, g)-> new SelectionBoxShaderPipeline(v, f, g)),
		MESH(2, ()-> new HDDiffuseShaderPipeline(), (v, f, g)-> new HDDiffuseShaderPipeline(v, f, g)),
		BONE(3, ()-> new BoneMarkerShaderPipeline(), (v, f, g)-> new BoneMarkerShaderPipeline(v, f, g)),
		VERT(3, ()-> new VertMarkerShaderPipeline(), (v, f, g)-> new VertMarkerShaderPipeline(v, f, g)),
		NORM(3, ()-> new NormalLinesShaderPipeline(), (v, f, g)-> new NormalLinesShaderPipeline(v, f, g)),
		GRID(3, ()-> new GridShaderPipeline(), (v, f, g)-> new GridShaderPipeline(v, f, g)),
		COLLISION(3, ()-> new CollisionMarkerShaderPipeline(), (v, f, g)-> new CollisionMarkerShaderPipeline(v, f, g)),
		CAMERA(3, ()-> new CameraShaderPipeline(), (v, f, g)-> new CameraShaderPipeline(v, f, g)),
		PARTICLE2(3, ()-> new ParticleShaderPipeline(), (v, f, g)-> new ParticleShaderPipeline(v, f, g)),
		RIBBON(2, ()-> new RibbonShaderPipeline(), (v, f, g)-> new RibbonShaderPipeline(v, f, g)),
		;
		int shadersPrograms;
		Supplier<ShaderPipeline> defaultSupplier;
		TriFunction<String, String, String, ShaderPipeline> customFunction;
		PipelineType(){

		}
		PipelineType(int shadersPrograms, Supplier<ShaderPipeline> defaultSupplier, TriFunction<String, String, String, ShaderPipeline> customFunction){
			this.shadersPrograms = shadersPrograms;
			this.defaultSupplier = defaultSupplier;
			this.customFunction = customFunction;
		}

		public int getNumShadersPrograms() {
			return shadersPrograms;
		}

		public Supplier<ShaderPipeline> getDefaultSupplier() {
			return defaultSupplier;
		}

		public TriFunction<String, String, String, ShaderPipeline> getCustomFunction() {
			return customFunction;
		}
	}
}
