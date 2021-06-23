package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.nodepanels.*;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.DisplayElementType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ComponentsPanel extends JPanel {
	private static final String BLANK = "BLANK";
	private final CardLayout cardLayout;
	private final Map<DisplayElementType, ComponentPanel<?>> componentPanelMap;
	private final Map<DisplayElementType, JPanel> overviewPanelMap;

	public ComponentsPanel(ModelHandler modelHandler, ModelStructureChangeListener modelStructureChangeListener) {
		componentPanelMap = new HashMap<>();
		overviewPanelMap = new HashMap<>();
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JPanel blankPanel = new JPanel();
		blankPanel.add(new JLabel("Select a model component to get started..."));
		add(blankPanel, BLANK);

		addPanelToMap(new ComponentHeaderPanel(modelHandler, modelStructureChangeListener), DisplayElementType.MODEL_ROOT);
		addPanelToMap(new ComponentHeaderPanel(modelHandler, modelStructureChangeListener), DisplayElementType.HEADER);

		addPanelToMap(new ComponentCommentPanel(modelHandler, modelStructureChangeListener), DisplayElementType.COMMENT);

		addPanelToMap(new ComponentAnimationPanel(modelHandler, modelStructureChangeListener), DisplayElementType.ANIMATION);

		addPanelToMap(new ComponentGlobalSequencePanel(modelHandler, modelStructureChangeListener), DisplayElementType.GLOBAL_SEQ);

		addPanelToMap(new ComponentBitmapPanel(modelHandler, modelStructureChangeListener), DisplayElementType.TEXTURE);

		JPanel editTexturesPanel = new EditTexturesPopupPanel(modelHandler.getModelView(), modelStructureChangeListener);
//		JScrollPane scrollPane = new JScrollPane(editTexturesPanel);
//		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
//		add(scrollPane, DisplayElementType.TEXTURE.getName());
		add(editTexturesPanel, DisplayElementType.TEXTURE.getName());
		overviewPanelMap.put(DisplayElementType.TEXTURE, editTexturesPanel);

		GeosetOverviewPanel geosetOverviewPanel = new GeosetOverviewPanel(modelHandler);
		add(geosetOverviewPanel, DisplayElementType.GEOSET_ITEM.getName());
		overviewPanelMap.put(DisplayElementType.GEOSET_ITEM, geosetOverviewPanel);


		addPanelToMap(new ComponentMaterialPanel(modelHandler, modelStructureChangeListener), DisplayElementType.MATERIAL);

		addPanelToMap(new ComponentGeosetPanel(modelHandler, modelStructureChangeListener), DisplayElementType.GEOSET_ITEM);

		addPanelToMap(new ComponentGeosetAnimPanel(modelHandler, modelStructureChangeListener), DisplayElementType.GEOSET_ANIM);

		addPanelToMap(new ComponentPopcornPanel(modelHandler, modelStructureChangeListener), DisplayElementType.POPCORN);

		addPanelToMap(new ComponentBonePanel(modelHandler, modelStructureChangeListener), DisplayElementType.BONE);

		addPanelToMap(new ComponentHelperPanel(modelHandler, modelStructureChangeListener), DisplayElementType.HELPER);

		addPanelToMap(new ComponentLightPanel(modelHandler, modelStructureChangeListener), DisplayElementType.LIGHT);

		addPanelToMap(new ComponentParticle2Panel(modelHandler, modelStructureChangeListener), DisplayElementType.PARTICLE2);

		addPanelToMap(new ComponentParticlePanel(modelHandler, modelStructureChangeListener), DisplayElementType.PARTICLE);

		addPanelToMap(new ComponentCollisionPanel(modelHandler, modelStructureChangeListener), DisplayElementType.COLLISION_SHAPE);

		addPanelToMap(new ComponentEventPanel(modelHandler, modelStructureChangeListener), DisplayElementType.EVENT_OBJECT);

		addPanelToMap(new ComponentRibbonPanel(modelHandler, modelStructureChangeListener), DisplayElementType.RIBBON);

		addPanelToMap(new ComponentAttatchmentPanel(modelHandler, modelStructureChangeListener), DisplayElementType.ATTACHMENT);

		addPanelToMap(new ComponentFaceEffectPanel(modelHandler, modelStructureChangeListener), DisplayElementType.FACEFX);

		addPanelToMap(new ComponentCameraPanel(modelHandler, modelStructureChangeListener), DisplayElementType.CAMERA);

		cardLayout.show(this, BLANK);
	}

	public void addPanelToMap(ComponentPanel<?> panel, DisplayElementType type) {
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, String.valueOf(type));
		componentPanelMap.put(type, panel);
	}

	public <T> void setSelectedPanel(T selectedItem, DisplayElementType type) {
		if (selectedItem != null && !(selectedItem instanceof String) && componentPanelMap.containsKey(type)) {
			// Typing of this would be nice, if ever possible...
			ComponentPanel<T> componentPanel = (ComponentPanel<T>) componentPanelMap.get(type);
			componentPanel.setSelectedItem(selectedItem);
			cardLayout.show(this, String.valueOf(type));
		} else if (selectedItem instanceof String && overviewPanelMap.containsKey(type)) {
			cardLayout.show(this, type.getName());
		} else {
			System.out.println("selectedItem: " + selectedItem);
			selectedBlank();
		}
	}

	public void selectedBlank() {
		cardLayout.show(this, BLANK);
	}
}
