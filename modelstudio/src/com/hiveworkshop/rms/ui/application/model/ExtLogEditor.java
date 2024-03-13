package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ExtLogEditor extends JPanel {
	private static final Dimension MAXIMUM_SIZE = new Dimension(99999, 25);
	private final JCheckBox minimumExtentBox;
	private final JCheckBox maximumExtentBox;
	private final JCheckBox boundsRadiusBox;
	private final FloatEditorJSpinner boundsRadius;
	JPanel minSpinnerPanel;
	JPanel maxSpinnerPanel;
	Vec3SpinnerArray minimumExtentV;
	Vec3SpinnerArray maximumExtentV;

	private Vec3 minExt;
	private Vec3 maxExt;
	private double boundsRadValue;

	private boolean hasMinimumExtent = true;
	private boolean hasMaximumExtent = true;
	private boolean hasBoundsRadius = true;

	Consumer<ExtLog> extLogConsumer;

	public ExtLogEditor() {
		super(new MigLayout("fill"));

		minimumExtentV = new Vec3SpinnerArray().setVec3Consumer(this::setMinExtValue);
		minimumExtentBox = CheckBox.create("Minimum Extent", this::setHasMinExtValue);

		maximumExtentV = new Vec3SpinnerArray().setVec3Consumer(this::setMaxExtValue);
		maximumExtentBox = CheckBox.create("Maximum Extent", this::setHasMaxExtValue);

		boundsRadius = new FloatEditorJSpinner(0.0f, (float) Integer.MIN_VALUE, (float) Integer.MAX_VALUE, this::setBoundsRadius);
		boundsRadius.setMaximumSize(MAXIMUM_SIZE);
		boundsRadiusBox = CheckBox.create("Bounds Radius", this::setHasBoundsRadius);

		minSpinnerPanel = minimumExtentV.spinnerPanel();
		maxSpinnerPanel = maximumExtentV.spinnerPanel();

		add(minimumExtentBox, "wrap");
		add(minSpinnerPanel, "wrap");
		add(maximumExtentBox, "wrap");
		add(maxSpinnerPanel, "wrap");
		add(boundsRadiusBox, "wrap");
		add(boundsRadius, "wrap");
	}

	private void setMinExtValue(Vec3 vec3Value) {
		if (!minExt.equalLocs(vec3Value)) {
			minExt = vec3Value;
			minimumExtentV.setEnabled(hasMinimumExtent);
			runExtLogConsumer();
		}
	}
	private void setHasMinExtValue(boolean has) {
		if (hasMinimumExtent != has) {
			hasMinimumExtent = has;
			minimumExtentV.setEnabled(has);
			runExtLogConsumer();
		}
	}

	private void setMaxExtValue(Vec3 vec3Value) {
		if (!maxExt.equalLocs(vec3Value)) {
			maxExt = vec3Value;
			maximumExtentV.setEnabled(hasMaximumExtent);
			runExtLogConsumer();
		}
	}
	private void setHasMaxExtValue(boolean has) {
		if (hasMaximumExtent != has) {
			hasMaximumExtent = has;
			maximumExtentV.setEnabled(has);
			runExtLogConsumer();
		}
	}

	private void setBoundsRadius(float value) {
		if (boundsRadValue != value) {
			boundsRadValue = value;
			boundsRadius.setEnabled(hasBoundsRadius);
			runExtLogConsumer();
		}
	}
	private void setHasBoundsRadius(boolean has) {
		if (hasBoundsRadius != has) {
			hasBoundsRadius = has;
			boundsRadius.setEnabled(has);
			runExtLogConsumer();
		}
	}

	public ExtLogEditor addExtLogConsumer(Consumer<ExtLog> extLogConsumer) {
		this.extLogConsumer = extLogConsumer;
		return this;
	}

	public void setExtLog(ExtLog extents) {
		if (extents != null) {
			minExt = new Vec3(extents.getMinimumExtent());
			hasMinimumExtent = extents.hasMinimumExtent();
			minimumExtentV.setValues(minExt);
			minimumExtentBox.setSelected(hasMinimumExtent);
			minimumExtentV.setEnabled(hasMinimumExtent);

			maxExt = new Vec3(extents.getMaximumExtent());
			hasMaximumExtent = extents.hasMaximumExtent();
			maximumExtentV.setValues(maxExt);
			maximumExtentBox.setSelected(hasMaximumExtent);
			maximumExtentV.setEnabled(hasMaximumExtent);


			boundsRadValue = extents.getBoundsRadius();
			hasBoundsRadius = extents.hasBoundsRadius();
			boundsRadius.reloadNewValue(boundsRadValue);
			boundsRadiusBox.setSelected(hasBoundsRadius);
			boundsRadius.setEnabled(hasBoundsRadius);
		} else {
			boundsRadius.setEnabled(false);
			boundsRadiusBox.setSelected(false);
			minimumExtentBox.setEnabled(false);
			minimumExtentV.setEnabled(false);
			maximumExtentBox.setEnabled(false);
			maximumExtentV.setEnabled(false);
		}
	}

	private void runExtLogConsumer() {
		if (extLogConsumer != null) {
			extLogConsumer.accept(getExtLog());
		}
	}


	public ExtLog getExtLog() {
		return new ExtLog(minExt, maxExt, boundsRadValue)
				.setHasMaximumExtent(hasMaximumExtent)
				.setHasMinimumExtent(hasMinimumExtent)
				.setHasBoundsRadius(hasBoundsRadius);
	}
}
