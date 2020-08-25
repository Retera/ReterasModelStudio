package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.util.QuaternionRotation;
import com.hiveworkshop.rms.util.Vertex3;

public interface InternalInstance {
    void setTransformation(Vertex3 worldLocation, QuaternionRotation rotation, Vertex3 worldScale);
    void setSequence(int index);
    void show();
    void setPaused(boolean paused);
    void move(Vertex3 deltaPosition);

    void hide();
}
