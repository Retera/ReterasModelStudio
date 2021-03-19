package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentParticle2Panel extends JPanel implements ComponentPanel<ParticleEmitter2> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	ParticleEmitter2 particleEmitter2;

	JLabel title;
	JLabel parentName;


	public ComponentParticle2Panel(final ModelViewManager modelViewManager,
	                               final UndoActionListener undoActionListener,
	                               final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
		setLayout(new MigLayout("fill, gap 0", "[][][grow]", "[][][grow]"));
		title = new JLabel("Select an Emitter");
		add(title, "wrap");
		add(new JLabel("Parent: "));
		parentName = new JLabel("Parent");
		add(parentName, "wrap");

	}

	@Override
	public void setSelectedItem(ParticleEmitter2 itemToSelect) {
		particleEmitter2 = itemToSelect;
		title.setText(particleEmitter2.getName());
		IdObject parent = particleEmitter2.getParent();
		if (parent != null) {
			this.parentName.setText(parent.getName());
		} else {
			parentName.setText("no parent");
		}
		revalidate();
		repaint();

	}

	@Override
	public void save(EditableModel model, UndoActionListener undoListener, ModelStructureChangeListener changeListener) {

	}
}
