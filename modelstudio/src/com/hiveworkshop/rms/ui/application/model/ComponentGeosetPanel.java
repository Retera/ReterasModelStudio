package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.ChangeFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.mesh.ChangeLoDAction;
import com.hiveworkshop.rms.editor.actions.mesh.ChangeLoDNameAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.model.SetGeosetAnimAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.ChangeMaterialAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.MakeModelHD;
import com.hiveworkshop.rms.ui.application.actionfunctions.MakeModelSD;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.application.tools.GeosetAnimCopyPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.*;

public class ComponentGeosetPanel extends ComponentPanel<Geoset> {
	private final JLabel geosetLabel = new JLabel("geoset name");
	private final JLabel trisLabel = new JLabel("0");
	private final JLabel vertLabel = new JLabel("0");
	private JPanel lodPanel;
	private final JPanel hdNamePanel;
	private IntEditorJSpinner lodSpinner;
	private TwiTextField nameTextField;
	private final JButton toggleSdHd;
	private IntEditorJSpinner selectionGroupSpinner;
	private Geoset geoset;

	private JPanel animVisPanel;
	private JPanel animPanel;

	private JComboBox<Material> materialChooser;

	public ComponentGeosetPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("hidemode 1", "[][grow][grow]", "[]"));

		add(geosetLabel, "");
		add(getDeleteButton(e -> removeGeoset()), "skip 1, wrap");
		hdNamePanel = getHdNamePanel();
		add(hdNamePanel, "wrap, growx, spanx");

		JPanel topPanel = new JPanel(new MigLayout("hidemode 1, ins 0", "[][grow][grow]", "[]"));
		add(topPanel, "wrap");
		topPanel.add(getGeosetInfoPanel(), "growx");

		JPanel materialPanelHolder = getMaterialPanelHolder(modelHandler);
		topPanel.add(materialPanelHolder, "wrap, growx, spanx");


		JButton editUvButton = new JButton("Edit Geoset UVs");

		toggleSdHd = new JButton("Make Geoset HD");
		toggleSdHd.addActionListener(e -> toggleSdHd());
		add(toggleSdHd, "wrap");

		animVisPanel = new JPanel();
		add(animVisPanel, "wrap");

		animPanel = new JPanel();
		add(animPanel, "wrap");
	}

	private JPanel getMaterialPanelHolder(ModelHandler modelHandler) {
		JPanel materialPanelHolder = new JPanel(new MigLayout("hidemode 1"));
		materialPanelHolder.add(new JLabel("Material:"), "wrap");

		materialChooser = getMaterialChooser();
		materialChooser.setRenderer(new MaterialListRenderer(modelHandler.getModel()));
		materialChooser.addItemListener(this::changeTexture);
		materialPanelHolder.add(materialChooser, "wrap");

		JButton cloneMaterial = new JButton("Clone This Material");
		cloneMaterial.addActionListener(e -> cloneMaterial());
		materialPanelHolder.add(cloneMaterial);

		return materialPanelHolder;
	}

	private JPanel getGeosetInfoPanel() {
		JPanel geosetInfoPanel = new JPanel(new MigLayout("fill, hidemode 1", "[][][grow][grow]"));

		lodPanel = getLodPanel();
		geosetInfoPanel.add(lodPanel, "growx, spanx, wrap");

		geosetInfoPanel.add(new JLabel("Triangles: "));
		geosetInfoPanel.add(trisLabel, "wrap");

		geosetInfoPanel.add(new JLabel("Vertices: "));
		geosetInfoPanel.add(vertLabel, "wrap");

		geosetInfoPanel.add(new JLabel("SelectionGroup: "));
		selectionGroupSpinner = new IntEditorJSpinner(0, 0, 10000, this::setSelectionGroup);
		geosetInfoPanel.add(selectionGroupSpinner, "wrap");
		return geosetInfoPanel;
	}

	private void changeTexture(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED
				&& materialChooser.getSelectedItem() != null
				&& materialChooser.getSelectedItem() != geoset.getMaterial()) {
			Material itemAt = (Material) materialChooser.getSelectedItem();
			undoManager.pushAction(new ChangeMaterialAction(geoset, itemAt, changeListener).redo());
		}
	}

	private JComboBox<Material> getMaterialChooser() {
		TwiComboBoxModel<Material> bitmapModel = new TwiComboBoxModel<>(modelHandler.getModel().getMaterials());
		return new JComboBox<>(bitmapModel);
	}

	private void cloneMaterial() {
		AddMaterialAction addMaterialAction = new AddMaterialAction(geoset.getMaterial().deepCopy(), modelHandler.getModel(), changeListener);
		undoManager.pushAction(addMaterialAction.redo());
	}

	private JPanel getHdNamePanel() {
		JPanel hdNamePanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		hdNamePanel.add(new JLabel("Name: "));
		nameTextField = new TwiTextField(26, this::setLoDName);
		hdNamePanel.add(nameTextField, "spanx 2, wrap");
		return hdNamePanel;
	}

	private JPanel getLodPanel() {
		JPanel lodPanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		lodPanel.add(new JLabel("LevelOfDetail: "));
		lodSpinner = new IntEditorJSpinner(0, -1, 100, this::setLoD);
		lodPanel.add(lodSpinner, "wrap");
		return lodPanel;
	}


	@Override
	public void setSelectedItem(final Geoset geoset) {
		this.geoset = geoset;
		geosetLabel.setText(geoset.getName());

		setToggleButtonText();
		materialChooser.setSelectedItem(geoset.getMaterial());

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());

		selectionGroupSpinner.reloadNewValue(geoset.getSelectionGroup());
		lodSpinner.reloadNewValue(geoset.getLevelOfDetail());
		nameTextField.setText(geoset.getLevelOfDetailName());

		hdNamePanel.setVisible(modelHandler.getModel().getFormatVersion() == 1000);
		lodPanel.setVisible(modelHandler.getModel().getFormatVersion() == 1000);

		remove(animVisPanel);
		animVisPanel = getVisButtonPanel();
		add(animVisPanel, "wrap");

		remove(animPanel);
		animPanel = getGeosetAnimPanel();
		add(animPanel, "wrap");

		revalidate();
		repaint();
	}

	private void setSelectionGroup(int newGroup) {
		geoset.setSelectionGroup(newGroup);
	}


	private void setLoD(int newLod) {
		if (newLod != geoset.getLevelOfDetail()){
			undoManager.pushAction(new ChangeLoDAction(newLod, geoset, changeListener).redo());
		}
	}

	private void toggleSdHd() {
		if (geoset != null) {
			if (geoset.isHD()) {
				MakeModelSD.makeSd(geoset);
			} else {
				MakeModelHD.makeHd(geoset);
			}
			setToggleButtonText();
		}
	}

	private void setToggleButtonText() {
		toggleSdHd.setVisible(modelHandler.getModel().getFormatVersion() >= 900);
		if (geoset.isHD()) {
			toggleSdHd.setText("Make Geoset SD");
		} else {
			toggleSdHd.setText("Make Geoset HD");
		}
	}

	private void setLoDName(String newName) {
		if (!newName.equals(geoset.getLevelOfDetailName())) {
			undoManager.pushAction(new ChangeLoDNameAction(newName, geoset, changeListener).redo());
		}
	}

	private void editUVs() {

	}

	private JPanel getGeosetAnimPanel() {
		JPanel panel = new JPanel(new MigLayout("fill", "[]", "[][][grow]"));
		GeosetAnim geosetAnim = geoset.getGeosetAnim();

		if (geosetAnim != null) {
			panel.add(new JLabel("GeosetAnim"), "wrap");

			JButton button = new JButton("copy all geosetAnim-info from other");
			button.addActionListener(e -> copyFromOther());
			panel.add(button, "wrap");

			FloatValuePanel alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
			panel.add(alphaPanel, "wrap, span 2");

			ColorValuePanel colorPanel = new ColorValuePanel(modelHandler, MdlUtils.TOKEN_COLOR);
			panel.add(colorPanel, "wrap, span 2");

			alphaPanel.reloadNewValue((float) geosetAnim.getStaticAlpha(), (FloatAnimFlag) geosetAnim.find(MdlUtils.TOKEN_ALPHA), geosetAnim, MdlUtils.TOKEN_ALPHA, geosetAnim::setStaticAlpha);
			colorPanel.reloadNewValue(geosetAnim.getStaticColor(), (Vec3AnimFlag) geosetAnim.find(MdlUtils.TOKEN_COLOR), geosetAnim, MdlUtils.TOKEN_COLOR, geosetAnim::setStaticColor);
		} else {
			JButton addAnim = new JButton("Add GeosetAnim");
			addAnim.addActionListener(e -> undoManager.pushAction(new SetGeosetAnimAction(model, geoset, changeListener).redo()));
			panel.add(addAnim);
		}
		return panel;
	}

	private void copyFromOther() {
		GeosetAnimCopyPanel.show(ProgramGlobals.getMainPanel(), model, geoset.getGeosetAnim(), undoManager);
		repaint();
	}

	private JPanel getVisButtonPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 3", "[sg group1][sg group1, center][sg group1, center]", ""));
		GeosetAnim geosetAnim = geoset.getGeosetAnim();

		Map<Animation, Visibility> geoAnimVisMap = new HashMap<>();
		Map<Animation, Visibility> matAnimVisMap = getMaterialAnimVisMap();
		if (geosetAnim != null && geosetAnim.getVisibilityFlag() != null) {
			for (Animation animation : model.getAnims()) {
				geoAnimVisMap.put(animation, getVis(geosetAnim.getVisibilityFlag().getEntryMap(animation)));
			}
		}
		if (!geoAnimVisMap.isEmpty() || !matAnimVisMap.isEmpty()) {
			panel.setBorder(BorderFactory.createTitledBorder("Visibility"));
			panel.add(new JLabel("Animation"));
			panel.add(new JLabel("GeosetAnimation"), "");
			panel.add(new JLabel("Material"), "");
			for (Animation animation : model.getAnims()) {
				panel.add(new JLabel(animation.getName()));
				if (geoAnimVisMap.containsKey(animation)) {
					JButton animButton = new JButton(geoAnimVisMap.get(animation).toString());
					animButton.addActionListener(e -> toggleVisibility(animation, geoAnimVisMap.get(animation)));
					if (geoAnimVisMap.get(animation) == Visibility.VISIBLE) {
//						animButton.setBackground(new Color(120, 150, 255));
						animButton.setBackground(new Color(167, 182, 235));
//						animButton.setForeground(Color.WHITE);
					} else if (geoAnimVisMap.get(animation) == Visibility.ANIMATED || geoAnimVisMap.get(animation) == Visibility.TRANSPARENT) {
						animButton.setBackground(new Color(200, 150, 255));
						animButton.setEnabled(false);
					}
					panel.add(animButton, "");
				} else {
					panel.add(new JLabel());
				}
				if (matAnimVisMap.containsKey(animation)) {
					JButton animButton = new JButton(matAnimVisMap.get(animation).toString());
					animButton.setEnabled(false);
					if (matAnimVisMap.get(animation) == Visibility.VISIBLE) {
//						animButton.setBackground(new Color(120, 150, 255, 120));
						animButton.setBackground(new Color(167, 182, 235));
					} else if (matAnimVisMap.get(animation) == Visibility.ANIMATED || matAnimVisMap.get(animation) == Visibility.TRANSPARENT) {
						animButton.setBackground(new Color(200, 150, 255));
						animButton.setEnabled(false);
					}
					panel.add(animButton, "");
				} else {
					panel.add(new JLabel());
				}

			}
		}

		return panel;
	}

	private Visibility getVis(TreeMap<Integer, Entry<Float>> entryMap) {
		if (entryMap != null && !entryMap.isEmpty()) {
			float firstValue = entryMap.get(entryMap.firstKey()).getValue();
			Collection<Entry<Float>> visEntries = entryMap.values();
			if (visEntries.stream().allMatch(e -> e.getValue() >= 1)) {
				return Visibility.VISIBLE;
			} else if (visEntries.stream().allMatch(e -> e.getValue() == 0)) {
				return Visibility.INVISIBLE;
			} else if (visEntries.stream().anyMatch(e -> e.getValue() != firstValue && 0.05 < Math.abs(e.getValue() - firstValue))) {
//			} else if (visEntries.stream().anyMatch(e -> e.getValue() != firstValue)) {
				return Visibility.ANIMATED;
			} else {
				return Visibility.TRANSPARENT;
			}
		} else {
			return Visibility.VISIBLE;
		}
	}

	private void toggleVisibility(Animation animation, Visibility currVis) {
		AnimFlag<Float> visibilityFlag = geoset.getGeosetAnim().getVisibilityFlag();
		TreeMap<Integer, Entry<Float>> entryMap = visibilityFlag.getEntryMap(animation);
		if (currVis == Visibility.VISIBLE) {
			System.out.println("going invis!");
			if (entryMap == null || entryMap.isEmpty()) {
				Entry<Float> entry = new Entry<>(0, 0f);
				UndoAction action = new AddFlagEntryAction<>(visibilityFlag, entry, animation, changeListener);
				undoManager.pushAction(action.redo());
			} else {
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : entryMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 0f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, animation, null));
				}
				UndoAction action = new CompoundAction("Set visible", actions, changeListener::geosetsUpdated);
				undoManager.pushAction(action.redo());
			}
		} else if (currVis == Visibility.INVISIBLE) {
			System.out.println("going vis!");
			if (!entryMap.isEmpty()) {
				List<UndoAction> actions = new ArrayList<>();
				for (Entry<Float> entry : entryMap.values()) {
					Entry<Float> newEntry = new Entry<>(entry.getTime(), 1f);
					actions.add(new ChangeFlagEntryAction<>(visibilityFlag, newEntry, entry, animation, null));
				}
				UndoAction action = new CompoundAction("Set invisible", actions, changeListener::geosetsUpdated);
				undoManager.pushAction(action.redo());
			}
		}
	}

	private void removeGeoset() {
		UndoAction action = new DeleteGeosetAction(model, geoset, changeListener);
		undoManager.pushAction(action.redo());
	}

	private Map<Animation, Visibility> getMaterialAnimVisMap() {
		Material material = geoset.getMaterial();
		Map<Animation, Visibility> animVisMap = new HashMap<>();
		if (material != null) {
			for (Animation animation : model.getAnims()) {
				if (material.isHD()) {
					Layer layer = material.getLayer(HD_Material_Layer.DIFFUSE.ordinal());
					if(layer.getVisibilityFlag() != null && layer.getVisibilityFlag().size() != 0){
						Visibility vis = getLayersAnimVis(animation, Collections.singletonList(layer));
						animVisMap.put(animation, vis);
					}
				} else {
					if(material.getLayers().stream().anyMatch(layer -> layer.getVisibilityFlag() != null && layer.getVisibilityFlag().size() != 0)){
						Visibility vis = getLayersAnimVis(animation, material.getLayers());
						animVisMap.put(animation, vis);
					}
				}
			}
		}
		return animVisMap;
	}

	private Visibility getLayersAnimVis(Animation animation, List<Layer> layers) {
		Visibility visibility = Visibility.INVISIBLE;
		for (Layer layer : layers) {
			AnimFlag<Float> animFlag = layer.getVisibilityFlag();
			if(animFlag == null || animFlag.size(animation) == 0){
				Visibility layerVis = getVisibility(layer.getStaticAlpha());
				if(layerVis == Visibility.VISIBLE){
					return Visibility.VISIBLE;
				} else if(visibility.ordinal() < layerVis.ordinal()){
					visibility = layerVis;
				}
			} else {
				TreeMap<Integer, Entry<Float>> entryMap = animFlag.getEntryMap(animation);
				if(!entryMap.isEmpty()) {
					float vis = entryMap.get(entryMap.firstKey()).getValue();
					Visibility firstVis = getVisibility(vis);
					Visibility animVis = firstVis;

					for (Entry<Float> entry : entryMap.values()) {
						animVis = getVisibility(entry.getValue());
						if (animVis != firstVis){
							animVis = Visibility.ANIMATED;
							break;
						} else if (animVis == Visibility.TRANSPARENT){
							float visDiff = Math.abs(entry.getValue() - vis);
							if(0.05 < visDiff){
								animVis = Visibility.ANIMATED;
								break;
							}
						}
					}
					if(animVis == Visibility.VISIBLE){
						return Visibility.VISIBLE;
					} else if(visibility.ordinal() < animVis.ordinal()){
						visibility = animVis;
					}
				} else {
					return Visibility.VISIBLE;
				}
			}
		}
		return visibility;
	}

	private Visibility getVisibility(double vis) {
		Visibility visibility;
		if(vis == 0){
			visibility = Visibility.INVISIBLE;
		} else if (vis >= 1) {
			visibility = Visibility.VISIBLE;
		} else {
			visibility = Visibility.TRANSPARENT;
		}
		return visibility;
	}

	private enum Visibility{
		INVISIBLE("invisible"),
		TRANSPARENT("transparent"),
		ANIMATED("animated"),
		VISIBLE("visible");
		String s;
		Visibility(String s){
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}
}
