package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;

import java.util.ArrayList;
import java.util.List;

public class TempStuffFromEditableModel {
	public static EditableModel deepClone(final EditableModel what, final String newName) {
		// Need to do a real save, because of strings being passed by reference.
		// Maybe other objects I didn't think about (or the code does by mistake).
		final EditableModel newModel = TempOpenModelStuff.createEditableModel(new MdlxModel(TempSaveModelStuff.toMdlx(what).saveMdx()));

		newModel.setName(newName);
		newModel.setFileRef(what.getFile());

		return newModel;
	}

	/**
	 * Copies the animations from another model into this model. Specifically,
	 * copies all motion from similarly named bones and copies in the "Anim" blocks
	 * at the top of the MDL for the newly added sections.
	 *
	 * In addition, any bones with significant amounts of motion that were not found
	 * to correlate with the contents of this model get added to this model's list
	 * of bones.
	 */
	public static void addAnimationsFrom(EditableModel model, EditableModel other) {
		// this process destroys the "other" model inside memory, so destroy a copy instead
		other = deepClone(other, "animation source file");

		final List<AnimFlag<?>> flags = model.getAllAnimFlags();
		final List<EventObject> eventObjs = model.getEvents();

		final List<AnimFlag<?>> othersFlags = other.getAllAnimFlags();
		final List<EventObject> othersEventObjs = other.getEvents();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to the information specific to
		// each node about how it will move if it gets translated into or onto the current model

		final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		for (final AnimFlag<?> af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(af.getEmptyCopy());
			} else {
				newImpFlags.add(af.deepCopy());
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with the exact same data, but shifted
		// forward relative to wherever the current model's last animation starts
		for (final Animation anim : other.anims) {
			final int animTrackEnd = model.animTrackEnd();
			final int newStart = animTrackEnd + 300;
			final int newEnd = newStart + anim.length();
			final Animation newAnim = new Animation(anim);
			// clone the animation from the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			model.add(newAnim); // add the new animation to this model
		}

		// destroy the other model's animations, filling them in with the new stuff
		for (final AnimFlag<?> af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current
		// model
		final List<Bone> leftBehind = new ArrayList<>();
		// the bones that don't find matches in current model
//		for (final IdObject object : other.idObjects) {
		for (final IdObject object : other.getAllObjects()) {
			if (object instanceof Bone) {
				// the bone from the other model
				final Bone bone = (Bone) object;
				// the object in this model of similar name
				final Object localObject = model.getObject(bone.getName());
				if ((localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone); // if it's a match, take the data
				} else {
					leftBehind.add(bone);
				}
			}
		}
		for (final Bone bone : leftBehind) {
			if (bone.animates()) {
				model.add(bone);
			}
		}

		// i think we're done????
	}

	public static Object getAnimFlagSource(EditableModel model, final AnimFlag<?> animFlag) {
		// Probably will cause a bunch of lag, be wary
		for (final Material m : model.getMaterials()) {
			for (final Layer lay : m.getLayers()) {
				final AnimFlag<?> timeline = lay.find(animFlag.getName());
				if (timeline != null) {
					return lay;
				}
			}
		}
		if (model.getTexAnims() != null) {
			for (final TextureAnim textureAnim : model.getTexAnims()) {
				final AnimFlag<?> timeline = textureAnim.find(animFlag.getName());
				if (timeline != null) {
					return textureAnim;
				}
			}
		}
		if (model.getGeosetAnims() != null) {
			for (final GeosetAnim geosetAnim : model.getGeosetAnims()) {
				final AnimFlag<?> timeline = geosetAnim.find(animFlag.getName());
				if (timeline != null) {
					return geosetAnim;
				}
			}
		}

//		for (final IdObject object : idObjects) {
		for (final IdObject object : model.getAllObjects()) {
			final AnimFlag<?> timeline = object.find(animFlag.getName());
			if (timeline != null) {
				return object;
			}
		}

		if (model.getCameras() != null) {
			for (final Camera x : model.getCameras()) {
				AnimFlag<?> timeline = x.getSourceNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}

				timeline = x.getTargetNode().find(animFlag.getName());
				if (timeline != null) {
					return x;
				}
			}
		}

		return null;
	}

	/**
	 * Deletes all the animation in the model from the time track.
	 *
	 * Might leave behind nice things like global sequences if the code works out.
	 */
	public static void deleteAllAnimation(EditableModel model, final boolean clearUnusedNodes) {
		if (clearUnusedNodes) {
			// check the emitters
			final List<ParticleEmitter> particleEmitters = model.getParticleEmitters();
			final List<ParticleEmitter2> particleEmitters2 = model.getParticleEmitter2s();
			final List<RibbonEmitter> ribbonEmitters = model.getRibbonEmitters();
			final List<ParticleEmitterPopcorn> popcornEmitters = model.getPopcornEmitters();
			final List<IdObject> emitters = new ArrayList<>();
			emitters.addAll(particleEmitters2);
			emitters.addAll(particleEmitters);
			emitters.addAll(ribbonEmitters);
			emitters.addAll(popcornEmitters);

			for (final IdObject emitter : emitters) {
				int talliesFor = 0;
				int talliesAgainst = 0;
//				final AnimFlag<?> visibility = ((VisibilitySource) emitter).getVisibilityFlag();
				final AnimFlag<?> visibility = emitter.getVisibilityFlag();
				for (final Animation anim : model.getAnims()) {
					final Integer animStartTime = anim.getStart();
					final Number visible = (Number) visibility.valueAt(animStartTime);
					if ((visible == null) || (visible.floatValue() > 0)) {
						talliesFor++;
					} else {
						talliesAgainst++;
					}
				}
				if (talliesAgainst > talliesFor) {
					model.remove(emitter);
				}
			}
		}
		final List<AnimFlag<?>> flags = model.getAllAnimFlags();
//		final List<EventObject> evts = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> evts = model.getEvents();
		for (final Animation anim : model.getAnims()) {
			anim.clearData(flags, evts);
		}
		if (clearUnusedNodes) {
			for (final EventObject e : evts) {
				if (e.size() <= 0) {
					model.remove(e);
//					idObjects.remove(e);
				}
			}
		}
		model.clearAnimations();
	}
}
