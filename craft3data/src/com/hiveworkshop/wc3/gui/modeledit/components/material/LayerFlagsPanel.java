package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.SetComponentPropertyAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Layer;

import net.miginfocom.swing.MigLayout;

/**
 * The layer's boolean flags; each check box is an undoable toggle of the MDL
 * flag string in the layer's flag list.
 */
public class LayerFlagsPanel extends JPanel {
	private static final String[] FLAGS = { "Unshaded", "SphereEnvMap", "TwoSided", "Unfogged", "NoDepthTest",
			"NoDepthSet", "Unlit" };
	private final List<JCheckBox> boxes = new ArrayList<>();
	private Layer layer;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean loading;

	public LayerFlagsPanel() {
		setLayout(new MigLayout("wrap 2"));
		for (final String flag : FLAGS) {
			final JCheckBox box = new JCheckBox(flag);
			box.addActionListener(e -> toggle(flag, box.isSelected()));
			boxes.add(box);
			add(box);
		}
	}

	private void toggle(final String flag, final boolean selected) {
		if (loading || (layer == null)) {
			return;
		}
		final Layer target = layer;
		final ModelStructureChangeListener listener = modelStructureChangeListener;
		final boolean was = target.getFlags().contains(flag);
		if (was == selected) {
			return;
		}
		final SetComponentPropertyAction<Boolean> action = new SetComponentPropertyAction<>("layer " + flag, was,
				selected, value -> {
					target.getFlags().remove(flag);
					if (value) {
						target.getFlags().add(flag);
					}
				}, () -> listener.componentChanged(target));
		action.redo();
		undoActionListener.pushAction(action);
	}

	public void setLayer(final Layer layer, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.layer = layer;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		loading = true;
		try {
			for (int i = 0; i < FLAGS.length; i++) {
				boxes.get(i).setSelected(layer.getFlags().contains(FLAGS[i]));
			}
		} finally {
			loading = false;
		}
	}
}
