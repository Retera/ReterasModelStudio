package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.model.material.ComponentHDLayersPanel;
import com.hiveworkshop.rms.ui.application.model.material.ComponentSDLayersPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

public class ComponentMaterialPanel extends ComponentPanel<Material> {
	private ComponentSDLayersPanel SD;
	private ComponentHDLayersPanel HD;

	public ComponentMaterialPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, gap 0, ins 0, hidemode 2", "[grow]", "[grow]"));

//		System.out.println("ComponentMaterialPanel2: creating SD-panel");
		SD = new ComponentSDLayersPanel(modelHandler);
//		System.out.println("ComponentMaterialPanel2: creating HD-panel");
		HD = new ComponentHDLayersPanel(modelHandler);
		SD.setVisible(false);
		HD.setVisible(false);

//		System.out.println("ComponentMaterialPanel2: adding panels");
		add(SD, "growx, growy, wrap");
		add(HD, "growx, growy, wrap");
	}

	@Override
	public void setSelectedItem(final Material material) {
		this.selectedItem = material;

		if (ModelUtils.isShaderStringSupported(model.getFormatVersion()) && material.isHD()) {
			System.out.println("setting HD Material");
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
	}

}
