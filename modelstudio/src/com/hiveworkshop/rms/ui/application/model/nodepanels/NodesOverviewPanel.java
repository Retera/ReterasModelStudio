package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.model.OverviewPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class NodesOverviewPanel extends OverviewPanel {

	public NodesOverviewPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("wrap 2", "[]20[Right]", ""));
		fillPanel();
	}

	private void fillPanel() {
		EditableModel model = modelHandler.getModel();

		add(new JLabel("Node type"));
		add(new JLabel("count"));
//		add(new JLabel("" + model.getIdObjectsSize()));

		addIfNotNull("Bones", model.getBones().size());
		addIfNotNull("Helpers", model.getHeader().size());
		addIfNotNull("Lights", model.getLights().size());
		addIfNotNull("Attachments", model.getAttachments().size());
		addIfNotNull("ParticleEmitters", model.getParticleEmitters().size());
		addIfNotNull("ParticleEmitter2s", model.getParticleEmitter2s().size());
		addIfNotNull("PopcornEmitters", model.getPopcornEmitters().size());
		addIfNotNull("RibbonEmitters", model.getRibbonEmitters().size());
		addIfNotNull("EventObjects", model.getEvents().size());
		addIfNotNull("CollisionShapes", model.getColliders().size());

		add(new JLabel("Total"), "gapy 10");
		add(new JLabel("" + model.getIdObjectsSize()));
	}

	private void addIfNotNull(String type, int size) {
		if (size > 0) {
			add(new JLabel(type));
			add(new JLabel("" + size));
		}
	}

	@Override
	public void update() {
		removeAll();
		fillPanel();
		revalidate();
		repaint();
	}
}
