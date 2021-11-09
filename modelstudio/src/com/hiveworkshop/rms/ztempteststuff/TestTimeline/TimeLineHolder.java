package com.hiveworkshop.rms.ztempteststuff.TestTimeline;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderTimeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.function.Consumer;

public class TimeLineHolder extends JPanel {
	private final Map<Integer, KeyFrame> timeToKey = new LinkedHashMap<>();
	List<TimeLinePanel.TimeListener> timeListeners = new ArrayList<>();
	TimeLinePanel.TimeListener setAnimationTime;
	private TimeLinePanel timeLinePanel;
	private Timer liveAnimationTimer;
	private TimeEnvironmentImpl renderEnv;
	private ModelView modelView;
	private ModelHandler modelHandler;
	private boolean showAllKeyframes;
	private SelectionManager nodeSelectionManager;
	private SelectionListener selectionListener = this::updateKeyframeDisplay;
	private TimeSliderTimeListener notifier;
	private Sequence sequence;

	//	public TimeLineHolder(ComPerspRenderEnv renderEnv, ModelView modelView){
//		this(renderEnv);
//		this.modelView = modelView;
//	}
	public TimeLineHolder(TimeEnvironmentImpl renderEnv) {
		setLayout(new MigLayout("fill, ins 0, gap 0", "[grow]", "[][grow]"));
		add(getButtonPanel(), "wrap");

		liveAnimationTimer = new Timer(16, e -> liveAnimationTimerListener());
		timeLinePanel = new TimeLinePanel();

		notifier = new TimeSliderTimeListener();
		this.renderEnv = renderEnv;
		addTestingStuff();

		if (renderEnv != null) {
			sequence = renderEnv.getCurrentSequence();
			if (sequence != null) {
				timeLinePanel.setLimits(sequence.getLength());
				setAnimationTime = renderEnv::setAnimationTime;
				timeLinePanel.addTimeListener(setAnimationTime);
			}
		}
//		add(timeLinePanel, "growx, growy");
		JScrollPane scrollPane = new JScrollPane(timeLinePanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(scrollPane, "growx, growy");
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setMaxSize(e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				setMaxSize(e);
			}
		});
	}

	public TimeLineHolder setRenderEnv(TimeEnvironmentImpl renderEnv) {
		if (this.renderEnv != null) {
			timeLinePanel.removeTimeListener(setAnimationTime);
		}
		this.renderEnv = renderEnv;
		if (renderEnv != null && renderEnv.getCurrentSequence() != null) {
			sequence = this.renderEnv.getCurrentSequence();
			timeLinePanel.setLimits(sequence.getLength());
//			setAnimationTime = renderEnv::setAnimationTime;
			setAnimationTime = this.renderEnv::setAnimationTime;
			timeLinePanel.addTimeListener(setAnimationTime);
//			this.renderEnv.addChangeListener((i) -> timeLinePanel.setLimits(this.renderEnv.getLength()));
		}
		return this;
	}

	public TimeLineHolder setModelView(ModelView modelView) {
		this.modelView = modelView;
		return this;
	}

	public TimeLineHolder setModelHandler(ModelHandler modelHandler) {
		if (modelHandler != null) {
//			this.undoManager = modelHandler.getUndoManager();
			this.modelView = modelHandler.getModelView();
			this.renderEnv = modelHandler.getEditTimeEnv();
//			this.renderEnv.addChangeListener(this);
			setAnimationTime = renderEnv::setAnimationTime;
			timeLinePanel.addTimeListener(setAnimationTime);
			sequence = this.renderEnv.getCurrentSequence();
		} else {
			timeLinePanel.removeTimeListener(setAnimationTime);
			setAnimationTime = null;
		}
		this.modelHandler = modelHandler;
		return this;
	}

	private void addTestingStuff() {
		Animation woop = new Animation("Woop", 1, 300);
		if (renderEnv != null) {
			renderEnv.setSequence(woop);
		}

		Vec3AnimFlag translation1 = new Vec3AnimFlag("Translation");
		QuatAnimFlag rot1 = new QuatAnimFlag("Rotation");
		Vec3AnimFlag scale1 = new Vec3AnimFlag("Scaling");
		Vec3AnimFlag translation2 = new Vec3AnimFlag("Translation");
		QuatAnimFlag rot2 = new QuatAnimFlag("Rotation");
		Vec3AnimFlag scale2 = new Vec3AnimFlag("Scaling");
		List<KeyFrame> keyFrames = new ArrayList<>();
		keyFrames.add(new KeyFrame(5).addAnimationFlag(translation1).addAnimationFlag(scale1).addAnimationFlag(rot1));
		keyFrames.add(new KeyFrame(30).addAnimationFlag(translation2).addAnimationFlag(scale2).addAnimationFlag(rot2));
		timeLinePanel.setKeyframes(keyFrames);
	}

	private void updateKeyframeDisplay(AbstractSelectionManager newSelection) {
		timeToKey.clear();
		if (modelView != null) {
//			System.out.println("new Selection size: " + newSelection.toString());
//			System.out.println("new Selection size: " + nodeSelectionManager.getSelection().size());
			reCalculateKeyframes(getSelectionToUse());
			timeLinePanel.setKeyframes(new ArrayList<>(timeToKey.values()));
		}
		if (renderEnv != null && renderEnv.getCurrentSequence() != null) {
			timeLinePanel.setLimits(renderEnv.getLength());
		}
	}

	private void reCalculateKeyframes(Iterable<IdObject> selection) {
		sequence = renderEnv.getCurrentSequence();
		if (sequence != null) {
			timeLinePanel.setLimits(sequence.getLength());
			for (IdObject object : selection) {
				for (AnimFlag<?> flag : object.getAnimFlags()) {
					TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(sequence);
					if (entryMap != null && !entryMap.isEmpty()) {
						for (Integer time : entryMap.keySet()) {
							KeyFrame keyFrame = timeToKey.get(time);
							if (keyFrame == null) {
								keyFrame = new KeyFrame(time);
								timeToKey.put(time, keyFrame);
							}
							keyFrame.addObject(object);
							keyFrame.addAnimationFlag(flag);
						}
					}
				}
			}
		}
	}

	public Collection<IdObject> getSelectionToUse() {
		if ((modelView == null) || (modelView.getModel() == null)) {
			System.out.println("no selection :O");
			return Collections.emptySet();
		}
		return showAllKeyframes ? modelView.getModel().getIdObjects() : modelView.getSelectedIdObjects();
	}

	private void liveAnimationTimerListener() {
		if (isShowing() && renderEnv.isLive()) {
			if (sequence != renderEnv.getCurrentSequence()) {
				reCalculateKeyframes(getSelectionToUse());
			}
			renderEnv.updateAnimationTime();
			System.out.println("anim time: " + renderEnv.getEnvTrackTime() + ", anim: " + renderEnv.getCurrentAnimation());
			updateTime(renderEnv.getEnvTrackTime());
			timeLinePanel.updateTime(renderEnv.getEnvTrackTime());
		}
	}


	private JScrollPane getButtonPanel() {
//		JPanel buttonPanel = new JPanel(new MigLayout("fill, flowy, ins 0, gap 0"));
//		buttonPanel.add(new Button("|<"), "wrap");
//		buttonPanel.add(new Button("<"), "wrap");
//		buttonPanel.add(new Button("Play"), "wrap");
//		buttonPanel.add(new Button(">"), "wrap");
//		buttonPanel.add(new Button(">|"), "wrap");
//		buttonPanel.add(new Button("add"), "wrap");
//		buttonPanel.add(new Button("set Bounds"), "wrap");
		JPanel buttonPanel = new JPanel(new MigLayout("fill, ins 0, gap 0"));
		JButton toStart = new JButton("|<");
		toStart.addActionListener(e -> timeLinePanel.updateTime(0));
		buttonPanel.add(toStart);
		JButton jumpBack = new JButton("<");
		jumpBack.addActionListener(e -> timeLinePanel.updateTime(renderEnv.getEnvTrackTime() - 1));
		buttonPanel.add(jumpBack);
		JButton play = new JButton("Play");
		play.addActionListener(e -> playPauseAnimation(play));
		buttonPanel.add(play);
		JButton jumpForward = new JButton(">");
		jumpForward.addActionListener(e -> timeLinePanel.updateTime(renderEnv.getEnvTrackTime() + 1));
		buttonPanel.add(jumpForward);
		JButton toEnd = new JButton(">|");
		toEnd.addActionListener(e -> timeLinePanel.updateTime(renderEnv.getCurrentSequence().getLength()));
		buttonPanel.add(toEnd);
		buttonPanel.add(new JButton("add"));
		buttonPanel.add(new JButton("set Bounds"));
		buttonPanel.add(new JCheckBox("all KF"));
		JScrollPane buttonHolder = new JScrollPane(buttonPanel);
//		buttonHolder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		buttonHolder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		new JPanel().add(buttonHolder.getHorizontalScrollBar()); //Hides the scrollbar
		buttonHolder.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		buttonHolder.setWheelScrollingEnabled(true);
		buttonHolder.setAutoscrolls(true);

		return buttonHolder;
	}

