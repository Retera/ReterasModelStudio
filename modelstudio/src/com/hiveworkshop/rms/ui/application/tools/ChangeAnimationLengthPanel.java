package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.animation.EditAnimationLengthsAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.SliderBarHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ChangeAnimationLengthPanel extends JPanel {
	private final Map<TimeBoundProvider, SliderBarHandler> animationBarMap = new HashMap<>();
	private final ModelHandler modelHandler;
	private final JFrame parentFrame;
	private final UndoManager undoManager;
	private final Runnable onFinish;

	public ChangeAnimationLengthPanel(ModelHandler modelHandler, JFrame frame) {
		super(new MigLayout("fill", "", ""));
		this.modelHandler = modelHandler;
		parentFrame = frame;
		undoManager = modelHandler.getUndoManager();
		onFinish = () -> ModelStructureChangeListener.changeListener.keyframesUpdated();
		EditableModel model = modelHandler.getModel();
		JPanel animationsPanel = new JPanel(new MigLayout("fill"));

		for (Animation anim : model.getAnims()) {
			SliderBarHandler handler = new SliderBarHandler(anim.length());
			animationBarMap.put(anim, handler);

			animationsPanel.add(new JLabel(anim.getName() + " (" + anim.length() / 1000.00 + " s)"), "wrap");
			animationsPanel.add(handler.getBar(), "growx");
			animationsPanel.add(handler.getSpinner(), "wrap");
		}
		for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
			SliderBarHandler handler = new SliderBarHandler(globalSeq.getLength());
			animationBarMap.put(globalSeq, handler);

			animationsPanel.add(new JLabel("Global Sequence " + (model.getGlobalSeqId(globalSeq) + 1) + " (" + globalSeq.getLength() / 1000.00 + " s)"), "wrap");
			animationsPanel.add(handler.getBar(), "growx");
			animationsPanel.add(handler.getSpinner(), "wrap");
		}

		JScrollPane scrollPane = new JScrollPane(animationsPanel);
		add(scrollPane, "spanx 2, growx, growy, wrap");

		JButton okay = new JButton("OK");
		okay.addActionListener(e -> applyNewAnimationLength());
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> parentFrame.setVisible(false));
		add(cancel);
		add(okay);
	}

	private void applyNewAnimationLength() {
		EditableModel mdl = modelHandler.getModel();
		Map<Animation, Integer> animationToNewLength = new HashMap<>();
		for (Animation myAnimation : mdl.getAnims()) {
			animationToNewLength.put(myAnimation, animationBarMap.get(myAnimation).getValue());
		}

		Map<GlobalSeq, Integer> newGlobalSeqLengths = new HashMap<>();
		for (GlobalSeq myAnimation : mdl.getGlobalSeqs()) {
			newGlobalSeqLengths.put(myAnimation, animationBarMap.get(myAnimation).getValue());
		}
		undoManager.pushAction(new EditAnimationLengthsAction(mdl, animationToNewLength, newGlobalSeqLengths).redo());
		parentFrame.setVisible(false);
		onFinish.run();
	}
}
