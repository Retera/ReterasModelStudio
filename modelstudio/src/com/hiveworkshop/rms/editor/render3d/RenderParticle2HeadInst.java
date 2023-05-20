package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public class RenderParticle2HeadInst extends RenderParticle2Inst {

	public RenderParticle2HeadInst(ParticleEmitter2 particleEmitter2) {
		super(particleEmitter2);
	}

	public RenderParticle2HeadInst reset(RenderNode2 node, boolean isHead, TimeEnvironmentImpl timeEnvironment) {
		double latitude = Math.toRadians(particleEmitter2.getRenderLatitude(timeEnvironment));
		this.node = node;
		verts[0] = null;

		resetScale();
		resetLocation(particleEmitter2.getRenderLength(timeEnvironment), particleEmitter2.getRenderWidth(timeEnvironment));
		resetRotation(latitude);

		fillColorHeaps();

		health = (float) particleEmitter2.getLifeSpan();
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
		location.set(MathUtils.randomSym(width/ 2), MathUtils.randomSym(length/ 2), 0);
		// World location
		if (!particleEmitter2.getModelSpace()) {
			location.add(particleEmitter2.getPivotPoint()).transform(node.getWorldMatrix());
		} else {
			location.add(node.getPivot());
		}
	}

	private void fillColorHeaps() {
		color1Heap.set(particleEmitter2.getSegmentColors()[0], particleEmitter2.getAlpha().x);
		color2Heap.set(particleEmitter2.getSegmentColors()[1], particleEmitter2.getAlpha().y);
		color3Heap.set(particleEmitter2.getSegmentColors()[2], particleEmitter2.getAlpha().z);
	}
	private static final Vec3[] SPACIAL_VECTORS = {
			new Vec3(-1, 1, 0),
			new Vec3(1, 1, 0),
			new Vec3(1, -1, 0),
			new Vec3(-1, -1, 0),
			new Vec3(1, 0, 0),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1)};
	private final Vec3[] billboardVectors = {
			new Vec3(0, 1, -1),
			new Vec3(0, -1, -1),
			new Vec3(0, -1, 1),
			new Vec3(0, 1, 1),
			new Vec3(0, 1, 0),
			new Vec3(0, 0, 1),
			new Vec3(1, 0, 0)};

	public void update(float dt, Sequence sequence, Vec3[] vectors, Vec3 normal, Quat inverseCameraRotation) {

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

		if (particleEmitter2.getModelSpace()) {
			worldLocation.transform(node.getWorldMatrix());
		}

		tailLocation.set(0,0,0);

		if(particleEmitter2.getXYQuad()){

			verticesV[0].set(-1,  1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[1].set( 1,  1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[2].set( 1, -1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[3].set(-1, -1, 0).multiply(this.scale).scale(scale).add(worldLocation);
		} else {
			verticesV[0].set(0,  1, -1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[1].set(0, -1, -1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[2].set(0, -1,  1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[3].set(0,  1,  1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
		}
	}
	public void update(float dt, Sequence sequence, Quat inverseCameraRotation) {

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

		if (particleEmitter2.getModelSpace()) {
			worldLocation.transform(node.getWorldMatrix());
		}

		tailLocation.set(0,0,0);

		if(particleEmitter2.getXYQuad()){

			verticesV[0].set(-1,  1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[1].set( 1,  1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[2].set( 1, -1, 0).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[3].set(-1, -1, 0).multiply(this.scale).scale(scale).add(worldLocation);
		} else {
			verticesV[0].set(0,  1, -1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[1].set(0, -1, -1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[2].set(0, -1,  1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
			verticesV[3].set(0,  1,  1).transform(inverseCameraRotation).multiply(this.scale).scale(scale).add(worldLocation);
		}
	}

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

		if (particleEmitter2.getModelSpace()) {
			worldLocation.transform(node.getWorldMatrix());
		}

		verticesV[0].set(vectors[0]).multiply(this.scale).scale(scale).add(worldLocation);
		verticesV[1].set(vectors[1]).multiply(this.scale).scale(scale).add(worldLocation);
		verticesV[2].set(vectors[2]).multiply(this.scale).scale(scale).add(worldLocation);
		verticesV[3].set(vectors[3]).multiply(this.scale).scale(scale).add(worldLocation);
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
		if(isDecaying){
			return particleEmitter2.getHeadDecayUVAnim();
		} else {
			return particleEmitter2.getHeadUVAnim();
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


	public RenderParticle2HeadInst setRenderData(int i, Vec3 v, int uv, int cols, int rows) {
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
