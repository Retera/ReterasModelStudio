package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

//public class RenderParticle2 extends EmittedObject<RenderParticleEmitter2View> {
public class RenderParticle2 {
	private static final Vec3 xAxis = new Vec3(1,0,0);
	private static final Vec3 yAxis = new Vec3(0,1,0);
	private static final Vec3 zAxis = new Vec3(0,0,1);
	public float health;
	public Vec3[] verticesV;
	public float lta, lba, rta, rba, rgb;
	public float[] lta_lba_rta_rba_rgb;

	private final ParticleEmitter2 particleEmitter2;
	private boolean head;
	private final Vec3 location = new Vec3();
	private final Vec3 worldLocation = new Vec3();
	private final Vec3 velocity = new Vec3();
	private final Quat rotation = new Quat();
	private final Quat tempRotation = new Quat();
	private final Vec3 scale = new Vec3();
	private float gravity;

	private RenderNode2 node;

	Vec4 color1Heap = new Vec4();
	Vec4 color2Heap = new Vec4();
	Vec4 color3Heap = new Vec4();
	Vec4 colorHeap = new Vec4();

	Vec3 tailLocation = new Vec3();
	Vec3 tailHeap = new Vec3();
	Vec3 normal = new Vec3();

	public RenderParticle2(ParticleEmitter2 particleEmitter2) {
		this.particleEmitter2 = particleEmitter2;
		health = 0;
		head = true;
		gravity = 0;

		verticesV = new Vec3[] {new Vec3(), new Vec3(), new Vec3(), new Vec3()};
		lta = 0;
		lba = 0;
		rta = 0;
		rba = 0;
		rgb = 0;
		lta_lba_rta_rba_rgb = new float[]{0, 0, 0, 0, 0};
	}

	public void reset(RenderModel renderModel, boolean isHead, TimeEnvironmentImpl timeEnvironment) {
		double latitude = Math.toRadians(particleEmitter2.getRenderLatitude(timeEnvironment));

		resetScale(renderModel);
//		resetLocation(emitterView);
//		resetLocation(emitterView.getLength(), emitterView.getWidth());
		resetLocation(particleEmitter2.getRenderLength(timeEnvironment), particleEmitter2.getRenderWidth(timeEnvironment));
		resetRotation(latitude);

		fillColorHeaps();

		health = (float) particleEmitter2.getLifeSpan();
		head = isHead;
		this.gravity = (float) (particleEmitter2.getRenderGravity(timeEnvironment) * scale.z);


		// Apply the rotation
		velocity.set(zAxis).transform(rotation);

		// Apply speed
		velocity.scale((float) particleEmitter2.getRenderSpeed(timeEnvironment) * (1 + MathUtils.randomSym(particleEmitter2.getRenderVariation(timeEnvironment))));

		// Apply the parent's scale
		velocity.multiply(scale);
	}

	private void resetScale(RenderModel renderModel) {
		node = renderModel.getRenderNode(particleEmitter2);
		scale.set(node.getWorldScale());
	}

	private void resetRotation(double latitude) {
		// Location rotation
		tempRotation.setFromAxisAngle(zAxis, 0);
		rotation.setFromAxisAngle(xAxis, MathUtils.randomSym(latitude));
		rotation.mul(tempRotation);

		// If this is not a line emitter, emit in a sphere rather than a circle
		if (!particleEmitter2.getLineEmitter()) {
			tempRotation.setFromAxisAngle(yAxis, MathUtils.randomSym(latitude));
			rotation.mul(tempRotation);
		}
		// World rotation
		if (!particleEmitter2.getModelSpace()) {
			rotation.mul(node.getWorldRotation());
		}
	}

	private void resetLocation(double width, double length) {
		// Local location
		location.set(MathUtils.randomSym(width/ 2), MathUtils.randomSym(length/ 2), 0).add(particleEmitter2.getPivotPoint());
		// World location
		if (!particleEmitter2.getModelSpace()) {
			location.transform(node.getWorldMatrix());
		}
	}

	private void fillColorHeaps() {
		color1Heap.set(particleEmitter2.getSegmentColors()[0], particleEmitter2.getAlpha().x);
		color2Heap.set(particleEmitter2.getSegmentColors()[1], particleEmitter2.getAlpha().y);
		color3Heap.set(particleEmitter2.getSegmentColors()[2], particleEmitter2.getAlpha().z);
	}

