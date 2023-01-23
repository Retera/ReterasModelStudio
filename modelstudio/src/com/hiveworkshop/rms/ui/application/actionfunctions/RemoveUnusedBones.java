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
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.Pair;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class RemoveUnusedBones extends ActionFunction {
	public RemoveUnusedBones(){
		super(TextKey.REMOVE_UNUSED_NODES, RemoveUnusedBones::doRemove);
	}

	public static void doRemove(ModelHandler modelHandler){
		EditableModel model = modelHandler.getModel();
		Set<Bone> usedBones = new HashSet<>();
		model.getGeosets().forEach(geoset -> usedBones.addAll(geoset.getBoneMap().keySet()));
		List<Sequence> allSequences = model.getAllSequences();
		List<Animation> anims = model.getAnims();

		Pair<Map<Class<? extends IdObject>, Boolean>, String> weedOptions = askTypes();

		if (weedOptions != null) {
			Map<Class<? extends IdObject>, Boolean> doWeedMap = weedOptions.getFirst();
			String preserve = weedOptions.getSecond();

			List<IdObject> sortedIdObjects = getSortedIdObjects(model);
			Collections.reverse(sortedIdObjects);

			Set<IdObject> unusedObjects = getUnusedObjects(usedBones, allSequences, anims, doWeedMap, preserve, sortedIdObjects);

			System.out.println("bonesToRemove: " + unusedObjects.size());

			if (!unusedObjects.isEmpty()) {
				modelHandler.getUndoManager().pushAction(new DeleteNodesAction(unusedObjects, ModelStructureChangeListener.changeListener, model).redo());
			}
		}
	}

	private static Set<IdObject> getUnusedObjects(Set<Bone> usedBones,
	                                              List<Sequence> allSequences,
	                                              List<Animation> anims,
	                                              Map<Class<? extends IdObject>, Boolean> doWeedMap,
	                                              String preserve,
	                                              List<IdObject> sortedIdObjects) {
		Set<IdObject> unusedObjects = new HashSet<>();
		Set<IdObject> bindJnts = new LinkedHashSet<>();
		for (IdObject idObject : sortedIdObjects) {
			if (preserve != null && idObject.getName().toLowerCase().contains(preserve)) {
				bindJnts.add(idObject);
			}
			if (doWeedMap.containsKey(idObject.getClass())
					&& doWeedMap.get(idObject.getClass())
					&& unusedObjects.containsAll(idObject.getChildrenNodes())) {
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

	private static Pair<Map<Class<? extends IdObject>, Boolean>, String> askTypes(){
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose types to weed"), "wrap");

		Map<Class<? extends IdObject>, Boolean> doMap = new LinkedHashMap<>();
		doMap.put(Bone.class, true);
		doMap.put(Light.class, true);
		doMap.put(Helper.class, true);
		doMap.put(ParticleEmitter.class, true);
		doMap.put(ParticleEmitter2.class, true);
		doMap.put(ParticleEmitterPopcorn.class, true);
		doMap.put(RibbonEmitter.class, true);
		doMap.put(EventObject.class, true);
		for (Class<? extends IdObject> ugg : doMap.keySet()) {
			JCheckBox comp = new JCheckBox(ugg.getSimpleName(), doMap.get(ugg));
			comp.addActionListener(e -> doMap.put(ugg, comp.isSelected()));
			panel.add(comp, "wrap");
		}

		JCheckBox preserve = new JCheckBox("Preserve", true);
		preserve.setToolTipText("Don't remove this node or it's children even if unused. Use this to ensure nodes used by FaceFX is kept.");
		panel.add(preserve, "split");
		String[] bindJntName = {"hd_anim_bind_jnt"};
		panel.add(new TwiTextField(bindJntName[0], 12, s -> bindJntName[0] = s));

		int remove_unused_nodes = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), panel, "Remove Unused Nodes", JOptionPane.OK_CANCEL_OPTION);
		if (remove_unused_nodes == JOptionPane.OK_OPTION) {
			return new Pair<>(doMap, preserve.isSelected() ? bindJntName[0] : null);
		} else {
			return null;
		}
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
				for (Entry<Float> entry : emissionFlag.getEntryMap(sequence).values()) {
					if (entry.getValue() != null && entry.getValue() != 0
							|| entry.getInTan() != null && entry.getInTan() != 0
							|| entry.getOutTan() != null && entry.getOutTan() != 0) {
						return false;
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
}
