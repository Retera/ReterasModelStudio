package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;

public class ComponentGeosetPanel extends JPanel implements ComponentPanel<Geoset> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private ComponentGeosetMaterialPanel materialPanel;
	private final Map<Geoset, ComponentGeosetMaterialPanel> materialPanels;
	private final JLabel trisLabel;
	private final JLabel vertLabel;
	JPanel hdPanel;
	JSpinner lodSpinner;
	JTextField nameTextField;
	//	private final JLabel selectionGroupLabel;
	private JSpinner selectionGroupSpinner;
	private Geoset geoset;

	private final boolean listenersEnabled = true;
	private final JPanel materialPanelHolder;


	public ComponentGeosetPanel(final ModelViewManager modelViewManager,
	                            final UndoActionListener undoActionListener,
	                            final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));

		materialPanels = new HashMap<>();

		materialPanelHolder = new JPanel(new MigLayout("hidemode 1"));
		add(materialPanelHolder, "wrap, growx, span 3");

		materialPanelHolder.add(new JLabel("Material:"), "wrap");
		materialPanel = new ComponentGeosetMaterialPanel();
		materialPanelHolder.add(materialPanel);

		JPanel geosetInfoPanel = new JPanel(new MigLayout("fill, hidemode 1"));
		add(geosetInfoPanel, "wrap, growx, span 2");

		geosetInfoPanel.add(new JLabel("Triangles: "));
		trisLabel = new JLabel("0");
		geosetInfoPanel.add(trisLabel, "wrap");

		geosetInfoPanel.add(new JLabel("Vertices: "));
		vertLabel = new JLabel("0");
		geosetInfoPanel.add(vertLabel, "wrap");

		geosetInfoPanel.add(new JLabel("SelectionGroup: "));
		selectionGroupSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		selectionGroupSpinner.addChangeListener(e -> setSelectionGroup());
		geosetInfoPanel.add(selectionGroupSpinner, "wrap");
//		selectionGroupLabel = new JLabel("0");

		hdPanel = new JPanel(new MigLayout("fill, novisualpadding"));
		hdPanel.add(new JLabel("LevelOfDetail: "));
		lodSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		hdPanel.add(lodSpinner, "wrap");
		lodSpinner.addChangeListener(e -> setLoD());
		hdPanel.add(new JLabel("Name: "));
		nameTextField = new JTextField();
		nameTextField.addFocusListener(setLoDName());
		hdPanel.add(nameTextField, "wrap");
		hdPanel.setVisible(modelViewManager.getModel().getFormatVersion() == 1000);
		geosetInfoPanel.add(hdPanel, "wrap, growx, span 2");
		JButton editUvButton = new JButton("Edit Geoset UVs");

	}


	@Override
	public void setSelectedItem(final Geoset geoset) {
		this.geoset = geoset;
		materialPanelHolder.remove(materialPanel);

		materialPanels.putIfAbsent(geoset, new ComponentGeosetMaterialPanel());
		materialPanel = materialPanels.get(geoset);

		materialPanel.setMaterialChooser(geoset, modelViewManager, undoActionListener, modelStructureChangeListener);
		materialPanelHolder.add(materialPanel);
		materialPanelHolder.revalidate();
		materialPanelHolder.repaint();

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());

		selectionGroupSpinner.setValue(geoset.getSelectionGroup());
		lodSpinner.setValue(geoset.getLevelOfDetail());
		nameTextField.setText(geoset.getLevelOfDetailName());

		hdPanel.setVisible(modelViewManager.getModel().getFormatVersion() == 1000);

		revalidate();
		repaint();
	}


	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {
	}


	private void setSelectionGroup() {
		geoset.setSelectionGroup((Integer) selectionGroupSpinner.getValue());
	}

	private void setLoD() {
		geoset.setLevelOfDetail((Integer) lodSpinner.getValue());
	}

	private FocusAdapter setLoDName() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				geoset.setLevelOfDetailName(nameTextField.getText());
			}
		};
	}

	private void editUVs() {

	}

}
