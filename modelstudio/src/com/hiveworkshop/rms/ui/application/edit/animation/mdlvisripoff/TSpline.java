package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public class TSpline extends JPanel {
	private final TTan ttDeriv; // in mdlvis this was just called der, and whatever, I'm copying them right now
	private int currentFrame;
	private final CurveRenderer curveRenderer;
	private final JSpinner tensionSpinner;
	private final JSpinner continuitySpinner;
	private final JSpinner biasSpinner;
	private AnimFlag timeline;

	public TSpline() {
		super(new MigLayout("fill", "[grow][]", "[][grow][][][]"));
		ttDeriv = new TTan();
		add(new JLabel("Curve properties"), "growx");
		add(new JButton("-"), "wrap");

		curveRenderer = new CurveRenderer(ttDeriv, timeline);
		curveRenderer.setBackground(Color.WHITE);
		add(curveRenderer, "wrap");

		add(new JLabel("Tension:"), "growx");
		tensionSpinner = new JSpinner(new SpinnerNumberModel(0.0, Long.MIN_VALUE, Long.MAX_VALUE, 0.01));
		add(tensionSpinner, "wrap");

		add(new JLabel("Continuity:"), "growx");
		continuitySpinner = new JSpinner(new SpinnerNumberModel(0.0, Long.MIN_VALUE, Long.MAX_VALUE, 0.01));
		add(continuitySpinner, "wrap");

		add(new JLabel("Bias:"), "growx");
		biasSpinner = new JSpinner(new SpinnerNumberModel(0.0, Long.MIN_VALUE, Long.MAX_VALUE, 0.01));
		add(biasSpinner, "wrap");
	}

	public void initFromKF() {
		if (timeline == null) {
			setVisible(false);
			return;
		}

		TreeMap<Integer, Entry> entryMap = timeline.getEntryMap();

		if (entryMap.ceilingKey(currentFrame) == null
				|| (timeline.getInterpolationType() != InterpolationType.HERMITE)) {
			setVisible(false);
			return;
		}

		ttDeriv.tang = timeline.getEntryAt(currentFrame);
		ttDeriv.cur = timeline.getEntryAt(currentFrame);
		ttDeriv.prev = timeline.getEntryAt(entryMap.lowerKey(currentFrame));
		ttDeriv.next = timeline.getEntryAt(entryMap.higherKey(currentFrame));

		ttDeriv.calcSplineParameters(entryMap.firstEntry().getValue().getValue() instanceof Quat, 4);

		tensionSpinner.setValue(Math.round(ttDeriv.tension * 100));
		continuitySpinner.setValue(Math.round(ttDeriv.continuity * 100));
		biasSpinner.setValue(Math.round(ttDeriv.bias * 100));

		setVisible(true);

		curveRenderer.repaint();
	}

	public float getTension() {
		return ttDeriv.tension;
	}

	public float getContinuity() {
		return ttDeriv.continuity;
	}

	public float getBias() {
		return ttDeriv.bias;
	}

	public void setTCB(float tension, float continuity, float bias) {
		ttDeriv.tension = tension;
		ttDeriv.continuity = continuity;
		ttDeriv.bias = bias;

		TreeMap<Integer, Entry> entryMap = timeline.getEntryMap();

		ttDeriv.cur = timeline.getEntryAt(currentFrame);
		ttDeriv.prev = timeline.getEntryAt(entryMap.lowerKey(currentFrame));
		ttDeriv.next = timeline.getEntryAt(entryMap.higherKey(currentFrame));

		ttDeriv.isLogsReady = false;
		if (timeline.getValueFromIndex(0) instanceof Quat) {
			ttDeriv.calcDerivative4D();
		} else if (timeline.getValueFromIndex(0) instanceof Vec3) {
			ttDeriv.calcDerivative3D();
		} else {
			ttDeriv.calcDerivativeXD(TTan.getSizeOfElement(timeline.getValueFromIndex(0)));
//			der.calcDerivativeXD(TTan.getSizeOfElement(entryMap.firstEntry().getValue().getValue()));
		}

		throw new UnsupportedOperationException(
				"Not finished here, need to have shared access to storing keyframe data and UndoManager");
	}

	public void setTimeline(AnimFlag timeline) {
		this.timeline = timeline;
		curveRenderer.setTTder(new TTan());
	}

	public void setSelection(int currentTime, AnimFlag timeline) {
		currentFrame = currentTime;
		this.timeline = timeline;
		curveRenderer.setTTder(new TTan());
		initFromKF();
	}

	public void setEmptySelection() {
		timeline = null;
		initFromKF();
	}
}
