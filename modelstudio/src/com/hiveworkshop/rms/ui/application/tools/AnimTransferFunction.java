package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import java.util.*;

public class AnimTransferFunction {
	boolean importSuccess = false;
	boolean importStarted = false;
	boolean importEnded = false;
	EditableModel receivingModel;
	EditableModel donatingModel;


	Map<TimelineContainer, TimelineContainer> animationSourceMap = new HashMap<>();

	AnimTransferFunction(EditableModel receivingModel, EditableModel donatingModel){
		this.receivingModel = receivingModel;
		this.donatingModel = donatingModel;
		fillStuff2();
	}

	private void fillStuff2(){
		// Getting animation names used by both models to use when matching geosets
		Set<String> animNames = getSeqNames(receivingModel.getAllSequences(), donatingModel.getAllSequences());

		Map<String, IdObject> nameMap = new HashMap<>();
		for (IdObject motionDest : receivingModel.getIdObjects()) {
			nameMap.put(motionDest.getName(), motionDest);
		}

		for (IdObject motionSource : donatingModel.getIdObjects()) {
			IdObject motionDest = nameMap.get(motionSource.getName());
			if (motionDest != null) {
				animationSourceMap.put(motionDest, motionSource);
			}
		}


		Map<TextureAnim, String> texAnimToAnimDesc = new LinkedHashMap<>();
		for(TextureAnim textureAnim : donatingModel.getTexAnims()) {
			texAnimToAnimDesc.put(textureAnim, getMatching(animNames, textureAnim));
		}
		for(TextureAnim textureAnim : receivingModel.getTexAnims()) {
			texAnimToAnimDesc.put(textureAnim, getMatching(animNames, textureAnim));
		}

		for(TextureAnim donTexAnim : donatingModel.getTexAnims()){
			// match on type? (blood, footsteps etc)
			String donTexAnimDesc = texAnimToAnimDesc.get(donTexAnim);
			for(TextureAnim recTexAnim : receivingModel.getTexAnims()){
				if(donTexAnimDesc.equals(texAnimToAnimDesc.get(recTexAnim))){
					animationSourceMap.put(recTexAnim, donTexAnim);
				}
			}
		}
//		if(donatingModel.getEvents().size() != 0 && receivingModel.getEvents().size() != 0){
//			eventMappings = new EventMappings(new SoundMappings());
//		}
		for(EventObject donEvent : donatingModel.getEvents()){
			// match on type? (blood, footsteps etc)
			for(EventObject recEvent : receivingModel.getEvents()){
				if(donEvent.getName().equals(recEvent.getName())){
//					visibilityMap.put(recEvent, donEvent);
				}

			}
		}

		Map<Geoset, String> geosetToAnimDesc = getGeosetAnimDescMap(animNames);

		for(Geoset donGeo : donatingModel.getGeosets()){
			// match on visibility in animations?
			String donGeoDesc = geosetToAnimDesc.get(donGeo);
			boolean donIsGutz = isGutz(donGeo);

			for(Geoset recGeo : receivingModel.getGeosets()){
				String recGeoDesc = geosetToAnimDesc.get(recGeo);
				if(donGeoDesc.equals(recGeoDesc)
						|| isGutz(recGeo) && donIsGutz
						|| !animationSourceMap.containsKey(recGeo) && !isGutz(recGeo) && !donIsGutz){
					animationSourceMap.put(recGeo, donGeo);
					List<Layer> recLayers = recGeo.getMaterial().getLayers();
					List<Layer> donLayers = donGeo.getMaterial().getLayers();
					for (int i = 0; i < recLayers.size() && i < donLayers.size(); i++) {
						animationSourceMap.put(recLayers.get(i), donLayers.get(i));
					}
				}
			}
		}

	}

