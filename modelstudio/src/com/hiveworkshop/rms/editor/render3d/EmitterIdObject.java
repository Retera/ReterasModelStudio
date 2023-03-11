package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Vec3;


public abstract class EmitterIdObject extends IdObject {
    double emissionRate = 0;
    double lifeSpan = 0;
    float initVelocity = 0;
    float alpha = 0;
    Vec3 color = new Vec3();
    double gravity = 0;


    public EmitterIdObject() {
    }

    public EmitterIdObject(EmitterIdObject emitter) {
        super(emitter);
    }

    public abstract int getBlendSrc();

    public abstract int getBlendDst();

    public abstract int getRows();

    public abstract int getCols();

    public abstract boolean isRibbonEmitter();
}
