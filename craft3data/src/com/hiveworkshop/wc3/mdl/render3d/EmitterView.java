package com.hiveworkshop.wc3.mdl.render3d;

public abstract class EmitterView {
    public int currentEmission;

    public RenderModel instance;

    public abstract void addToScene(InternalInstance internalInstance);
}
