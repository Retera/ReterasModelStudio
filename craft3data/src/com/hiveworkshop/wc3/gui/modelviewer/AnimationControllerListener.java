package com.hiveworkshop.wc3.gui.modelviewer;

import com.hiveworkshop.wc3.mdl.Animation;

public interface AnimationControllerListener {
	void setAnimation(Animation animation);

	void playAnimation();

	void setLoop(LoopType loopType);

	public enum LoopType {
		DEFAULT_LOOP, ALWAYS_LOOP, NEVER_LOOP;
	}
}
