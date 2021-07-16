package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class TSpline extends JPanel {
	private int currentFrame;
	private final CurveRenderer curveRenderer;
	private SplineTracker<?> splineTracker;
	private final JSpinner tensionSpinner;
	private final JSpinner continuitySpinner;
	private final JSpinner biasSpinner;
	private AnimFlag<?> timeline;

	public TSpline() {
		super(new MigLayout("fill, debug", "[][]", "[][grow][][][]"));
		add(new JLabel("Curve properties"), "growx, spanx, wrap");
		JButton loadButton = new JButton("-");
		loadButton.addActionListener(e -> load());
		add(loadButton, "");
		JButton loadButton2 = new JButton(":");
		loadButton2.addActionListener(e -> loadTime());
		add(loadButton2, "wrap");

		curveRenderer = new CurveRenderer();
//		add(curveRenderer, "growx, growy, spanx, wrap");
		add(curveRenderer, "spanx, wrap");
		curveRenderer.setBackground(Color.WHITE);

		add(new JLabel("Tension:"), "");
		tensionSpinner = getSpinner();
		add(tensionSpinner, "wrap");

		add(new JLabel("Continuity:"), "");
		continuitySpinner = getSpinner();
		add(continuitySpinner, "wrap");

		add(new JLabel("Bias:"), "");
		biasSpinner = getSpinner();
		add(biasSpinner, "wrap");
		setEmptySelection();
		JButton testNewValues = new JButton("x");
		testNewValues.addActionListener(e -> testNewValues());
		add(testNewValues, "wrap");
	}

	public JSpinner getSpinner() {
		SpinnerNumberModel model = new SpinnerNumberModel(0.0, Integer.MIN_VALUE, Integer.MAX_VALUE, 5);
		JSpinner jSpinner = new JSpinner(model);
		jSpinner.setMaximumSize(new Dimension(50, 50));
		return jSpinner;
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
	private void loadTime() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			int animationTime = modelHandler.getEditTimeEnv().getAnimationTime();
			setSelection(animationTime);
		}
	}
	private void testNewValues() {
		if(splineTracker != null){
//			float newT = 50;
//			float newC = 50;
//			float newB = 50;
			float newT = ((Double) tensionSpinner.getValue()).floatValue()/100;
			float newC = ((Double) continuitySpinner.getValue()).floatValue()/100;
			float newB = ((Double) biasSpinner.getValue()).floatValue()/100;
			splineTracker.setTCB(newT, newC, newB);
			curveRenderer.makeSplines();
		}
	}

	public void initFromKF() {
		if (timeline == null) {
//			setVisible(false);
			curveRenderer.clearCurve();
			return;
		}

		if (timeline.valueAt(currentFrame) == null
				|| (timeline.getInterpolationType() != InterpolationType.HERMITE)) {
//			setVisible(false);
			curveRenderer.clearCurve();
			return;
		}

		splineTracker.initFromKF();
		curveRenderer.makeSplines();

		tensionSpinner.setValue(Math.round(splineTracker.getTension() * 100));
		continuitySpinner.setValue(Math.round(splineTracker.getContinuity() * 100));
		biasSpinner.setValue(Math.round(splineTracker.getBias() * 100));

		setVisible(true);

		curveRenderer.repaint();
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
