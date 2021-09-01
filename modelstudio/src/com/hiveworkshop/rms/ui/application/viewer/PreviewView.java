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
	private JPanel dudPanel;

	public PreviewView() {
		super("Preview", null, new JPanel());
//		scrollPane = new JScrollPane(new JPanel());
		dudPanel = new JPanel(new MigLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dudPanel, getSpecialPane());
//		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());
		previewPanel = new PreviewPanel();
		animationController = new AnimationController(previewPanel);
		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);
	}

	@Override
	public PreviewView setModelPanel(ModelPanel modelPanel) {
//		System.out.println("update PreviewView!");
		if (modelPanel == null) {
			splitPane.setLeftComponent(dudPanel);
			scrollPane.setViewportView(new JPanel());
			previewPanel.setModel(null, false, null);
			animationController.setModel(null, true, null);
//			splitPane.setRightComponent(new JPanel());
		} else {
//			System.out.println("update PreviewView! " + modelPanel.getModel().getName());
			previewPanel.setModel(modelPanel.getModelHandler(), true, modelPanel.getViewportActivityManager());
			animationController.setModel(modelPanel.getModelHandler(), true, previewPanel.getCurrentAnimation());
			splitPane.setLeftComponent(previewPanel);
			scrollPane.setViewportView(animationController);
		}
		splitPane.setDividerLocation(0.8);
		reload();
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
