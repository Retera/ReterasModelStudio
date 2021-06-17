package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

public class TSpline extends JPanel {
	private int currentFrame;
	private final CurveRenderer curveRenderer;
	private SplineTracker<?> splineTracker;
	private final JSpinner tensionSpinner;
	private final JSpinner continuitySpinner;
	private final JSpinner biasSpinner;
	private AnimFlag<?> timeline;

	public TSpline() {
		super(new MigLayout("fill", "[][]", "[][grow][][][]"));
		add(new JLabel("Curve properties"), "growx");
		JButton loadButton = new JButton("-");
		loadButton.addActionListener(e -> load());
		add(loadButton, "wrap");

		curveRenderer = new CurveRenderer();
		add(curveRenderer, "growx, growy, spanx, wrap");
		curveRenderer.setBackground(Color.WHITE);

		add(new JLabel("Tension:"), "");
		tensionSpinner = new JSpinner(new SpinnerNumberModel(0.0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.1));
		add(tensionSpinner, "wrap");

		add(new JLabel("Continuity:"), "");
		continuitySpinner = new JSpinner(new SpinnerNumberModel(0.0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.1));
		add(continuitySpinner, "wrap");

		add(new JLabel("Bias:"), "");
		biasSpinner = new JSpinner(new SpinnerNumberModel(0.0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0.1));
		add(biasSpinner, "wrap");
		setEmptySelection();
	}

	private void load() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			int animationTime = modelHandler.getEditTimeEnv().getAnimationTime();
			Set<IdObject> selectedIdObjects = modelHandler.getModelView().getSelectedIdObjects();
			if (selectedIdObjects.size() == 1) {
				IdObject idObject = selectedIdObjects.stream().findFirst().get();
				AnimFlag<?> rotation = idObject.find("Rotation");
				if (rotation != null && rotation.tans()) {
					setTimeline(animationTime, rotation);
				}
			}
		}
	}

	public void initFromKF() {
		if (timeline == null) {
//			setVisible(false);
			return;
		}

		TreeMap<Integer, ? extends Entry<?>> entryMap = timeline.getEntryMap();

		if (!timeline.hasEntryAt(currentFrame) || entryMap.get(currentFrame) == null
				|| (timeline.getInterpolationType() != InterpolationType.HERMITE)) {
//			setVisible(false);
			return;
		}

		splineTracker.initFromKF();

		tensionSpinner.setValue(Math.round(splineTracker.getTension() * 100));
		continuitySpinner.setValue(Math.round(splineTracker.getContinuity() * 100));
		biasSpinner.setValue(Math.round(splineTracker.getBias() * 100));

		setVisible(true);

		curveRenderer.repaint();
	}

	public void setTimeline2(AnimFlag<?> timeline) {
		this.timeline = timeline;
//		curveRenderer.setTTder(new TTan());
		splineTracker = new SplineTracker<>(timeline);
		curveRenderer.setSplineTracker(splineTracker);
		initFromKF();

		revalidate();
		repaint();
	}

	public void setTimeline(int currentTime, AnimFlag<?> timeline) {
		this.timeline = timeline;

//		splineTracker = new SplineTracker<>(TTan.getNewTTan2(timeline));
		splineTracker = new SplineTracker<>(timeline);
		curveRenderer.setSplineTracker(splineTracker);
		splineTracker.setTime(currentTime);
		initFromKF();

		revalidate();
		repaint();
	}

	public void setSelection(int currentTime) {
		currentFrame = currentTime;
		splineTracker.setTime(currentTime);
		initFromKF();
	}

	public void setEmptySelection() {
		timeline = null;
		initFromKF();
	}
}
