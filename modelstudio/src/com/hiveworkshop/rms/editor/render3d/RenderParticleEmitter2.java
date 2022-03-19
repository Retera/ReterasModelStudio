package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class RenderParticleEmitter2 {
	private final RenderModel renderModel;
	private final TimeEnvironmentImpl timeEnvironment;
	private static final int MAX_POWER_OF_TWO = 1 << 30;
	private final int elementsPerEmit;
	private float[] data;
	private RenderData[] renderData;
	private final ParticleEmitter2 particleEmitter2;
	private final List<RenderParticle2> renderParticle2s;
	private int alive;

	private float currentEmission;
	private double lastEmissionRate;

	private boolean textureLoaded = false;
	private final TextureThing textureThing;

	public RenderParticleEmitter2(ParticleEmitter2 particleEmitter2, TextureThing textureThing, RenderModel renderModel) {
		this.particleEmitter2 = particleEmitter2;
		this.textureThing = textureThing;
		this.renderModel = renderModel;

		timeEnvironment = renderModel.getTimeEnvironment();
		this.renderParticle2s = new ArrayList<>();
		this.alive = 0;
		this.elementsPerEmit = (particleEmitter2.isBoth() ? 2 : 1) * 30;
		this.data = new float[0];
		this.renderData = new RenderData[0];

		currentEmission = 0;
		lastEmissionRate = -1;

		AnimFlag<?> emissionRateFlag = particleEmitter2.find("EmissionRate");
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if (emissionRateFlag != null && currentSequence != null && emissionRateFlag.getCeilEntry(0, currentSequence) != null) {
			if (emissionRateFlag.size() > 0) {
				lastEmissionRate = (Float) emissionRateFlag.getValueFromIndex(currentSequence, 0);
			}
		}
	}

	public ParticleEmitter2 getParticleEmitter2() {
		return particleEmitter2;
	}

	public void fill() {
		if (renderModel.allowParticleSpawn()) {
			double emissionRate = particleEmitter2.getRenderEmissionRate(timeEnvironment);
			if (particleEmitter2.getSquirt()) {
				// TODO not correct for any interp type other than "DontInterp", ghostwolf did this differently
				if (emissionRate != lastEmissionRate) {
					currentEmission += emissionRate;
				}

				lastEmissionRate = emissionRate;
			} else {
				currentEmission += emissionRate * TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001 * timeEnvironment.getAnimationSpeed();
			}
//			renderEmitter.fill(this);
			float emission = currentEmission;

			if (emission >= 1) {
				for (int i = 0; i < emission; i += 1, currentEmission--) {
					emit();
				}
			}
		}
	}

	public void emit() {
		if (particleEmitter2.isHead() || particleEmitter2.isBoth()) {
			emitObject(true);
		}

		if (particleEmitter2.isTail() || particleEmitter2.isBoth()) {
			emitObject(false);
		}
	}

	public RenderParticleEmitter2 emitObject(boolean flag) {
		if (alive == renderParticle2s.size()) {
			renderParticle2s.add(new RenderParticle2(particleEmitter2));
		}

		RenderParticle2 renderParticle2 = renderParticle2s.get(alive);
		this.alive += 1;
		renderParticle2.reset(renderModel, flag, timeEnvironment);

		return this;
	}

	public void updateData() {
		int sizeNeeded = alive * elementsPerEmit;

		if (data.length < sizeNeeded) {
			data = new float[powerOfTwo(sizeNeeded)];
			renderData = new RenderData[powerOfTwo(sizeNeeded) / 5];

			// GL15.glBindBuffer();
			// glBufferData
		}

		for (int i = 0, offset = 0; i < alive; i += 1, offset += 30) {
			RenderParticle2 object = renderParticle2s.get(i);
			Vec3[] verticesV = object.verticesV;
			float lta = object.lta;
			float lba = object.lba;
			float rta = object.rta;
			float rba = object.rba;
			float rgb = object.rgb;

			fillRenderData(offset, verticesV, lta, lba, rta, rba, rgb);
		}
	}

//	private void fillRenderData2(int offset, RenderParticle2 object) {
//		Vec3[] verticesV = object.verticesV;
//		float[] lta_lba_rta_rba_rgb = object.lta_lba_rta_rba_rgb;
//		if(renderData[(offset / 5) + 5] == null){
//			renderData[(offset / 5) + 0] = new RenderData(verticesV[0], lta_lba_rta_rba_rgb[0], object.rgb);
//			renderData[(offset / 5) + 1] = new RenderData(verticesV[1], lta_lba_rta_rba_rgb[1], object.rgb);
//			renderData[(offset / 5) + 2] = new RenderData(verticesV[2], lta_lba_rta_rba_rgb[3], object.rgb);
//			renderData[(offset / 5) + 3] = new RenderData(verticesV[0], lta_lba_rta_rba_rgb[0], object.rgb);
//			renderData[(offset / 5) + 4] = new RenderData(verticesV[2], lta_lba_rta_rba_rgb[1], object.rgb);
//			renderData[(offset / 5) + 5] = new RenderData(verticesV[3], lta_lba_rta_rba_rgb[2], object.rgb);
//		} else {
//			renderData[(offset / 5) + 0].set(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 1].set(verticesV[1], object.lba, object.rgb);
//			renderData[(offset / 5) + 2].set(verticesV[2], object.rba, object.rgb);
//			renderData[(offset / 5) + 3].set(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 4].set(verticesV[2], object.lba, object.rgb);
//			renderData[(offset / 5) + 5].set(verticesV[3], object.rta, object.rgb);
//		}
//	}
//	private void fillRenderData(int offset, RenderParticle2 object) {
//		Vec3[] verticesV = object.verticesV;
//		if(renderData[(offset / 5) + 5] == null){
//			renderData[(offset / 5) + 0] = new RenderData(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 1] = new RenderData(verticesV[1], object.lba, object.rgb);
//			renderData[(offset / 5) + 2] = new RenderData(verticesV[2], object.rba, object.rgb);
//			renderData[(offset / 5) + 3] = new RenderData(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 4] = new RenderData(verticesV[2], object.lba, object.rgb);
//			renderData[(offset / 5) + 5] = new RenderData(verticesV[3], object.rta, object.rgb);
//		} else {
//			renderData[(offset / 5) + 0].set(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 1].set(verticesV[1], object.lba, object.rgb);
//			renderData[(offset / 5) + 2].set(verticesV[2], object.rba, object.rgb);
//			renderData[(offset / 5) + 3].set(verticesV[0], object.lta, object.rgb);
//			renderData[(offset / 5) + 4].set(verticesV[2], object.lba, object.rgb);
//			renderData[(offset / 5) + 5].set(verticesV[3], object.rta, object.rgb);
//		}
//	}
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

	public void update() {
		for (int i = 0; i < alive; i++) {
			RenderParticle2 renderParticle2 = renderParticle2s.get(i);

			renderParticle2.update(timeEnvironment.getAnimationSpeed(), renderModel);

			if (renderParticle2.health <= 0) {
				alive -= 1;

				// Swap between this object and the first unused object.
				// Decrement the iterator so the moved object is indexed
				if (i != this.alive) {
					renderParticle2s.set(i, renderParticle2s.get(alive));
					renderParticle2s.set(this.alive, renderParticle2);
					i -= 1;
				}
			}
		}

		this.updateData();
	}

	public int getPriorityPlane() {
		return particleEmitter2.getPriorityPlane();
	}

	public void render() {
		if ((textureThing != null) && (alive > 0)) {
			renderParticles();
		}
	}

	private void renderParticles() {
		int numItemsToRender = alive * 6;
		int blendSrc = particleEmitter2.getBlendSrc();
		int blendDst = particleEmitter2.getBlendDst();
		int rows = particleEmitter2.getRows();
		int cols = particleEmitter2.getCols();
		boolean isRibbonEmitter = particleEmitter2.isRibbonEmitter();

		bind();
		GL11.glBlendFunc(blendSrc, blendDst);
		GL11.glBegin(GL11.GL_TRIANGLES);
		if ((numItemsToRender) > renderData.length) {
			new IllegalStateException(numItemsToRender + " alive items, " + renderData.length + " len buffer").printStackTrace();
			return;
		}
		for (int i = 0; i < numItemsToRender; i++) {
			RenderParticleEmitter2.RenderData bufferDatum = renderData[i];
			int colorInt = (int) bufferDatum.color;
			int uvInt = (int) bufferDatum.uv;
			GL11.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF), (byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));

			float uv_u = (byte) ((uvInt >> 16) & 0xFF);
			float uv_v = (byte) ((uvInt >> 8) & 0xFF);
			if (isRibbonEmitter) {
				uv_u /= 255.0f;
				uv_v /= 255.0f;
			} else {
				uv_u /= cols;
				uv_v /= rows;
			}
			GL11.glTexCoord2f(uv_u, uv_v);
			GL11.glVertex3f(bufferDatum.v.x, bufferDatum.v.y, bufferDatum.v.z);
		}
		GL11.glEnd();
	}

	public void bind() {
		Bitmap bitmap = particleEmitter2.getTexture();
		if (!textureLoaded) {
			textureThing.loadToTexMap(bitmap);
			textureLoaded = true;
		}
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}

//	public void clear(final Object owner) {
//		for (RenderParticle2 renderParticle2 : this.renderParticle2s) {
//			if (owner == renderParticle2.emitterView.getRenderModel()) {
//				renderParticle2.health = 0;
//			}
//		}
//	}

	/**
	 * Returns a power of two size for the given target capacity.
	 */
	private static int powerOfTwo(final int capacity) {
//		int numElements = Integer.highestOneBit(capacity);
//		numElements = numElements << 1;
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
		public float uv_u;
		public float uv_v;

		RenderData(Vec3 v, float uv, float color) {
			this.v = v;
			this.uv = uv;
			this.color = color;
		}

		RenderData set(Vec3 v, float uv, float color) {
			this.v = v;
			this.uv = uv;
			int uvInt = (int) uv;
			this.uv_u = (byte) ((uvInt >> 16) & 0xFF);;
			this.uv_v = (byte) ((uvInt >> 8) & 0xFF);;
			this.color = color;
			return this;
		}

		@Override
		public String toString() {
			return "RenderData{" + "v=" + v + ", uv=" + uv + ", color=" + color + '}';
		}
	}
}
