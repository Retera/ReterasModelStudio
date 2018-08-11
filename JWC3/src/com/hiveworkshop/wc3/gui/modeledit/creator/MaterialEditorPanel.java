package com.hiveworkshop.wc3.gui.modeledit.creator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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
