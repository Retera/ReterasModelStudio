package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.MathUtils;

/**
 * One approximated popcorn particle: born at the emitter node, drifting on a
 * random mostly-upward velocity, fading in and out over its life. Sizes are
 * in game units.
 */
public class RenderPopcornParticle extends EmittedObject<RenderPopcornEmitterView> {
	private static final Vector4f vector4Heap = new Vector4f();
	private static final float BASE_LIFE_SECONDS = 1.1f;
	private static final float BASE_SPEED = 28f;
	private static final float BASE_SIZE = 14f;

	private final RenderPopcornEmitter emitter;
	private final Vector3f location = new Vector3f();
	private final Vector3f velocity = new Vector3f();
	private float lifeSpan = 1;
	private float baseSize = BASE_SIZE;
	private float spin;
	private float red = 1;
	private float green = 1;
	private float blue = 1;
	private float alpha = 1;

	public RenderPopcornParticle(final RenderPopcornEmitter emitter) {
		this.emitter = emitter;
		this.emitterView = null;
		this.health = 0;
		vertices = new float[12];
	}

	@Override
	public void reset(final RenderPopcornEmitterView emitterView, final boolean flag) {
		this.emitterView = emitterView;
		final ParticleEmitterPopcorn modelObject = emitter.getEmitter();
		final RenderNode node = emitterView.instance.getRenderNode(modelObject);
		final Vertex pivot = modelObject.getPivotPoint();
		final Vector3f scale = node.getWorldScale();
		final float scaleFactor = (float) Math.max(0.001, (scale.x + scale.y + scale.z) / 3.0);

		lifeSpan = (float) Math.max(0.05, BASE_LIFE_SECONDS * emitterView.getLifeSpanMultiplier()
				* MathUtils.randomInRange(0.8, 1.25));
		health = lifeSpan;
		// later renderers of the same effect tend to be the softer, larger glows
		final float layerBoost = 1f + (0.35f * emitter.getRendererIndex());
		baseSize = BASE_SIZE * layerBoost * scaleFactor * MathUtils.randomInRange(0.75, 1.3);
		spin = MathUtils.randomInRange(-1.5, 1.5);

		// spawn inside a small disc around the pivot, in node space
		final double angle = MathUtils.randomInRange(0, Math.PI * 2);
		final double radius = MathUtils.randomInRange(0, 8) * layerBoost;
		vector4Heap.set((float) (pivot.x + (Math.cos(angle) * radius)),
				(float) (pivot.y + (Math.sin(angle) * radius)), (float) pivot.z, 1);
		Matrix4f.transform(node.getWorldMatrix(), vector4Heap, vector4Heap);
		location.set(vector4Heap.x, vector4Heap.y, vector4Heap.z);

		// mostly upward drift with some spread, scaled by the emitter's speed multiplier
		final double spread = MathUtils.randomInRange(0, Math.toRadians(35));
		final double around = MathUtils.randomInRange(0, Math.PI * 2);
		final float speed = (float) (BASE_SPEED * emitterView.getSpeedMultiplier()
				* MathUtils.randomInRange(0.6, 1.4) * scaleFactor);
		velocity.set((float) (Math.sin(spread) * Math.cos(around) * speed),
				(float) (Math.sin(spread) * Math.sin(around) * speed), (float) (Math.cos(spread) * speed));

		final Vertex color = emitterView.getColor();
		// model colours are BGR
		blue = color == null ? 1 : clamp01((float) color.x);
		green = color == null ? 1 : clamp01((float) color.y);
		red = color == null ? 1 : clamp01((float) color.z);
		alpha = clamp01((float) emitterView.getAlpha());
		update();
	}

	private static float clamp01(final float value) {
		return Math.max(0, Math.min(1, value));
	}

	@Override
	public void update() {
		final float dt = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
		health -= dt;
		location.x += velocity.x * dt;
		location.y += velocity.y * dt;
		location.z += velocity.z * dt;
		// slow down over life like a puff of air would
		velocity.scale(1f - (0.9f * dt));

		final float lifeFactor = Math.max(0, Math.min(1, 1f - (health / lifeSpan)));
		// fade in quickly, hold, fade out
		final float fadeIn = Math.min(1f, lifeFactor / 0.15f);
		final float fadeOut = Math.min(1f, (1f - lifeFactor) / 0.45f);
		final float envelope = fadeIn * fadeOut;
		final float size = baseSize * (0.6f + (0.9f * lifeFactor));

		final int a = Math.round(255 * alpha * envelope) & 0xFF;
		final PkbFrame frame = frameFor(lifeFactor);
		lta = MathUtils.uint8ToUint24((byte) frame.right, (byte) frame.bottom, (byte) a);
		lba = MathUtils.uint8ToUint24((byte) frame.left, (byte) frame.bottom, (byte) a);
		rta = MathUtils.uint8ToUint24((byte) frame.right, (byte) frame.top, (byte) a);
		rba = MathUtils.uint8ToUint24((byte) frame.left, (byte) frame.top, (byte) a);
		rgb = MathUtils.uint8ToUint24((byte) (Math.round(blue * 255) & 0xFF), (byte) (Math.round(green * 255) & 0xFF),
				(byte) (Math.round(red * 255) & 0xFF));

		final Vector3f[] vectors = emitterView.instance.getBillboardVectors();
		final double rotation = spin * lifeFactor * lifeSpan;
		final float cos = (float) Math.cos(rotation);
		final float sin = (float) Math.sin(rotation);
		// rotate the billboard corners around the view axis
		final Vector3f right = vectors[4];
		final Vector3f up = vectors[5];
		final float[] cornersX = { -1, 1, 1, -1 };
		final float[] cornersY = { 1, 1, -1, -1 };
		for (int i = 0; i < 4; i++) {
			final float cx = (cornersX[i] * cos) - (cornersY[i] * sin);
			final float cy = (cornersX[i] * sin) + (cornersY[i] * cos);
			vertices[i * 3] = location.x + (((right.x * cx) + (up.x * cy)) * size);
			vertices[(i * 3) + 1] = location.y + (((right.y * cx) + (up.y * cy)) * size);
			vertices[(i * 3) + 2] = location.z + (((right.z * cx) + (up.z * cy)) * size);
		}
	}

	private static final class PkbFrame {
		int left;
		int top;
		int right;
		int bottom;
	}

	private static final PkbFrame frameHeap = new PkbFrame();

	/** Atlas cell for the particle's age; a single-cell texture is the whole image. */
	private PkbFrame frameFor(final float lifeFactor) {
		final int columns = Math.max(1, emitter.getRenderer().atlasSubDivX);
		final int rows = Math.max(1, emitter.getRenderer().atlasSubDivY);
		final int frames = columns * rows;
		final int index = Math.min(frames - 1, (int) (lifeFactor * frames));
		frameHeap.left = index % columns;
		frameHeap.top = index / columns;
		frameHeap.right = frameHeap.left + 1;
		frameHeap.bottom = frameHeap.top + 1;
		return frameHeap;
	}
}
