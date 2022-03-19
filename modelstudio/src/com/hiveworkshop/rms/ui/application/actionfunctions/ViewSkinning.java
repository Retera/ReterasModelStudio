package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.InfoPopup;

import java.util.*;
import java.util.stream.Collectors;

public class ViewSkinning extends ActionFunction {

	public ViewSkinning() {
		super(TextKey.VIEW_SKINNING, ViewSkinning::viewSkinning);
	}

	public static void viewMatrices(ModelHandler modelHandler) {
		if (modelHandler != null) {
			InfoPopup.show(ProgramGlobals.getMainPanel(), getSelectedMatricesDescription(modelHandler.getModelView().getSelectedVertices()));
		}
	}

	public static void viewHDSkinning(ModelHandler modelHandler) {
		if (modelHandler != null) {
			InfoPopup.show(ProgramGlobals.getMainPanel(), getSelectedHDSkinningDescription(modelHandler.getModelView().getSelectedVertices()));
		}
	}

	public static void viewSkinning(ModelHandler modelHandler) {
		if (modelHandler != null) {
			InfoPopup.show(ProgramGlobals.getMainPanel(), getSkinningDescription(modelHandler.getModelView().getSelectedVertices()));
		}
	}

	public static String getSkinningDescription(Collection<GeosetVertex> selection){
		List<GeosetVertex> hdVerts = selection.stream().filter(gv -> gv.getSkinBones() != null).collect(Collectors.toList());
		List<GeosetVertex> sdVerts = selection.stream().filter(gv -> gv.getSkinBones() == null).collect(Collectors.toList());

		String hdDesc = "";
		String sdDesc = "";

		if(!hdVerts.isEmpty()){
			hdDesc =  getSelectedHDSkinningDescription2(hdVerts);
		}
		if (!sdVerts.isEmpty()){
			sdDesc = getSelectedMatricesDescription2(sdVerts);
		}
		if(!hdVerts.isEmpty() && !sdVerts.isEmpty()){
			return "Selection contains both HD and SD vertices\n\n" + hdDesc + "\n\n" + sdDesc;
		} else {
			return hdDesc + sdDesc;
		}

	}

	public static String getSelectedMatricesDescription(Collection<GeosetVertex> selection) {
		List<Bone> boneRefs = new ArrayList<>();
		for (GeosetVertex gv : selection) {
			for (Bone b : gv.getBones()) {
				if (!boneRefs.contains(b)) {
					boneRefs.add(b);
				}
			}
		}
		StringBuilder boneList = new StringBuilder();
		for (int i = 0; i < boneRefs.size(); i++) {
			if (i == (boneRefs.size() - 2)) {
				boneList.append(boneRefs.get(i).getName()).append(" and ");
			} else if (i == (boneRefs.size() - 1)) {
				boneList.append(boneRefs.get(i).getName());
			} else {
				boneList.append(boneRefs.get(i).getName()).append(", ");
			}
		}
		if (boneRefs.size() == 0) {
			boneList = new StringBuilder("Nothing was selected that was attached to any bones.");
		}
		return boneList.toString();
	}

	public static String getSelectedMatricesDescription2(Collection<GeosetVertex> selection) {
		Map<Matrix, Integer> vertCountMap = new HashMap<>();
		for (GeosetVertex gv : selection) {
			Integer count = vertCountMap.computeIfAbsent(gv.getMatrix(), k -> 0) + 1;
			vertCountMap.put(gv.getMatrix(), count);
		}
		StringBuilder boneList = new StringBuilder();
		for (Matrix boneRefs : vertCountMap.keySet()){
			int numBones = boneRefs.size();
			if(numBones>0){
				boneList.append("Matrix:{");
				for (int i = 0; i < numBones; i++) {
					if (i == (numBones - 2)) {
						boneList.append(boneRefs.get(i).getName()).append(" and ");
					} else if (i == (numBones - 1)) {
						boneList.append(boneRefs.get(i).getName());
					} else {
						boneList.append(boneRefs.get(i).getName()).append(", ");
					}
				}
				boneList.append("}: ");
				boneList.append(vertCountMap.get(boneRefs));
				boneList.append(" vertices\n");
			}
		}
		if (vertCountMap.size() == 0) {
			boneList = new StringBuilder("Nothing was selected that was attached to any bones.");
		}
		return boneList.toString();
	}


