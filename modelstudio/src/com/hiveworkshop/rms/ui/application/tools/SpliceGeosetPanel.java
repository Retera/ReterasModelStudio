package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;
import java.util.function.Consumer;

public class SpliceGeosetPanel extends JPanel{
	EditableModel donModel;
	EditableModel recModel;
	ModelHandler recModelHandler;
	List<Geoset> chosenDonGeos = new ArrayList<>();
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private boolean tryMatchVisibility = false;

	public SpliceGeosetPanel(EditableModel donModel, ModelHandler recModelHandler) {
		super(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
		this.donModel = donModel;
		this.recModel = recModelHandler.getModel();
		this.recModelHandler = recModelHandler;


		BoneReplacementWizard wizard2 = new BoneReplacementWizard(this, recModel, donModel)
				.setClassSet(Collections.singleton(Bone.class))
				.setIsGeometryMode(true)
				.setCheckHelperBones(true);
		JPanel editMappingPanel = wizard2.getEditMappingPanel(-1, true, true);

		JCheckBox matchVis = new JCheckBox("Try to keep visibility");
		matchVis.addActionListener(e -> tryMatchVisibility = matchVis.isSelected());

		JButton anImport = new JButton("Import");
		anImport.addActionListener(e -> importGeosets(chosenDonGeos, wizard2.fillAndGetChainMap()));

		JPanel geosetsPanel = new JPanel(new MigLayout("fill", "[]", "[grow][][]"));
		geosetsPanel.add(getGeosetTogglePanel(), "wrap");
		geosetsPanel.add(matchVis, "wrap");
		geosetsPanel.add(anImport, "");

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Choose Geosets", geosetsPanel);
		tabbedPane.addTab("Map Nodes", editMappingPanel);
		add(tabbedPane);
	}

	private JPanel getGeosetTogglePanel(){
		JPanel geosetPanel = new JPanel(new MigLayout("fill"));
		for(Geoset geoset : donModel.getGeosets()){
			geosetPanel.add(new JLabel(iconHandler.getImageIcon(geoset, donModel)));
			JCheckBox checkBox = new JCheckBox(geoset.getName(), true);
			chosenDonGeos.add(geoset);
			checkBox.addActionListener(e -> checkboxAction(checkBox, geoset));
			geosetPanel.add(checkBox, "growx, wrap");
		}
		return geosetPanel;
	}

	private void checkboxAction(JCheckBox checkBox, Geoset geoset){
		if(checkBox.isSelected()){
			System.out.println("added Geoset: " + geoset.getName());
			chosenDonGeos.add(geoset);
		} else {
			System.out.println("removed Geoset: " + geoset.getName());
			chosenDonGeos.remove(geoset);
		}
	}


	protected JButton getButton(String text, Consumer<JButton> buttonConsumer, EditableModel model) {
		JButton button = new JButton(text);
		if(model != null){
			button.setIcon(iconHandler.getImageIcon(model));
		}
		button.addActionListener(e -> buttonConsumer.accept(button));
		return button;
	}


	private void importGeosets(List<Geoset> newGeosets, Map<IdObject, IdObject> chainMap) {
		List<UndoAction> undoActions = new ArrayList<>();
		Set<GeosetVertex> addedVertexes = new HashSet<>();

		if(tryMatchVisibility){
			matchVisibility(newGeosets);
		}

		for(Geoset newGeoset : newGeosets){
			for (GeosetVertex vertex : newGeoset.getVertices()) {
				vertex.replaceBones(chainMap);
			}
			newGeoset.getMatrices().clear();
			addedVertexes.addAll(newGeoset.getVertices());
			AddGeosetAction addGeosetAction = new AddGeosetAction(newGeoset, recModel, ModelStructureChangeListener.changeListener);
			undoActions.add(addGeosetAction);
		}

		CompoundAction addedModelPart = new CompoundAction("added model part", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);

		SetSelectionUggAction selectionAction = new SetSelectionUggAction(new SelectionBundle(addedVertexes), recModelHandler.getModelView(), "", null);
		recModelHandler.getUndoManager().pushAction(new CompoundAction("added model part", ModelStructureChangeListener.changeListener::nodesUpdated, addedModelPart, selectionAction).redo());
	}
	
	private void matchVisibility(List<Geoset> newGeosets){
		Map<Animation, Animation> recToDonAnimMap = getRecToDonAnimMap();

		for(Geoset geoset : newGeosets){
			GeosetAnim geosetAnim = geoset.getGeosetAnim();
			if(geosetAnim != null){
				copyGeosetAnims(recToDonAnimMap, geosetAnim);
			}
		}
		
		for (Material material : donModel.getMaterials()){
			addMaterialAnimations(recToDonAnimMap, material);
		}
		
	}

	private void addMaterialAnimations(Map<Animation, Animation> recToDonAnimMap, Material material) {
		for (Layer layer : material.getLayers()){
			for (AnimFlag<?> animFlag : layer.getAnimFlags()){
				for(Animation recAnim : donModel.getAnims()){
					Animation donAnim = recToDonAnimMap.get(recAnim);
					if(donAnim != null && animFlag.hasSequence(donAnim)){
						AnimFlagUtils.copyFrom(animFlag, animFlag, donAnim, recAnim);
					}
				}
			}
		}
	}

	private void copyGeosetAnims(Map<Animation, Animation> recToDonAnimMap, GeosetAnim geosetAnim) {
		for (AnimFlag<?> animFlag : geosetAnim.getAnimFlags()){
			for(Animation recAnim : recModel.getAnims()){
				Animation donAnim = recToDonAnimMap.get(recAnim);
				if(donAnim != null && animFlag.hasSequence(donAnim)){
					AnimFlagUtils.copyFrom(animFlag, animFlag, donAnim, recAnim);
				}
			}
		}
	}

	private Map<Animation, Animation> getRecToDonAnimMap() {
//		Map<Animation, Animation> donToRecAnimMap = new HashMap<>();
		Map<Animation, Animation> recToDonAnimMap = new HashMap<>();
		for(Animation recAnim : recModel.getAnims()){
			String recAnimName = recAnim.getName().toLowerCase();
			for(Animation donAnim : donModel.getAnims()){
				String donAnimName = donAnim.getName().toLowerCase();
				if (recAnimName.equals(donAnimName)){
					System.out.println(donAnimName + " equals " + recAnimName);
					recToDonAnimMap.put(recAnim, donAnim);
					break;
				} else {
					String dan2 = donAnimName.replaceAll("[\\d-]", "");
					String ran2 = recAnimName.replaceAll("[\\d-]", "");

//					System.out.println(dan2 + " == " + ran2 + " ?");
					String[] donSplit = dan2.split(" +");
					String[] recSplit = ran2.split(" +");
					if(Arrays.equals(donSplit, recSplit)){
						System.out.println("all equals: " + dan2 + " == " + ran2 + " !" + " (" + donAnimName + " -> " + recAnimName + ")");
						recToDonAnimMap.put(recAnim, donAnim);
					} else {
						Animation curMatch = recToDonAnimMap.get(recAnim);
						if(curMatch != null){
							String[] curSplit = curMatch.getName().toLowerCase().replaceAll("[\\d-]", "").split(" +");
							for (int i = 0; i<donSplit.length; i++){
								if(recSplit.length<=i || !donSplit[i].equals(recSplit[i])){
									break;
								} else if(curSplit.length <=i || !donSplit[i].equals(curSplit[i])){
									if(donSplit[i].equals(recSplit[i])){
										System.out.println("part #"+ i + ": " + donSplit[i] + " == " + recSplit[i] + " !" + " (" + donAnimName + " -> " + recAnimName + ")");
										recToDonAnimMap.put(recAnim, donAnim);
									}
									break;
								}
							}
						} else if (0 < donSplit.length && 0 < recSplit.length && donSplit[0].equals(recSplit[0])){
							System.out.println("first word: " + donSplit[0] + " == " + recSplit[0] + " !" + " (" + donAnimName + " -> " + recAnimName + ")");
							recToDonAnimMap.put(recAnim, donAnim);
						}
					}
				}
			}
			if(recToDonAnimMap.get(recAnim) == null){
				System.out.println("no match for: " + recAnimName + "!");
			}
		}
		return recToDonAnimMap;
	}

}
