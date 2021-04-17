package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.Map;

public final class EditAnimationLengthsAction implements UndoAction {
	private final EditableModel mdl;
	private final Map<Animation, Integer> animationToNewLength;
	private final Map<Animation, Integer> animationToOldLength;
	private final int[] newGlobalSeqLengths;
	private final int[] oldGlobalSeqLengths;

	public EditAnimationLengthsAction(final EditableModel mdl, final Map<Animation, Integer> animationToNewLength,
			final Map<Animation, Integer> animationToOldLength, final int[] newGlobalSeqLengths,
			final int[] oldGlobalSeqLengths) {
		this.mdl = mdl;
		this.animationToNewLength = animationToNewLength;
		this.animationToOldLength = animationToOldLength;
		this.newGlobalSeqLengths = newGlobalSeqLengths;
		this.oldGlobalSeqLengths = oldGlobalSeqLengths;
	}

	@Override
	public void undo() {
		doEdit(animationToOldLength, oldGlobalSeqLengths);
	}

	@Override
	public void redo() {
		doEdit(animationToNewLength, newGlobalSeqLengths);
	}

	private void doEdit(final Map<Animation, Integer> lengthUpdates, final int[] newGlobalSeqLengths) {

		int myAnimationsIndex = 0;
		for (final Animation myAnimation : mdl.getAnims()) {

			final int newLength = lengthUpdates.get(myAnimation);
			final int lengthIncrease = (newLength) - myAnimation.length();

			if (lengthIncrease > 0) {
				// first move all the animations after it, so that when we
				// make it 2x as long we don't get interlocking keyframes

				// (getAnimsSize is a badly named "number of animations"
				// function)
				for (int index = mdl.getAnimsSize() - 1; index > myAnimationsIndex; index--) {
					final Animation anim = mdl.getAnim(index);
					final int startOfAnim = anim.getStart();
					// I didn't know eclipse is smart enough to write
					// functions like this one for me, so I haven't pushed
					// "generate getters and setters" to auto-write this
					// function, but it could exist
					final int endOfAnim = anim.getEnd();
					// same
					anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
				}
			}

			// now actually scale animation
			myAnimation.setInterval(myAnimation.getStart(), myAnimation.getStart() + newLength, mdl);

			if (lengthIncrease < 0) {
				// afterwards move all the animations after it, so that they're
				// not all spread apart

				// (getAnimsSize is a badly named "number of animations"
				// function)
				for (int index = myAnimationsIndex + 1; index < mdl.getAnimsSize(); index++) {
					final Animation anim = mdl.getAnim(index);
					final int startOfAnim = anim.getStart();
					// I didn't know eclipse is smart enough to write
					// functions like this one for me, so I haven't pushed
					// "generate getters and setters" to auto-write this
					// function, but it could exist
					final int endOfAnim = anim.getEnd();
					// same
					anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
				}
			}
			myAnimationsIndex++;
		}
		myAnimationsIndex = 0;
		for (final Integer myAnimation : mdl.getGlobalSeqs()) {

			final int newLength = newGlobalSeqLengths[myAnimationsIndex];

			for (final AnimFlag<?> flag : mdl.getAllAnimFlags()) {
				if (flag.hasGlobalSeq() && flag.getGlobalSeq().equals(myAnimation)) {
					flag.timeScale(0, myAnimation, 0, newLength);
					flag.setGlobSeq(newLength);
				}
			}
			myAnimationsIndex++;
		}
	}

	@Override
	public String actionName() {
		return "edit animation length(s)";
	}

}
