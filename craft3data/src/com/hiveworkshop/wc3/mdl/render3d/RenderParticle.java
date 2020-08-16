package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.MathUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RenderParticle {
    private static final Vertex UP = new Vertex(0, 0, 1);

    private final RenderParticleEmitter emitter;
    private final Vector3f velocity;
    private float gravity;
    private RenderParticleEmitterView emitterView;
    private final InternalInstance internalInstance;
    private RenderNode node;
    private double health;
    private static Quaternion rotationZHeap = new Quaternion();
    private static Quaternion rotationYHeap = new Quaternion();
    private static Vector4f vector4Heap = new Vector4f();
    private static Matrix4f matrixHeap = new Matrix4f();
    private static Vector3f velocityTimeHeap = new Vector3f();

    public RenderParticle(RenderParticleEmitter emitter) {
        this.emitter = emitter;
        this.emitterView = null;

        this.internalInstance = emitter.internalResource.addInstance();
        this.velocity = new Vector3f();
        this.gravity = 0;
    }

    public void reset(RenderParticleEmitterView emitterView) {
        RenderModel instance = emitterView.instance;
        RenderNode renderNode = instance.getRenderNode(emitter.modelObject);
        Vector3f scale = renderNode.getWorldScale();

        double latitude = emitterView.getLatitude();
        double lifeSpan = emitterView.getLifeSpan();
        double gravity = emitterView.getGravity();
        double speed = emitterView.getSpeed();

        this.emitterView = emitterView;
        this.node = renderNode;
        this.health = lifeSpan;
        this.gravity = (float) (gravity * scale.z);

        // Local rotation
        rotationZHeap.setIdentity();
        vector4Heap.set(0, 0, 1, MathUtils.randomInRange(-Math.PI, Math.PI));
        rotationZHeap.setFromAxisAngle(vector4Heap);
        vector4Heap.set(0, 1, 0, MathUtils.randomInRange(-latitude, latitude));
        rotationYHeap.setFromAxisAngle(vector4Heap);
        Quaternion.mul(rotationYHeap, rotationZHeap, rotationYHeap);
        MathUtils.fromQuat(rotationYHeap, matrixHeap);
        vector4Heap.set(0, 0, 1, 1);
        Matrix4f.transform(matrixHeap, vector4Heap, vector4Heap);

        // World rotation
        MathUtils.fromQuat(renderNode.getWorldRotation(), matrixHeap);
        Matrix4f.transform(matrixHeap, vector4Heap, vector4Heap);

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
        float frameTimeS = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
        internalInstance.setPaused(false);

        this.health -= frameTimeS;

        velocity.z -= gravity * frameTimeS;

        velocityTimeHeap.x = velocity.x * frameTimeS;
        velocityTimeHeap.y = velocity.y * frameTimeS;
        velocityTimeHeap.z = velocity.z * frameTimeS;

        internalInstance.move(velocityTimeHeap);

        if (this.health <= 0) {
            this.internalInstance.hide();
        }
    }
}
