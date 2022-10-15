package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialFlagAction;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.LinkedHashMap;

public class MaterialFlagsPanel extends JPanel {

	private final ModelHandler modelHandler;
	private Material material;
	private final LinkedHashMap<Material.flag, JCheckBox> checkboxes = new LinkedHashMap<>();

	public MaterialFlagsPanel(ModelHandler modelHandler) {
		super(new MigLayout("ins 3, gap 3, fill", "", ""));
		setBorder(BorderFactory.createTitledBorder("Material Flags"));
		this.modelHandler = modelHandler;


		for (Material.flag flag : Material.flag.values()){
			JCheckBox checkBox = new JCheckBox(flag.getName());
			checkBox.addActionListener(e -> toggleFlag(flag, checkBox.isSelected()));
			add(checkBox, "");
			checkboxes.put(flag, checkBox);
		}
	}

	public MaterialFlagsPanel setMaterial(Material material) {
		this.material = material;
		for(Material.flag flag : checkboxes.keySet()){
			checkboxes.get(flag).setSelected(material.isFlagSet(flag));
		}
		return this;
	}

	private void toggleFlag(Material.flag flag, boolean selected) {
		if (material != null && material.isFlagSet(flag) != selected) {
			modelHandler.getUndoManager().pushAction(new SetMaterialFlagAction(material, flag, selected, ModelStructureChangeListener.changeListener).redo());
		}
	}
}
