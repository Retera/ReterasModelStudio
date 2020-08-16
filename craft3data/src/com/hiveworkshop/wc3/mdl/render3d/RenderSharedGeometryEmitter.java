package com.hiveworkshop.wc3.mdl.render3d;

import java.nio.ByteBuffer;

public abstract class RenderSharedGeometryEmitter<MODEL_OBJECT extends EmitterIdObject, EMITTER_VIEW extends EmitterView>
		extends RenderSharedEmitter<MODEL_OBJECT, EMITTER_VIEW> {
	private static final int MAX_POWER_OF_TWO = 1 << 30;
	private final int elementsPerEmit;
	private float[] data;
	private final ByteBuffer buffer;
	protected InternalResource internalResource;

	public RenderSharedGeometryEmitter(final MODEL_OBJECT model_object, final int elementsPerEmit,
			final InternalResource internalResource) {
		super(model_object);
		this.elementsPerEmit = elementsPerEmit;
		this.internalResource = internalResource;
		this.data = new float[0];
		this.buffer = ByteBuffer.allocate(0);
	}

	@Override
	public void updateData() {
		final int sizeNeeded = alive * elementsPerEmit;

		if (data.length < sizeNeeded) {
			data = new float[powerOfTwo(sizeNeeded)];

			// GL15.glBindBuffer();
			// glBufferData
		}

		for (int i = 0, offset = 0; i < alive; i += 1, offset += 30) {
			final EmittedObject<EMITTER_VIEW> object = objects.get(i);
			final float[] vertices = object.vertices;
			final float lta = object.lta;
			final float lba = object.lba;
			final float rta = object.rta;
			final float rba = object.rba;
			final float rgb = object.rgb;

			data[offset + 0] = vertices[0];
			data[offset + 1] = vertices[1];
			data[offset + 2] = vertices[2];
			data[offset + 3] = lta;
			data[offset + 4] = rgb;

			data[offset + 5] = vertices[3];
			data[offset + 6] = vertices[4];
			data[offset + 7] = vertices[5];
			data[offset + 8] = lba;
			data[offset + 9] = rgb;

			data[offset + 10] = vertices[6];
			data[offset + 11] = vertices[7];
			data[offset + 12] = vertices[8];
			data[offset + 13] = rba;
			data[offset + 14] = rgb;

			data[offset + 15] = vertices[0];
			data[offset + 16] = vertices[1];
			data[offset + 17] = vertices[2];
			data[offset + 18] = lta;
			data[offset + 19] = rgb;

			data[offset + 20] = vertices[6];
			data[offset + 21] = vertices[7];
			data[offset + 22] = vertices[8];
			data[offset + 23] = rba;
			data[offset + 24] = rgb;

			data[offset + 25] = vertices[9];
			data[offset + 26] = vertices[10];
			data[offset + 27] = vertices[11];
			data[offset + 28] = rta;
			data[offset + 29] = rgb;
		}
	}

	@Override
	public void render(final RenderModel modelView, final ParticleEmitterShader shader) {
		if ((internalResource != null) && (alive > 0)) {
			shader.renderParticles(modelObject.getBlendSrc(), modelObject.getBlendDst(), modelObject.getRows(),
					modelObject.getCols(), internalResource, data, modelObject.isRibbonEmitter(), alive * 6);
		}
	}

	/**
	 * Returns a power of two size for the given target capacity.
	 */
	private static final int powerOfTwo(final int capacity) {
		int numElements = capacity - 1;
		numElements |= numElements >>> 1;
		numElements |= numElements >>> 2;
		numElements |= numElements >>> 4;
		numElements |= numElements >>> 8;
		numElements |= numElements >>> 16;
		return (numElements < 0) ? 1 : (numElements >= MAX_POWER_OF_TWO) ? MAX_POWER_OF_TWO : numElements + 1;
	}
}
