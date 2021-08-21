package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PreviewView extends ModelDependentView {
	private JSplitPane splitPane;
	private AnimationController animationController;
	private JScrollPane scrollPane;
	private PreviewPanel previewPanel;
	public PreviewView() {
		super("Preview", null, new JPanel());
//		scrollPane = new JScrollPane(new JPanel());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), getSpecialPane());
//		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());
		previewPanel = new PreviewPanel();
		animationController = new AnimationController(previewPanel);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);
	}

	@Override
	public PreviewView setModelPanel(ModelPanel modelPanel){
		if(modelPanel != null){
//			animationController = modelPanel.getAnimationController();
//			previewPanel = modelPanel.getAnimationViewer();
			previewPanel.setModel(modelPanel.getModelHandler(), true, modelPanel.getViewportActivityManager());
//			animationController = new AnimationController(previewPanel).setModel(modelPanel.getModelHandler(), true, previewPanel.getCurrentAnimation());
			animationController.setModel(modelPanel.getModelHandler(), true, previewPanel.getCurrentAnimation());
			splitPane.setLeftComponent(previewPanel);
			scrollPane.setViewportView(animationController);
//			splitPane.setRightComponent(animationController);
		} else {
			splitPane.setLeftComponent(new JPanel());
			scrollPane.setViewportView(new JPanel());
//			splitPane.setRightComponent(new JPanel());
		}
		splitPane.setDividerLocation(0.8);
		return this;
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
	public PreviewView reload() {
		if (animationController != null) {
			animationController.reload().repaint();
		}
		if (previewPanel != null) {
			previewPanel.reload().repaint();
		}
		return this;
	}
}
