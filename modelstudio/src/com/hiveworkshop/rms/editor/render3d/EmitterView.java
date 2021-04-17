package com.hiveworkshop.rms.editor.render3d;

public abstract class EmitterView {
    public float currentEmission;

    public RenderModel instance;

    public abstract void addToScene(InternalInstance internalInstance);
}
