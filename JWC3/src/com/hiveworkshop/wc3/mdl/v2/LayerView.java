package com.hiveworkshop.wc3.mdl.v2;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.v2.timelines.Animatable;

public interface LayerView {
	Layer.FilterMode getFilterMode();

	Animatable<Bitmap> getTexture();

	Animatable<Double> getAlpha();

	boolean isUnshaded();

	boolean isUnfogged();

	boolean isTwoSided();

	int getCoordId();

	boolean isSphereEnvironmentMap();

	boolean isNoDepthTest();

	boolean isNoDepthSet();
}