//	private void jumpKf(int time){
//		renderEnv.setAnimationTime(time);
//		timeLinePanel.setCurrentTime(time);
//	}

	private void playPauseAnimation(JButton playButton) {
		if (liveAnimationTimer.isRunning()) {
			liveAnimationTimer.stop();
			renderEnv.setLive(false);
			playButton.setText("Play");
//			playButton.setIcon(RMSIcons.PLAY);
		} else {
			liveAnimationTimer.start();
			renderEnv.setLive(true);
			playButton.setText("Pause");
//			playButton.setIcon(RMSIcons.PAUSE);
		}
		repaint();
	}


	private void setMaxSize(ComponentEvent e) {
		if (this.getParent() != null) {
			setMaximumSize(getParent().getSize());
			System.out.println("maxSize: " + getMaximumSize());
		}
	}

	public TimeLineHolder addTimeListener(TimeLinePanel.TimeListener timeListener) {
		timeListeners.add(timeListener);
		return this;
	}

	public void addListener(Consumer<Integer> listener) {
		notifier.subscribe(listener);
	}

	public TimeLineHolder removeTimeListener(TimeLinePanel.TimeListener timeListener) {
		timeListeners.remove(timeListener);
		return this;
	}

	private void updateTime(int time) {
		System.out.println("time changed?! time: " + time);
		timeLinePanel.setCurrentTime(time);
//		timeLinePanel.setCurrentTime(renderEnv.getRawAnimationTime());
		for (TimeLinePanel.TimeListener timeListener : timeListeners) {
			timeListener.timeChanged(time);
		}
		notifier.timeChanged(time);
	}


}
