package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ComponentNodePanel extends JPanel implements ComponentPanel<AnimatedNode> {
	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoActionListener;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final Map<AnimatedNode, ComponentGeosetMaterialPanel> nodePanels;
	private final boolean listenersEnabled = true;
	//	private final JLabel trisLabel;
//	private final JLabel vertLabel;
	private final JPanel nodePanelHolder;
	private ComponentGeosetMaterialPanel nodePanel;


	public ComponentNodePanel(final ModelViewManager modelViewManager,
	                          final UndoActionListener undoActionListener,
	                          final ModelStructureChangeListener modelStructureChangeListener) {
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
		setLayout(new MigLayout("fill", "[][][grow]", "[][][grow]"));

		nodePanels = new HashMap<>();

		nodePanelHolder = new JPanel(new MigLayout());
		add(nodePanelHolder, "wrap, growx, span 3");

		nodePanelHolder.add(new JLabel("Node"), "wrap");
//		nodePanel = new ComponentGeosetMaterialPanel();
//		nodePanelHolder.add(nodePanel);

//		JPanel geosetInfoPanel = new JPanel(new MigLayout());
//		add(geosetInfoPanel, "wrap, growx, span 3");

//		geosetInfoPanel.add(new JLabel("Triangles: "));
//		trisLabel = new JLabel("0");
//		geosetInfoPanel.add(trisLabel, "wrap");
//
//		geosetInfoPanel.add(new JLabel("Vertices: "));
//		vertLabel = new JLabel("0");
//		geosetInfoPanel.add(vertLabel, "wrap");

	}

	@Override
	public void setSelectedItem(final AnimatedNode node) {

//		nodePanelHolder.remove(nodePanel);
//
//		nodePanels.putIfAbsent(node, new ComponentGeosetMaterialPanel());
//		nodePanel = nodePanels.get(node);

//		nodePanel.setMaterialChooser(node, modelViewManager, undoActionListener, modelStructureChangeListener);
//		nodePanelHolder.add(nodePanel);
		nodePanelHolder.revalidate();
		nodePanelHolder.repaint();

		revalidate();
		repaint();
	}


	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {
	}

}
