package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.RemoveUnusedPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.ui.util.TwiPopup;

import javax.swing.*;
import java.util.*;

public class RemoveUnusedBones extends ActionFunction {
	public RemoveUnusedBones(){
		super(TextKey.REMOVE_UNUSED_NODES, RemoveUnusedBones::doRemove);
	}

	public static void doRemove(ModelHandler modelHandler){

		RemoveUnusedPanel removeUnusedPanel = new RemoveUnusedPanel();
		int remove_unused_nodes = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), removeUnusedPanel, TextKey.REMOVE_UNUSED_NODES.toString(), JOptionPane.OK_CANCEL_OPTION);

		if (remove_unused_nodes == JOptionPane.OK_OPTION) {
			EditableModel model = modelHandler.getModel();
			Set<Bone> usedBones = new HashSet<>();
			model.getGeosets().forEach(geoset -> usedBones.addAll(geoset.getBoneMap().keySet()));
			List<Sequence> allSequences = model.getAllSequences();
			List<Animation> anims = model.getAnims();

			Map<Class<? extends IdObject>, Boolean> doWeedMap = removeUnusedPanel.getDoMap();
			String preserve = removeUnusedPanel.getBindJntName();

			List<IdObject> sortedIdObjects = getSortedNodes(model.getIdObjects(), false);


			Set<IdObject> unusedObjects = getUnusedObjects(usedBones, allSequences, anims, doWeedMap, preserve, removeUnusedPanel.keepParents(), sortedIdObjects);

			System.out.println("bonesToRemove: " + unusedObjects.size());

			if (!unusedObjects.isEmpty()) {
				TwiPopup.quickDismissPopup(ProgramGlobals.getMainPanel(), unusedObjects.size() + " unused nodes removed", "Removed Unused Nodes");
				modelHandler.getUndoManager().pushAction(new DeleteNodesAction(unusedObjects, ModelStructureChangeListener.changeListener, model).redo());
			} else {
				TwiPopup.quickDismissPopup(ProgramGlobals.getMainPanel(), "Found no unused nodes", "No Nodes Removed");
			}
		}
	}

	private static Set<IdObject> getUnusedObjects(Set<Bone> usedBones,
	                                              List<Sequence> allSequences,
	                                              List<Animation> anims,
	                                              Map<Class<? extends IdObject>, Boolean> doWeedMap,
	                                              String preserve, boolean keepParents,
	                                              List<IdObject> sortedIdObjects) {
		Set<IdObject> unusedObjects = new HashSet<>();
		Set<IdObject> bindJnts = new LinkedHashSet<>();
		for (IdObject idObject : sortedIdObjects) {
			if (preserve != null && idObject.getName().toLowerCase().contains(preserve)) {
				bindJnts.add(idObject);
			}
			if (doWeedMap.getOrDefault(idObject.getClass(), false)
					&& (unusedObjects.containsAll(idObject.getChildrenNodes()) || !keepParents && idObject.getAnimFlags().isEmpty())) {
				// CollisionShapes and Attachments should not be weeded since
				// there's no reliable way to check if they're unused
				if (idObject instanceof Bone && !usedBones.contains(idObject)) {
					unusedObjects.add(idObject);
				} else if (idObject instanceof Light) {
					if  (isNeverVis(allSequences, idObject.getVisibilityFlag())) {
						unusedObjects.add(idObject);
					}
				} else if (idObject instanceof Helper) {
					unusedObjects.add(idObject);
				} else if (idObject instanceof ParticleEmitter) {
					if (isNeverVis(allSequences, idObject.getVisibilityFlag())
							|| hasNoEmission(allSequences, idObject.find(MdlUtils.TOKEN_EMISSION_RATE))
							&& ((ParticleEmitter) idObject).getEmissionRate() == 0) {
						unusedObjects.add(idObject);
					}
				} else if (idObject instanceof ParticleEmitter2) {
					if (isNeverVis(allSequences, idObject.getVisibilityFlag())
							|| hasNoEmission(allSequences, idObject.find(MdlUtils.TOKEN_EMISSION_RATE))
							&& ((ParticleEmitter2) idObject).getEmissionRate() == 0) {
						unusedObjects.add(idObject);
					}
				} else if (idObject instanceof ParticleEmitterPopcorn) {
					ParticleEmitterPopcorn popcorn = (ParticleEmitterPopcorn) idObject;
					if (isNeverVis(allSequences, idObject.getVisibilityFlag())
							|| hasNoEmission(allSequences, idObject.find(MdlUtils.TOKEN_EMISSION_RATE))
							&& popcorn.getEmissionRate() == 0
							|| !isEverOn(popcorn, anims)) {
						unusedObjects.add(idObject);
					}
				} else if (idObject instanceof RibbonEmitter) {
					if (isNeverVis(allSequences, idObject.getVisibilityFlag())
							|| hasNoEmission(allSequences, idObject.find(MdlUtils.TOKEN_EMISSION_RATE))
							&& ((RibbonEmitter) idObject).getEmissionRate() == 0) {
						unusedObjects.add(idObject);
					}
				} else if (idObject instanceof EventObject) {
					if (((EventObject) idObject).size() == 0) {
						unusedObjects.add(idObject);
					}
				}
			}
		}
		if (bindJnts.size() != 0) {
			Set<IdObject> faceBones = new HashSet<>();
			for (IdObject bindJnt : bindJnts) {
				collectChilds(bindJnt, faceBones);
			}
			unusedObjects.removeAll(faceBones);
		}
		return unusedObjects;
	}

	private static void collectChilds(IdObject idObject, Set<IdObject> childs){
		childs.add(idObject);
		for (IdObject child : idObject.getChildrenNodes()) {
			collectChilds(child, childs);
		}
	}

	private static boolean isEverOn(ParticleEmitterPopcorn popcorn, List<Animation> anims) {
		popcorn.updateAnimsVisMap(anims);
		boolean defaultOn = popcorn.getAlwaysState() == ParticleEmitterPopcorn.State.on;
		boolean isEverOn = false;
		for (Animation animation : anims) {
			ParticleEmitterPopcorn.State animVisState = popcorn.getAnimVisState(animation);
			if (animVisState == ParticleEmitterPopcorn.State.on
					|| animVisState != ParticleEmitterPopcorn.State.off && defaultOn) {
				isEverOn = true;
				break;
			}
		}
		return isEverOn;
	}

	private static boolean isNeverVis(List<Sequence> allSequences, AnimFlag<Float> visibilityFlag) {
		if (visibilityFlag != null && 0 < visibilityFlag.size()) {
			for (Sequence sequence : allSequences) {
				if (visibilityFlag.hasSequence(sequence)) {
					for (Entry<Float> entry : visibilityFlag.getEntryMap(sequence).values()) {
						if (entry.getValue() != null && entry.getValue() != 0
								|| entry.getInTan() != null && entry.getInTan() != 0
								|| entry.getOutTan() != null && entry.getOutTan() != 0) {
							return false;
						}
					}
				}
			}
		}
		return false;
	}
	private static boolean hasNoEmission(List<Sequence> allSequences, AnimFlag<?> flag) {
		if (flag instanceof FloatAnimFlag && 0 < flag.size()) {
			AnimFlag<Float> emissionFlag = (FloatAnimFlag) flag;
			for (Sequence sequence : allSequences){
				if(emissionFlag.getEntryMap(sequence) != null){
					for (Entry<Float> entry : emissionFlag.getEntryMap(sequence).values()) {
						if (entry.getValue() != null && entry.getValue() != 0
								|| entry.getInTan() != null && entry.getInTan() != 0
								|| entry.getOutTan() != null && entry.getOutTan() != 0) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private static List<IdObject> getSortedIdObjects(EditableModel model) {
		List<IdObject> roots = new ArrayList<>();
		for (IdObject object : model.getIdObjects()) {
			if (object.getParent() == null) {
				roots.add(object);
			}
		}
		Queue<IdObject> bfsQueue = new LinkedList<>(roots);
		List<IdObject> sortedIdObjects = new ArrayList<>();
		while (!bfsQueue.isEmpty()) {
			IdObject nextItem = bfsQueue.poll();
			bfsQueue.addAll(nextItem.getChildrenNodes());
			sortedIdObjects.add(nextItem);
		}
		return sortedIdObjects;
	}

	private static List<IdObject> getSortedNodes(List<IdObject> allIdObjects, boolean decending){
		TreeMap<Integer, Set<IdObject>> sortedNodeMap = new TreeMap<>();
		Set<IdObject> roots = new LinkedHashSet<>();
		sortedNodeMap.put(0, roots);

		allIdObjects.stream().filter(idObject -> idObject.getParent() == null).forEach(roots::add);
		for (IdObject node : roots){
			collectDepthSortedNodes(sortedNodeMap, node, 1);
		}

		List<IdObject> sortedNodes = new ArrayList<>();

		if (decending) {
			for (int i = sortedNodeMap.firstKey(); i <= sortedNodeMap.lastKey(); i++) {
				sortedNodes.addAll(sortedNodeMap.get(i));
			}
		} else {
			for (int i = sortedNodeMap.lastKey(); sortedNodeMap.firstKey() <= i; i--) {
				sortedNodes.addAll(sortedNodeMap.get(i));
			}
		}

		return sortedNodes;
	}

	private static void collectDepthSortedNodes(Map<Integer, Set<IdObject>> sortedObj, IdObject node, int depth) {
		sortedObj.computeIfAbsent(depth, k -> new LinkedHashSet<>()).addAll(node.getChildrenNodes());
		for (IdObject child : node.getChildrenNodes()){
			collectDepthSortedNodes(sortedObj, child, depth+1);
		}
	}



}
