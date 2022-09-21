package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.View;

import javax.swing.*;

public class TimeSliderView extends ModelDependentView {
	private TimeSliderPanel timeSliderPanel;
//	private TimeLineHolder timeLineHolder;

	public TimeSliderView() {
		super("Timeline", null, new JPanel());
		timeSliderPanel = createTimeSliderPanel();
		setComponent(timeSliderPanel);

//		JMenu filtersMenu = new JMenu("Options");
//		filtersMenu.add(new JCheckBoxMenuItem("All Transform Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Geoset Alpha Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Geoset Color Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Layer Alpha Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Layer TextureId Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Event Keyframes", true));
//		filtersMenu.add(new JCheckBoxMenuItem("Camera Keyframes", true));
//		JButton button = new JButton("Options!");
//		System.out.println("popupMenu: " + filtersMenu.getPopupMenu());
//		button.addActionListener(e -> filtersMenu.getPopupMenu().show(button, 0, 0));
////		button.addActionListener(e -> filtersMenu.doClick());
////		getCustomTitleBarComponents().add(filtersMenu);
//		getCustomTitleBarComponents().add(button);


//		timeLineHolder = createTimeLineHolder();
//		setComponent(timeLineHolder);
	}

	public TimeSliderView setModelHandler(ModelHandler modelHandler) {
		timeSliderPanel.setModelHandler(modelHandler);
		return this;
	}

	@Override
	public TimeSliderView setModelPanel(ModelPanel modelPanel){
		if(modelPanel != null){
			timeSliderPanel.setModelHandler(modelPanel.getModelHandler());
//			timeLineHolder.setModelHandler(modelPanel.getModelHandler());
		} else {
			timeSliderPanel.setModelHandler(null);
//			timeLineHolder.setModelHandler(null);
		}
		return this;
	}

	public TimeSliderView setAnimationMode(boolean animationModeState) {
//		System.out.println("TimeSliderView: setAnimationMode");
		timeSliderPanel.setDrawing(animationModeState);
//		System.out.println("TimeSliderView: setting KF-Mode");
		timeSliderPanel.setKeyframeModeActive(animationModeState);
//		System.out.println("TimeSliderView: repainting timeSliderPanel");
		timeSliderPanel.repaint();
//		System.out.println("TimeSliderView: done");
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


	private TimeSliderPanel createTimeSliderPanel() {
		timeSliderPanel = new TimeSliderPanel(ProgramGlobals.getPrefs());
		timeSliderPanel.setDrawing(false);
//		Consumer<Integer> timeSliderTimeListener = currentTime -> {
//			if (ProgramGlobals.getCurrentModelPanel() != null) {
////				ProgramGlobals.getCurrentModelPanel().getEditorRenderModel().updateNodes(false);
////				ProgramGlobals.getCurrentModelPanel().repaintSelfAndRelatedChildren();
//			}
//		};
//		timeSliderPanel.addListener(timeSliderTimeListener);
		return timeSliderPanel;
	}

//	private TimeLineHolder createTimeLineHolder() {
//		timeLineHolder = new TimeLineHolder(null);
////		timeLineHolder.setDrawing(false);
//		Consumer<Integer> timeSliderTimeListener = currentTime -> {
//			if (ProgramGlobals.getCurrentModelPanel() != null) {
//				ProgramGlobals.getCurrentModelPanel().getEditorRenderModel().updateNodes(false);
//				ProgramGlobals.getCurrentModelPanel().repaintSelfAndRelatedChildren();
//			}
//		};
//		timeLineHolder.addListener(timeSliderTimeListener);
//		return timeLineHolder;
//	}
}
