package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;

public abstract class EmitterIdObject extends IdObject {
    double emissionRate = 0;
    double lifeSpan = 0;
    float initVelocity = 0;

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
