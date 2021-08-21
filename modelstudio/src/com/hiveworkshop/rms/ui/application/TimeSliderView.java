package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundChooserPanel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class TimeSliderView extends ModelDependentView {
	private TimeSliderPanel timeSliderPanel;

	public TimeSliderView(){
		super("Timeline", null, new JPanel());
		timeSliderPanel = createTimeSliderPanel();
		setComponent(timeSliderPanel);
	}

	public TimeSliderView setModelHandler(ModelHandler modelHandler){
		timeSliderPanel.setModelHandler(modelHandler);
		return this;
	}

	@Override
	public TimeSliderView setModelPanel(ModelPanel modelPanel){
		if(modelPanel != null){
			timeSliderPanel.setModelHandler(modelPanel.getModelHandler());
		} else {
			timeSliderPanel.setModelHandler(null);
		}
		return this;
	}

	public TimeSliderView setAnimationMode(boolean animationModeState) {
		timeSliderPanel.setDrawing(animationModeState);
		timeSliderPanel.setKeyframeModeActive(animationModeState);
		timeSliderPanel.repaint();
		return this;
	}

	public TimeSliderPanel getTimeSliderPanel() {
		return timeSliderPanel;
	}

	static View createTimeSliderView(TimeSliderPanel timeSliderPanel) {
		final View timeSliderView;
		timeSliderView = new View("Timeline", null, timeSliderPanel);
		return timeSliderView;
	}

	static void createMouseCoordDisp(JTextField[] mouseCoordDisplay) {
		for (int i = 0; i < mouseCoordDisplay.length; i++) {
			mouseCoordDisplay[i] = new JTextField("");
			mouseCoordDisplay[i].setMaximumSize(new Dimension(80, 18));
			mouseCoordDisplay[i].setMinimumSize(new Dimension(50, 15));
			mouseCoordDisplay[i].setEditable(false);
		}
	}

	public static JButton createSetTimeBoundsButton() {
		final JButton setTimeBounds;
		setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
		setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
		setTimeBounds.setToolTipText("Choose Time Bounds");
		setTimeBounds.addActionListener(e -> timeBoundsChooserPanel());
		return setTimeBounds;
	}

	private static void timeBoundsChooserPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();

		if (modelPanel != null) {
			TimeBoundChooserPanel tbcPanel = new TimeBoundChooserPanel(modelPanel.getModelHandler());
			int confirmDialogResult = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), tbcPanel, "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);

			if (confirmDialogResult == JOptionPane.OK_OPTION) {
				//			tbcPanel.applyTo(mainPanel.animatedRenderEnvironment);
				tbcPanel.applyTo(modelPanel.getModelHandler().getEditTimeEnv());
				ModelStructureChangeListener.refreshFromEditor(modelPanel);
				modelPanel.getEditorRenderModel().updateNodes(false);
			}
		}
	}

	public static void setMouseCoordDisplay(JTextField[] mouseCoordDisplay, CoordinateSystem coordinateSystem, double value1, double value2) {
		for (final JTextField jTextField : mouseCoordDisplay) {
			jTextField.setText("");
		}
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		if (coordinateSystem.getPortFirstXYZ() < 0) {
			dim1 = (byte) (-dim1 - 1);
			value1 = -value1;
		}
		if (dim2 < 0) {
			dim2 = (byte) (-dim2 - 1);
			value2 = -value2;
		}
		mouseCoordDisplay[dim1].setText((float) value1 + "");
		mouseCoordDisplay[dim2].setText((float) value2 + "");
	}

	private TimeSliderPanel createTimeSliderPanel() {
		timeSliderPanel = new TimeSliderPanel(ProgramGlobals.getPrefs());
		timeSliderPanel.setDrawing(false);
		Consumer<Integer> timeSliderTimeListener = currentTime -> {
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime);
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime - mainPanel.animatedRenderEnvironment.getStart());
			if (ProgramGlobals.getCurrentModelPanel() != null) {
				ProgramGlobals.getCurrentModelPanel().getEditorRenderModel().updateNodes(false);
				ProgramGlobals.getCurrentModelPanel().repaintSelfAndRelatedChildren();
			}
		};
		timeSliderPanel.addListener(timeSliderTimeListener);
		//		timeSliderPanel.addListener(creatorPanel);
		return timeSliderPanel;
	}
}
