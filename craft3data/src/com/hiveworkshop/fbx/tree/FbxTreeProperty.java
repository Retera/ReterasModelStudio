package com.hiveworkshop.fbx.tree;

public class FbxTreeProperty<T> {
    public char type;
    public T value;

    public FbxTreeProperty(final char type, final T value) {
        this.type = type;
        this.value = value;
    }
}
