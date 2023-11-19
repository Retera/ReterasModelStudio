package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class GeosetVisPanel extends JPanel {
	private final EditableModel model;
	private final UndoManager undoManager;
	private final ModelStructureChangeListener changeListener;
	private Geoset geoset;
	private Material material;

	public GeosetVisPanel(ModelHandler modelHandler) {
		super(new MigLayout("wrap 3", "[sg group1][sg group1, center][sg group1, center]", ""));
		this.model = modelHandler.getModel();
		this.undoManager = modelHandler.getUndoManager();
		this.changeListener = ModelStructureChangeListener.changeListener;
	}

	public GeosetVisPanel setGeoset(Geoset geoset){
		this.geoset = geoset;
		this.material = geoset.getMaterial();
		removeAll();
		setBorder(null);

		Map<Animation, Visibility> geoAnimVisMap = getGeosetAnimVisMap();
		Map<Animation, Visibility> matAnimVisMap = getMaterialAnimVisMap();

		if (!geoAnimVisMap.isEmpty() || !matAnimVisMap.isEmpty()) {
			generatePanel(geoAnimVisMap, matAnimVisMap);
		}
		return this;
	}

	private void generatePanel(Map<Animation, Visibility> geoAnimVisMap, Map<Animation, Visibility> matAnimVisMap) {
		setBorder(BorderFactory.createTitledBorder("Visibility"));
		add(new JLabel("Animation"));
		add(new JLabel("GeosetAnimation"), "");
		add(new JLabel("Material"), "");
		for (Animation animation : model.getAnims()) {
			add(new JLabel(animation.getName()));

			add(getVisPanelComponent(geoAnimVisMap.get(animation), e -> toggleVisibility(animation, geoAnimVisMap.get(animation))));

			JComponent visPanelComponent = getVisPanelComponent(matAnimVisMap.get(animation), null);
			visPanelComponent.setEnabled(false);
			add(visPanelComponent);
		}
	}

	private Map<Animation, Visibility> getGeosetAnimVisMap() {
		Map<Animation, Visibility> geoAnimVisMap = new HashMap<>();
		if (geoset.getVisibilityFlag() != null) {
			for (Animation animation : model.getAnims()) {
				geoAnimVisMap.put(animation, getVis(geoset.getVisibilityFlag().getEntryMap(animation)));
			}
		}
		return geoAnimVisMap;
	}

	private JComponent getVisPanelComponent(Visibility visibility, ActionListener actionListener) {
		if(visibility != null){
			return getVisButton(visibility, actionListener);
		}
		return new JLabel();
	}

	private JButton getVisButton(Visibility visibility, ActionListener actionListener) {
		JButton animButton = new JButton(visibility.toString());
		animButton.addActionListener(actionListener);
		if (visibility == Visibility.VISIBLE) {
//			animButton.setBackground(new Color(120, 150, 255));
			animButton.setBackground(new Color(167, 182, 235));
//			animButton.setForeground(Color.WHITE);
		} else if (visibility == Visibility.ANIMATED || visibility == Visibility.TRANSPARENT) {
			animButton.setBackground(new Color(200, 150, 255));
			animButton.setEnabled(false);
		}
		return animButton;
	}


	private void toggleVisibility(Animation animation, Visibility currVis) {
		AnimFlag<Float> visibilityFlag = geoset.getVisibilityFlag();
		TreeMap<Integer, Entry<Float>> entryMap = visibilityFlag.getEntryMap(animation);
		if (currVis == Visibility.VISIBLE) {
			System.out.println("going invis!");
			UndoAction action;
			if (entryMap == null || entryMap.isEmpty()) {
				Entry<Float> entry = new Entry<>(0, 0f);
				action = new AddFlagEntryAction<>(visibilityFlag, entry, animation, changeListener);
			} else {
				List<Entry<Float>> newEntries = entryMap.values().stream()
						.map(e -> new Entry<>(e.getTime(), 0f)).collect(Collectors.toList());
				action = new SetFlagEntryAction<>(visibilityFlag, newEntries, animation, changeListener);
			}
			undoManager.pushAction(new CompoundAction("Set Invisible", action).redo());
		} else if (currVis == Visibility.INVISIBLE) {
			System.out.println("going vis!");
			if (!entryMap.isEmpty()) {
				List<Entry<Float>> newEntries = entryMap.values().stream()
						.map(e -> new Entry<>(e.getTime(), 1f))
						.collect(Collectors.toList());
				UndoAction action = new SetFlagEntryAction<>(visibilityFlag, newEntries, animation, changeListener);
				undoManager.pushAction(new CompoundAction("Set Visible", action).redo());
			}
		}
	}

	private Map<Animation, Visibility> getMaterialAnimVisMap() {
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
		final String s;
		Visibility(String s){
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}
	}
}
