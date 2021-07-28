package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
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
			int anim1Length = animation.getEnd() - animation.getStart();
			int anim2Length = animation2.getEnd() - animation2.getStart();
			int totalLength = anim1Length + anim2Length;

			int animTrackEnd = model.animTrackEnd();
			int start = animTrackEnd + 1000;
			animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(), model.getEvents());
			animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(), model.getEvents());

			Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
			model.add(newAnimation);
			newAnimation.setNonLooping(true);
			newAnimation.setExtents(animation.getExtents().deepCopy());
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
					JOptionPane.PLAIN_MESSAGE);
		}
	}
}
