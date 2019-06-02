package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.IdObject;

public abstract class EmitterIdObject extends IdObject {
    public abstract int getBlendSrc();
    public abstract int getBlendDst();
    public abstract int getRows();
    public abstract int getCols();

    public abstract boolean isRibbonEmitter();
}