	private Map<Geoset, String> getGeosetAnimDescMap(Set<String> animNames) {
		Map<Material, String> materialToAnimDesc = getMaterialAnimDescMap(animNames);
		Map<Geoset, String> geosetToAnimDesc = new LinkedHashMap<>();
		for(Geoset geoset : donatingModel.getGeosets()){
			geosetToAnimDesc.put(geoset, "Geo " + getMatching(animNames, geoset) + " " + materialToAnimDesc.get(geoset.getMaterial()));
		}
		for(Geoset geoset : receivingModel.getGeosets()){
			geosetToAnimDesc.put(geoset, "Geo " + getMatching(animNames, geoset) + " " + materialToAnimDesc.get(geoset.getMaterial()));
		}
		return geosetToAnimDesc;
	}

	private Map<Material, String> getMaterialAnimDescMap(Set<String> animNames) {
		Map<Material, String> materialToAnimDesc = new LinkedHashMap<>();
		for(Material material : donatingModel.getMaterials()){
			StringBuilder descKey = new StringBuilder();
			descKey.append("Mat: ");
			for(Layer layer : material.getLayers()) {
				descKey.append("Lay ");
				descKey.append(getMatching(animNames, layer));
			}
			materialToAnimDesc.put(material, descKey.toString());
		}
		for(Material material : receivingModel.getMaterials()){
			StringBuilder descKey = new StringBuilder();
			descKey.append("Mat: ");
			for(Layer layer : material.getLayers()) {
				descKey.append("Lay ");
				descKey.append(getMatching(animNames, layer));
			}
			materialToAnimDesc.put(material, descKey.toString());

		}
		return materialToAnimDesc;
	}

	boolean isMatchingEvent(Set<String> animNames, EventObject recEvent, EventObject donEvent){
		if(recEvent.getName().equals(donEvent.getName())) {
			return true;
//		} else if (recEvent.getName().substring(0, 3).equals(donEvent.getName().substring(0, 3))){
//			EventTarget recET = eventMappings.getEvent(recEvent.getName());
//			EventTarget donET = eventMappings.getEvent(donEvent.getName());
//
//
//			String recEventId = recEvent.getName().substring(3);
//			String donEventId = donEvent.getName().substring(3);

		}
		return false;
	}

	EventObject getBestMatchingEvent(Set<String> animNames, EventObject recEventFound, EventObject recEventAlt, EventObject donEvent){

		String recEventFoundName = recEventFound != null ? recEventFound.getName() : "________";
		String recEventAltName = recEventAlt.getName();
		String donEventName = donEvent.getName();
		if(recEventFoundName.equals(donEventName)) {
			return recEventFound;
		} else if (recEventAltName.equals(donEventName)){
			return recEventAlt;
//		} else {
//			String typeFound = recEventFoundName.substring(0, 3);
//			String typeAlt = recEventAltName.substring(0, 3);
//			String typeDon = donEventName.substring(0, 3);
//			EventTarget recFoundET = eventMappings.getEvent(recEventFoundName);
//			EventTarget recAltET = eventMappings.getEvent(recEventAltName);
//			EventTarget donET = eventMappings.getEvent(donEventName);
//
//
//
//			String[] spawnKeywords = new String[] {"Dissipate", "Blood", "Small", "Large", "Birth", "Death", "Crumble"};
//			String UGG = "Birth, Target, Death, Puke, Crumble, SpawnFootPrint";
//			String UGG2 = "Dust, Boom, SpawnObj, BoomSpawn, ExplosionUltimate";
//
//			String[] uberSplatKeyWords = new String[]{};
//			String Uss2 = "Small, Large, Med, Building, Death, Uber, Splat";
//
//			String[] splatKeyWords = new String[]{"BloodLarge", "BloodSmall",
//					"Footprint",
//					"Bare", "Boot", "Cloven", "Horse", "Paw", "Root", "Wheel",
//					"ReallySmall", "Small", "Large", "XtraLarge", "Giant", "Huge",
//					"Left", "Right",
//					"Troll", "Murloc", "Snake", "Lizzard", "SpikeGiant", "Drag", "Flame", "Skeleton", "Bear", "Demon",
//					"Splash"
//			};
//
//			String[] soundKeyWords = new String[]{"MissileHit", "MissileLaunch", "Missile",
//					"MeteorLaunch", "MeteorHit",
//					"ArrowLaunch", "ArrowHit",
//					"AcidLaunch", "AcidHit",
//					"BoltLaunch", "BoltHit",
//					"Attack", "MissileAttack", "MeleeAttack", "AttackSlam",
//					"MissileImpact",
//					"MagicLaunch",
//					"RocketsLaunch",
//					"Spell",
//					"Death",
//					"DeathSwim",
//					"DeathAlternate",
//					"Morph", "MorphDeath", "MorphAlternate",
//					"Building", "SmallBuilding", "LargeBuilding", "BuildingCancel",
//					"Explode",
//					"Dissipate",
//					"Step",
//			};

		}
		return null;
	}

