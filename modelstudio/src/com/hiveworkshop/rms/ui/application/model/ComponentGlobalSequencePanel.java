package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.SetSequenceLengthAction;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentGlobalSequencePanel extends ComponentPanel<GlobalSeq> {
	private final JLabel indexLabel;
	private final IntEditorJSpinner lengthSpinner;
	private GlobalSeq globalSeq;

	public ComponentGlobalSequencePanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);

		setLayout(new MigLayout());
		lengthSpinner = new IntEditorJSpinner(0, 0, this::setLength);
		add(new JLabel("GlobalSequence "), "cell 0 0");

		indexLabel = new JLabel();
		add(indexLabel, "cell 1 0");

		add(getDeleteButton(e -> removeSequence()), "cell 2 0");

		add(new JLabel("Duration: "), "cell 0 1");
		add(lengthSpinner, "cell 1 1");
	}

	private void setLength(int newLength) {
		if (newLength != globalSeq.getLength()) {
			UndoAction action = new SetSequenceLengthAction(globalSeq, newLength, changeListener);
			undoManager.pushAction(action.redo());
		}
	}

	@Override
	public ComponentPanel<GlobalSeq> setSelectedItem(GlobalSeq itemToSelect) {
		this.globalSeq = itemToSelect;
		indexLabel.setText(Integer.toString(modelHandler.getModel().getGlobalSeqId(itemToSelect)));
		lengthSpinner.reloadNewValue(itemToSelect.getLength());
		return this;
	}

	private void removeSequence() {
		UndoAction action = new RemoveSequenceAction(modelHandler.getModel(), globalSeq, changeListener);
		undoManager.pushAction(action.redo());
	}
}
