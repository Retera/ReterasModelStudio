package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.*;

//public class RenderParticle2 extends EmittedObject<RenderParticleEmitter2View> {
public class RenderParticle2Inst {
	public float health;
	public Vec3[] verticesV;
	public float rgb;
	public int[] lta_lba_rba_rta;
	private int uv_iX;
	private int uv_iY;

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
	float uniformScale = 1;


	//RenderData
	private Vec3[] verts = new Vec3[6];
	private int[] uv = new int[6];
	private Vec2[] uvs;
	private float[] uv_u = new float[6];
	private float[] uv_v = new float[6];

	public RenderParticle2Inst(ParticleEmitter2 particleEmitter2) {
		this.particleEmitter2 = particleEmitter2;
		health = 0;
		head = true;
		gravity = 0;

		verticesV = new Vec3[] {new Vec3(), new Vec3(), new Vec3(), new Vec3()};
		rgb = 0;
		lta_lba_rba_rta = new int[]{0, 1, 1, 0};
	}

	public RenderParticle2Inst reset(RenderNode2 node, boolean isHead, TimeEnvironmentImpl timeEnvironment) {
		double latitude = Math.toRadians(particleEmitter2.getRenderLatitude(timeEnvironment));
		this.node = node;
		verts[0] = null;

		resetScale();
		resetLocation(particleEmitter2.getRenderLength(timeEnvironment), particleEmitter2.getRenderWidth(timeEnvironment));
		resetRotation(latitude);

		fillColorHeaps();

		health = (float) particleEmitter2.getLifeSpan();
		head = isHead;
		this.gravity = (float) (particleEmitter2.getRenderGravity(timeEnvironment) * scale.z);


		// Apply the rotation
		velocity.set(Vec3.Z_AXIS).transform(rotation);

		// Apply speed
		velocity.scale((float) particleEmitter2.getRenderSpeed(timeEnvironment) * (1 + MathUtils.randomSym(particleEmitter2.getRenderVariation(timeEnvironment))));

		// Apply the parent's scale
		velocity.multiply(scale);

		return this;
	}

	private void resetScale() {
		scale.set(node.getWorldScale());
	}

