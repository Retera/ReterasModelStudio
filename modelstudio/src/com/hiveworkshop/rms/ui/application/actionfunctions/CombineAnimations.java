package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.util.List;

public class CombineAnimations extends ActionFunction {
	public CombineAnimations(){
		super(TextKey.BACK2BACK_ANIMATION, () -> combineAnimations());
	}

	public static void combineAnimations() {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		List<Animation> anims = model.getAnims();
		Animation[] array = anims.toArray(new Animation[0]);
		Object choice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the first animation",
				"Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation = (Animation) choice;

		Object choice2 = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the second animation",
				"Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation2 = (Animation) choice2;

		String nameChoice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"What should the combined animation be called?");
		if (nameChoice != null) {
			int anim1Length = animation.getLength();
			int anim2Length = animation2.getLength();
			int totalLength = anim1Length + anim2Length;

			int animTrackEnd = ModelUtils.animTrackEnd(model);
			int newStart = animTrackEnd + 1000;
			Animation newAnimation = new Animation(nameChoice, newStart, newStart + totalLength);
			copyFromInterval(animation, newAnimation, 0, ModelUtils.getAllAnimFlags(model), model.getEvents());
			copyFromInterval(animation2, newAnimation, anim1Length, ModelUtils.getAllAnimFlags(model), model.getEvents());

			model.add(newAnimation);
			newAnimation.setNonLooping(true);
			newAnimation.setExtents(animation.getExtents().deepCopy());
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	private static void copyFromInterval(Animation source, Animation animation, int offset, List<AnimFlag<?>> flags, List<EventObject> eventObjs) {
		for (AnimFlag<?> af : flags) {
			if (!af.hasGlobalSeq()) {
//				af.copyFrom(af, source, animation, offset, animation.getLength() + offset);
				AnimFlagUtils.copyFrom(af, af, source, animation, offset);
			}
		}
		for (EventObject e : eventObjs) {
			if (!e.hasGlobalSeq()) {
				e.copyFrom(e.copy(), source, animation);
			}
		}
	}
}
