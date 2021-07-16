package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;
import com.hiveworkshop.rms.util.Vec3;

import java.nio.ByteBuffer;

public abstract class RenderSharedGeometryEmitter<MODEL_OBJECT extends EmitterIdObject, EMITTER_VIEW extends EmitterView>
		extends RenderSharedEmitter<MODEL_OBJECT, EMITTER_VIEW> {
	private static final int MAX_POWER_OF_TWO = 1 << 30;
	private final int elementsPerEmit;
	private float[] data;
	private RenderData[] renderData;
	private final ByteBuffer buffer;
	protected Particle2TextureInstance internalResource;

	public RenderSharedGeometryEmitter(final MODEL_OBJECT model_object, final int elementsPerEmit,
	                                   final Particle2TextureInstance internalResource) {
		super(model_object);
		this.elementsPerEmit = elementsPerEmit;
		this.internalResource = internalResource;
		this.data = new float[0];
		this.renderData = new RenderData[0];
		this.buffer = ByteBuffer.allocate(0);
	}

	@Override
	public void updateData() {
		int sizeNeeded = alive * elementsPerEmit;

		if (data.length < sizeNeeded) {
			data = new float[powerOfTwo(sizeNeeded)];
			renderData = new RenderData[powerOfTwo(sizeNeeded) / 5];

			// GL15.glBindBuffer();
			// glBufferData
		}

		for (int i = 0, offset = 0; i < alive; i += 1, offset += 30) {
			EmittedObject<EMITTER_VIEW> object = objects.get(i);
			Vec3[] verticesV = object.verticesV;
			float lta = object.lta;
			float lba = object.lba;
			float rta = object.rta;
			float rba = object.rba;
			float rgb = object.rgb;

			fillRenderData(offset, verticesV, lta, lba, rta, rba, rgb);
		}
	}

	private void fillRenderData(int offset, Vec3[] verticesV, float lta, float lba, float rta, float rba, float rgb) {
		if(renderData[(offset / 5) + 5] == null){
			renderData[(offset / 5) + 0] = new RenderData(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 1] = new RenderData(verticesV[1], lba, rgb);
			renderData[(offset / 5) + 2] = new RenderData(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 3] = new RenderData(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 4] = new RenderData(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 5] = new RenderData(verticesV[3], rta, rgb);
		} else {
			renderData[(offset / 5) + 0].set(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 1].set(verticesV[1], lba, rgb);
			renderData[(offset / 5) + 2].set(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 3].set(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 4].set(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 5].set(verticesV[3], rta, rgb);
		}
	}

	@Override
	public void render(RenderModel modelView, ParticleEmitterShader shader) {
		if ((internalResource != null) && (alive > 0)) {
//			shader.renderParticles(modelObject.getBlendSrc(),
//					modelObject.getBlendDst(),
//					modelObject.getRows(),
//					modelObject.getCols(),
//					internalResource,
//					renderData,
//					modelObject.isRibbonEmitter(),
//					alive * 6);
			shader.renderParticles(modelObject, internalResource, renderData, alive * 6);
		}
	}

	/**
	 * Returns a power of two size for the given target capacity.
	 */
	private static int powerOfTwo(final int capacity) {
		int numElements = capacity - 1;
		numElements |= numElements >>> 1;
		numElements |= numElements >>> 2;
		numElements |= numElements >>> 4;
		numElements |= numElements >>> 8;
		numElements |= numElements >>> 16;
		return (numElements < 0) ? 1 : (numElements >= MAX_POWER_OF_TWO) ? MAX_POWER_OF_TWO : numElements + 1;
	}

	static class RenderData {
		public Vec3 v;
		public float uv;
		public float color;

		RenderData(Vec3 v, float uv, float color) {
			this.v = v;
			this.uv = uv;
			this.color = color;
		}

		RenderData set(Vec3 v, float uv, float color) {
			this.v = v;
			this.uv = uv;
			this.color = color;
			return this;
		}

		@Override
		public String toString() {
			return "RenderData{" + "v=" + v + ", uv=" + uv + ", color=" + color + '}';
		}
	}
}
