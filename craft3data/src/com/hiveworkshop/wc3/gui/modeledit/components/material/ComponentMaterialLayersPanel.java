package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.AddLayerAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.MoveLayerAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.RemoveLayerAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentNavigationListener;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

import net.miginfocom.swing.MigLayout;

/**
 * The stack of layer editors inside the material card, with Add / Delete /
 * Move Up / Move Down. Layer editors are cached and reused because they are
 * expensive to build.
 */
public class ComponentMaterialLayersPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = { "Diffuse", "Normal", "ORM", "Emissive", "Team Color",
			"Reflections" };
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private Material material;
	private ModelViewManager modelViewManager;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private final JButton addLayerButton;
	private final List<ComponentLayerPanel> cachedLayerPanels = new ArrayList<>();
	private final List<LayerHeader> cachedHeaders = new ArrayList<>();
	private int currentlyDisplayedLayerCount = -1;
	private ModelComponentNavigationListener navigationListener = ModelComponentNavigationListener.NONE;

	private final class LayerHeader extends JPanel {
		private final JLabel label = new JLabel("Layer");
		private final JButton up = new JButton("▲");
		private final JButton down = new JButton("▼");
		private final JButton delete = new JButton("Delete");
		private int index;

		LayerHeader() {
			setLayout(new MigLayout("insets 0, fillx", "[grow][][][]", ""));
			delete.setBackground(Color.RED);
			delete.setForeground(Color.WHITE);
			up.setToolTipText("Draw earlier (move up)");
			down.setToolTipText("Draw later (move down)");
			add(label, "growx");
			add(up);
			add(down);
			add(delete);
			up.addActionListener(e -> moveLayer(index, index - 1));
			down.addActionListener(e -> moveLayer(index, index + 1));
			delete.addActionListener(e -> deleteLayer(index));
		}

		void bind(final int index, final int count, final String text, final boolean bold) {
			this.index = index;
			label.setText(text);
			label.setFont(label.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN));
			up.setEnabled(index > 0);
			down.setEnabled(index < (count - 1));
			delete.setEnabled(count > 1);
		}
	}

	public ComponentMaterialLayersPanel() {
		setLayout(new MigLayout("fillx", "[grow]", ""));
		addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
		addLayerButton.addActionListener(e -> addLayer());
	}

	public void setNavigationListener(final ModelComponentNavigationListener navigationListener) {
		this.navigationListener = navigationListener == null ? ModelComponentNavigationListener.NONE
				: navigationListener;
		for (final ComponentLayerPanel panel : cachedLayerPanels) {
			panel.setNavigationListener(navigationListener);
		}
	}

	private void addLayer() {
		if (material == null) {
			return;
		}
		final List<Bitmap> textures = modelViewManager.getModel().getTextures();
		if (textures.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Add a texture to the model first; a layer needs one.", "Add Layer",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final Layer layer = new Layer("None", textures.get(0));
		final AddLayerAction action = new AddLayerAction(material, layer, material.getLayers().size(),
				modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
	}

	private void deleteLayer(final int index) {
		if ((material == null) || (index < 0) || (index >= material.getLayers().size())) {
			return;
		}
		if (material.getLayers().size() <= 1) {
			JOptionPane.showMessageDialog(this, "A material needs at least one layer.", "Delete Layer",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final RemoveLayerAction action = new RemoveLayerAction(material, material.getLayers().get(index),
				modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
	}

	private void moveLayer(final int from, final int to) {
		if ((material == null) || (to < 0) || (to >= material.getLayers().size())) {
			return;
		}
		final MoveLayerAction action = new MoveLayerAction(material, material.getLayers().get(from), to,
				modelStructureChangeListener);
		action.redo();
		undoActionListener.pushAction(action);
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		final boolean hdShader = false;// Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());
		final int count = material.getLayers().size();

		final boolean rebuild = currentlyDisplayedLayerCount != count;
		if (rebuild) {
			removeAll();
		}
		for (int i = 0; i < count; i++) {
			final Layer layer = material.getLayers().get(i);
			final ComponentLayerPanel panel;
			final LayerHeader header;
			if (i < cachedLayerPanels.size()) {
				panel = cachedLayerPanels.get(i);
				header = cachedHeaders.get(i);
			} else {
				panel = new ComponentLayerPanel();
				panel.setNavigationListener(navigationListener);
				header = new LayerHeader();
				cachedLayerPanels.add(panel);
				cachedHeaders.add(header);
			}
			final String text;
			if (hdShader) {
				text = (i < REFORGED_LAYER_DEFINITIONS.length ? REFORGED_LAYER_DEFINITIONS[i] : "Unknown") + " Layer";
			} else {
				text = "Layer " + (i + 1);
			}
			header.bind(i, count, text, hdShader);
			panel.setLayer(modelViewManager.getModel().getWrappedDataSource(), layer,
					modelViewManager.getModel().getFormatVersion(), undoActionListener, modelStructureChangeListener,
					modelViewManager);
			if (rebuild) {
				add(header, "growx, wrap");
				add(panel, "growx, wrap");
			}
		}
		if (rebuild) {
			add(addLayerButton, "wrap");
			currentlyDisplayedLayerCount = count;
			revalidate();
			repaint();
		}
	}
}
