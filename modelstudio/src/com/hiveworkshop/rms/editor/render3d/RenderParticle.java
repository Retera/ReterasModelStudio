package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Mat4;
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
    private static final Quat rotationZHeap = new Quat();
    private static final Quat rotationYHeap = new Quat();
    private static final Vec4 vector4Heap = new Vec4();
    private static final Mat4 matrixHeap = new Mat4();
    private static final Vec3 velocityTimeHeap = new Vec3();

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
        rotationZHeap.setIdentity();
        vector4Heap.set(0, 0, 1, MathUtils.randomInRange(-Math.PI, Math.PI));
        rotationZHeap.setFromAxisAngle(vector4Heap);
        vector4Heap.set(0, 1, 0, MathUtils.randomInRange(-latitude, latitude));
        rotationYHeap.setFromAxisAngle(vector4Heap);
        rotationYHeap.mul(rotationZHeap);
        matrixHeap.fromQuat(rotationYHeap);
        vector4Heap.set(0, 0, 1, 1);
        matrixHeap.transform(vector4Heap);

        // World rotation
        matrixHeap.fromQuat(renderNode.getWorldRotation());
        matrixHeap.transform(vector4Heap);

        // Apply speed
        velocity.set(vector4Heap);
        velocity.scale((float) speed);

        // Apply the parent's scale
        velocity.x *= scale.x;
        velocity.y *= scale.y;
        velocity.z *= scale.z;

        emitterView.addToScene(internalInstance);

        vector4Heap.set(0, 0, 1, MathUtils.randomInRange(0, Math.PI * 2));
        rotationZHeap.setFromAxisAngle(vector4Heap);
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

        velocityTimeHeap.x = velocity.x * frameTimeS;
        velocityTimeHeap.y = velocity.y * frameTimeS;
        velocityTimeHeap.z = velocity.z * frameTimeS;

        internalInstance.move(velocityTimeHeap);

        if (health <= 0) {
            internalInstance.hide();
        }
    }
}
