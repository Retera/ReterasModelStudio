package com.hiveworkshop.rms.ui.application.scripts;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.EditAnimationLengthsAction;
import com.hiveworkshop.rms.ui.util.SliderBarHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeAnimationLengthPanel extends JPanel implements ActionListener {
	List<SliderBarHandler> bars = new ArrayList<>();
	JButton okay, cancel;
	ModelView mdlDisp;
	ChangeAnimationLengthFrame parentFrame;
	private final UndoActionListener undoActionListener;
	private final Runnable onFinish;

	public ChangeAnimationLengthPanel(final ModelView mdlDisp, final ChangeAnimationLengthFrame frame,
									  final UndoActionListener undoActionListener, final Runnable onFinish) {
		this.mdlDisp = mdlDisp;
		parentFrame = frame;
		this.undoActionListener = undoActionListener;
		this.onFinish = onFinish;
		final GridLayout layout = new GridLayout(
				(mdlDisp.getModel().getAnimsSize() + mdlDisp.getModel().getGlobalSeqs().size()) * 2 + 2, 2);
		setLayout(layout);

		for (final Animation anim : mdlDisp.getModel().getAnims()) {
			final JLabel label = new JLabel(anim.getName() + " (" + anim.length() / 1000.00 + " s)");
			final int maxLength = Math.max(100000, anim.length() * 4);
			final JSlider bar = new JSlider(0, maxLength);
			bar.setValue(anim.length());
			final JSpinner spinner = new JSpinner(
					new SpinnerNumberModel(anim.length() / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			final SliderBarHandler handler = new SliderBarHandler(bar, spinner);
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
		for (final Integer globalSeq : mdlDisp.getModel().getGlobalSeqs()) {
			final JLabel label = new JLabel("Global Sequence " + ++i + " (" + globalSeq / 1000.00 + " s)");
			final int maxLength = Math.max(100000, globalSeq * 4);
			final JSlider bar = new JSlider(0, maxLength);
			bar.setValue(globalSeq);
			final JSpinner spinner = new JSpinner(
					new SpinnerNumberModel(globalSeq / 1000.00, 0.0, maxLength / 1000.00, 0.001));

			final SliderBarHandler handler = new SliderBarHandler(bar, spinner);
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
		okay.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		add(cancel);
		add(okay);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == cancel) {
			parentFrame.setVisible(false);
		}
		if (e.getSource() == okay) {
			final EditableModel mdl = mdlDisp.getModel();
			int myAnimationsIndex = 0;
			final Map<Animation, Integer> animationToNewLength = new HashMap<>();
			final Map<Animation, Integer> animationToOldLength = new HashMap<>();
			final int[] oldGlobalSeqLengths = new int[mdl.getGlobalSeqs().size()];
			final int[] newGlobalSeqLengths = new int[mdl.getGlobalSeqs().size()];
			for (final Animation myAnimation : mdl.getAnims()) {
				animationToNewLength.put(myAnimation, bars.get(myAnimationsIndex).bar.getValue());
				animationToOldLength.put(myAnimation, myAnimation.length());
				myAnimationsIndex++;
			}
			for (final Integer myAnimation : mdl.getGlobalSeqs()) {
				final int newLength = bars.get(myAnimationsIndex).bar.getValue();
				newGlobalSeqLengths[myAnimationsIndex - mdl.getAnimsSize()] = newLength;
				oldGlobalSeqLengths[myAnimationsIndex - mdl.getAnimsSize()] = myAnimation;
				myAnimationsIndex++;
			}
			final EditAnimationLengthsAction editAnimationLengths = new EditAnimationLengthsAction(mdl,
					animationToNewLength, animationToOldLength, newGlobalSeqLengths, oldGlobalSeqLengths);
			editAnimationLengths.redo();
			undoActionListener.pushAction(editAnimationLengths);
			parentFrame.setVisible(false);
			onFinish.run();
		}
	}
}