	Set<String> getSeqNames(Collection<Sequence> recSeqs, Collection<Sequence> donSeqs){
		Set<String> names = new HashSet<>();
		for (Sequence recAnim : recSeqs) {
			for (Sequence donAnim : donSeqs) {
				if (recAnim.getName().equals(donAnim.getName())) {
					names.add(recAnim.getName());
					break;
				}
			}
		}
		return names;
	}

	String getMatching(Set<String> animNames, TimelineContainer timelineContainer){

		Map<String, String> tempAnimMap = new HashMap<>();
		AnimFlag<Float> visibilityFlag = timelineContainer.getVisibilityFlag();
		StringBuilder descKey = new StringBuilder();
		if(visibilityFlag != null){
			Map<Sequence, TreeMap<Integer, Entry<Float>>> animMap = visibilityFlag.getAnimMap();
			for (Sequence sequence : animMap.keySet()) {
				if(animNames.contains(sequence.getName())){
					TreeMap<Integer, Entry<Float>> entryMap = animMap.get(sequence);
					if(!entryMap.isEmpty()){
						float firstVis = entryMap.get(entryMap.firstKey()).getValue();
						float animVis = firstVis;
						for (Entry<Float> entry : entryMap.values()) {
							animVis = entry.getValue();
							if (entry.getValue() != firstVis){
								break;
							}
						}
						if (firstVis != animVis){
							tempAnimMap.put(sequence.getName(), sequence.getName() + " ANI, ");
						} else if (firstVis == 0){
							tempAnimMap.put(sequence.getName(), sequence.getName() + " INV, ");
						} else if (1 <= firstVis){
							tempAnimMap.put(sequence.getName(), sequence.getName() + " VIS, ");
						} else {
							tempAnimMap.put(sequence.getName(), sequence.getName() + " TRS, ");
						}
					} else {
						tempAnimMap.put(sequence.getName(), sequence.getName() + " VIS, ");
					}
				}
			}
			for (String animName : animNames) {
				String animDesc = tempAnimMap.get(animName);
				if (animDesc != null) {
					descKey.append(animDesc);
				}
			}
		} else {
			float renderVisibility = timelineContainer.getRenderVisibility(null);
			if (renderVisibility == 0){
				descKey.append(" ALWAYS_INV, ");
			} else if (1 <= renderVisibility){
				descKey.append(" ALWAYS_VIS, ");
			} else {
				descKey.append(" ALWAYS_TRS, ");
			}
		}

		return descKey.toString();
	}

	public void animTransfer(Collection<Sequence> pickedAnims, Animation visFromAnim) {

//		List<Sequence> anims = new ArrayList<>(receivingModel.getAnims());
		List<Sequence> anims = new ArrayList<>(pickedAnims);
		System.out.println("animTransferSingle2");
		doImport(visFromAnim, anims);
	}


