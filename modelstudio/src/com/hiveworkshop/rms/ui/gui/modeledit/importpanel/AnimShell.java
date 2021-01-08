package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;

class AnimShell {
	Animation anim;
	Animation importAnim;

	public AnimShell(final Animation anim) {
		this.anim = anim;
	}

	public void setImportAnim(final Animation a) {
		importAnim = a;
	}
}
