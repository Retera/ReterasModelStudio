package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public class RenderParticle2 extends EmittedObject<RenderParticleEmitter2View> {
	private final RenderParticleEmitter2 emitter;
	private boolean head;
	private final Vec3 location;
	private final Vec3 velocity;
	private float gravity;
	private final Vec3 nodeScale;

	private RenderNode node;

	Vec4 color1Heap = new Vec4();
	Vec4 color2Heap = new Vec4();
	Vec4 color3Heap = new Vec4();
	Vec4 colorHeap = new Vec4();

	public RenderParticle2(final RenderParticleEmitter2 emitter) {
		this.emitter = emitter;
		emitterView = null;
		health = 0;
		head = true;
		location = new Vec3();
		velocity = new Vec3();
		gravity = 0;
		nodeScale = new Vec3();

//		verticesV = new Vec3[4];
		verticesV = new Vec3[] {new Vec3(), new Vec3(), new Vec3(), new Vec3()};
		lta = 0;
		lba = 0;
		rta = 0;
		rba = 0;
		rgb = 0;
	}

	@Override
	public void reset(final RenderParticleEmitter2View emitterView, final boolean isHead) {
		double latitude = Math.toRadians(emitterView.getLatitude());

		ParticleEmitter2 particleEmitter2 = emitter.modelObject;

		node = emitterView.instance.getRenderNode(particleEmitter2);
		Vec3 scale = node.getWorldScale();


		color1Heap.set(particleEmitter2.getSegmentColors()[0], particleEmitter2.getAlpha().x);
		color2Heap.set(particleEmitter2.getSegmentColors()[1], particleEmitter2.getAlpha().y);
		color3Heap.set(particleEmitter2.getSegmentColors()[2], particleEmitter2.getAlpha().z);

		this.emitterView = emitterView;
		health = (float) particleEmitter2.getLifeSpan();
		head = isHead;
		this.gravity = (float) (emitterView.getGravity() * scale.z);

		nodeScale.set(scale);

		// Local location

		double width = emitterView.getLength() / 2;
		float randomWidth = MathUtils.randomInRange(-width, width);
		double length = emitterView.getWidth() / 2;
		float randomLength = MathUtils.randomInRange(-length, length);

		location.set(particleEmitter2.getPivotPoint()).add(new Vec3(randomWidth, randomLength, 0));

		// World location
		if (!particleEmitter2.getModelSpace()) {
			Vec4 vec4loc = new Vec4(location, 1);
			vec4loc.transform(node.getWorldMatrix());
			location.set(vec4loc);
		}

		// Location rotation
		Vec4 vec4perpZ = new Vec4(0, 0, 1, 0);
		Quat rotationZHeap = new Quat().setFromAxisAngle(vec4perpZ);
		Vec4 vec4randomX = new Vec4(1, 0, 0, MathUtils.randomInRange(-latitude, latitude));
		Quat rotationYHeap = new Quat().setFromAxisAngle(vec4randomX);
		rotationYHeap.mul(rotationZHeap);

		// If this is not a line emitter, emit in a sphere rather than a circle
		if (!particleEmitter2.getLineEmitter()) {
			Vec4 vec4randomY = new Vec4(0, 1, 0, MathUtils.randomInRange(-latitude, latitude));
			Quat rotationXHeap = new Quat().setFromAxisAngle(vec4randomY);
			rotationYHeap.mul(rotationXHeap);
		}

		// World rotation
		if (!particleEmitter2.getModelSpace()) {
			rotationYHeap.mul(node.getWorldRotation());
		}

		// Apply the rotation
		Vec4 vec4Z = new Vec4(0, 0, 1, 1);
		vec4Z.transform(rotationYHeap);
		velocity.set(vec4Z);

		// Apply speed
		velocity.scale((float) emitterView.getSpeed() * (1 + MathUtils.randomInRange(-emitterView.getVariation(), emitterView.getVariation())));

		// Apply the parent's scale
		velocity.multiply(scale);
	}

	@Override
	public void update() {
		ParticleEmitter2 modelObject = emitter.modelObject;
		float dt = (float) (TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001f * emitterView.getTimeScale());

		health -= dt;
		velocity.z -= gravity * dt;
		location.add(Vec3.getScaled(velocity, dt));

		float lifeFactor = (float) ((modelObject.getLifeSpan() - health) / modelObject.getLifeSpan());
		float timeMiddle = (float) modelObject.getTime();
		float factor;
		int firstColor = 0;
		Vec3 interval;

		if (lifeFactor < timeMiddle) {
			factor = lifeFactor / timeMiddle;

			colorHeap = Vec4.getLerped(color1Heap, color2Heap, factor);

			if (head) {
				interval = modelObject.getHeadUVAnim();
			} else {
				interval = modelObject.getTailUVAnim();
			}
		} else {
			firstColor = 1;
			factor = (lifeFactor - timeMiddle) / (1 - timeMiddle);
			colorHeap = Vec4.getLerped(color2Heap, color3Heap, factor);

			if (head) {
				interval = modelObject.getHeadDecayUVAnim();
			} else {
				interval = modelObject.getTailDecayUVAnim();
			}
		}

		factor = Math.min(factor, 1);

		float left = 0;
		float top = 0;

		// If this is a team colored emitter, get the team color tile from the atlas
		// Otherwise do normal texture atlas handling.
		// except that Matrix Eater has no such atlas and we are simply copying from Ghostwolf
		if (!modelObject.isTeamColored()) {
			int columns = modelObject.getCols();
			float index = 0;

			float start = interval.x;
			float end = interval.y;
			float spriteCount = end - start;
			if ((spriteCount > 0) && ((columns > 1) || (modelObject.getRows() > 1))) {
				// Repeating speeds up the sprite animation, which makes it effectively run N times in its interval.
				// E.g. if repeat is 4, the sprite animation will be seen 4 times, and thus also run 4 times as fast
				float repeat = interval.z;
				index = (float) (start + (Math.floor(spriteCount * repeat * factor) % spriteCount));
			}

			left = index % columns;
			top = (int) (index / columns);
		}
		float right = left + 1;
		float bottom = top + 1;

//		final Vec3 firstColorVertexME = colors[firstColor];
//		final Vec3 secondColorVertexME = colors[firstColor + 1];
//		color1Heap.set(firstColorVertexME.x, firstColorVertexME.y, firstColorVertexME.z, modelObject.getAlpha().getCoord((byte) firstColor));
//		color2Heap.set(secondColorVertexME.x, secondColorVertexME.y, secondColorVertexME.z, modelObject.getAlpha().getCoord((byte) (firstColor + 1)));
//		color1Heap.lerp(color2Heap, factor, colorHeap);
		Vec3[] colors = modelObject.getSegmentColors();
//		Vec4 color1Heap = new Vec4(colors[firstColor], modelObject.getAlpha().getCoord((byte) firstColor));
//		Vec4 color2Heap = new Vec4(colors[firstColor + 1], modelObject.getAlpha().getCoord((byte) (firstColor + 1)));
//		Vec4 colorHeap = Vec4.getLerped(color1Heap, color2Heap, factor);

		int a = ((int) colorHeap.w) & 0xFF;

		lta = MathUtils.uint8ToUint24((byte) right, (byte) bottom, (byte) a);
		lba = MathUtils.uint8ToUint24((byte) left, (byte) bottom, (byte) a);
		rta = MathUtils.uint8ToUint24((byte) right, (byte) top, (byte) a);
		rba = MathUtils.uint8ToUint24((byte) left, (byte) top, (byte) a);
//		rgb = MathUtils.uint8ToUint24((byte) ((int) (colorHeap.z * 255) & 0xFF), (byte) ((int) (colorHeap.y * 255) & 0xFF), (byte) ((int) (colorHeap.x * 255) & 0xFF));
		rgb = MathUtils.uint8ToUint24((byte) ((int) (colorHeap.x * 255) & 0xFF), (byte) ((int) (colorHeap.y * 255) & 0xFF), (byte) ((int) (colorHeap.z * 255) & 0xFF));

		final Vec4[] vectors;

		// Choose between a default rectangle or a billboarded one
		final RenderModel instance = emitterView.instance;
		if (modelObject.getXYQuad()) {
			vectors = instance.getSpacialVectors();
		} else {
			vectors = instance.getBillboardVectors();
		}

		Vec3 scaling = modelObject.getParticleScaling();
		float scale = MathUtils.lerp(scaling.getCoord((byte) firstColor), scaling.getCoord((byte) (firstColor + 1)), factor);

//		Vec3 scaleV = Vec3.getScaled(this.nodeScale, scale);

		Vec4 worldLocation4f = new Vec4(location, 1);

		if (head) {
			// If this is a model space emitter, the particle location is in local space, so
			// convert it now to world space.
			if (modelObject.getModelSpace()) {
				worldLocation4f.transform(node.getWorldMatrix());
			}

			Vec3 p = worldLocation4f.getVec3();

//			final Vec3 pv1 = vectors[0].getVec3();
//			final Vec3 pv2 = vectors[1].getVec3();
//			final Vec3 pv3 = vectors[2].getVec3();
//			final Vec3 pv4 = vectors[3].getVec3();

//			verticesV[0].set(vectors[0]).multiply(scaleV).add(p);
//			verticesV[1].set(vectors[1]).multiply(scaleV).add(p);
//			verticesV[2].set(vectors[2]).multiply(scaleV).add(p);
//			verticesV[3].set(vectors[3]).multiply(scaleV).add(p);

			verticesV[0].set(vectors[0]).multiply(this.nodeScale).scale(scale).add(p);
			verticesV[1].set(vectors[1]).multiply(this.nodeScale).scale(scale).add(p);
			verticesV[2].set(vectors[2]).multiply(this.nodeScale).scale(scale).add(p);
			verticesV[3].set(vectors[3]).multiply(this.nodeScale).scale(scale).add(p);
//			verticesV[0] = Vec3.getSum(p, Vec3.getProd(pv1, scaleV));
//			verticesV[1] = Vec3.getSum(p, Vec3.getProd(pv2, scaleV));
//			verticesV[2] = Vec3.getSum(p, Vec3.getProd(pv3, scaleV));
//			verticesV[3] = Vec3.getSum(p, Vec3.getProd(pv4, scaleV));
		} else {
			double tailLength = modelObject.getTailLength();
			Vec3 offsetV = Vec3.getScaled(velocity, (float) tailLength);

			// The start and end of the tail
			Vec3 startHeap = worldLocation4f.getVec3().sub(offsetV);
			Vec3 endHeap = worldLocation4f.getVec3();

			// If this is a model space emitter, the start and end are in local space, so
			// convert them to world space.
			if (modelObject.getModelSpace()) {

				startHeap.transform(node.getWorldMatrix());
				endHeap.transform(node.getWorldMatrix());
			}

			// Get the normal to the tail in camera space
			// This allows to build a 2D rectangle around the 3D tail
			Vec3 tailHeap = Vec3.getDiff(endHeap, startHeap).normalize();

//			Vec3 normal = instance.getBillboardVectors()[6].getVec3().cross(tailHeap).normalize().multiply(scaleV);
			Vec3 normal = instance.getBillboardVectors()[6].getVec3().cross(tailHeap).normalize().multiply(this.nodeScale).scale(scale);

			verticesV[0].set(startHeap).sub(normal);
			verticesV[1].set(endHeap).add(normal);
			verticesV[2].set(endHeap).sub(normal);
			verticesV[3].set(startHeap).add(normal);
//			verticesV[0] = Vec3.getDiff(startV, normal);
//			verticesV[1] = Vec3.getSum(endV, normal);
//			verticesV[2] = Vec3.getDiff(endV, normal);
//			verticesV[3] = Vec3.getSum(scaleV, normal);
		}
	}
}
