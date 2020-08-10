package com.hiveworkshop.wc3.gui.modeledit.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.globalsequence.SetGlobalSequenceLengthAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.mdl.EditableModel;

import net.miginfocom.swing.MigLayout;

public class ComponentGlobalSequencePanel extends JPanel {
	private final JLabel indexLabel;
	private final ComponentEditorJSpinner lengthSpinner;
	private EditableModel model;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private int globalSequenceId;
	private Integer value;

	public ComponentGlobalSequencePanel() {
		setLayout(new MigLayout());
		lengthSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		lengthSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetGlobalSequenceLengthAction setGlobalSequenceLengthAction = new SetGlobalSequenceLengthAction(
						model, globalSequenceId, value, ((Number) lengthSpinner.getValue()).intValue(),
						modelStructureChangeListener);
				setGlobalSequenceLengthAction.redo();
				undoActionListener.pushAction(setGlobalSequenceLengthAction);
			}
		});
		add(new JLabel("GlobalSequence "), "cell 0 0");
		indexLabel = new JLabel();
		add(indexLabel, "cell 1 0");
		add(new JLabel("Duration: "), "cell 0 1");
		add(lengthSpinner, "cell 1 1");
	}

	public void setGlobalSequence(final EditableModel model, final Integer value, final int globalSequenceId,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.model = model;
		this.value = value;
		this.globalSequenceId = globalSequenceId;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		indexLabel.setText(Integer.toString(globalSequenceId));
		lengthSpinner.reloadNewValue(value);
	}
}
