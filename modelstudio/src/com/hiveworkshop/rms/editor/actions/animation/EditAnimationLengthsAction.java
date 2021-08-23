package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import java.util.HashMap;
import java.util.Map;

public final class EditAnimationLengthsAction implements UndoAction {
	private final EditableModel mdl;
	private final Map<Animation, Integer> animationToNewLength;
	private final Map<Animation, Integer> animationToOldLength;
	private final Map<GlobalSeq, Integer> newGlobalSeqLengths;
	private final Map<GlobalSeq, Integer> oldGlobalSeqLengths;

	public EditAnimationLengthsAction(EditableModel mdl,
	                                  Map<Animation, Integer> animationToNewLength,
	                                  Map<GlobalSeq, Integer> newGlobalSeqLengths) {
		this.mdl = mdl;
		this.animationToNewLength = animationToNewLength;
		this.newGlobalSeqLengths = newGlobalSeqLengths;

		this.animationToOldLength = new HashMap<>();
		for (Animation animation : mdl.getAnims()) {
			animationToOldLength.put(animation, animation.length());
		}
		this.oldGlobalSeqLengths = new HashMap<>();
		for (GlobalSeq globalSeq : mdl.getGlobalSeqs()) {
			oldGlobalSeqLengths.put(globalSeq, globalSeq.getLength());
		}
	}

	@Override
	public UndoAction undo() {
		setAnimationLengths(animationToOldLength);
		setGlobalSeqLengths(oldGlobalSeqLengths);
		return this;
	}

	@Override
	public UndoAction redo() {
		setAnimationLengths(animationToNewLength);
		setGlobalSeqLengths(newGlobalSeqLengths);
		return this;
	}

	private void setGlobalSeqLengths(Map<GlobalSeq, Integer> globalSeqLengthMap) {
		for (GlobalSeq globalSeq : globalSeqLengthMap.keySet()) {

			for (final AnimFlag<?> flag : mdl.getAllAnimFlags()) {
				if (flag.hasGlobalSeq() && flag.getGlobalSeq().equals(globalSeq)) {
					flag.timeScale(0, globalSeq.getLength(), 0, globalSeqLengthMap.get(globalSeq));
				}
			}
			globalSeq.setLength(globalSeqLengthMap.get(globalSeq));
		}
	}

	private void setAnimationLengths(Map<Animation, Integer> animLengthMap) {
		int myAnimationsIndex = 0;
		for (final Animation myAnimation : mdl.getAnims()) {

			final int newLength = animLengthMap.get(myAnimation);
			final int lengthIncrease = (newLength) - myAnimation.length();

			if (lengthIncrease > 0) {
				// first move all the animations after it, so that when we
				// make it 2x as long we don't get interlocking keyframes

				// (getAnimsSize is a badly named "number of animations" function)
				for (int index = mdl.getAnimsSize() - 1; index > myAnimationsIndex; index--) {
					final Animation anim = mdl.getAnim(index);
					final int startOfAnim = anim.getStart();
					// I didn't know eclipse is smart enough to write functions like this one for me, so
					// I haven't pushed "generate getters and setters" to auto-write this function, but it could exist
					final int endOfAnim = anim.getEnd();
					// same
					anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
				}
			}

			// now actually scale animation
			myAnimation.setInterval(myAnimation.getStart(), myAnimation.getStart() + newLength, mdl);

			if (lengthIncrease < 0) {
				// afterwards move all the animations after it, so that they're not all spread apart

				// (getAnimsSize is a badly named "number of animations" function)
				for (int index = myAnimationsIndex + 1; index < mdl.getAnimsSize(); index++) {
					final Animation anim = mdl.getAnim(index);
					final int startOfAnim = anim.getStart();
					// I didn't know eclipse is smart enough to write functions like this one for me, so
					// I haven't pushed "generate getters and setters" to auto-write this function, but it could exist
					final int endOfAnim = anim.getEnd();
					anim.setInterval(startOfAnim + lengthIncrease, endOfAnim + lengthIncrease, mdl);
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
