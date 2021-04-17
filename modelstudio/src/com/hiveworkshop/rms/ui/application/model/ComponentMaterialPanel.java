package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.material.SetMaterialPriorityPlaneAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.SetMaterialShaderStringAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorTextField;
import com.hiveworkshop.rms.ui.application.model.material.ComponentMaterialLayersPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

public class ComponentMaterialPanel extends JPanel implements ComponentPanel<Material> {
	private static final String SD = "SD";
	private static final String HD = "HD";
	private Material material;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final ModelViewManager modelViewManager;

	private final JComboBox<String> shaderOptionComboBox;
	private final ComponentEditorJSpinner priorityPlaneSpinner;
	private ComponentEditorTextField comboBoxEditor;
	private boolean listenForChanges = true;
	private final ComponentMaterialLayersPanel multipleLayersPanel;

	public ComponentMaterialPanel(final ModelViewManager modelViewManager,
	                              final UndoActionListener undoActionListener,
	                              final ModelStructureChangeListener modelStructureChangeListener) {
		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;

		shaderOptionComboBox = getShaderComboBox();

		priorityPlaneSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(-1, -1, Integer.MAX_VALUE, 1));
		priorityPlaneSpinner.addActionListener(this::priorityPlaneSpinnerListener);

		multipleLayersPanel = new ComponentMaterialLayersPanel();

		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));
		add(new JLabel("Shader:"));
		add(shaderOptionComboBox, "wrap, growx, span 2");
		add(new JLabel("Priority Plane:"));
		add(priorityPlaneSpinner, "wrap, growx, span 2");
		add(multipleLayersPanel, "growx, growy, span 3");
	}

	@Override
	public void setSelectedItem(final Material material) {
		this.material = material;

		final String shaderString;
		if (material.getShaderString() != null) {
			shaderString = material.getShaderString();
		} else {
			shaderString = "";
		}
		listenForChanges = false;
		try {
			shaderOptionComboBox.setSelectedItem(shaderString);
			comboBoxEditor.setColorToSaved();
			priorityPlaneSpinner.reloadNewValue(material.getPriorityPlane());
		} finally {
			listenForChanges = true;
		}

		multipleLayersPanel.setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	private JComboBox<String> getShaderComboBox() {
		final JComboBox<String> shaderOptionComboBox;
		final String[] shaderOptions = {"", "Shader_SD_FixedFunction", "Shader_HD_DefaultUnit"};
		shaderOptionComboBox = new JComboBox<>(shaderOptions);
		shaderOptionComboBox.setRenderer(ShaderBoxRenderer());
		shaderOptionComboBox.setEditor(ShaderBoxEditor());
		shaderOptionComboBox.setEditable(true);
		shaderOptionComboBox.addActionListener(e -> shaderOptionComboBoxListener());
		return shaderOptionComboBox;
	}

	private void priorityPlaneSpinnerListener() {
		final SetMaterialPriorityPlaneAction setMaterialPriorityPlaneAction = new SetMaterialPriorityPlaneAction(
				material, material.getPriorityPlane(), ((Number) priorityPlaneSpinner.getValue()).intValue(),
				modelStructureChangeListener);
		setMaterialPriorityPlaneAction.redo();
		undoActionListener.pushAction(setMaterialPriorityPlaneAction);
	}

	private void shaderOptionComboBoxListener() {
		if (listenForChanges) {
			final SetMaterialShaderStringAction setMaterialShaderStringAction = new SetMaterialShaderStringAction(
					material, material.getShaderString(), (String) shaderOptionComboBox.getSelectedItem(),
					modelStructureChangeListener);
			setMaterialShaderStringAction.redo();
			undoActionListener.pushAction(setMaterialShaderStringAction);
		}
	}

	private BasicComboBoxRenderer ShaderBoxRenderer() {
		return new BasicComboBoxRenderer() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				if ((getText() == null) || getText().isEmpty()) {
					g.setColor(Color.LIGHT_GRAY);
					g.drawString("<empty>", 0, (getHeight() + g.getFontMetrics().getMaxAscent()) / 2);
				}
			}
		};
	}

	private BasicComboBoxEditor ShaderBoxEditor() {
		return new BasicComboBoxEditor() {
			@Override
			protected JTextField createEditorComponent() {
				final ComponentEditorTextField editor = new ComponentEditorTextField("", 9) {
					@Override
					protected void paintComponent(final Graphics g) {
						super.paintComponent(g);
						if ((getText() == null) || getText().isEmpty()) {
							g.setColor(Color.LIGHT_GRAY);
							g.drawString("<empty>", 0, (getHeight() + g.getFontMetrics().getMaxAscent()) / 2);
						}
					}

					@Override
					public void setText(final String s) {
						if (getText().equals(s)) {
							return;
						}
						super.setText(s);
					}

					@Override
					public void setBorder(final Border b) {
						if (!(b instanceof UIResource)) {
							super.setBorder(b);
						}
					}
				};
				comboBoxEditor = editor;
				editor.setBorder(null);
				return editor;
			}
		};
	}

	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {
	}

}