	public void doImport(Animation visAnim, Collection<Sequence> sequencesToUse) {
		importStarted = true;
		System.out.println("\timportStarted!");
		try {
			Set<Sequence> sequencesToNotUse = new HashSet<>(donatingModel.getAllSequences());
			sequencesToNotUse.removeAll(sequencesToUse);

			Set<Sequence> donSequencesToAdd = new LinkedHashSet<>(donatingModel.getAllSequences());
			donSequencesToAdd.removeAll(sequencesToNotUse);


			Set<Sequence> sequencesToRemove = new HashSet<>(receivingModel.getAllSequences());
			sequencesToRemove.removeAll(sequencesToUse);


			for(TimelineContainer idObject : animationSourceMap.keySet()){
				for(AnimFlag<?> animFlag : idObject.getAnimFlags()){
					for(Sequence sequence : sequencesToRemove){
						if (visAnim == null || sequence != visAnim || animFlag != idObject.getVisibilityFlag()) {
							animFlag.deleteAnim(sequence);
						}
					}
				}
				TimelineContainer motionSource = animationSourceMap.get(idObject);
				for (AnimFlag<?> donFlag : motionSource.getAnimFlags()) {
					if (visAnim == null || donFlag != motionSource.getVisibilityFlag()) {
						AnimFlag<?> recFlag = idObject.find(donFlag.getName());
						if (recFlag == null) {
							recFlag = donFlag.getEmptyCopy();
							idObject.add(recFlag);
						}
						if (donFlag.getGlobalSeq() == recFlag.getGlobalSeq()) {
//							for (Sequence animation : sequencesToUse) {
							for (Sequence animation : donSequencesToAdd) {
								AnimFlagUtils.copyFrom(recFlag, donFlag, animation, animation);
							}
						}
					}
				}
			}

			if (visAnim != null) {
				for(TimelineContainer idObject : animationSourceMap.keySet()){
					AnimFlag<Float> visibilityFlag = idObject.getVisibilityFlag();
					if(visibilityFlag != null && !visibilityFlag.hasGlobalSeq()){
						for (Sequence animation : donSequencesToAdd) {
							AnimFlagUtils.copyFrom(visibilityFlag, visibilityFlag, visAnim, animation);
						}
					}
				}
				if(sequencesToRemove.contains(visAnim)) {
					for(TimelineContainer idObject : animationSourceMap.keySet()){
						AnimFlag<Float> visibilityFlag = idObject.getVisibilityFlag();
						if(visibilityFlag != null){
							visibilityFlag.deleteAnim(visAnim);
						}
					}
				}
			}

			for (Sequence sequence : sequencesToRemove){
				System.out.println("removing Seq: " + sequence.getName());
				if (sequence instanceof Animation) {
					receivingModel.remove((Animation) sequence);
				} else if (sequence instanceof GlobalSeq) {
					receivingModel.remove((GlobalSeq) sequence);
				}
			}
			for(Sequence sequence : donSequencesToAdd) {
				System.out.println("adding Seq: " + sequence.getName());
				if (sequence instanceof Animation) {
					receivingModel.add((Animation) sequence);
				} else if (sequence instanceof GlobalSeq) {
					receivingModel.add((GlobalSeq) sequence);
				}
			}

			importSuccess = true;
			System.out.println("\timportSuccess!");

		} catch (final Exception e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
		}

		importEnded = true;
		System.out.println("\timportEnded!");
	}

	private boolean isGutz(Geoset donVis) {
		if(donVis != null){
			boolean hasGeoAnim = donVis.hasAnim();
			if(hasGeoAnim){
				Bitmap bitmap = donVis.getMaterial().firstLayer().firstTexture();
//					return bitmap.getPath().equalsIgnoreCase("textures\\gutz.blp");
				return bitmap.getPath().toLowerCase(Locale.ROOT).matches("(.*\\\\)?gutz(_dif\\w*)?\\.(blp|tga|dds)");
			}
		}
		return false;
	}

}
