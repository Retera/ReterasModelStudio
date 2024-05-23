package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.*;

public class RenderParticleRibbonInst {
	public float health;
	public float rgb;
	public int[] lta_lba_rba_rta;
	protected int uv_iX;
	protected int uv_iY;

	protected final RibbonEmitter ribbon;
	protected final Vec3 above = new Vec3();
	protected final Vec3 below = new Vec3();
	protected final Vec3 worldAbove = new Vec3();
	protected final Vec3 worldBelow = new Vec3();
	protected final Vec3 location = new Vec3();
	protected final Vec3 worldLocation = new Vec3();
	protected final Vec3 velocity = new Vec3();
	protected final Quat rotation = new Quat();
	protected final Vec3 scale = new Vec3();
	protected float gravity;

	protected RenderNode2 node;
	Vec2 uvAdv = new Vec2();

	Vec4 colorHeap = new Vec4();

	float uniformScale = 1;

	protected final Vec2 uvAbove = new Vec2();
	protected final Vec2 uvBelow = new Vec2();

	//RenderData
//	protected Vec3[] verts = new Vec3[6];
	protected int[] uv = new int[6];
	protected Vec2[] uvs;
	protected float[] uv_u = new float[6];
	protected float[] uv_v = new float[6];

	public RenderParticleRibbonInst(RibbonEmitter ribbon) {
		this.ribbon = ribbon;
		health = 0;
		gravity = 0;

		rgb = 0;
		lta_lba_rba_rta = new int[]{0, 1, 1, 0};
	}

	public RenderParticleRibbonInst reset(RenderNode2 node, Vec2 uvScale, Vec2 uvAdv, TimeEnvironmentImpl timeEnvironment) {
//		double latitude = Math.toRadians(particleEmitter2.getRenderLatitude(timeEnvironment));
		this.node = node;
		this.uvAdv.set(uvAdv);

		scale.set(node.getWorldScale());
		location.set(ribbon.getPivotPoint()).transform(node.getWorldMatrix());
		rotation.setIdentity().mul(node.getWorldRotation());

		float f_above = ribbon.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_HEIGHT_ABOVE, (float) ribbon.getHeightAbove());
		float f_below = ribbon.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_HEIGHT_BELOW, (float) ribbon.getHeightBelow());
		above.set(0, f_above, 0);
		below.set(0, -f_below, 0);

		uvAbove.set(0,0).mul(uvScale);
		uvBelow.set(0,1).mul(uvScale);

		worldAbove.set(above).add(ribbon.getPivotPoint()).transform(node.getWorldMatrix());
		worldBelow.set(below).add(ribbon.getPivotPoint()).transform(node.getWorldMatrix());

		fillColorHeaps(timeEnvironment);

		health = (float) ribbon.getLifeSpan();
		this.gravity = ribbon.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_GRAVITY, (float) ribbon.getGravity()) * scale.z;

		velocity.set(Vec3.ZERO);
		return this;
	}

	private void fillColorHeaps(TimeEnvironmentImpl timeEnvironment) {
		colorHeap.set(ribbon.getInterpolatedVector(timeEnvironment, MdlUtils.TOKEN_COLOR, ribbon.getStaticColor()), ribbon.getInterpolatedFloat(timeEnvironment, MdlUtils.TOKEN_ALPHA, (float) ribbon.getAlpha()));
//		colorHeap.set(1,0,1,1);
	}

	//	@Override
	Vec3 dLoc = new Vec3();
	public void update(float dt, Sequence sequence) {

		if(sequence instanceof Animation){
			dLoc.x = -((Animation) sequence).getMoveSpeed()*dt;
		} else {
			dLoc.x = 0;
		}


		health -= dt;
		velocity.z -= gravity * dt;
		location.addScaled(velocity, dt);
		location.add(dLoc);

		uniformScale = 1f;

		worldLocation.set(location);
		if(health <= 0){
			worldAbove.set(Vec3.ZERO);
			worldBelow.set(Vec3.ZERO);
		} else {
			worldAbove.addScaled(velocity, dt).add(dLoc);
			worldBelow.addScaled(velocity, dt).add(dLoc);

			uvAbove.addScaled(uvAdv, dt);
			uvBelow.addScaled(uvAdv, dt);
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


	private void updateFlipBookTexture(float factor, Vec3 interval) {
		int left = 0;
		int top = 0;

		// If this is a team colored emitter, get the team color tile from the atlas
		// Otherwise do normal texture atlas handling.
		// except that Matrix Eater has no such atlas and we are simply copying from Ghostwolf

		int columns = ribbon.getCols();
		int index = 0;

		float start = interval.x;
		float end = interval.y;
		float spriteCount = end - start;
		if ((spriteCount > 0) && ((columns > 1) || (ribbon.getRows() > 1))) {
			// Repeating speeds up the sprite animation, which makes it effectively run N times in its interval.
			// E.g. if repeat is 4, the sprite animation will be seen 4 times, and thus also run 4 times as fast
			float repeat = interval.z;
			index = (int) (start + (Math.floor(spriteCount * repeat * factor) % spriteCount));
		}

		left = index % columns;
		top = (index / columns);

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


	public RenderParticleRibbonInst setRenderData(int i, Vec3 v, int uv, int cols, int rows) {
//		this.verts[i] = v;
		this.uv[i] = uv;

		uv_u[i] = (byte) ((uv >> 8) & 0xFF);
		uv_v[i] = (byte) ((uv >> 0) & 0xFF);

		if (ribbon.isRibbonEmitter()) {
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

//	public Vec3 getVert(int i) {
//		return verts[i];
//	}

	public Vec3 getWorldAbove() {
		return worldAbove;
	}
	public Vec3 getWorldBelow() {
		return worldBelow;
	}
	public Vec2 getUvAbove() {
		return uvAbove;
	}
	public Vec2 getUvBelow() {
		return uvBelow;
	}
}
