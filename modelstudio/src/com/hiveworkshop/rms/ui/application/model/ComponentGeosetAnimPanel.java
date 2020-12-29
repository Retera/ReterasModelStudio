package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


public class ComponentGeosetAnimPanel extends JPanel implements ComponentPanel<GeosetAnim> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final Map<GeosetAnim, ComponentGeosetMaterialPanel> animPanels;
	private final boolean listenersEnabled = true;
	private final JPanel animsPanelHolder;
	private FloatValuePanel alphaPanel;
	private ComponentGeosetMaterialPanel geosetAnimPanel;


	public ComponentGeosetAnimPanel(final ModelViewManager modelViewManager,
	                                final UndoActionListener undoActionListener,
	                                final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));

		animPanels = new HashMap<>();

		animsPanelHolder = new JPanel(new MigLayout());
		add(animsPanelHolder, "wrap, growx, span 3");

		animsPanelHolder.add(new JLabel("GeosetAnim"), "wrap");

		alphaPanel = new FloatValuePanel("Alpha");
		animsPanelHolder.add(alphaPanel, "wrap, span 2");
//		geosetAnimPanel = new ComponentGeosetMaterialPanel();
//		animsPanelHolder.add(geosetAnimPanel);

//		JPanel geosetInfoPanel = new JPanel(new MigLayout());
//		add(geosetInfoPanel, "wrap, growx, span 3");

//		geosetInfoPanel.add(new JLabel("Triangles: "));
//		trisLabel = new JLabel("0");
//		geosetInfoPanel.add(trisLabel, "wrap");

//		geosetInfoPanel.add(new JLabel("Vertices: "));
//		vertLabel = new JLabel("0");
//		geosetInfoPanel.add(vertLabel, "wrap");

	}

	@Override
	public void setSelectedItem(final GeosetAnim geosetAnim) {

//		animsPanelHolder.remove(geosetAnimPanel);

//		animPanels.putIfAbsent(geosetAnim, new ComponentGeosetMaterialPanel());
//		geosetAnimPanel = animPanels.get(geosetAnim);

//		geosetAnimPanel.setMaterialChooser(geosetAnim, modelViewManager, undoActionListener, modelStructureChangeListener);
//		animsPanelHolder.add(geosetAnimPanel);
		animsPanelHolder.revalidate();
		animsPanelHolder.repaint();
		alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), geosetAnim.find("Alpha"), geosetAnim, "Alpha");

		revalidate();
		repaint();
	}


	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {
	}

}
