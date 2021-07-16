package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.editor.actions.animation.EditAnimationLengthsAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.SliderBarHandler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeAnimationLengthPanel extends JPanel {
	List<SliderBarHandler> bars = new ArrayList<>();
	JButton okay, cancel;
	ModelHandler modelHandler;
	ChangeAnimationLengthFrame parentFrame;
	private final UndoManager undoManager;
	private final Runnable onFinish;

	public ChangeAnimationLengthPanel(ModelHandler modelHandler, ChangeAnimationLengthFrame frame) {
		this.modelHandler = modelHandler;
		parentFrame = frame;
		this.undoManager = modelHandler.getUndoManager();
		this.onFinish = () -> ProgramGlobals.getMainPanel().getMainLayoutCreator().getTimeSliderPanel().revalidateKeyframeDisplay();
		GridLayout layout = new GridLayout(
				(modelHandler.getModel().getAnimsSize() + modelHandler.getModel().getGlobalSeqs().size()) * 2 + 2, 2);
		setLayout(layout);

		for (Animation anim : modelHandler.getModel().getAnims()) {
			JLabel label = new JLabel(anim.getName() + " (" + anim.length() / 1000.00 + " s)");
			int maxLength = Math.max(100000, anim.length() * 4);
			JSlider bar = new JSlider(0, maxLength);
			bar.setValue(anim.length());
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(anim.length() / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			SliderBarHandler handler = new SliderBarHandler(bar, spinner);
			bar.addChangeListener(handler);
			spinner.addChangeListener(handler);
			// defBar.addChangeListener(this);
			// defSpinner.addChangeListener(this);
			bars.add(handler);

			add(label);
			add(new JSeparator());
			add(bar);
			add(spinner);
		}
		int i = 0;
		for (Integer globalSeq : modelHandler.getModel().getGlobalSeqs()) {
			JLabel label = new JLabel("Global Sequence " + ++i + " (" + globalSeq / 1000.00 + " s)");
			int maxLength = Math.max(100000, globalSeq * 4);
			JSlider bar = new JSlider(0, maxLength);
			bar.setValue(globalSeq);
			JSpinner spinner = new JSpinner(
					new SpinnerNumberModel(globalSeq / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			SliderBarHandler handler = new SliderBarHandler(bar, spinner);
			bar.addChangeListener(handler);
			spinner.addChangeListener(handler);
			// defBar.addChangeListener(this);
			// defSpinner.addChangeListener(this);
			bars.add(handler);

			add(label);
			add(new JSeparator());
			add(bar);
			add(spinner);
		}

		okay = new JButton("OK");
		okay.addActionListener(e -> applyNewAnimationLength());
		cancel = new JButton("Cancel");
		cancel.addActionListener(e -> parentFrame.setVisible(false));
		add(cancel);
		add(okay);
	}

	private void applyNewAnimationLength() {
		EditableModel mdl = modelHandler.getModel();
		int myAnimationsIndex = 0;
		Map<Animation, Integer> animationToNewLength = new HashMap<>();
		Map<Animation, Integer> animationToOldLength = new HashMap<>();
		int[] oldGlobalSeqLengths = new int[mdl.getGlobalSeqs().size()];
		int[] newGlobalSeqLengths = new int[mdl.getGlobalSeqs().size()];
		for (Animation myAnimation : mdl.getAnims()) {
			animationToNewLength.put(myAnimation, bars.get(myAnimationsIndex).bar.getValue());
			animationToOldLength.put(myAnimation, myAnimation.length());
			myAnimationsIndex++;
		}
		for (Integer myAnimation : mdl.getGlobalSeqs()) {
			int newLength = bars.get(myAnimationsIndex).bar.getValue();
			newGlobalSeqLengths[myAnimationsIndex - mdl.getAnimsSize()] = newLength;
			oldGlobalSeqLengths[myAnimationsIndex - mdl.getAnimsSize()] = myAnimation;
			myAnimationsIndex++;
		}
		undoManager.pushAction(new EditAnimationLengthsAction(mdl, animationToNewLength, animationToOldLength, newGlobalSeqLengths, oldGlobalSeqLengths).redo());
		parentFrame.setVisible(false);
		onFinish.run();
	}
}
