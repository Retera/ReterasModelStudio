package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.globalsequence.SetGlobalSequenceLengthAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

//public class ComponentGlobalSequencePanel extends JPanel implements ComponentPanel<EditableModel> {
public class ComponentGlobalSequencePanel extends JPanel implements ComponentPanel<Integer> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final JLabel indexLabel;
	private final JSpinner lengthSpinner;
	private int globalSequenceId;
	private Integer value;

	public ComponentGlobalSequencePanel(final ModelViewManager modelViewManager,
	                                    final UndoActionListener undoActionListener,
	                                    final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
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
		final SetGlobalSequenceLengthAction setGlobalSequenceLengthAction = new SetGlobalSequenceLengthAction(
				modelViewManager.getModel(), globalSequenceId, value, (Integer) lengthSpinner.getValue(),
				modelStructureChangeListener);
		setGlobalSequenceLengthAction.redo();
		undoActionListener.pushAction(setGlobalSequenceLengthAction);
	}
//
//	private void lengthSpinner() {
//		final SetGlobalSequenceLengthAction setGlobalSequenceLengthAction = new SetGlobalSequenceLengthAction(
//				model, globalSequenceId, value, (Integer) lengthSpinner.getValue(),
//				modelStructureChangeListener);
//		setGlobalSequenceLengthAction.redo();
//		undoActionListener.pushAction(setGlobalSequenceLengthAction);
//	}

	public void setGlobalSequence(final EditableModel model, final Integer value, final int globalSequenceId,
	                              final UndoActionListener undoActionListener,
	                              final ModelStructureChangeListener modelStructureChangeListener) {
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
		value = modelViewManager.getModel().getGlobalSeq(itemToSelect);
		indexLabel.setText(Integer.toString(globalSequenceId));
		lengthSpinner.setValue(value);
//	public void setSelectedItem(EditableModel itemToSelect) {

	}

	@Override
	public void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener) {

	}
}
