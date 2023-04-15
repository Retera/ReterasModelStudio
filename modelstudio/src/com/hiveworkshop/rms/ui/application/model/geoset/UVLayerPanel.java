package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.AddUVLayerAction;
import com.hiveworkshop.rms.editor.actions.mesh.MoveUVLayerAction;
import com.hiveworkshop.rms.editor.actions.mesh.RemoveUVLayerAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

public class UVLayerPanel extends JPanel {
	Geoset geoset;
	UndoManager undoManager;
	Map<Integer, List<Integer>> uvLayerToLayers = new HashMap<>();

	List<UVLayer> allUvLayers = new ArrayList<>();
	List<UVLayer> uvLayers = new ArrayList<>();
	List<UVLayer> removedUvLayers = new ArrayList<>();
	private int nextIndex;


	public UVLayerPanel(Geoset geoset, UndoManager undoManager){
		super(new MigLayout("", "[sg layerLab][sg matLay][sg mUp][sg mDown][sg add][sg rem]", ""));
		this.geoset = geoset;
		this.undoManager = undoManager;

		for (int i = 0; i < geoset.numUVLayers(); i++) {
			uvLayers.add(new UVLayer(i, null));
		}
		nextIndex = uvLayers.size();

		List<Layer> layers = geoset.getMaterial().getLayers();
		for (int i = 0; i < layers.size(); i++) {
			uvLayerToLayers.computeIfAbsent(layers.get(i).getCoordId(), k -> new ArrayList<>()).add(i);
		}

		remakePanel();
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		add(new JLabel(" "), "wrap");
		revalidate();
		repaint();
	}

	private void remakePanel(){
		removeAll();

		for (int i = 0; i < uvLayers.size(); i++) {
			UVLayer uvLayer = uvLayers.get(i);
			add(new JLabel(uvLayer.tempName));
			add(new JLabel("" + Arrays.toString(uvLayerToLayers.getOrDefault(i, Collections.emptyList()).toArray())));

			JButton move_up = Button.create("^", e -> moveUVLayerC(uvLayer, -1));
			move_up.setEnabled(0 < i && 1 < uvLayers.size());
			add(move_up, "");

			JButton move_down = Button.create("v", e -> moveUVLayerC(uvLayer, 1));
			move_down.setEnabled(i < uvLayers.size()-1);
			add(move_down, "");

			add(Button.create("+", e -> addUVLayerC(uvLayer)), "");
			JButton xButton = getXButton(e -> removeUVLayerC(uvLayer));
			xButton.setEnabled(uvLayers.size() != 1);
			add(xButton, "wrap");
		}
//			add(Button.create("Add layer", e -> addUVLayer()));
		revalidate();
		repaint();
	}

	public void applyEdit() {
		List<UVLayer> remainingOrgLayers = new ArrayList<>();
		List<UVLayer> orderedLayers = new ArrayList<>();

		for (UVLayer uvLayer : uvLayers) {
			if(uvLayer.copiedFrom != null && uvLayer.copiedFrom.isRemoved){
				orderedLayers.add(uvLayer.copiedFrom.setRemoved(false));
				removedUvLayers.remove(uvLayer.copiedFrom);
				remainingOrgLayers.add(uvLayer.copiedFrom);
			} else if (uvLayer.copiedFrom != null) {
				orderedLayers.add(uvLayer);
			} else {
				remainingOrgLayers.add(uvLayer);
				orderedLayers.add(uvLayer);
			}
		}
		List<UndoAction> undoActions = new ArrayList<>();

		removedUvLayers.sort((o1, o2) -> o2.layerId - o1.layerId);
		for (UVLayer layer : removedUvLayers) {
			System.out.println("removing layer: " + layer.layerId);
			undoActions.add(new RemoveUVLayerAction(geoset, layer.layerId, null));
		}

		List<UVLayer> layerOrderAfterAction = new ArrayList<>(remainingOrgLayers);
		layerOrderAfterAction.sort(Comparator.comparingInt(o -> o.layerId));

		for (UVLayer layer : remainingOrgLayers) {
			int destInd = remainingOrgLayers.indexOf(layer);
			int startInd = layerOrderAfterAction.indexOf(layer);
			if (destInd != startInd) {
				System.out.println("Moving layer from: " + startInd + " to " + destInd);
				undoActions.add(new MoveUVLayerAction(geoset, startInd, destInd, null));
				layerOrderAfterAction.remove(layer);
				layerOrderAfterAction.add(destInd, layer);
			}
		}

		for (int i = 0; i < orderedLayers.size(); i++){
			UVLayer uvLayer = orderedLayers.get(i);
			if (uvLayer.copiedFrom != null){
				System.out.println("adding new layer at " + i + " from org layer " + uvLayer.copiedFrom.layerId);
//				undoActions.add(new AddUVLayerAction(geoset, i, layerOrderAfterAction.indexOf(uvLayer.copiedFrom), null));
				undoActions.add(new AddUVLayerAction(geoset, i, uvLayer.copiedFrom.layerId, null));
				layerOrderAfterAction.add(i, uvLayer);
			}
		}
		if(!undoActions.isEmpty()){
			undoManager.pushAction(new CompoundAction("Edit UvLayers for " + geoset.getName(), undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());
		}
	}

	private void addUVLayerC(UVLayer layer){
		int newIndex = uvLayers.indexOf(layer);
		UVLayer uvLayer = new UVLayer(nextIndex++, layer);
		uvLayers.add(newIndex + 1, uvLayer);
		SwingUtilities.invokeLater(this::remakePanel);
	}

	private void moveUVLayerC(UVLayer layer, int dir){
		int i = uvLayers.indexOf(layer);
		int newIndex = i + dir;
		System.out.println("move layer " + i + " -> " + newIndex);
		if (0<=newIndex && newIndex<uvLayers.size()){
			uvLayers.remove(layer);
			uvLayers.add(newIndex, layer);
		}
		SwingUtilities.invokeLater(this::remakePanel);
	}
	private void removeUVLayerC(UVLayer layer){
		if(layer != null){
			layer.setRemoved(true);

			uvLayers.remove(layer);
			if(layer.copiedFrom == null){
				removedUvLayers.add(layer);
			}
		}
		SwingUtilities.invokeLater(this::remakePanel);
	}

	private void addBackLayer(UVLayer layer){

		if(layer != null){
			layer.setRemoved(false);

			uvLayers.add(layer);
			removedUvLayers.remove(layer);
		}
		SwingUtilities.invokeLater(this::remakePanel);
	}

	protected JButton getXButton(ActionListener actionListener) {
		JButton deleteButton = new JButton("X");
		deleteButton.setBackground(Color.RED);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.addActionListener(actionListener);
		return deleteButton;
	}

	private static class UVLayer {
		String tempName;
		int layerId;
		UVLayer copiedFrom;
		boolean isRemoved;

		UVLayer(int layerId, UVLayer from){
			this.layerId = layerId;
			if(from != null && from.copiedFrom != null){
				this.copiedFrom = from.copiedFrom;
			} else {
				this.copiedFrom = from;
			}
			if (copiedFrom != null){
				this.tempName = "[ " + layerId  + " (" + copiedFrom.layerId + ")" + " ]";
			} else {
				this.tempName = "[ " + layerId  + " (org) ]";
			}
		}

		public UVLayer setRemoved(boolean removed){
			isRemoved = removed;
			return this;
		}

//		public UVLayer setNewIndex(int newIndex) {
//			this.newIndex = newIndex;
//			return this;
//		}
	}
}
