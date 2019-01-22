package com.hiveworkshop.wc3.gui.modelviewer;

import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;

public interface AnimatedRenderEnvironment {
	int getAnimationTime();

	BasicTimeBoundProvider getCurrentAnimation(); // nullable

	int getGlobalSeqTime(int length); // for glob seq
}
