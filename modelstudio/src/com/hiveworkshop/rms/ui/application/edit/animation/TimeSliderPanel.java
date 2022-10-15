package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddKeyframeAction3;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.TimeSkip;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TimeSliderPanel extends JPanel implements SelectionListener {
	private static final Color GLASS_TICK_COVER_COLOR = new Color(100, 190, 255, 100);
	private static final Color GLASS_TICK_COVER_BORDER_COLOR = new Color(0, 80, 255, 220);
	private static final int SLIDER_SIDE_BUTTON_SIZE = 15;
	private static final int SLIDER_TIME_BUTTON_SIZE = 50;
	private static final int SLIDING_TIME_CHOOSER_WIDTH = SLIDER_TIME_BUTTON_SIZE + (SLIDER_SIDE_BUTTON_SIZE * 2);
	private static final int VERTICAL_TICKS_HEIGHT = 10;
	private static final int VERTICAL_SLIDER_HEIGHT = 15;
	private static final int PLAY_BUTTON_SIZE = 30;
	private static final Dimension PLAY_BUTTON_DIMENSION = new Dimension(PLAY_BUTTON_SIZE, PLAY_BUTTON_SIZE);
	private static final int SIDE_OFFSETS = SLIDING_TIME_CHOOSER_WIDTH / 2;
	private static final Stroke WIDTH_2_STROKE = new BasicStroke(2);
	private static final Stroke WIDTH_1_STROKE = new BasicStroke(1);

	private final TimeSlider timeSlider;
	private final TimeBarPainter timeBarPainter;

	private boolean keyframeModeActive;
//	private int tickStep = 300;
	private ModelHandler modelHandler;
	private TimeEnvironmentImpl timeEnvironment;


	private final JCheckBox allKF;
	private final Timer liveAnimationTimer;
//	private final ProgramPreferences preferences;
	private final GUITheme theme;
	private boolean drawing;
	private final JButton setKeyframe;
	private final JButton setTimeBounds;
	private final JButton playButton;
	private final KeyframeHandler keyframeHandler;

	private final TimeLineMouseListener mouseAdapter;

	public TimeSliderPanel(ProgramPreferences preferences) {
//		this.changeListener = ModelStructureChangeListener.changeListener;
//		this.preferences = preferences;
		theme = preferences.getTheme();
		setLayout(new MigLayout("fill, gap 0, ins 0, aligny top", "3[]3[grow]3", "[]"));
		JPanel buttonPanel = new JPanel(new MigLayout("ins 0"));
		playButton = new JButton(RMSIcons.PLAY);
		playButton.addActionListener(e -> pausePlayAnimation());
//		JButton playButton = new JButton("||>");
		playButton.setPreferredSize(PLAY_BUTTON_DIMENSION);
		playButton.setSize(PLAY_BUTTON_DIMENSION);
		buttonPanel.add(playButton, "spany 2");
		allKF = new JCheckBox("All KF");
		allKF.addActionListener(e -> setShowAllKFs(allKF.isSelected()));


		setKeyframe = createSetKeyframeButton();
		buttonPanel.add(setKeyframe, "wrap");
		setTimeBounds = createSetTimeBoundsButton();
		buttonPanel.add(setTimeBounds, "wrap");

		buttonPanel.add(allKF, "wrap");
		buttonPanel.setOpaque(true);
		add(buttonPanel, "aligny top, shrink");

//		JPanel timelinePanel = new TimeLinePanel_test();
		JPanel timelinePanel = getTimelinePanel();
		timelinePanel.setOpaque(true);
		add(timelinePanel, "growx, growy, aligny top");

		timeSlider = new TimeSlider(timelinePanel);
		timeBarPainter = new TimeBarPainter(timeSlider);

		setForeground(Color.WHITE);
		setFont(new Font("Courier New", Font.PLAIN, 12));

//		copiedKeyframes = new ArrayList<>();

		liveAnimationTimer = new Timer(16, e -> liveAnimationTimerListener());

		timelinePanel.addComponentListener(getComponentAdapter());

		keyframeHandler = new KeyframeHandler(timelinePanel);

		mouseAdapter = new TimeLineMouseListener(this, keyframeHandler, timeSlider);
		timelinePanel.addMouseListener(mouseAdapter);
		timelinePanel.addMouseMotionListener(mouseAdapter);

		TimeSkip.getPlayItem();
		TimeSkip.getFfw1Item();
		TimeSkip.getFfw10Item();
		TimeSkip.getBbw1Item();
		TimeSkip.getBbw10Item();
		TimeSkip.getNextKFItem();
		TimeSkip.getPrevKFItem();
	}

	public KeyframeHandler getKeyframeHandler() {
		return keyframeHandler;
	}

	public JButton createSetKeyframeButton() {
		JButton setKeyframe = new JButton(RMSIcons.setKeyframeIcon);
		setKeyframe.setMargin(new Insets(0, 0, 0, 0));
		setKeyframe.setToolTipText("Create Keyframe");
		setKeyframe.addActionListener(e -> createKeyframe());
		return setKeyframe;
	}

	private void createKeyframe() {
		if (modelHandler != null) {
			UndoAction undoAction = new AddKeyframeAction3(
					modelHandler.getModelView().getSelectedIdObjects(),
					modelHandler.getRenderModel(),
					ProgramGlobals.getEditorActionType());
			modelHandler.getUndoManager().pushAction(undoAction.redo());
		}
	}

	public JButton createSetTimeBoundsButton() {
		JButton setTimeBounds = new JButton(RMSIcons.setTimeBoundsIcon);
		setTimeBounds.setMargin(new Insets(0, 0, 0, 0));
		setTimeBounds.setToolTipText("Choose Time Bounds");
		setTimeBounds.addActionListener(e -> timeBoundsChooserPanel());
		return setTimeBounds;
	}


	private void timeBoundsChooserPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();

		if (modelPanel != null) {
			TimeBoundChooserPanel tbcPanel = new TimeBoundChooserPanel(modelHandler);
			int confirmDialogResult = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), tbcPanel, "Set Time Bounds", JOptionPane.OK_CANCEL_OPTION);

			if (confirmDialogResult == JOptionPane.OK_OPTION) {
				RenderModel editorRenderModel = modelHandler.getRenderModel();
				tbcPanel.applyTo(editorRenderModel.getTimeEnvironment());
				modelPanel.refreshFromEditor();
				editorRenderModel.updateNodes(false);
			}
		}
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				keyframeHandler.slideExistingKeyFramesForResize();
				if (timeEnvironment != null) {
					timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
//					timeChooserRect.x = computeSliderXFromTime();
				}
			}
		};
	}

	private void liveAnimationTimerListener() {
		if (!drawing || timeEnvironment == null || !timeEnvironment.isLive()) {
			return;
		}
		timeEnvironment.updateAnimationTime();
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		repaint();
	}

	public void setModelHandler(ModelHandler modelHandler) {
		keyframeHandler.setModelHandler(modelHandler);
		mouseAdapter.setModelHandler(modelHandler);
		this.modelHandler = modelHandler;
		if(modelHandler != null) {
			timeEnvironment = modelHandler.getRenderModel().getTimeEnvironment();
			timeEnvironment.addChangeListener(this);
			timeBarPainter.setTimeEnvironment(timeEnvironment);
		} else {
			timeBarPainter.setTimeEnvironment(null);
		}
	}

	public void jumpToPreviousTime() {
		Integer newTime = keyframeHandler.getPrevFrame();
		setCurrentTime(newTime == null ? 0 : newTime);
	}

	public void jumpToNextTime() {
		Integer newTime = keyframeHandler.getNextFrame();
		setCurrentTime(newTime == null ? 0 : newTime);
	}


	public void setCurrentTime(int newTime) {
		timeEnvironment.setAnimationTime(newTime);
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		repaint();
	}

	public void timeStep(int step) {
		timeEnvironment.stepAnimationTime(step);
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		repaint();
	}


	//	@Override
	public void timeBoundsChanged(int start, int end) {
		liveAnimationTimer.stop();
		playButton.setIcon(RMSIcons.PLAY);
		timeSlider.setFromTimeFraction(timeEnvironment.getTimeRatio());
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	public void setKeyframeModeActive(final boolean keyframeModeActive) {
		this.keyframeModeActive = keyframeModeActive;
	}

	@Override
	public void onSelectionChanged(final AbstractSelectionManager newSelection) {
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	private void setShowAllKFs(boolean showAll){
		keyframeHandler.setShowAllKFs(showAll);
		repaint();
	}

	// called when user is editing the keyframes and they need to be updated
	public void revalidateKeyframeDisplay() {
		keyframeHandler.updateKeyframeDisplay();
		repaint();
	}

	private JPanel getTimelinePanel() {
		return new JPanel(new MigLayout("fill")) {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if (!drawing || timeEnvironment == null) {
					return;
				}
				int width = getWidth();
				timeBarPainter.drawTimeBar(g, width);
				if (SLIDING_TIME_CHOOSER_WIDTH <= width) {
					// keyframes
					keyframeHandler.drawKeyframeMarkers(g);
					// time label of dragged keyframe
					if (mouseAdapter.isDraggingKeyframe()) {
						mouseAdapter.getDraggingFrame().drawFloatingTime(g, Color.WHITE, mouseAdapter.getDraggingTimeDiffString());
					} else if (mouseAdapter.isHoveringKeyframe()) {
						mouseAdapter.getHoveringFrame().drawFloatingTime(g);
					}
					// time slider and glass covering current tick
					timeSlider.drawTimeSlider(g, timeEnvironment.getEnvTrackTime(), keyframeHandler.hasKeyFrameAt(timeEnvironment.getEnvTrackTime()));
				}

			}

		};
	}

	public void play() {
		if (liveAnimationTimer.isRunning()) {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
			timeEnvironment.setLive(false);
		} else {
			timeEnvironment.setLive(true);
			liveAnimationTimer.start();
			playButton.setIcon(RMSIcons.PAUSE);
		}
		repaint();
	}

	private void pausePlayAnimation() {
		if (timeEnvironment != null) {
			if (liveAnimationTimer.isRunning()) {
				liveAnimationTimer.stop();
				playButton.setIcon(RMSIcons.PLAY);
				timeEnvironment.setLive(false);
			} else {
				timeEnvironment.setLive(true);
				liveAnimationTimer.start();
				playButton.setIcon(RMSIcons.PAUSE);
			}
		} else {
			liveAnimationTimer.stop();
			playButton.setIcon(RMSIcons.PLAY);
		}
		repaint();
	}

	public void setDrawing(final boolean drawing) {
		this.drawing = drawing;
		mouseAdapter.setTimelineVisible(drawing);
		System.out.println("is drawing: " + drawing);
		for (Component component : getComponents()) {
			component.setEnabled(drawing);
		}
		playButton.setVisible(drawing);
		setKeyframe.setVisible(drawing);
		setTimeBounds.setVisible(drawing);
		allKF.setVisible(drawing);
		repaint();
	}
}
