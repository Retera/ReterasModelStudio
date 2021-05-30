package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.globalsequence.SetGlobalSequenceLengthAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

//public class ComponentGlobalSequencePanel extends JPanel implements ComponentPanel<EditableModel> {
public class ComponentGlobalSequencePanel extends ComponentPanel<Integer> {
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final JLabel indexLabel;
	private final JSpinner lengthSpinner;
	private int globalSequenceId;
	private Integer value;

	public ComponentGlobalSequencePanel(ModelHandler modelHandler,
	                                    ModelStructureChangeListener modelStructureChangeListener) {
		this.modelHandler = modelHandler;
		this.modelStructureChangeListener = modelStructureChangeListener;

		setLayout(new MigLayout());
//		lengthSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		lengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		lengthSpinner.addChangeListener(e -> lengthSpinner());
//		lengthSpinner.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentHidden(ComponentEvent e) {
//				lengthSpinner();
//			}
//		});
		add(new JLabel("GlobalSequence "), "cell 0 0");

		indexLabel = new JLabel();
		add(indexLabel, "cell 1 0");

		add(new JLabel("Duration: "), "cell 0 1");
		add(lengthSpinner, "cell 1 1");
	}

	private void lengthSpinner() {
		SetGlobalSequenceLengthAction setGlobalSequenceLengthAction = new SetGlobalSequenceLengthAction(
				modelHandler.getModel(), globalSequenceId, value, (Integer) lengthSpinner.getValue(),
				modelStructureChangeListener);
		setGlobalSequenceLengthAction.redo();
		modelHandler.getUndoManager().pushAction(setGlobalSequenceLengthAction);
	}
//
//	private void lengthSpinner() {
//		final SetGlobalSequenceLengthAction setGlobalSequenceLengthAction = new SetGlobalSequenceLengthAction(
//				model, globalSequenceId, value, (Integer) lengthSpinner.getValue(),
//				modelStructureChangeListener);
//		setGlobalSequenceLengthAction.redo();
//		undoActionListener.pushAction(setGlobalSequenceLengthAction);
//	}

	public void setGlobalSequence(EditableModel model, Integer value, int globalSequenceId,
	                              UndoManager undoManager,
	                              ModelStructureChangeListener modelStructureChangeListener) {
		this.value = value;
		this.globalSequenceId = globalSequenceId;
//		this.undoActionListener = undoActionListener;
//		this.modelStructureChangeListener = modelStructureChangeListener;
		indexLabel.setText(Integer.toString(globalSequenceId));
//		lengthSpinner.reloadNewValue(value);
		lengthSpinner.setValue(value);
	}

	@Override
	public void setSelectedItem(Integer itemToSelect) {
		this.globalSequenceId = itemToSelect;
		value = modelHandler.getModel().getGlobalSeq(itemToSelect);
		indexLabel.setText(Integer.toString(globalSequenceId));
		lengthSpinner.setValue(value);
//	public void setSelectedItem(EditableModel itemToSelect) {

	}

	@Override
	public void save(EditableModel model, UndoManager undoManager, ModelStructureChangeListener changeListener) {

	}
}
