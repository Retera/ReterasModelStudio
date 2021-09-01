package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SimplifyKeyframesAction implements UndoAction {
	Map<AnimFlag<?>, Map<Sequence, Set<Entry<?>>>> entriesToRemove = new HashMap<>();
//	Map<AnimFlag<Float>, Map<Sequence, Set<Entry<Float>>>> floatEntriesToRemove = new HashMap<>();
//	Map<AnimFlag<Vec3>, Map<Sequence, Set<Entry<Vec3>>>> vec3EntriesToRemove = new HashMap<>();
//	Map<AnimFlag<Quat>, Map<Sequence, Set<Entry<Quat>>>> quatEntriesToRemove = new HashMap<>();

	public SimplifyKeyframesAction(ModelView modelView, float trans, float scale, float rot) {
		List<AnimFlag<?>> transFlags = new ArrayList<>();
		List<AnimFlag<?>> scaleFlags = new ArrayList<>();
		List<AnimFlag<?>> rotFlags = new ArrayList<>();
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			if (trans >= 0 && idObject.has(MdlUtils.TOKEN_TRANSLATION)) {
				transFlags.add(idObject.find(MdlUtils.TOKEN_TRANSLATION));
			}
			if (scale >= 0 && idObject.has(MdlUtils.TOKEN_SCALING)) {
				scaleFlags.add(idObject.find(MdlUtils.TOKEN_SCALING));
			}
			if (rot >= 0 && idObject.has(MdlUtils.TOKEN_ROTATION)) {
				rotFlags.add(idObject.find(MdlUtils.TOKEN_ROTATION));
			}
		}
		findFramedToRemove(transFlags, modelView.getModel(), trans);
		findFramedToRemove(scaleFlags, modelView.getModel(), scale);
		findFramedToRemove(rotFlags, modelView.getModel(), rot);

	}

	public SimplifyKeyframesAction(Collection<AnimFlag<?>> animFlags, EditableModel model, float valueDiff) {
		findFramedToRemove(animFlags, model, valueDiff);
	}

	private void findFramedToRemove(Collection<AnimFlag<?>> animFlags, EditableModel model, float valueDiff) {
		List<Animation> anims = model.getAnims();

		for (AnimFlag<?> animFlag : animFlags) {
			if (!animFlag.hasGlobalSeq()) {
				for (Animation anim : anims) {
					removeTransitionalKeyframes2(animFlag, anim.getStart(), valueDiff, anim);
				}
			}
			for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
				if (animFlag.hasGlobalSeq() && animFlag.getGlobalSeq() == globalSeq) {
					removeTransitionalKeyframes2(animFlag, 0, valueDiff, globalSeq);
				}
			}
		}
	}

