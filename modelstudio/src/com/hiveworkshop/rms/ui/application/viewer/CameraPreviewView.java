package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.actions.editor.CameraShrinkFattenAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentCameraPanel;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.CameraManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiNumberSlider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;

public class CameraPreviewView extends ModelDependentView {
	private final AnimationController animationController;
	private JScrollPane scrollPane;
	private final ViewportPanel viewportPanel;
	private final JPanel smartPanel;
	private final JPanel topLeftPanel;
	private final JPanel viewMainPanel;
	private ComponentCameraPanel cameraPanel;
	private Camera chosenCamera;
	private RenderModel renderModel;
	private final CameraManager cameraHandler;
	private TwiNumberSlider posSlider;
	private CameraShrinkFattenAction shrinkFattenAction;
	private float lastValue = 0;

	public CameraPreviewView() {
		super("Camera Preview", null, new JPanel());
		smartPanel = getSpecialPane();
		topLeftPanel = new JPanel(new MigLayout("fill", "", ""));
		viewMainPanel = new JPanel(new MigLayout("fill, ins 0, gap 0","[][]", "[grow][]"));
		viewportPanel = new ViewportPanel(false, false, true);
		cameraHandler = viewportPanel.getViewport().getCameraHandler();
		animationController = new AnimationController(viewportPanel.getViewport()::setLevelOfDetail);
		this.setComponent(viewMainPanel);
	}

	@Override
	public CameraPreviewView setModelPanel(ModelPanel modelPanel) {
		viewMainPanel.removeAll();
		topLeftPanel.removeAll();
		if (modelPanel == null || modelPanel.getModelHandler().getModel().getCameras().isEmpty()) {
			scrollPane.setViewportView(new JPanel());
			viewportPanel.setModel(null, null, true);
			animationController.setModel(null, null, true);
			renderModel = null;
		} else {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			renderModel = modelHandler.getPreviewRenderModel();
			viewportPanel.setModel(renderModel, modelPanel.getViewportActivityManager(), true);
			animationController.setModel(renderModel, renderModel.getTimeEnvironment().getCurrentAnimation(), true);
			topLeftPanel.add(viewportPanel, "wrap");
			topLeftPanel.add(getCameraChooserPanel(modelHandler.getModel()), "spanx, growx, wrap");
			cameraPanel = new ComponentCameraPanel(modelHandler, null);

			chosenCamera = modelHandler.getModel().getCameras().get(0);
			posSlider = new TwiNumberSlider(-1000, 1000, 0, true, true);
			cameraPanel.add(posSlider, "cell 2 2");
			if (chosenCamera != null) {
				cameraPanel.setSelectedItem(chosenCamera);
//				cameraHandler.setCamera(renderModel.getRenderNode(chosenCamera.getSourceNode()));
				cameraHandler.setCamera(renderModel, chosenCamera.getSourceNode());
				posSlider.addChangeListener(e -> onShrinkFatten(e, posSlider, chosenCamera.getSourceNode(), modelHandler));
			}

			viewMainPanel.add(topLeftPanel, "");
			viewMainPanel.add(smartPanel, "wrap, growx");
			viewMainPanel.add(cameraPanel, "spanx, wrap");

			scrollPane.setViewportView(animationController);
		}
		reload();
		return this;
	}

	private TwiComboBox<Camera> getCameraChooserPanel(EditableModel model) {
		TwiComboBox<Camera> cameraComboBox = new TwiComboBox<>(model.getCameras());
		cameraComboBox.addOnSelectItemListener(this::setChoosenCamera);
		cameraComboBox.setStringFunctionRender(this::getCameraName);

		return cameraComboBox;
	}

	private String getCameraName(Object object) {
		if (object instanceof Camera camera) {
			return camera.getName();
		}
		return "null";
	}

	private void setChoosenCamera(Camera chosenCamera) {
		this.chosenCamera = chosenCamera;
		cameraHandler.setCamera(renderModel, chosenCamera.getSourceNode());
		if (cameraPanel != null) {
			cameraPanel.setSelectedItem(chosenCamera);
		}
	}

	private JPanel getSpecialPane() {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0, ins 0"));
		scrollPane = new JScrollPane(new JPanel());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				scrollPane.setPreferredSize(panel.getSize());
			}
		});
		panel.add(scrollPane, "growx, growy");
		return panel;
	}

	@Override
	public CameraPreviewView reload() {
		if (animationController != null) {
			animationController.reload().repaint();
		}
		if (viewportPanel != null) {
			viewportPanel.reload().repaint();
		}
		if (cameraPanel != null && chosenCamera != null) {
			cameraPanel.setSelectedItem(chosenCamera);
			cameraPanel.repaint();
		}
		return this;
	}


	private void onShrinkFatten(ChangeEvent e, JSlider slider, CameraNode cameraNode, ModelHandler modelHandler) {
		if (modelHandler != null && cameraNode != null) {
			if (slider.getValueIsAdjusting()) {
				float newValue = getValue(slider.getValue());
				float value = newValue - lastValue;

				if (shrinkFattenAction == null) {
					shrinkFattenAction = new CameraShrinkFattenAction(Collections.singleton(cameraNode), value, false, ModelStructureChangeListener.changeListener);
				} else {
					shrinkFattenAction.updateAmount(value);
				}
				lastValue = newValue;
			} else if (shrinkFattenAction != null) {
				modelHandler.getUndoManager().pushAction(shrinkFattenAction);
				shrinkFattenAction = null;
				lastValue = 0;
				slider.setValue(0);
			}
			renderModel.getRenderNode(cameraNode).setDirty(true).recalculateTransformation();
		}
	}

	private float getValue(int i) {
		float a = .2f;
		float b = 0.1f;
		float c = 0.01f;
		return getValue(i, a, b, c);
	}
	private static float getValue(int i, float a, float b, float c) {
		float x = Math.abs(i/100f);
		float x2 = x*x;
		float x3 = x*x*x;
		return Math.copySign(x*a + x2*b + x3*c, i);
	}
}
