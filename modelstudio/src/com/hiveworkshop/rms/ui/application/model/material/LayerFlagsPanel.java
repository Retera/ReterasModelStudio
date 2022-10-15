package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFlagAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.LinkedHashMap;

public class LayerFlagsPanel extends JPanel {

	private final ModelHandler modelHandler;
	private Layer layer;
	private final LinkedHashMap<Layer.flag, JCheckBox> checkboxes = new LinkedHashMap<>();

	public LayerFlagsPanel(ModelHandler modelHandler) {
		super(new MigLayout("ins 3, gap 3, wrap 1", "", ""));
		setBorder(BorderFactory.createTitledBorder("Flags"));
		this.modelHandler = modelHandler;

//		setOpaque(true);
//		setBackground(Color.MAGENTA);

		for (Layer.flag flag : Layer.flag.values()){
			JCheckBox checkBox = new JCheckBox(flag.getName());
			checkBox.addActionListener(e -> toggleFlag(flag, checkBox.isSelected()));
			add(checkBox, "");
			checkboxes.put(flag, checkBox);
		}
	}

	public LayerFlagsPanel setLayer(Layer layer) {
		this.layer = layer;
		for(Layer.flag flag : checkboxes.keySet()){
			checkboxes.get(flag).setSelected(layer.isFlagSet(flag));
		}
		return this;
	}

	private void toggleFlag(Layer.flag flag, boolean selected) {
		if (layer != null && layer.isFlagSet(flag) != selected) {
			modelHandler.getUndoManager().pushAction(new SetLayerFlagAction(layer, flag, selected, ModelStructureChangeListener.changeListener).redo());
		}
	}
}
