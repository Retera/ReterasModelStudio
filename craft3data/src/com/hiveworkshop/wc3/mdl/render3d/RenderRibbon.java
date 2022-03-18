package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.MathUtils;

public class RenderRibbon extends EmittedObject<RenderRibbonEmitterView> {
	private static final Vector4f vector4Heap = new Vector4f();
	private static final Vector4f colorHeap = new Vector4f();
	private static final Vector4f color1Heap = new Vector4f();
	private static final Vector4f color2Heap = new Vector4f();
	private static Quaternion rotationZHeap = new Quaternion();
	private static Quaternion rotationYHeap = new Quaternion();
	private static Quaternion rotationXHeap = new Quaternion();
	private static Matrix4f matrixHeap = new Matrix4f();
	private static Vector3f locationHeap = new Vector3f();
	private static Vector4f location4Heap = new Vector4f();
	private static Vector4f startHeap = new Vector4f();
	private static Vector4f endHeap = new Vector4f();
	private static Vector3f tailHeap = new Vector3f();
	private static Vector3f normalHeap = new Vector3f();

	private static Vector4f belowHeap = new Vector4f();
	private static Vector4f aboveHeap = new Vector4f();
	private final RenderRibbonEmitter emitter;
	private int index;

	public RenderRibbon(final RenderRibbonEmitter emitter) {
		this.emitter = emitter;
		this.emitterView = null;
		this.health = 0;

		vertices = new float[12];
		this.lta = 0;
		this.lba = 0;
		this.rta = 0;
		this.rba = 0;
		this.rgb = 0;
	}

	@Override
	public void reset(final RenderRibbonEmitterView emitterView, final boolean isHead) {
		index = emitterView.currentRibbon++;

		emitterView.ribbonCount++;

		this.emitterView = emitterView;
		this.health = (float) emitter.getEmitter().getLifeSpan();

		final EmittedObject<RenderRibbonEmitterView> lastEmit = emitterView.lastEmit;

		// If this isn't the first ribbon, construct a quad.
		// Otherwise, the vertices will be filled with zeroes, and the ribbon will not
		// render.
		// This allows the emitter to always work with quads, and therefore it can work
		// with many views, because the ribbon chains are implicit.
		if ((lastEmit != null) && (lastEmit.health > 0)) {
			final RibbonEmitter modelObject = emitter.modelObject;
			final RenderNode node = emitterView.instance.getRenderNode(modelObject);
			final Vertex pivotPoint = modelObject.getPivotPoint();
			belowHeap.set((float) pivotPoint.x, (float) (pivotPoint.y - emitterView.getHeightBelow()),
					(float) pivotPoint.z, 1);
			Matrix4f.transform(node.getWorldMatrix(), belowHeap, belowHeap);

			aboveHeap.set((float) pivotPoint.x, (float) (pivotPoint.y + emitterView.getHeightAbove()),
					(float) pivotPoint.z, 1);
			Matrix4f.transform(node.getWorldMatrix(), aboveHeap, aboveHeap);

			final float[] lastVertices = lastEmit.vertices;

			// Left top
			vertices[0] = aboveHeap.x;
			vertices[1] = aboveHeap.y;
			vertices[2] = aboveHeap.z;

			// Left bottom
			vertices[3] = belowHeap.x;
			vertices[4] = belowHeap.y;
			vertices[5] = belowHeap.z;

			// Right bottom
			vertices[6] = lastVertices[3];
			vertices[7] = lastVertices[4];
			vertices[8] = lastVertices[5];

			// Right top
			vertices[9] = lastVertices[0];
			vertices[10] = lastVertices[1];
			vertices[11] = lastVertices[2];
		}
		else {
			for (int i = 0; i < 12; i++) {
				vertices[i] = 0;
			}
		}
	}

	@Override
	public void update() {
		final float dt = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
		// TODO some older ghostwolf code I'm copying from indicates animated gravity
		// might not exist, but i load it from a string map so i put it in anyway
		final double gravity = emitterView.getGravity() * dt * dt;
		final Vector3f animatedColor = emitterView.getColor();
		double animatedAlpha = emitterView.getAlpha();
		final int animatedSlot = emitterView.getTextureSlot();
		final float chainLengthFactor = 1 / (float) emitterView.ribbonCount;
		final int locationInChain = (emitterView.currentRibbon - this.index - 1);

		this.health -= dt;

		vertices[1] -= gravity;
		vertices[4] -= gravity;
		vertices[7] -= gravity;
		vertices[10] -= gravity;

		if (this.health <= 0) {
			emitterView.ribbonCount--;
		}
		else {
			final int columns = emitter.getEmitter().getColumns();
			float left = (animatedSlot % columns) + (locationInChain * chainLengthFactor);
			float top = (float) Math.floor(animatedSlot / (float) columns);
			float right = left + chainLengthFactor;
			float bottom = top + 1;

			left = (float) Math.floor(left * 255);
			top = (float) Math.floor(top * 255);
			// Paladin - when the UV rectangle reaches 254-255 on the X axis, it has a row
			// or two of white pixels in the end for some reason.
			// This happens also if the texture coordinates are clamped to [0, 1] in the
			// shader.
			// The only thing that removes it is to change the texture to being clamped
			// rather than repeating.
			// How is this possible?
			right = (float) Math.floor(right * 255);
			bottom = (float) Math.floor(bottom * 255);
			animatedAlpha = (float) Math.floor(animatedAlpha * 255);

			this.lta = MathUtils.uint8ToUint24((byte) left, (byte) top, (byte) animatedAlpha);
			this.lba = MathUtils.uint8ToUint24((byte) left, (byte) bottom, (byte) animatedAlpha);
			this.rta = MathUtils.uint8ToUint24((byte) right, (byte) top, (byte) animatedAlpha);
			this.rba = MathUtils.uint8ToUint24((byte) right, (byte) bottom, (byte) animatedAlpha);
			this.rgb = MathUtils.uint8ToUint24((byte) ((int) (animatedColor.z * 255) & 0xFF),
					(byte) ((int) (animatedColor.y * 255) & 0xFF), (byte) ((int) (animatedColor.x * 255) & 0xFF));

		}
	}
}
