package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec3SpinnerArray;
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

	Consumer<ExtLog> extLogConsumer;

	public ExtLogEditor() {
		super(new MigLayout("fill"));

		minimumExtentV = new Vec3SpinnerArray().setVec3Consumer(this::setMinExtValue);
		minimumExtentBox = new JCheckBox("Minimum Extent");
		minimumExtentBox.addActionListener(e -> setMinExtValue(minimumExtentBox.isSelected() ? minimumExtentV.getValue() : null));

		maximumExtentV = new Vec3SpinnerArray().setVec3Consumer(this::setMaxExtValue);
		maximumExtentBox = new JCheckBox("Maximum Extent");
		maximumExtentBox.addActionListener(e -> setMaxExtValue(maximumExtentBox.isSelected() ? maximumExtentV.getValue() : null));

		boundsRadius = new FloatEditorJSpinner(0.0f, (float) Integer.MIN_VALUE, this::setBoundsRadius);
		boundsRadius.setMaximumSize(MAXIMUM_SIZE);
		boundsRadiusBox = new JCheckBox("Bounds Radius");
		boundsRadiusBox.addActionListener(e -> setBoundsRadius(boundsRadiusBox.isSelected() ? (float) ExtLog.NO_BOUNDS_RADIUS : boundsRadius.getFloatValue()));

		minSpinnerPanel = minimumExtentV.spinnerPanel();
		maxSpinnerPanel = maximumExtentV.spinnerPanel();

		add(minimumExtentBox, "wrap");
		add(minSpinnerPanel, "wrap");
		add(maximumExtentBox, "wrap");
		add(maxSpinnerPanel, "wrap");
		add(boundsRadiusBox, "wrap");
		add(boundsRadius, "wrap");
	}

	private void setMinExtValue(Vec3 vec3Value){
		if(!minExt.equalLocs(vec3Value)){
			minExt = vec3Value;
			minimumExtentV.setEnabled(minExt != null);
			runExtLogConsumer();
		}
	}
	private void setMaxExtValue(Vec3 vec3Value){
		if(!maxExt.equalLocs(vec3Value)){
			maxExt = vec3Value;
			maximumExtentV.setEnabled(maxExt != null);
			runExtLogConsumer();
		}
	}

	private void setBoundsRadius(float value){
		if(boundsRadValue != value) {
			boundsRadValue = value;
			boundsRadius.setEnabled(boundsRadiusBox.isSelected());
			runExtLogConsumer();
		}
	}

	public ExtLogEditor addExtLogConsumer(Consumer<ExtLog> extLogConsumer){
		this.extLogConsumer = extLogConsumer;
		return this;
	}

	public void setExtLog(ExtLog extents) {
		if(extents != null){
			minExt = extents.getMinimumExtent() == null ? null : new Vec3(extents.getMinimumExtent());
			if (minExt != null) {
				minimumExtentV.setValues(minExt);
			}

			maxExt = extents.getMaximumExtent() == null ? null : new Vec3(extents.getMaximumExtent());
			if (maxExt != null) {
				maximumExtentV.setValues(maxExt);
			}

			boundsRadiusBox.setSelected(extents.hasBoundsRadius());
			boundsRadius.setEnabled(extents.hasBoundsRadius());
			if (extents.hasBoundsRadius()) {
				boundsRadValue = extents.getBoundsRadius();
//				boundsRadius.reloadNewValue(extents.getBoundsRadius());
				boundsRadius.reloadNewValue(boundsRadValue);
			}
		} else {
			boundsRadius.setEnabled(false);
			boundsRadiusBox.setSelected(false);
		}

		minimumExtentV.setEnabled(minExt != null);
		minimumExtentBox.setSelected(minExt != null);
		maximumExtentV.setEnabled(maxExt != null);
		maximumExtentBox.setSelected(maxExt != null);
	}

	private void runExtLogConsumer(){
		if(extLogConsumer != null){
			extLogConsumer.accept(getExtLog());
		}
	}


	public ExtLog getExtLog() {
		return new ExtLog(minExt, maxExt, boundsRadValue);
	}
}
