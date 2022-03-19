package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.ui.application.model.material.ComponentTextureAnimPanel;
import com.hiveworkshop.rms.ui.application.model.nodepanels.*;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPanel;
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
	private final ModelHandler modelHandler;

	public ComponentsPanel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		componentPanelMap = new HashMap<>();
		overviewPanelMap = new HashMap<>();
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JPanel blankPanel = new JPanel();
		blankPanel.add(new JLabel("Select a model component to get started..."), "");
		add(new JScrollPane(blankPanel), BLANK);

		addOverviewPanel(DisplayElementType.TEXTURE, new EditTexturesPanel(modelHandler));
		addOverviewPanel(DisplayElementType.GEOSET_ITEM, new GeosetOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.ANIMATION, new AnimationOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.GLOBAL_SEQ, new GlobalSeqOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.NODES, new NodesOverviewPanel(modelHandler));

		cardLayout.show(this, BLANK);
	}

	public void addOverviewPanel(DisplayElementType type, JPanel panel) {
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, type.getName());
		overviewPanelMap.put(type, panel);
	}

	public <T> void setSelectedPanel(T selectedItem, DisplayElementType type) {
		if (selectedItem != null && !(selectedItem instanceof String) && type != null) {
			// Typing of this would be nice, if ever possible...
			ComponentPanel<T> componentPanel = (ComponentPanel<T>) componentPanelMap.computeIfAbsent(type, k -> getAndAddPanel(type, modelHandler));
			if(componentPanel != null){
				componentPanel.setSelectedItem(selectedItem);
				cardLayout.show(this, String.valueOf(type));
			} else {
				selectedBlank();
			}
		} else if (selectedItem instanceof String && overviewPanelMap.containsKey(type)) {
			cardLayout.show(this, type.getName());
		} else {
			selectedBlank();
		}
	}

	public void selectedBlank() {
		cardLayout.show(this, BLANK);
	}

	public ComponentPanel<?> getAndAddPanel(DisplayElementType type, ModelHandler modelHandler) {
		ComponentPanel<?> panel = getComponentPanel(type, modelHandler);
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, String.valueOf(type));

		return panel;
	}

	private JPanel getOverviewPanel(DisplayElementType type, ModelHandler modelHandler){
		return switch (type){
			case DUMMY, MODEL_ROOT, HEADER, COMMENT, DATA, MATERIAL, TEXTURE_ANIM,
					TVERT_ANIM, GEOSET_ANIM, HELPER, RIBBON, EVENT_OBJECT, BINDPOSE,
					GROUP, FACEFX, CAMERA, ATTACHMENT, COLLISION_SHAPE, PARTICLE2,
					PARTICLE, LIGHT, BONE, POPCORN -> null;
			case GLOBAL_SEQ -> new GlobalSeqOverviewPanel(modelHandler);
			case ANIMATION -> new AnimationOverviewPanel(modelHandler);
			case TEXTURE -> new EditTexturesPanel(modelHandler);
			case GEOSET_ITEM -> new GeosetOverviewPanel(modelHandler);
			case NODES -> new NodesOverviewPanel(modelHandler);
		};
	}

	private ComponentPanel<?> getComponentPanel(DisplayElementType type, ModelHandler modelHandler){
		return switch (type){
			case DUMMY -> null;
			case MODEL_ROOT -> new ComponentHeaderPanel(modelHandler);
			case HEADER -> new ComponentHeaderPanel(modelHandler);
			case COMMENT -> new ComponentCommentPanel(modelHandler);
			case DATA -> null;
			case GLOBAL_SEQ -> new ComponentGlobalSequencePanel(modelHandler);
			case ANIMATION -> new ComponentAnimationPanel(modelHandler);
			case TEXTURE -> new ComponentBitmapPanel(modelHandler);
			case MATERIAL -> new ComponentMaterialPanel(modelHandler);
			case TEXTURE_ANIM -> new ComponentTextureAnimPanel(modelHandler);
			case TVERT_ANIM -> new ComponentTextureAnimPanel(modelHandler);
			case GEOSET_ITEM -> new ComponentGeosetPanel(modelHandler);
			case GEOSET_ANIM -> new ComponentGeosetAnimPanel(modelHandler);
			case BONE -> new ComponentBonePanel(modelHandler);
			case HELPER -> new ComponentHelperPanel(modelHandler);
			case LIGHT -> new ComponentLightPanel(modelHandler);
			case PARTICLE -> new ComponentParticlePanel(modelHandler);
			case PARTICLE2 -> new ComponentParticle2Panel(modelHandler);
			case RIBBON -> new ComponentRibbonPanel(modelHandler);
			case POPCORN -> new ComponentPopcornPanel(modelHandler);
			case COLLISION_SHAPE -> new ComponentCollisionPanel(modelHandler);
			case EVENT_OBJECT -> new ComponentEventPanel(modelHandler);
			case ATTACHMENT -> new ComponentAttatchmentPanel(modelHandler);
			case FACEFX -> new ComponentFaceEffectPanel(modelHandler);
			case CAMERA -> new ComponentCameraPanel(modelHandler);
			case BINDPOSE -> null;
			case GROUP -> null;
			case NODES -> null;
		};
	}
}
