package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundChooserPanel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderTimeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class TimeSliderView {

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

	public static JButton createSetTimeBoundsButton(MainPanel mainPanel) {
		final JButton setTimeBounds;
		setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
		setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
		setTimeBounds.setToolTipText("Choose Time Bounds");
		setTimeBounds.addActionListener(e -> timeBoundsChooserPanel(mainPanel));
		return setTimeBounds;
	}

	private static void timeBoundsChooserPanel(MainPanel mainPanel) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		ModelView modelView = modelPanel == null ? null : modelPanel.getModelView();
		TimeBoundChooserPanel tbcPanel = new TimeBoundChooserPanel(modelView, mainPanel.modelStructureChangeListener);
		int confirmDialogResult = JOptionPane.showConfirmDialog(mainPanel, tbcPanel, "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);

		if (confirmDialogResult == JOptionPane.OK_OPTION) {
//			tbcPanel.applyTo(mainPanel.animatedRenderEnvironment);
			if (modelPanel != null) {
				tbcPanel.applyTo(modelPanel.getModelHandler().getEditTimeEnv());
				modelPanel.getEditorRenderModel().refreshFromEditor(
//						mainPanel.animatedRenderEnvironment,
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						modelPanel.getPerspArea().getViewport().getParticleTextureInstance());
				modelPanel.getEditorRenderModel().updateNodes(false);
			}
		}
	}

	static void createTimeSliderPanel(MainPanel mainPanel) {
//		mainPanel.getTimeSliderPanel() = new TimeSliderPanel(mainPanel, mainPanel.animatedRenderEnvironment, mainPanel.modelStructureChangeListener, ProgramGlobals.getPrefs());
		mainPanel.getTimeSliderPanel().setDrawing(false);
		TimeSliderTimeListener timeSliderTimeListener = currentTime -> {
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime);
//			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime - mainPanel.animatedRenderEnvironment.getStart());
			if (ProgramGlobals.getCurrentModelPanel() != null) {
				ProgramGlobals.getCurrentModelPanel().getEditorRenderModel().updateNodes(false);
				ProgramGlobals.getCurrentModelPanel().repaintSelfAndRelatedChildren();
			}
		};
		mainPanel.getTimeSliderPanel().addListener(timeSliderTimeListener);
		//		timeSliderPanel.addListener(creatorPanel);
	}

	public static void setMouseCoordDisplay(JTextField[] mouseCoordDisplay, byte dim1, byte dim2, double value1, double value2) {
		for (final JTextField jTextField : mouseCoordDisplay) {
			jTextField.setText("");
		}
		if (dim1 < 0) {
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
}