	//	@Override
	Vec3 dLoc = new Vec3();
	public void update(float animationSpeed, RenderModel renderModel) {
		float dt = (float) (TimeEnvironmentImpl.FRAMES_PER_UPDATE * 0.001f * animationSpeed);

		if(renderModel.getTimeEnvironment().getCurrentSequence() instanceof Animation && !particleEmitter2.getModelSpace()){
			dLoc.x = -((Animation) renderModel.getTimeEnvironment().getCurrentSequence()).getMoveSpeed()*dt;
		} else {
			dLoc.x = 0;
		}

		health -= dt;
		velocity.z -= gravity * dt;
		location.addScaled(velocity, dt);
		location.add(dLoc);

		float lifeFactor = (float) ((particleEmitter2.getLifeSpan() - health) / particleEmitter2.getLifeSpan());
		float timeMiddle = (float) particleEmitter2.getTime();
		boolean isDecaying = !(lifeFactor < timeMiddle);

		float factor = getTimeFactor(lifeFactor, timeMiddle, isDecaying);

		setColor(factor, isDecaying);

		Vec3 interval;
		if(isDecaying){
			if (head) {
				interval = particleEmitter2.getHeadDecayUVAnim();
			} else {
				interval = particleEmitter2.getTailDecayUVAnim();
			}
		} else {
			if (head) {
				interval = particleEmitter2.getHeadUVAnim();
			} else {
				interval = particleEmitter2.getTailUVAnim();
			}
		}

		factor = Math.min(factor, 1);

		updateFlipBookTexture(factor, interval);


		// Choose between a default rectangle or a billboarded one
		Vec3[] vectors;
		if (particleEmitter2.getXYQuad()) {
			vectors = renderModel.getSpacialVectors();
		} else {
			vectors = renderModel.getBillboardVectors();
		}

		float scale = getScale(factor, isDecaying);

		worldLocation.set(location);

		if (head) {
			// If this is a model space emitter, the particle location is in local space, so convert it now to world space.
			if (particleEmitter2.getModelSpace()) {
				worldLocation.transform(node.getWorldMatrix());
			}

			verticesV[0].set(vectors[0]).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[1].set(vectors[1]).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[2].set(vectors[2]).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[3].set(vectors[3]).multiply(this.scale).scale(scale).add(worldLocation);
		} else {

			// The start of the tail
			tailLocation.set(worldLocation).addScaled(velocity, (float) -particleEmitter2.getTailLength());

			// If this is a model space emitter, the start and end are in local space, so convert them to world space.
			if (particleEmitter2.getModelSpace()) {

				tailLocation.transform(node.getWorldMatrix());
				worldLocation.transform(node.getWorldMatrix());
			}

			// Get the normal to the tail in camera space
			// This allows to build a 2D rectangle around the 3D tail
//			Vec3 tailHeap = Vec3.getDiff(endHeap, startHeap).normalize();
			normal.set(renderModel.getBillboardVectors()[6]);

			tailHeap.set(worldLocation).sub(tailLocation).normalize();
			normal.cross(tailHeap).normalize().multiply(this.scale).scale(scale);
			verticesV[0].set(tailLocation).sub(normal);
			verticesV[1].set(worldLocation).add(normal);
			verticesV[2].set(worldLocation).sub(normal);
			verticesV[3].set(tailLocation).add(normal);
//			verticesV[0].set(startHeap).sub(normal);
//			verticesV[1].set(endHeap).add(normal);
//			verticesV[2].set(endHeap).sub(normal);
//			verticesV[3].set(startHeap).add(normal);
		}
	}

	private void updateFlipBookTexture(float factor, Vec3 interval) {
		float left = 0;
		float top = 0;

		// If this is a team colored emitter, get the team color tile from the atlas
		// Otherwise do normal texture atlas handling.
		// except that Matrix Eater has no such atlas and we are simply copying from Ghostwolf
		if (!particleEmitter2.isTeamColored()) {
			int columns = particleEmitter2.getCols();
			float index = 0;

			float start = interval.x;
			float end = interval.y;
			float spriteCount = end - start;
			if ((spriteCount > 0) && ((columns > 1) || (particleEmitter2.getRows() > 1))) {
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

		int a = ((int) colorHeap.w) & 0xFF;

		lta = MathUtils.uint8ToUint24((byte) right, (byte) bottom, (byte) a);
		lba = MathUtils.uint8ToUint24((byte) left, (byte) bottom, (byte) a);
		rta = MathUtils.uint8ToUint24((byte) right, (byte) top, (byte) a);
		rba = MathUtils.uint8ToUint24((byte) left, (byte) top, (byte) a);
		rgb = MathUtils.uint8ToUint24((byte) ((int) (colorHeap.x * 255) & 0xFF), (byte) ((int) (colorHeap.y * 255) & 0xFF), (byte) ((int) (colorHeap.z * 255) & 0xFF));
		lta_lba_rta_rba_rgb[0] = lta;
		lta_lba_rta_rba_rgb[1] = lba;
		lta_lba_rta_rba_rgb[2] = rta;
		lta_lba_rta_rba_rgb[3] = rba;
		lta_lba_rta_rba_rgb[4] = rgb;
	}

	private float getTimeFactor(float lifeFactor, float timeMiddle, boolean isDecaying) {
		float factor;
		if(isDecaying){
			factor = (lifeFactor - timeMiddle) / (1 - timeMiddle);
		} else {
			factor = lifeFactor / timeMiddle;
		}
		return factor;
	}

	private void setColor(float factor, boolean isDecaying) {
		if(isDecaying){
			colorHeap.set(color2Heap).lerp(color3Heap, factor);
		} else {
			colorHeap.set(color1Heap).lerp(color2Heap, factor);
		}
	}

	private float getScale(float factor, boolean isDecaying) {
		Vec3 scaling = particleEmitter2.getParticleScaling();
		float scale;
		if (isDecaying){
			scale = MathUtils.lerp(scaling.y, scaling.z, factor);
		} else {
			scale = MathUtils.lerp(scaling.x, scaling.y, factor);
		}
		return scale;
	}
}
