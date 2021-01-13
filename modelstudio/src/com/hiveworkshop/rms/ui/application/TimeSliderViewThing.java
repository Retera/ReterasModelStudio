package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundChooserPanel;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;

public class TimeSliderViewThing {

	JTextField[] mouseCoordDisplay = new JTextField[3];
	TimeSliderPanel timeSliderPanel;
	JButton setKeyframe;
	JButton setTimeBounds;

	static View createTimeSliderView(JTextField[] mouseCoordDisplay, JButton setKeyframe, JButton setTimeBounds, TimeSliderPanel timeSliderPanel) {
		final View timeSliderView;
		final JPanel timeSliderAndExtra = new JPanel();
		final GroupLayout tsaeLayout = new GroupLayout(timeSliderAndExtra);
		final Component horizontalGlue = Box.createHorizontalGlue();
		final Component verticalGlue = Box.createVerticalGlue();
		tsaeLayout.setHorizontalGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(timeSliderPanel)
				.addGroup(tsaeLayout.createSequentialGroup()
						.addComponent(mouseCoordDisplay[0])
						.addComponent(mouseCoordDisplay[1])
						.addComponent(mouseCoordDisplay[2])
						.addComponent(horizontalGlue)
						.addComponent(setKeyframe)
						.addComponent(setTimeBounds)));
		tsaeLayout.setVerticalGroup(tsaeLayout.createSequentialGroup()
				.addComponent(timeSliderPanel)
				.addGroup(tsaeLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(mouseCoordDisplay[0])
						.addComponent(mouseCoordDisplay[1])
						.addComponent(mouseCoordDisplay[2])
						.addComponent(horizontalGlue)
						.addComponent(setKeyframe)
						.addComponent(setTimeBounds)));
		timeSliderAndExtra.setLayout(tsaeLayout);

		timeSliderView = new View("Timeline", null, timeSliderAndExtra);
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

	static JButton createSetTimeBoundsButton(MainPanel mainPanel) {
		final JButton setTimeBounds;
		setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
		setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
		setTimeBounds.setToolTipText("Choose Time Bounds");
		setTimeBounds.addActionListener(e -> timeBoundsChooserPanel(mainPanel));
		return setTimeBounds;
	}

	private static void timeBoundsChooserPanel(MainPanel mainPanel) {
		final TimeBoundChooserPanel timeBoundChooserPanel = new TimeBoundChooserPanel(
				mainPanel.currentModelPanel() == null ? null : mainPanel.currentModelPanel().getModelViewManager(),
				mainPanel.modelStructureChangeListener);
		final int confirmDialogResult = JOptionPane.showConfirmDialog(mainPanel, timeBoundChooserPanel,
				"Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);
		if (confirmDialogResult == JOptionPane.OK_OPTION) {
			timeBoundChooserPanel.applyTo(mainPanel.animatedRenderEnvironment);
			if (mainPanel.currentModelPanel() != null) {
				mainPanel.currentModelPanel().getEditorRenderModel().refreshFromEditor(
						mainPanel.animatedRenderEnvironment,
						ModelStructureChangeListenerImplementation.IDENTITY,
						ModelStructureChangeListenerImplementation.IDENTITY,
						ModelStructureChangeListenerImplementation.IDENTITY,
						mainPanel.currentModelPanel().getPerspArea().getViewport());
				mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
			}
		}
	}

	static void createTimeSliderPanel(MainPanel mainPanel) {
		mainPanel.timeSliderPanel = new TimeSliderPanel(mainPanel.animatedRenderEnvironment, mainPanel.modelStructureChangeListener, mainPanel.prefs);
		mainPanel.timeSliderPanel.setDrawing(false);
		mainPanel.timeSliderPanel.addListener(currentTime -> {
			mainPanel.animatedRenderEnvironment.setCurrentTime(currentTime - mainPanel.animatedRenderEnvironment.getStart());
			if (mainPanel.currentModelPanel() != null) {
				mainPanel.currentModelPanel().getEditorRenderModel().updateNodes(true, false);
				mainPanel.currentModelPanel().repaintSelfAndRelatedChildren();
			}
		});
		//		timeSliderPanel.addListener(creatorPanel);
	}

	static JButton createSetKeyframeButton(MainPanel mainPanel) {
		final JButton setKeyframe;
		setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
		setKeyframe.setMargin(new Insets(0, 0, 0, 0));
		setKeyframe.setToolTipText("Create Keyframe");
		setKeyframe.addActionListener(e -> createKeyframe(mainPanel));
		return setKeyframe;
	}

	private static void createKeyframe(MainPanel mainPanel) {
		final ModelPanel mpanel = mainPanel.currentModelPanel();
		if (mpanel != null) {
			mpanel.getUndoManager().pushAction(mpanel.getModelEditorManager().getModelEditor().createKeyframe(mainPanel.actionType));
		}
		MainPanel.repaintSelfAndChildren(mainPanel);
		mpanel.repaintSelfAndRelatedChildren();
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
