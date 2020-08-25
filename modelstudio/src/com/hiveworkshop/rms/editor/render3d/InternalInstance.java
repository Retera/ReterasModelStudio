package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.QuaternionRotation;
import com.hiveworkshop.rms.editor.model.Vertex;

public interface InternalInstance {
    void setTransformation(Vertex worldLocation, QuaternionRotation rotation, Vertex worldScale);
    void setSequence(int index);
    void show();
    void setPaused(boolean paused);
    void move(Vertex deltaPosition);

    void hide();
}
