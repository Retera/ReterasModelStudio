package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.ModelIconHandler;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooserButton;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.SmartButtonGroup;

import javax.swing.*;
import java.util.*;

public abstract class TwiImportPanel extends JPanel {
	ModelIconHandler iconHandler = new ModelIconHandler();
	EditableModel donModel;
	EditableModel recModel;
	ModelHandler recModelHandler;
	IdObjectChooserButton donBoneChooserButton;
	IdObjectChooserButton recBoneChooserButton;

	BoneOption boneOption = BoneOption.rebindGeometry;
	Integer boneChainDepth = -1;

	String donBoneButtonText = "Choose Bone Chain Start";
	String recBoneButtonText = "Choose Bone Chain Start";

	AnimationMappingPanel animationMappingPanel;

	public TwiImportPanel(EditableModel donModel, ModelHandler recModelHandler) {
//		super(new MigLayout("fill, wrap 2, debug", "[sgx half, grow][sgx half, grow][0%:0%:1%, grow 0]", "[grow 0][grow 0][grow 0][grow 0][grow 0]"));
//		super(new MigLayout("fill, debug", "[sg half][sg half]", ""));
//		super(new MigLayout("fill, debug", "[][]", ""));
		this.donModel = donModel;
		this.recModel = recModelHandler.getModel();
		this.recModelHandler = recModelHandler;

		animationMappingPanel = new AnimationMappingPanel(donModel.getAnims(), recModel.getAnims());

		donBoneChooserButton = new IdObjectChooserButton(donModel, this).setClasses(Bone.class, Helper.class);
		recBoneChooserButton = new IdObjectChooserButton(recModel, this).setClasses(Bone.class, Helper.class);
	}

	protected JPanel getBoneOptionPanel(){
		SmartButtonGroup extraBonesOption = new SmartButtonGroup("Handle bones of geometry found outside of bone chain");
//		SmartButtonGroup extraBonesOption = new SmartButtonGroup();
		extraBonesOption.setButtonConst("");
		for(BoneOption option : BoneOption.values()){
			extraBonesOption.addJRadioButton(option.getText(), e -> boneOption = option).setToolTipText(option.getTooltip());
		}
		extraBonesOption.setSelectedIndex(BoneOption.rebindGeometry.ordinal());
		return extraBonesOption.getButtonPanel();
	}

	protected JPanel getAnimMapPanel() {
		return animationMappingPanel;
	}


	protected Map<Sequence, Sequence> getRecToDonSequenceMap() {
		return animationMappingPanel.getRecToDonSequenceMap();
	}

	protected Map<IdObject, IdObject> getChainMap(IdObject mapToBone, EditableModel mapToModel, IdObject mapFromBone, EditableModel mapFromModel, int depth, boolean presentParent){
		return new BoneChainMapWizard(this, mapToModel, mapFromModel).getChainMap2(mapToBone, mapFromBone, depth, presentParent);
	}


	protected Set<Geoset> getNewGeosets(Set<IdObject> selectedBones) {
		System.out.println("selected bones: " + selectedBones.size());
		Set<IdObject> extraBones = new HashSet<>();
		Set<Geoset> newGeosets = getCopiedGeosets(selectedBones, extraBones);

		for (Geoset newGeoset : newGeosets) {
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if (boneOption == BoneOption.importBonesExtra) {
				vertexSet.addAll(getVertexSet(extraBones, newGeoset));
			}

			Set<Triangle> trianglesToRemove = new HashSet<>();
			Set<GeosetVertex> verticesToCull = new HashSet<>();
			for (Triangle triangle : newGeoset.getTriangles()) {
				List<GeosetVertex> triVerts = Arrays.asList(triangle.getVerts());
				if (!vertexSet.containsAll(triVerts)) {
					trianglesToRemove.add(triangle);
					verticesToCull.addAll(triVerts);
				}
			}

			verticesToCull.removeAll(vertexSet);
			newGeoset.remove(verticesToCull);

			trianglesToRemove.forEach(newGeoset::removeExtended);
		}
		selectedBones.addAll(extraBones);
		return newGeosets;
	}

