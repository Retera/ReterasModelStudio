package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

/**
 * Something to keep track of which stuff has visibility,
 *
 * Eric Theller
 * 6/28/2012
 */
public interface VisibilitySource {
    AnimFlag<Float> getVisibilityFlag();

    void setVisibilityFlag(AnimFlag<Float> what);

    //     public String getVisTagname();
    String visFlagName();
}
