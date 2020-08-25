package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Matrix4;
import com.hiveworkshop.rms.editor.model.QuaternionRotation;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.MathUtils;

import org.lwjgl.util.vector.Vector4f;

public class RenderParticle {
    private static final Vertex UP = new Vertex(0, 0, 1);

    private final RenderParticleEmitter emitter;
    private final Vertex velocity;
    private float gravity;
    private RenderParticleEmitterView emitterView;
    private final InternalInstance internalInstance;
    private RenderNode node;
    private double health;
    private static final QuaternionRotation rotationZHeap = new QuaternionRotation();
    private static final QuaternionRotation rotationYHeap = new QuaternionRotation();
    private static final Vector4f vector4Heap = new Vector4f();
    private static final Matrix4 matrixHeap = new Matrix4();
    private static final Vertex velocityTimeHeap = new Vertex();

    public RenderParticle(final RenderParticleEmitter emitter) {
        this.emitter = emitter;
        emitterView = null;

        internalInstance = emitter.internalResource.addInstance();
        velocity = new Vertex();
        gravity = 0;
    }

    public void reset(final RenderParticleEmitterView emitterView) {
        final RenderModel instance = emitterView.instance;
        final RenderNode renderNode = instance.getRenderNode(emitter.modelObject);
        final Vertex scale = renderNode.getWorldScale();

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
        QuaternionRotation.mul(rotationYHeap, rotationZHeap, rotationYHeap);
        MathUtils.fromQuat(rotationYHeap, matrixHeap);
        vector4Heap.set(0, 0, 1, 1);
        Matrix4.transform(matrixHeap, vector4Heap, vector4Heap);

        // World rotation
        MathUtils.fromQuat(renderNode.getWorldRotation(), matrixHeap);
        Matrix4.transform(matrixHeap, vector4Heap, vector4Heap);

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
