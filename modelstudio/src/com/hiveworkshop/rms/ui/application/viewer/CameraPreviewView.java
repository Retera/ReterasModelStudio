package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.model.ComponentCameraPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;

public class CameraPreviewView extends ModelDependentView {
	private AnimationController animationController;
	private JScrollPane scrollPane;
	private PreviewPanel previewPanel;
	private JPanel smartPanel;
	private JPanel topLeftPanel;
	private JPanel uggPanel;
	private ComponentCameraPanel cameraPanel;
	private Camera chosenCamera;

	public CameraPreviewView() {
		super("Camera Preview", null, new JPanel());
		smartPanel = getSpecialPane();
		topLeftPanel = new JPanel(new MigLayout("fill", "", ""));
		uggPanel = new JPanel(new MigLayout("fill, ins 0, gap 0","[][]", "[grow][]"));
		previewPanel = new PreviewPanel();
		animationController = new AnimationController(previewPanel);
		this.setComponent(uggPanel);
	}

	@Override
	public CameraPreviewView setModelPanel(ModelPanel modelPanel) {
		uggPanel.removeAll();
		topLeftPanel.removeAll();
		if (modelPanel == null || modelPanel.getModelHandler().getModel().getCameras().isEmpty()) {
			scrollPane.setViewportView(new JPanel());
			previewPanel.setModel(null, false, null);
			animationController.setModel(null, true, null);
		} else {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			previewPanel.setModel(modelHandler, true, modelPanel.getViewportActivityManager());
			animationController.setModel(modelHandler, true, previewPanel.getCurrentAnimation());
			topLeftPanel.add(previewPanel, "wrap");
			topLeftPanel.add(getCameraChooserPanel(modelHandler.getModel(), previewPanel.getPerspectiveViewport().getCameraHandler()), "spanx, growx, wrap");
			cameraPanel = new ComponentCameraPanel(modelHandler);

			chosenCamera = modelHandler.getModel().getCameras().get(0);
			if(chosenCamera != null) {
				cameraPanel.setSelectedItem(chosenCamera);
				previewPanel.getPerspectiveViewport().getCameraHandler().setCamera(chosenCamera);
			}

			uggPanel.add(topLeftPanel, "");
			uggPanel.add(smartPanel, "wrap, growx");
			uggPanel.add(cameraPanel, "spanx, wrap");

			scrollPane.setViewportView(animationController);
		}
		reload();
		return this;
	}

	private JComboBox<Camera> getCameraChooserPanel(EditableModel model, CameraHandler cameraHandler){
		TwiComboBoxModel<Camera> cameraTwiComboBoxModel = new TwiComboBoxModel<>(model.getCameras());
		JComboBox<Camera> cameraJComboBox = new JComboBox<>(cameraTwiComboBoxModel);
		cameraJComboBox.setRenderer(new ListCellRenderer<Camera>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends Camera> list, Camera value, int index, boolean isSelected, boolean cellHasFocus) {
				if(value != null){
					String name = value.getName();
					return new JLabel(name);
				}
				return null;
			}
		});
		cameraJComboBox.addItemListener(e ->  cameraChoosen(e, cameraHandler));
		return cameraJComboBox;
	}

	private void cameraChoosen(ItemEvent e, CameraHandler cameraHandler){
		if(e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Camera){
			chosenCamera = (Camera) e.getItem();
			cameraHandler.setCamera(chosenCamera);
			if (cameraPanel != null){
				cameraPanel.setSelectedItem(chosenCamera);
			}
			System.out.println("camera got choosen!");
		}
	}

	private JPanel getSpecialPane(){
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
		if (previewPanel != null) {
			previewPanel.reload().repaint();
		}
		if (cameraPanel != null && chosenCamera != null){
			cameraPanel.setSelectedItem(chosenCamera);
		}
		return this;
	}
}