	private Set<Geoset> getCopiedGeosets(Set<IdObject> selectedBones, Set<IdObject> extraBones) {
		Set<Geoset> newGeosets = new HashSet<>();
		for (Geoset donGeoset : donModel.getGeosets()) {
			Geoset newGeoset = donGeoset.deepCopy();
			Set<GeosetVertex> vertexSet = getVertexSet(selectedBones, newGeoset);
			if (!vertexSet.isEmpty()) {
				switch (boneOption) {
					case importBones, importBonesExtra -> extraBones.addAll(getExtraBones(vertexSet));
					case rebindGeometry -> removeBonesNotInSet(selectedBones, vertexSet);
					case leaveGeometry -> removeVerticesNotFullyCovered(selectedBones, vertexSet);
				}

				newGeoset.setParentModel(recModel);
				newGeosets.add(newGeoset);
			}
		}
		return newGeosets;
	}

	private Set<Bone> getExtraBones(Set<GeosetVertex> vertexSet) {
		Set<Bone> extraBones = new HashSet<>();
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null) {
						extraBones.add(skinBone.getBone());
					}
				}
			} else {
				extraBones.addAll(vertex.getBones());
			}
		}
		return extraBones;
	}

	private Set<GeosetVertex> getVertexSet(Set<IdObject> selectedBones, Geoset newGeoset) {
		Set<GeosetVertex> vertexSet = new HashSet<>();
		for (IdObject idObject : selectedBones) {
			System.out.println("Bone: " + idObject.getName());
			if(idObject instanceof Bone) {
				List<GeosetVertex> vertices = newGeoset.getBoneMap().get(idObject);
				if (vertices != null) {
					System.out.println("\tfound " + vertices.size() + " verts!");
					vertexSet.addAll(vertices);
				}
			}
		}
		System.out.println("Found " + vertexSet.size() + " vertices belonging to the bone chain!");
		return vertexSet;
	}

	private void removeVerticesNotFullyCovered(Set<IdObject> selectedBones, Set<GeosetVertex> vertexSet) {
		Set<GeosetVertex> verticesToPurge = new HashSet<>();
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
						verticesToPurge.add(vertex);
						break;
					}
				}

			} else {
				if(!selectedBones.containsAll(vertex.getBones())){
					verticesToPurge.add(vertex);
				}
			}
		}

		vertexSet.removeAll(verticesToPurge);
	}

	private void removeBonesNotInSet(Set<IdObject> selectedBones, Set<GeosetVertex> vertexSet) {
		for (GeosetVertex vertex : vertexSet) {
			if (vertex.getSkinBones() != null) {
				short extraWeight = 0;
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && !selectedBones.contains(skinBone.getBone())) {
						extraWeight += skinBone.getWeight();
						skinBone.setBone(null);
						skinBone.setWeight((short) 0);
					}
				}
				for (SkinBone skinBone : vertex.getSkinBones()) {
					if (skinBone != null && skinBone.getBone() != null && skinBone.getWeight() != 0){
						skinBone.setWeight((short) (skinBone.getWeight() + extraWeight));
						break;
					}
				}

			} else {
				Set<Bone> vertBones = new HashSet<>(vertex.getBones());
				selectedBones.stream().filter(o -> o instanceof Bone).forEach(vertBones::remove);
//				vertBones.removeAll(selectedBones);
				vertex.removeBones(vertBones);
			}
		}
	}

//	private boolean containsAll(Triangle triangle, Set<GeosetVertex> vertexSet) {
//		return vertexSet.contains(triangle.get(0))
//				&& vertexSet.contains(triangle.get(1))
//				&& vertexSet.contains(triangle.get(2));
//	}


	protected enum BoneOption {
		rebindGeometry("Rebind Geometry", "Remove bones not in bone chain from bone bindings"),
		leaveGeometry("Leave Geometry", "Remove vertices bound to bones not in bone chain"),
		importBones("Import Bones", "Import bones which has vertices to be imported bound to them"),
		importBonesExtra("Import Bones and Extra Geometry", "Import bones which has vertices to be imported bound to them, and extra vertices bound only to these bones");
		String text;
		String tooltip;
		BoneOption(String text, String tooltip){
			this.text = text;
			this.tooltip = tooltip;
		}

		public String getText() {
			return text;
		}

		public String getTooltip() {
			return tooltip;
		}
	}
}
