package com.hiveworkshop.wc3.gui.modeledit.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetMaterialPriorityPlaneAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.material.ComponentMaterialLayersPanel;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

import net.miginfocom.swing.MigLayout;

public class ComponentMaterialPanel extends JPanel implements ComponentPanel {
	private static final String SD = "SD";
	private static final String HD = "HD";
	private Material material;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;

	private final ComponentEditorJSpinner priorityPlaneSpinner;
	private boolean listenForChanges = true;
	private final ComponentMaterialLayersPanel multipleLayersPanel;

	public ComponentMaterialPanel() {

		priorityPlaneSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		priorityPlaneSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetMaterialPriorityPlaneAction setMaterialPriorityPlaneAction = new SetMaterialPriorityPlaneAction(
						material, material.getPriorityPlane(), ((Number) priorityPlaneSpinner.getValue()).intValue(),
						modelStructureChangeListener);
				setMaterialPriorityPlaneAction.redo();
				undoActionListener.pushAction(setMaterialPriorityPlaneAction);
			}
		});

		multipleLayersPanel = new ComponentMaterialLayersPanel();

		setLayout(new MigLayout("fill", "[][grow][grow]", "[][][grow]"));
		add(new JLabel("Priority Plane:"));
		add(priorityPlaneSpinner, "wrap, growx, span 2");
		add(multipleLayersPanel, "growx, growy, span 3");
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;

		listenForChanges = false;
		try {
			priorityPlaneSpinner.reloadNewValue(material.getPriorityPlane());
		}
		finally {
			listenForChanges = true;
		}
		multipleLayersPanel.setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
			final ModelStructureChangeListener changeListener) {
	}

}
