package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;

public class MaterialEditorPanel extends JPanel {
	private ModelView model;

	public MaterialEditorPanel(final ModelView model) {
		this.model = model;
	}

	public void setModel(final ModelView model) {
		this.model = model;
	}

	public void rebuildUI() {
		final DefaultListModel<Material> materials = new DefaultListModel<>();
		for (final Material material : model.getModel().getMaterials()) {
			materials.addElement(material);
		}

		final JList<Material> materialsList = new JList<>();

		materialsList.setCellRenderer(new MaterialListRenderer(model.getModel()));
	}

}
