package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;

import javax.swing.*;

public class FloatValuePanel extends ValuePanel<Float> {

	//	private final JPanel keyFrameHeaderHolder;
//	boolean doSave;
//	String preEditValue;
//	private FloatTrackTableModel floatTrackTableModel;
//	private AnimFlag animFlag;
//	TimelineKeyNamer timelineKeyNamer;
//	private TimelineContainer timelineContainer;
//	private String flagName;
//	private AnimFlag oldAnimFlag;
	private ComponentEditorJSpinner staticSpinner;


	public FloatValuePanel(final String title, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		this(title, Double.MAX_VALUE, -Double.MAX_VALUE, undoActionListener, modelStructureChangeListener);
//		super(title, undoActionListener, modelStructureChangeListener);
//		this(title, 1.0, 0.0);
	}

	public FloatValuePanel(final String title, double maxValue, double minValue, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		super(title, maxValue, minValue, undoActionListener, modelStructureChangeListener);
	}

	@Override
	ComponentEditorJSpinner getStaticComponent() {
//		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, minValue, maxValue, 0.01));
		staticSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(1.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01));

		((JSpinner.NumberEditor) staticSpinner.getEditor()).getFormat().setMinimumFractionDigits(2);

		final JSpinner standinGuiSpinner = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
		staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
		staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());

		// below: fix this stupid nonsense java bug that a spinner with infinite value
		// range takes infinite GUI space on the screen
//		if (maxValue == Double.MAX_VALUE) {
//			final JSpinner standinGuiSpinner = new JSpinner(new SpinnerNumberModel(1.0, -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
//			staticSpinner.setPreferredSize(standinGuiSpinner.getPreferredSize());
//			staticSpinner.setMaximumSize(standinGuiSpinner.getMaximumSize());
//			staticSpinner.setMinimumSize(standinGuiSpinner.getMinimumSize());
//		}
//		spinInterpPanel.add(staticSpinner, "wrap");
		return staticSpinner;
	}

	@Override
	void reloadStaticValue(Float value) {
		staticSpinner.reloadNewValue(value);
	}

//	public void reloadNewValue(final float value, final AnimFlag animFlag, final TimelineContainer timelineContainer, final String flagName) {
//		this.animFlag = animFlag;
//		this.timelineContainer = timelineContainer;
//		this.flagName = flagName;
//
//		if (animFlag == null) {
//			toggleStaticDynamicPanel(true);
//		} else {
//			toggleStaticDynamicPanel(false);
//			interpTypeBox.setSelectedItem(animFlag.getInterpolationType());
//		}
////		System.out.println("colums: " + keyframeTable.getModel().getColumnCount());
//		staticSpinner.reloadNewValue(value);
//		setTableModel();
//	}

//	private void setTableModel() {
//		if (floatTrackTableModel == null) {
//			floatTrackTableModel = new FloatTrackTableModel(animFlag);
//
//			keyframeTable.setModel(floatTrackTableModel);
//			keyframeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//		}
//
//		if (floatTrackTableModel != null) {
//			floatTrackTableModel.setTrack(animFlag);
//			TableColumn column = keyframeTable.getColumn("");
//			if (column != null) {
//				int rowHeight = keyframeTable.getRowHeight();
//				column.setMaxWidth(rowHeight);
//				column.setPreferredWidth(rowHeight);
//				column.setMinWidth(5);
//			}
//		}
//	}

	@Override
	Float getZeroValue() {
		return 0.0f;
	}

	@Override
	Float parseValue(String valueString) {
		return null;
	}


}
