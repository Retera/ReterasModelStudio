package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.animation.globalsequence.SetGlobalSequenceLengthAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentGlobalSequencePanel extends ComponentPanel<GlobalSeq> {
	private final JLabel indexLabel;
	private final JSpinner lengthSpinner;
	private GlobalSeq globalSeq;
	private Integer length;

	public ComponentGlobalSequencePanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout());
		lengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		lengthSpinner.addChangeListener(e -> setLength());
		add(new JLabel("GlobalSequence "), "cell 0 0");

		indexLabel = new JLabel();
		add(indexLabel, "cell 1 0");

		add(new JLabel("Duration: "), "cell 0 1");
		add(lengthSpinner, "cell 1 1");
	}

	private void setLength() {
		SetGlobalSequenceLengthAction action = new SetGlobalSequenceLengthAction(globalSeq, (Integer) lengthSpinner.getValue(), changeListener);
		modelHandler.getUndoManager().pushAction(action.redo());
	}

	@Override
	public void setSelectedItem(GlobalSeq itemToSelect) {
		this.globalSeq = itemToSelect;
		length = itemToSelect.getLength();
		indexLabel.setText(Integer.toString(modelHandler.getModel().getGlobalSeqId(itemToSelect)));
		lengthSpinner.setValue(length);
	}

	@Override
	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}
}
