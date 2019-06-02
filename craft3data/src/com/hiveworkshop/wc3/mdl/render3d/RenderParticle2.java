package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.MathUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RenderParticle2 {
    private static final Vector4f vector4Heap = new Vector4f();
    private static final Vector4f colorHeap = new Vector4f();
    private static Quaternion rotationZHeap = new Quaternion();
    private static Quaternion rotationYHeap = new Quaternion();
    private static Quaternion rotationXHeap = new Quaternion();
    private static Matrix4f matrixHeap = new Matrix4f();
    private static Vector3f locationHeap = new Vector3f();
    private RenderParticleEmitter2 emitter;
    private RenderParticleEmitter2View emitterView;
    private float health;
    private boolean head;
    private Vector3f location;
    private Vector3f velocity;
    private float gravity;
    private Vector3f nodeScale;

    private float[] vertices;
    private int lta, lba, rta, rba, rgb;
    private RenderNode node;


    public RenderParticle2(RenderParticleEmitter2 emitter) {
        this.emitter = emitter;
        this.emitterView = null;
        this.health = 0;
        this.head = true;
        this.location = new Vector3f();
        this.velocity = new Vector3f();
        this.gravity = 0;
        this.nodeScale = new Vector3f();

        vertices = new float[12];
        this.lta = 0;
        this.lba = 0;
        this.rta = 0;
        this.rba = 0;
        this.rgb = 0;
    }

    public void reset(RenderParticleEmitter2View emitterView, boolean isHead) {
        double width = emitterView.getWidth();
        double length = emitterView.getLength();
        double latitude = emitterView.getLatitude();
        double variation = emitterView.getVariation();
        double speed = emitterView.getSpeed();
        double gravity = emitterView.getGravity();

        ParticleEmitter2 modelObject = emitter.modelObject;
        RenderNode node = emitterView.instance.getRenderNode(modelObject);
        Vertex pivotPoint = modelObject.getPivotPoint();
        Vector3f scale = node.getWorldScale();
        width *= 0.5;
        length *= 0.5;
        latitude = Math.toRadians(latitude);

        this.emitterView = emitterView;
        this.node = node;
        this.health = (float) modelObject.getLifeSpan();
        this.head = isHead;
        this.gravity = (float) (gravity * scale.z);

        this.nodeScale.set(scale);

        // Local location
        location.x = (float) (pivotPoint.x + MathUtils.randomInRange(-width, width));
        location.y = (float) (pivotPoint.y + MathUtils.randomInRange(-length, length));
        location.z = (float) (pivotPoint.z);

        // World location
        if (!modelObject.isModelSpace()) {
            vector4Heap.set(location.x, location.y, location.z, 1);
            Matrix4f.transform(node.getWorldMatrix(), vector4Heap, vector4Heap);
            location.set(vector4Heap);
        }

        // Location rotation
        rotationZHeap.setIdentity();
        vector4Heap.set(0, 0, 1, (float) (Math.PI / 2));
        rotationZHeap.setFromAxisAngle(vector4Heap);
        vector4Heap.set(0, 1, 0, MathUtils.randomInRange(-latitude, latitude));
        rotationYHeap.setFromAxisAngle(vector4Heap);
        Quaternion.mul(rotationYHeap, rotationZHeap, rotationYHeap);

        // World rotation
        if (!modelObject.isLineEmitter()) {
            vector4Heap.set(1, 0, 0, MathUtils.randomInRange(-latitude, latitude));
            rotationXHeap.setFromAxisAngle(vector4Heap);
            Quaternion.mul(rotationXHeap, rotationYHeap, rotationYHeap);
        }

        // Apply the rotation
        MathUtils.fromQuat(rotationYHeap, matrixHeap);
        vector4Heap.set(0, 0, 1, 1);
        Matrix4f.transform(matrixHeap, vector4Heap, vector4Heap);
        velocity.set(vector4Heap);

        // Apply speed
        velocity.scale((float) speed + MathUtils.randomInRange(-variation, variation));

        // Apply the parent's scale
        velocity.x *= scale.x;
        velocity.y *= scale.y;
        velocity.z *= scale.z;
    }

    public void update() {
        ParticleEmitter2 modelObject = emitter.modelObject;
        float dt = AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001f;
        Vector3f worldLocation = locationHeap;

        this.health -= dt;

        velocity.z -= this.gravity * dt;

        location.x = location.x + velocity.x * dt;
        location.y = location.y + velocity.y * dt;
        location.z = location.z + velocity.z * dt;

        worldLocation.set(location);

        float lifeFactor = (float) ((modelObject.getLifeSpan() - this.health) / modelObject.getLifeSpan());
        float timeMiddle = (float) modelObject.getTime();
        float factor;
        int firstColor;
        Vertex interval;

        if (lifeFactor < timeMiddle) {
            factor = lifeFactor / timeMiddle;

            firstColor = 0;

            if (head) {
                interval = modelObject.getLifeSpanUVAnim();
            } else {
                interval = modelObject.getTailUVAnim();
            }
        } else {
            factor = (lifeFactor / timeMiddle) / (1 - timeMiddle);

            firstColor = 1;

            if (head) {
                interval = modelObject.getDecayUVAnim();
            } else {
                interval = modelObject.getTailDecayUVAnim();
            }
        }

        factor = Math.min(factor, 1);

        float start = (float) interval.x;
        float end = (float) interval.y;
        float repeat = (float) interval.z;
        Vertex scaling = modelObject.getParticleScaling();
        Vertex[] colors = modelObject.getSegmentColors();
        float scale = (float) MathUtils.lerp((float) scaling.getCoord((byte) firstColor), (float) scaling.getCoord((byte) (firstColor + 1)), factor);
        float left, top, right, bottom;

        // If this is a team colored emitter, get the team color tile from the atlas
        // Otherwise do normal texture atlas handling.
        if(modelObject.isTeamColored()) {
            // except that Matrix Eater has no such atlas and we are simply copying from Ghostwolf
            left = 0;
            top = 0;
            right = left + 1;
            bottom = top + 1;
        } else {
            int columns = modelObject.getColumns();
            float index = 0;
            float spriteCount = end - start;
            if(spriteCount > 0) {
                // Repeating speeds up the sprite animation, which makes it effectively run N times in its interval.
                // E.g. if repeat is 4, the sprite animation will be seen 4 times, and thus also run 4 times as fast
                index = (float)(start + Math.floor(spriteCount * repeat * factor) % spriteCount);
            }

            left = index % columns;
            top = (int)(index / columns);
            right = left + 1;
            bottom = top + 1;
        }

//        MathUtils.lerp()
    }
}