	public static String getSelectedHDSkinningDescription(Collection<GeosetVertex> selection) {
		Map<String, SkinBone[]> skinBonesArrayMap = new TreeMap<>();

		for (GeosetVertex gv : selection) {
			SkinBone[] skinBones = gv.getSkinBones();

			String sbId = skinBonesId(skinBones);
			if (!skinBonesArrayMap.containsKey(sbId)) {
				skinBonesArrayMap.put(sbId, skinBones);
			}
		}

		StringBuilder output = new StringBuilder();
		String ugg = ":                            ";
		for (SkinBone[] skinBones : skinBonesArrayMap.values()) {
			for (int i = 0; i < 4; i++) {
				if (skinBones == null) {
					output.append("null");
				} else {
					String s;
					if (skinBones[i].getBone() == null) {
						s = "null";
					} else {
						s = skinBones[i].getBone().getName();
					}
					s = (s + ugg).substring(0, ugg.length());
					output.append(s);
					String w = "   " + skinBones[i].getWeight();
					w = w.substring(w.length() - 3);
					String w2 = (Math.round(skinBones[i].getWeight() / .255) / 1000.0 + "000000").substring(0, 6);
					output.append(w).append(" ( ").append(w2).append(" )\n");
				}
			}
			output.append("\n");
		}
		return output.toString();
	}


	public static String getSelectedHDSkinningDescription2(Collection<GeosetVertex> selection) {
		Map<String, SkinBone[]> skinBonesArrayMap = new TreeMap<>();
		Map<String, Integer> skinBonesCountMap = new HashMap<>();

		for (GeosetVertex gv : selection) {
			SkinBone[] skinBones = gv.getSkinBones();

			String sbId = skinBonesId(skinBones);

			Integer count = skinBonesCountMap.computeIfAbsent(sbId, k -> 0) + 1;
			skinBonesCountMap.put(sbId, count);
			if (!skinBonesArrayMap.containsKey(sbId)) {
				skinBonesArrayMap.put(sbId, skinBones);
			}
		}

		StringBuilder output = new StringBuilder();
		String ugg = ":                            ";
		for (String sbId : skinBonesArrayMap.keySet()) {
			SkinBone[] skinBones = skinBonesArrayMap.get(sbId);
			output.append(skinBonesCountMap.get(sbId)).append(" vertices:\n");
			for (int i = 0; i < 4; i++) {
				if (skinBones == null) {
					output.append("null");
				} else {
					String s;
					if (skinBones[i].getBone() == null) {
						s = "null";
					} else {
						s = skinBones[i].getBone().getName();
					}
					s = (s + ugg).substring(0, ugg.length());
					output.append(s);
					String w = "   " + skinBones[i].getWeight();
					w = w.substring(w.length() - 3);
					String w2 = (Math.round(skinBones[i].getWeight() / .255) / 1000.0 + "000000").substring(0, 6);
					output.append(w).append(" ( ").append(w2).append(" )\n");
				}
			}
			output.append("\n");
		}
		return output.toString();
	}

	private static String skinBonesId(SkinBone[] skinBones) {
		// this creates an id-string from the memory addresses of the bones and the weights.
		// keeping weights and bones separated lets us use the string to sort on common bones
		// inverting the weight lets us sort highest weight first
		if (skinBones != null) {
			StringBuilder output = new StringBuilder();
			StringBuilder output2 = new StringBuilder();
			for (SkinBone skinBone : skinBones) {
				output.append(skinBone.getBone());
				output2.append(255 - skinBone.getWeight());
			}
			return output.toString() + output2.toString();
		}
		return "null";
	}
}
