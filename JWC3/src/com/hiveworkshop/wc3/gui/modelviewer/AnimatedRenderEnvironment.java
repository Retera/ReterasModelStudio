package com.hiveworkshop.wc3.gui.modelviewer;

import com.hiveworkshop.wc3.mdl.Animation;

public interface AnimatedRenderEnvironment {
	int getAnimationTime();

	Animation getCurrentAnimation(); // nullable

	int getGlobalSeqTime(int length); // for glob seq
}
