package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.ui.application.model.material.ComponentTextureAnimPanel;
import com.hiveworkshop.rms.ui.application.model.nodepanels.*;
import com.hiveworkshop.rms.ui.application.tools.EditTexturesPopupPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.DisplayElementType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ComponentsPanel extends JPanel {
	private static final String BLANK = "BLANK";
	private final CardLayout cardLayout;
	private final Map<DisplayElementType, ComponentPanel<?>> componentPanelMap;
	private final Map<DisplayElementType, JPanel> overviewPanelMap;

	public ComponentsPanel(ModelHandler modelHandler) {
		componentPanelMap = new HashMap<>();
		overviewPanelMap = new HashMap<>();
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JPanel blankPanel = new JPanel(new MigLayout("", "[]", "[]"));
		blankPanel.add(new JLabel("Select a model component to get started..."), "");
		add(blankPanel, BLANK);

		addPanelToMap(new ComponentHeaderPanel(modelHandler), DisplayElementType.MODEL_ROOT);
		addPanelToMap(new ComponentHeaderPanel(modelHandler), DisplayElementType.HEADER);
		addPanelToMap(new ComponentCommentPanel(modelHandler), DisplayElementType.COMMENT);

		addPanelToMap(new ComponentGlobalSequencePanel(modelHandler), DisplayElementType.GLOBAL_SEQ);
		addPanelToMap(new ComponentAnimationPanel(modelHandler), DisplayElementType.ANIMATION);

		addPanelToMap(new ComponentBitmapPanel(modelHandler), DisplayElementType.TEXTURE);
		addPanelToMap(new ComponentMaterialPanel(modelHandler), DisplayElementType.MATERIAL);
		addPanelToMap(new ComponentTextureAnimPanel(modelHandler), DisplayElementType.TEXTURE_ANIM);
		addPanelToMap(new ComponentGeosetPanel(modelHandler), DisplayElementType.GEOSET_ITEM);
		addPanelToMap(new ComponentGeosetAnimPanel(modelHandler), DisplayElementType.GEOSET_ANIM);

		addPanelToMap(new ComponentBonePanel(modelHandler), DisplayElementType.BONE);
		addPanelToMap(new ComponentHelperPanel(modelHandler), DisplayElementType.HELPER);
		addPanelToMap(new ComponentLightPanel(modelHandler), DisplayElementType.LIGHT);
		addPanelToMap(new ComponentParticlePanel(modelHandler), DisplayElementType.PARTICLE);
		addPanelToMap(new ComponentParticle2Panel(modelHandler), DisplayElementType.PARTICLE2);
		addPanelToMap(new ComponentRibbonPanel(modelHandler), DisplayElementType.RIBBON);
		addPanelToMap(new ComponentPopcornPanel(modelHandler), DisplayElementType.POPCORN);
		addPanelToMap(new ComponentCollisionPanel(modelHandler), DisplayElementType.COLLISION_SHAPE);
		addPanelToMap(new ComponentEventPanel(modelHandler), DisplayElementType.EVENT_OBJECT);
		addPanelToMap(new ComponentAttatchmentPanel(modelHandler), DisplayElementType.ATTACHMENT);
		addPanelToMap(new ComponentFaceEffectPanel(modelHandler), DisplayElementType.FACEFX);

		addPanelToMap(new ComponentCameraPanel(modelHandler), DisplayElementType.CAMERA);

		addOverviewPanel(DisplayElementType.TEXTURE, new EditTexturesPopupPanel(modelHandler));
		addOverviewPanel(DisplayElementType.GEOSET_ITEM, new GeosetOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.ANIMATION, new AnimationOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.GLOBAL_SEQ, new GlobalSeqOverviewPanel(modelHandler));
		addOverviewPanel(DisplayElementType.NODES, new NodesOverviewPanel(modelHandler));

		cardLayout.show(this, BLANK);
	}

	public void addOverviewPanel(DisplayElementType type, JPanel panel) {
		add(panel, type.getName());
		overviewPanelMap.put(type, panel);
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
