package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.util.Vec3;

import java.nio.ByteBuffer;

public abstract class RenderSharedGeometryEmitter<MODEL_OBJECT extends EmitterIdObject, EMITTER_VIEW extends EmitterView>
		extends RenderSharedEmitter<MODEL_OBJECT, EMITTER_VIEW> {
	private static final int MAX_POWER_OF_TWO = 1 << 30;
	private final int elementsPerEmit;
	private float[] data;
	private RenderData[] renderData;
	private final ByteBuffer buffer;
	protected InternalResource internalResource;

	public RenderSharedGeometryEmitter(final MODEL_OBJECT model_object, final int elementsPerEmit,
	                                   final InternalResource internalResource) {
		super(model_object);
		this.elementsPerEmit = elementsPerEmit;
		this.internalResource = internalResource;
		this.data = new float[0];
		this.renderData = new RenderData[0];
		this.buffer = ByteBuffer.allocate(0);
	}

	@Override
	public void updateData() {
		final int sizeNeeded = alive * elementsPerEmit;

		if (data.length < sizeNeeded) {
			data = new float[powerOfTwo(sizeNeeded)];
			renderData = new RenderData[powerOfTwo(sizeNeeded) / 5];

			// GL15.glBindBuffer();
			// glBufferData
		}

		for (int i = 0, offset = 0; i < alive; i += 1, offset += 30) {
			final EmittedObject<EMITTER_VIEW> object = objects.get(i);
			final Vec3[] verticesV = object.verticesV;
			final float lta = object.lta;
			final float lba = object.lba;
			final float rta = object.rta;
			final float rba = object.rba;
			final float rgb = object.rgb;

			renderData[(offset / 5) + 0] = new RenderData(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 1] = new RenderData(verticesV[1], lba, rgb);
			renderData[(offset / 5) + 2] = new RenderData(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 3] = new RenderData(verticesV[0], lta, rgb);
			renderData[(offset / 5) + 4] = new RenderData(verticesV[2], rba, rgb);
			renderData[(offset / 5) + 5] = new RenderData(verticesV[3], rta, rgb);
		}
	}

	@Override
	public void render(final RenderModel modelView, final ParticleEmitterShader shader) {
		if ((internalResource != null) && (alive > 0)) {
			shader.renderParticles(modelObject.getBlendSrc(), modelObject.getBlendDst(), modelObject.getRows(),
					modelObject.getCols(), internalResource, renderData, modelObject.isRibbonEmitter(), alive * 6);
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

		@Override
		public String toString() {
			return "RenderData{" + "v=" + v + ", uv=" + uv + ", color=" + color + '}';
		}
	}
}
