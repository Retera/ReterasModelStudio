package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.ScaleSequencesLengthsAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.uiFactories.FontHelper;
import com.hiveworkshop.rms.util.uiFactories.Label;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Locale;

public class ChangeSingleAnimLengthPanel extends JPanel {
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	private final ModelHandler modelHandler;
	private final EditableModel model;
	private final Sequence sequence;
	private final int orgLength;
	private Integer newLength;
	private final JLabel speedLabel;

	public ChangeSingleAnimLengthPanel(ModelHandler modelHandler, Sequence sequence) {
		super(new MigLayout("fill"));
		this.modelHandler = modelHandler;
		this.model = modelHandler.getModel();
		this.sequence = sequence;
		orgLength = sequence.getLength();
		newLength = sequence.getLength();

		add(FontHelper.set(Label.create("Old Length: "), Font.BOLD, null), "");
		add(Label.create("" + orgLength), "wrap");
		speedLabel = Label.create(getText());
		speedLabel.setHorizontalAlignment(JLabel.CENTER);
		add(FontHelper.set(Label.create("New Length:"), Font.BOLD, null), "");
		IntEditorJSpinner customSpinner = new IntEditorJSpinner(orgLength, 1, Integer.MAX_VALUE, this::setNewLength);
		add(customSpinner, "wrap");
		add(speedLabel, "growx, spanx, center");
	}

	private void setNewLength(int newLength) {
		this.newLength = newLength;
		speedLabel.setText(getText());
	}

	private String getText() {
		float diff = (orgLength / (float) newLength) * 100f;

		String shiftType;
		if (orgLength == newLength) {
			shiftType = "(Same)";
		} else if (newLength < orgLength) {
			shiftType = "(Faster)";
		} else {
			shiftType = "(Slower)";
		}
		return String.format(Locale.US, "%3.1f", diff) +  "% Speed " + shiftType;
	}


	public UndoAction getScaleAction() {
		if (newLength != orgLength) {
			return new ScaleSequencesLengthsAction(model, Collections.singletonMap(sequence, newLength), changeListener);
		} else {
			return null;
		}
	}

	public void doScaleSequence() {
		UndoAction scaleAction = getScaleAction();
		if (scaleAction != null) {
			modelHandler.getUndoManager().pushAction(scaleAction.redo());
		}
	}

	public static void showPopup(ModelHandler modelHandler, Sequence sequence, Component parent) {
		ChangeSingleAnimLengthPanel panel = new ChangeSingleAnimLengthPanel(modelHandler, sequence);
		String title = "Scale " + (sequence instanceof GlobalSeq ?  "GlobalSequence" : ("\"" + sequence.getName() + "\""));
		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option == JOptionPane.OK_OPTION) {
			panel.doScaleSequence();
		}
	}
}