	private void resetRotation(double latitude) {
		// Location rotation
		tempRotation.setFromAxisAngle(Vec3.Z_AXIS, 0);
		rotation.setFromAxisAngle(Vec3.X_AXIS, MathUtils.randomSym(latitude));
		rotation.mul(tempRotation);

		// If this is not a line emitter, emit in a sphere rather than a circle
		if (!particleEmitter2.getLineEmitter()) {
			tempRotation.setFromAxisAngle(Vec3.Y_AXIS, MathUtils.randomSym(latitude));
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
	public void update(float dt, Sequence sequence, Vec3[] vectors, Vec3 normal) {

		if(sequence instanceof Animation && !particleEmitter2.getModelSpace()){
			dLoc.x = -((Animation) sequence).getMoveSpeed()*dt;
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

		float factor = getTimeFactor(lifeFactor, timeMiddle);

		setColor(factor, isDecaying);


		factor = Math.min(factor, 1);

		updateFlipBookTexture(factor, getTextureInterval(isDecaying, head));


		float scale = getScale(factor, isDecaying);
		uniformScale = scale;

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
			normal.set(normal);

			tailHeap.set(worldLocation).sub(tailLocation).normalize();
			normal.cross(tailHeap).normalize().multiply(this.scale).scale(scale);
			verticesV[0].set(tailLocation).sub(normal);
			verticesV[1].set(worldLocation).add(normal);
			verticesV[2].set(worldLocation).sub(normal);
			verticesV[3].set(tailLocation).add(normal);
		}
	}

	public Vec3 getWorldLocation() {
		return worldLocation;
	}

	public Vec3 getLocation() {
		return location;
	}

	public float getUniformScale() {
		return uniformScale;
	}

	private Vec3 getTextureInterval(boolean isDecaying, boolean isHead) {
		if(isDecaying && isHead){
			return particleEmitter2.getHeadDecayUVAnim();
		} else if (isDecaying){
			return particleEmitter2.getTailDecayUVAnim();
		} else if(isHead){
			return particleEmitter2.getHeadUVAnim();
		} else {
			return particleEmitter2.getTailUVAnim();
		}
	}

	private void updateFlipBookTexture(float factor, Vec3 interval) {
		int left = 0;
		int top = 0;

		// If this is a team colored emitter, get the team color tile from the atlas
		// Otherwise do normal texture atlas handling.
		// except that Matrix Eater has no such atlas and we are simply copying from Ghostwolf
		if (!particleEmitter2.isTeamColored()) {
			int columns = particleEmitter2.getCols();
			int index = 0;

			float start = interval.x;
			float end = interval.y;
			float spriteCount = end - start;
			if ((spriteCount > 0) && ((columns > 1) || (particleEmitter2.getRows() > 1))) {
				// Repeating speeds up the sprite animation, which makes it effectively run N times in its interval.
				// E.g. if repeat is 4, the sprite animation will be seen 4 times, and thus also run 4 times as fast
				float repeat = interval.z;
				index = (int) (start + (Math.floor(spriteCount * repeat * factor) % spriteCount));
			}

			left = index % columns;
			top = (index / columns);
		}
		uv_iX = left;
		uv_iY = top;
		int right = left + 1;
		int bottom = top + 1;

		lta_lba_rba_rta[0] = MathUtils.uint8ToUint16((byte) right, (byte) bottom);
		lta_lba_rba_rta[1] = MathUtils.uint8ToUint16((byte) left, (byte) bottom);
		lta_lba_rba_rta[2] = MathUtils.uint8ToUint16((byte) left, (byte) top);
		lta_lba_rba_rta[3] = MathUtils.uint8ToUint16((byte) right, (byte) top);

//		rgb = MathUtils.uint8ToUint24((byte) ((int) (colorHeap.x * 255) & 0xFF), (byte) ((int) (colorHeap.y * 255) & 0xFF), (byte) ((int) (colorHeap.z * 255) & 0xFF));
		rgb = MathUtils.uint8ToUint32((byte) ((int) (colorHeap.x * 255) & 0xFF), (byte) ((int) (colorHeap.y * 255) & 0xFF), (byte) ((int) (colorHeap.z * 255) & 0xFF), (byte) ((int) (colorHeap.w * 255) & 0xFF));
	}

	private float getTimeFactor(float lifeFactor, float timeMiddle) {
		float factor;
		if(lifeFactor < timeMiddle){
			factor = lifeFactor / timeMiddle;
		} else {
			factor = (lifeFactor - timeMiddle) / (1 - timeMiddle);
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

	private void setColor(float lifeFactor, float timeMiddle) {
		if(lifeFactor < timeMiddle){
			colorHeap.set(color1Heap).lerp(color2Heap, lifeFactor / timeMiddle);
		} else {
			colorHeap.set(color2Heap).lerp(color3Heap, (lifeFactor - timeMiddle) / (1 - timeMiddle));
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

	private float getScale(float lifeFactor, float timeMiddle) {
		Vec3 scaling = particleEmitter2.getParticleScaling();
		float scale;
		if (lifeFactor < timeMiddle){
			scale = MathUtils.lerp(scaling.x, scaling.y, lifeFactor / timeMiddle);
		} else {
			scale = MathUtils.lerp(scaling.y, scaling.z, (lifeFactor - timeMiddle) / (1 - timeMiddle));
		}
		return scale;
	}

	private static final int[] quadVertOrder = new int[]{0,1,2,0,2,3};
	public void updateRenderData(){
		int cols = particleEmitter2.getCols();
		int rows = particleEmitter2.getRows();
		for(int i = 0; i<6; i++){
			setRenderData(i, verticesV[quadVertOrder[i]], lta_lba_rba_rta[quadVertOrder[i]], cols, rows);
		}
	}


	public RenderParticle2Inst setRenderData(int i, Vec3 v, int uv, int cols, int rows) {
		this.verts[i] = v;
		this.uv[i] = uv;

		uv_u[i] = (byte) ((uv >> 8) & 0xFF);
		uv_v[i] = (byte) ((uv >> 0) & 0xFF);

		if (particleEmitter2.isRibbonEmitter()) {
			uv_u[i] /= 255.0f;
			uv_v[i] /= 255.0f;
		} else {
			uv_u[i] /= cols;
			uv_v[i] /= rows;
		}


		return this;
	}

	public Vec4 getColorV(){
		return colorHeap;
	}
	public float getUv_u(int i) {
		return uv_u[i];
	}

	public float getUv_v(int i) {
		return uv_v[i];
	}

	public int getUv(int i) {
		return uv[i];
	}

	public int getUv_iX() {
		return uv_iX;
	}

	public int getUv_iY() {
		return uv_iY;
	}

	public Vec3 getVert(int i) {
		return verts[i];
	}
}
