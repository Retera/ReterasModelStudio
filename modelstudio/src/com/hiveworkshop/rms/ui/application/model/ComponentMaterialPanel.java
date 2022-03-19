package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.model.material.ComponentHDLayersPanel;
import com.hiveworkshop.rms.ui.application.model.material.ComponentSDLayersPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

public class ComponentMaterialPanel extends ComponentPanel<Material> {
	private final ComponentSDLayersPanel SD;
	private final ComponentHDLayersPanel HD;

	public ComponentMaterialPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, gap 0, ins 0, hidemode 2", "[grow]", "[grow]"));

		SD = new ComponentSDLayersPanel(modelHandler);
		HD = new ComponentHDLayersPanel(modelHandler);
		SD.setVisible(false);
		HD.setVisible(false);

		add(SD, "growx, growy, wrap");
		add(HD, "growx, growy, wrap");
	}

	@Override
	public ComponentPanel<Material> setSelectedItem(final Material material) {
		this.selectedItem = material;

		if (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.isHD()) {
			HD.setSelectedItem(material);
			SD.setVisible(false);
			HD.setVisible(true);
		} else {
			SD.setSelectedItem(material);
			SD.setVisible(true);
			HD.setVisible(false);
		}

		revalidate();
		repaint();
		return this;
	}

}
