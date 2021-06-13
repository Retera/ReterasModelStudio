package com.hiveworkshop.rms.editor.model.util.ModelSaving;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.parsers.mdlx.MdlxSequence;

public class AnimToMdlx {

	public static MdlxSequence toMdlx(Animation animation) {
		final MdlxSequence sequence = new MdlxSequence();

		sequence.name = animation.getName();
		sequence.interval[0] = animation.getStart();
		sequence.interval[1] = animation.getEnd();
		sequence.extent = animation.getExtents().toMdlx();
		sequence.moveSpeed = animation.getMoveSpeed();

		if (animation.isNonLooping()) {
			sequence.flags = 1;
		}

		sequence.rarity = animation.getRarity();

		return sequence;
	}
}
