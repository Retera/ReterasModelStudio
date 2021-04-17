package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

public class RenderParticle {
    private static final Vec3 UP = new Vec3(0, 0, 1);

    private final RenderParticleEmitter emitter;
    private final Vec3 velocity;
    private float gravity;
    private RenderParticleEmitterView emitterView;
    private final InternalInstance internalInstance;
    private RenderNode node;
    private double health;

    public RenderParticle(final RenderParticleEmitter emitter) {
        this.emitter = emitter;
        emitterView = null;

        internalInstance = emitter.internalResource.addInstance();
        velocity = new Vec3();
        gravity = 0;
    }

    public void reset(final RenderParticleEmitterView emitterView) {
        final RenderModel instance = emitterView.instance;
        final RenderNode renderNode = instance.getRenderNode(emitter.modelObject);
        final Vec3 scale = renderNode.getWorldScale();

        final double latitude = emitterView.getLatitude();
	    final double lifeSpan = emitterView.getLifeSpan();
	    final double gravity = emitterView.getGravity();
	    final double speed = emitterView.getSpeed();

	    this.emitterView = emitterView;
	    node = renderNode;
	    health = lifeSpan;
	    this.gravity = (float) (gravity * scale.z);

	    // Local rotation
	    Vec4 randomZRotVec = new Vec4(0, 0, 1, MathUtils.randomInRange(-Math.PI, Math.PI));
	    Quat rotationZHeap = new Quat().setFromAxisAngle(randomZRotVec);
	    Vec4 randomYRotVec = new Vec4(0, 1, 0, MathUtils.randomInRange(-latitude, latitude));
	    Quat rotationYHeap = new Quat().setFromAxisAngle(randomYRotVec);
	    rotationYHeap.mul(rotationZHeap);
	    Vec4 speedVec = new Vec4(0, 0, 1, 1);
	    speedVec.transform(rotationYHeap);

	    // World rotation
	    speedVec.transform(renderNode.getWorldRotation());

	    // Apply speed
	    velocity.set(speedVec);
	    velocity.scale((float) speed);

	    // Apply the parent's scale
	    velocity.multiply(scale);

	    emitterView.addToScene(internalInstance);

	    Vec4 zRandomAngle = new Vec4(0, 0, 1, MathUtils.randomInRange(0, Math.PI * 2));
	    rotationZHeap.setFromAxisAngle(zRandomAngle);
	    internalInstance.setTransformation(renderNode.getWorldLocation(), rotationZHeap, renderNode.getWorldScale());
	    internalInstance.setSequence(0);
	    internalInstance.show();
    }

    public void update() {
//        float frameTimeS = emitterView.instance.getAnimatedRenderEnvironment().getFrameTime()* 0.001f;
        final float frameTimeS = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
        internalInstance.setPaused(false);

        health -= frameTimeS;

        velocity.z -= gravity * frameTimeS;

	    Vec3 velocityTimeHeap = Vec3.getScaled(velocity, frameTimeS);

        internalInstance.move(velocityTimeHeap);

        if (health <= 0) {
            internalInstance.hide();
        }
    }
}