//	public void removeTransitionalKeyframes(AnimFlag<?> flag, int start, float valueDiff, Sequence anim) {
////		Set<Entry<?>> entrySet = entriesToRemove.computeIfAbsent(flag, eS -> new HashSet<>());
//		Map<Sequence, Set<Entry<?>>> entrySet1 = entriesToRemove.computeIfAbsent(flag, eS -> new HashMap<>());
//		Set<Entry<?>> entrySet = entrySet1.computeIfAbsent(anim, a -> new HashSet<>());
//
//		TreeMap<Integer, ? extends Entry<?>> entryMap = flag.getEntryMap(anim);
//		int end = entryMap.lastEntry().getKey();
//
//		Entry<?> entryBefore = null;
//		Entry<?> entryAfter = null;
//		Entry<?> entryBetween = null;
//		Integer beforeTime = 0;
//		Integer afterTime = 0;
//		float time_factor = 1;
////		for (Integer time = entryMap.ceilingKey(start); time != null && time <= entryMap.floorKey(end); time = entryMap.higherKey(time)) {
//		for (Integer time = entryMap.floorKey(end); time != null && time >= entryMap.ceilingKey(start); time = entryMap.lowerKey(time)) {
//			if (entryMap.lowerKey(time) != null && entryMap.higherKey(time) != null) {
//				beforeTime = entryMap.lowerKey(time);
//				afterTime = entryMap.higherKey(time);
//
//				time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);
//
//				entryBefore = entryMap.get(beforeTime);
//				entryAfter = entryMap.get(afterTime);
//				entryBetween = entryMap.get(time);
//			}
//
//
//			if (entryBetween != null && entryBefore != null && entryAfter != null) {
//				if (entryBetween.value instanceof Float && entryBefore.value instanceof Float && entryAfter.value instanceof Float) {
//					Float between = (Float) entryBetween.value;
//					Float before = (Float) entryBefore.value;
//					Float after = (Float) entryAfter.value;
//					if (MathUtils.isBetween2(before, after, between)) {
//						Float value = ((FloatAnimFlag) flag).getInterpolatedValue((Entry<Float>) entryBefore, (Entry<Float>) entryAfter, time_factor);
//						if (Math.abs(between - value) < valueDiff) {
//							entrySet.add(entryBetween);
//						}
//					}
//				} else if (entryBetween.value instanceof Vec3 && entryBefore.value instanceof Vec3 && entryAfter.value instanceof Vec3) {
//					Vec3 between = (Vec3) entryBetween.value;
//					Vec3 before = (Vec3) entryBefore.value;
//					Vec3 after = (Vec3) entryAfter.value;
//					if (MathUtils.isBetween2(before.x, after.x, between.x)
//							&& MathUtils.isBetween2(before.y, after.y, between.y)
//							&& MathUtils.isBetween2(before.z, after.z, between.z)) {
//
//						Vec3 value = ((Vec3AnimFlag) flag).getInterpolatedValue((Entry<Vec3>) entryBefore, (Entry<Vec3>) entryAfter, time_factor);
//						if (Math.abs(between.x - value.x) < valueDiff
//								|| Math.abs(between.y - value.y) < valueDiff
//								|| Math.abs(between.z - value.z) < valueDiff) {
//							entrySet.add(entryBetween);
//						}
//					}
//				} else if (entryBetween.value instanceof Quat) {
////					System.out.println("Quat");
//					Quat between = (Quat) entryBetween.value;
//					Quat before = (Quat) entryBefore.value;
//					Quat after = (Quat) entryAfter.value;
//
//					Vec3 betweenEuler = between.toEuler();
//					Vec3 beforeEuler = before.toEuler();
//					Vec3 afterEuler = after.toEuler();
//					if (MathUtils.isBetween2(beforeEuler.x, afterEuler.x, betweenEuler.x)
//							&& MathUtils.isBetween2(beforeEuler.y, afterEuler.y, betweenEuler.y)
//							&& MathUtils.isBetween2(beforeEuler.z, afterEuler.z, betweenEuler.z)) {
//
//						Quat value = ((QuatAnimFlag) flag).getInterpolatedValue((Entry<Quat>) entryBefore, (Entry<Quat>) entryAfter, time_factor);
//						if (Math.abs(between.x - value.x) < valueDiff
//								|| Math.abs(between.y - value.y) < valueDiff
//								|| Math.abs(between.z - value.z) < valueDiff) {
//							entrySet.add(entryBetween);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	//	public void removeTransitionalKeyframesVec3(Vec3AnimFlag flag, int start, float valueDiff) {
//	public void removeTransitionalKeyframesVec3(AnimFlag<Vec3> flag, int start, float valueDiff, Sequence anim) {
////		Set<Entry<?>> entriesToRemoveSet = entriesToRemove.computeIfAbsent(flag, eS -> new HashSet<>());
//		Map<Sequence, Set<Entry<?>>> entrySet1 = entriesToRemove.computeIfAbsent(flag, eS -> new HashMap<>());
//		Set<Entry<?>> entrySet = entrySet1.computeIfAbsent(anim, a -> new HashSet<>());
//
//		TreeMap<Integer, Entry<Vec3>> entryMap = flag.getEntryMap(anim);
//		int end = entryMap.lastEntry().getKey();
//
//		for (Integer time = entryMap.floorKey(end); time != null && time >= entryMap.ceilingKey(start); time = entryMap.lowerKey(time)) {
//			int beforeTime = entryMap.lowerKey(time) == null ? -1 : entryMap.lowerKey(time);
//			int afterTime = entryMap.higherKey(time) == null ? -1 : entryMap.higherKey(time);
//
//			float time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);
//
//			Entry<Vec3> entryBefore = entryMap.get(beforeTime);
//			Entry<Vec3> entryAfter = entryMap.get(afterTime);
//			Entry<Vec3> entryBetween = entryMap.get(time);
//
//			if (entryBetween != null && entryBefore != null && entryAfter != null) {
//				Vec3 between = entryBetween.value;
//				Vec3 before = entryBefore.value;
//				Vec3 after = entryAfter.value;
//				if (MathUtils.isBetween2(before.x, after.x, between.x)
//						&& MathUtils.isBetween2(before.y, after.y, between.y)
//						&& MathUtils.isBetween2(before.z, after.z, between.z)) {
//
//					Vec3 value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
//					if (Math.abs(between.x - value.x) < valueDiff
//							|| Math.abs(between.y - value.y) < valueDiff
//							|| Math.abs(between.z - value.z) < valueDiff) {
//						entrySet.add(entryBetween);
//					}
//				}
//			}
//		}
//	}
////	public void removeTransitionalKeyframes2(AnimFlag<Vec3> flag, int start, float valueDiff) {
////		Set<Entry<?>> entriesToRemoveSet = entriesToRemove.computeIfAbsent(flag, eS -> new HashSet<>());
////
////		TreeMap<Integer, Entry<Vec3>> entryMap = flag.getEntryMap();
////		int end = entryMap.lastEntry().getKey();
////
////		for (Integer time = entryMap.floorKey(end); time != null && time >= entryMap.ceilingKey(start); time = entryMap.lowerKey(time)) {
////			int beforeTime = entryMap.lowerKey(time) == null ? -1 : entryMap.lowerKey(time);
////			int afterTime = entryMap.higherKey(time) == null ? -1 : entryMap.higherKey(time);
////
////			float time_factor = (time - beforeTime) / (float)(afterTime - beforeTime);
////
////			Entry<Vec3> entryBefore = entryMap.get(beforeTime);
////			Entry<Vec3> entryAfter = entryMap.get(afterTime);
////			Entry<Vec3> entryBetween = entryMap.get(time);
////
////			if (entryBetween != null && entryBefore != null && entryAfter != null) {
////				Vec3 value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
////				boolean toRemove = isToRemove(valueDiff, entryBetween.value, entryBefore.value, entryAfter.value, value);
////				if (toRemove){
////					entriesToRemoveSet.add(entryBetween);
////				}
////			}
////		}
////	}

	public <T> void removeTransitionalKeyframes2(AnimFlag<T> flag, int start, float valueDiff, Sequence anim) {
//		Set<Entry<?>> entriesToRemoveSet = entriesToRemove.computeIfAbsent(flag, eS -> new HashSet<>());
		Map<Sequence, Set<Entry<?>>> entrySet1 = entriesToRemove.computeIfAbsent(flag, eS -> new HashMap<>());
		Set<Entry<?>> entrySet = entrySet1.computeIfAbsent(anim, a -> new HashSet<>());

		TreeMap<Integer, Entry<T>> entryMap = flag.getEntryMap(anim);
		int end = entryMap.lastEntry().getKey();

		for (Integer time = entryMap.floorKey(end); time != null && entryMap.ceilingKey(start) != null && time >= entryMap.ceilingKey(start); time = entryMap.lowerKey(time)) {
			int beforeTime = entryMap.lowerKey(time) == null ? -1 : entryMap.lowerKey(time);
			int afterTime = entryMap.higherKey(time) == null ? -1 : entryMap.higherKey(time);

			float time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);

			Entry<T> entryBefore = entryMap.get(beforeTime);
			Entry<T> entryAfter = entryMap.get(afterTime);
			Entry<T> entryBetween = entryMap.get(time);

			if (entryBetween != null && entryBefore != null && entryAfter != null) {
				T value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
				boolean toRemove = false;
				if (entryBetween.value instanceof Float) {
					toRemove = isToRemove(valueDiff, (float) entryBetween.value, (float) entryBefore.value, (float) entryAfter.value, (float) value);
				}
				if (entryBetween.value instanceof Vec3) {
					toRemove = isToRemove(valueDiff, (Vec3) entryBetween.value, (Vec3) entryBefore.value, (Vec3) entryAfter.value, (Vec3) value);

				}
				if (entryBetween.value instanceof Quat) {
					toRemove = isToRemove(valueDiff, (Quat) entryBetween.value, (Quat) entryBefore.value, (Quat) entryAfter.value, (Quat) value);

				}
				if (toRemove) {
					entrySet.add(entryBetween);
				}
			}
		}
	}

	private boolean isToRemove(float valueDiff, float between, float before, float after, float value) {
		if (MathUtils.isBetween2(before, after, between)) {

			return Math.abs(between - value) < valueDiff;
		}
		return false;
	}

	private boolean isToRemove(float valueDiff, Vec3 between, Vec3 before, Vec3 after, Vec3 value) {
		if (MathUtils.isBetween2(before.x, after.x, between.x)
				&& MathUtils.isBetween2(before.y, after.y, between.y)
				&& MathUtils.isBetween2(before.z, after.z, between.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	private boolean isToRemove(float valueDiff, Quat between, Quat before, Quat after, Quat value) {
		Vec3 betweenEuler = between.toEuler();
		Vec3 beforeEuler = before.toEuler();
		Vec3 afterEuler = after.toEuler();

		if (MathUtils.isBetween2(beforeEuler.x, afterEuler.x, betweenEuler.x)
				&& MathUtils.isBetween2(beforeEuler.y, afterEuler.y, betweenEuler.y)
				&& MathUtils.isBetween2(beforeEuler.z, afterEuler.z, betweenEuler.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	@Override
	public UndoAction undo() {
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			Map<Sequence, Set<Entry<?>>> entryMap = entriesToRemove.get(animFlag);
			for (Sequence anim : entryMap.keySet()) {
				for (Entry<?> entry : entryMap.get(anim)) {
					animFlag.setOrAddEntryT(entry.getTime(), entry, anim);
				}
			}
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (AnimFlag<?> animFlag : entriesToRemove.keySet()) {
			Map<Sequence, Set<Entry<?>>> entryMap = entriesToRemove.get(animFlag);
			for (Sequence anim : entryMap.keySet()) {
				for (Entry<?> entry : entryMap.get(anim)) {
					animFlag.removeKeyframe(entry.getTime(), anim);
				}
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "simplify keyframes";
	}
}
