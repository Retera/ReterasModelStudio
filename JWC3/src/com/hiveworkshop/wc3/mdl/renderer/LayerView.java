package com.hiveworkshop.wc3.mdl.renderer;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Layer;

public interface LayerView {
	Layer.FilterMode getFilterMode();

	Bitmap getTexture();

	double getStaticAlpha();

	boolean isUnshaded();

	boolean isUnfogged();

	boolean isTwoSided();

	int getCoordId();

	boolean isSphereEnvironmentMap();

	boolean isNoDepthTest();

	boolean isNoDepthSet();
}
