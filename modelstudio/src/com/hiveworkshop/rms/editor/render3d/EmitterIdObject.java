package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;

public abstract class EmitterIdObject extends IdObject {
    public abstract int getBlendSrc();
    public abstract int getBlendDst();
    public abstract int getRows();
    public abstract int getCols();

    public abstract boolean isRibbonEmitter();
}
