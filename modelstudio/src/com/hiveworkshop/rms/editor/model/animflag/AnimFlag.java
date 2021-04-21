package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public abstract class AnimFlag<T> {
	// Types of AnimFlags:
	// 0 Alpha
	public static final int ALPHA = 0;
	// 1 Scaling
	public static final int SCALING = 1;
	// 2 Rotation
	public static final int ROTATION = 2;
	// 3 Translation
	public static final int TRANSLATION = 3;
	// 4 Color
	public static final int COLOR = 4;
	// 5 TextureID
	public static final int TEXTUREID = 5;

	/**
	 * Use for titles like "Intensity", "AmbIntensity", and other extraneous things
	 * not included in the options above.
	 */
	public static final int OTHER_TYPE = 0;
	public static final Quat ROTATE_IDENTITY = new Quat(0, 0, 0, 1);
	public static final Vec3 SCALE_IDENTITY = new Vec3(1, 1, 1);
	public static final Vec3 TRANSLATE_IDENTITY = new Vec3(0, 0, 0);
	String name;
	public InterpolationType interpolationType = InterpolationType.DONT_INTERP;
	public Integer globalSeq;
	int globalSeqId = -1;
	public boolean hasGlobalSeq = false;
	protected List<Integer> times = new ArrayList<>();
	protected List<T> values = new ArrayList<>();
	protected List<T> inTans = new ArrayList<>();
	protected List<T> outTans = new ArrayList<>();
	int typeid = 0;
	int vectorSize = 1;
	boolean isFloat = true;

	public AnimFlag(final MdlxTimeline<?> timeline) {
		name = AnimationMap.ID_TO_TAG.get(timeline.name).getMdlToken();
		generateTypeId();

		interpolationType = timeline.interpolationType;

		final int globalSequenceId = timeline.globalSequenceId;
		if (globalSequenceId >= 0) {
			setGlobalSeqId(globalSequenceId);
			setHasGlobalSeq(true);
		}
	}

	// end special constructors
	public AnimFlag(final String title, final List<Integer> times, final List<T> values) {
		name = title;
		this.times = times;
		this.values = values;
		generateTypeId();
	}

	public AnimFlag(final AnimFlag<T> af) {
		name = af.name;
		globalSeq = af.globalSeq;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;
		times = new ArrayList<>(af.times);
		values = deepCopy(af.values);
		inTans = deepCopy(af.inTans);
		outTans = deepCopy(af.outTans);
		vectorSize = af.vectorSize;
		isFloat = af.isFloat;
	}

//	public static AnimFlag<?> createFromName(final AnimFlag<?> af) {
//		if(af instanceof IntAnimFlag){
//			return new IntAnimFlag((IntAnimFlag) af);
//		}else if(af instanceof FloatAnimFlag){
//			return new FloatAnimFlag((FloatAnimFlag) af);
//		}else if(af instanceof Vec3AnimFlag){
//			return new Vec3AnimFlag((Vec3AnimFlag) af);
//		}else if(af instanceof QuatAnimFlag){
//			return new QuatAnimFlag((QuatAnimFlag) af);
//		}
//		else return null;
//	}


//	public static AnimFlag createEmpty2018(final String title, final InterpolationType interpolationType, final Integer globalSeq) {
//		final AnimFlag flag = new AnimFlag();
//		flag.name = title;
//		flag.interpolationType = interpolationType;
//		flag.generateTypeId();
//		flag.setGlobSeq(globalSeq);
//		return flag;
//	}

	public AnimFlag(final String title) {
		name = title;
		generateTypeId();
	}

	public static AnimFlag<?> createFromTimeline(final MdlxTimeline<?> timeline) {
		Object firstValue = timeline.values[0];
		if (firstValue instanceof float[]) {
			if (((float[]) firstValue).length == 1) {
				return new FloatAnimFlag((MdlxFloatTimeline) timeline);
			} else if (((float[]) firstValue).length == 3) {
				return new Vec3AnimFlag((MdlxFloatArrayTimeline) timeline);
			} else if (((float[]) firstValue).length == 4) {
				return new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
			}
		} else if (firstValue instanceof long[]) {
			if (timeline.name.toString().equalsIgnoreCase("rotation")) {
				return new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
			}
			return new IntAnimFlag((MdlxUInt32Timeline) timeline);
//			return new IntAnimFlag((MdlxTimeline<long[]>) timeline);
		}
		return null;
	}

	public static AnimFlag<?> createFromAnimFlag(final AnimFlag<?> af) {
		if (af instanceof IntAnimFlag) {
			return new IntAnimFlag((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag) {
			return new FloatAnimFlag((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag) {
			return new Vec3AnimFlag((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag) {
			return new QuatAnimFlag((QuatAnimFlag) af);
		} else return null;
	}

	public static Object cloneValue(final Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer || value instanceof Float) {
			return value;
		} else if (value instanceof Vec3) {
			return new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return new Quat((Quat) value);
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public static Object cloneValueAsEmpty(final Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer) {
			return 0;
		} else if (value instanceof Float) {
			return 0f;
		} else if (value instanceof Vec3) {
			return new Vec3();
		} else if (value instanceof Quat) {
			return new Quat();
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public static AnimFlag<?> buildEmptyFrom(final AnimFlag<?> af) {
		if (af instanceof IntAnimFlag) {
			return new IntAnimFlag((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag) {
			return new FloatAnimFlag((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag) {
			return new Vec3AnimFlag((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag) {
			return new QuatAnimFlag((QuatAnimFlag) af);
		} else return null;
	}

	public boolean equals(final AnimFlag<T> animFlag) {
		boolean does = animFlag != null;
		if (!does) {
			return false;
		}
		does = (name.equals(animFlag.getName()))
				|| (hasGlobalSeq == animFlag.hasGlobalSeq)
				|| (values.equals(animFlag.values)
				&& (Objects.equals(globalSeq, animFlag.globalSeq))
				&& (interpolationType == animFlag.interpolationType)
				&& (Objects.equals(inTans, animFlag.inTans))
				&& (Objects.equals(outTans, animFlag.outTans))
				&& (typeid == animFlag.typeid));
		return does;
	}

	public Integer getGlobalSeq() {
		return globalSeq;
	}

	public void setGlobalSeq(final Integer globalSeq) {
		this.globalSeq = globalSeq;
	}

	public void setGlobSeq(final Integer integer) {
		globalSeq = integer;
		hasGlobalSeq = integer != null;
	}

	public War3ID getWar3ID(final TimelineContainer container) {
		final AnimationMap id = getAnimationMap(container);

		if (id == null) {
			throw new RuntimeException("Got an unknown timeline name: " + name);
		}

		return id.getWar3id();
	}

	public AnimationMap getAnimationMap(final TimelineContainer container) {
		if (container instanceof Layer) {
			switch (name) {
				case MdlUtils.TOKEN_TEXTURE_ID:
					return AnimationMap.KMTF;
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KMTA;
				case MdlUtils.TOKEN_EMISSIVE_GAIN:
					return AnimationMap.KMTE;
				case MdlUtils.TOKEN_EMISSIVE:
					return AnimationMap.KMTE;
				case MdlUtils.TOKEN_FRESNEL_COLOR:
					return AnimationMap.KFC3;
				case MdlUtils.TOKEN_FRESNEL_OPACITY:
					return AnimationMap.KFCA;
				case MdlUtils.TOKEN_FRESNEL_TEAM_COLOR:
					return AnimationMap.KFTC;
			}
		} else if (container instanceof TextureAnim) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KTAT;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KTAR;
				case MdlUtils.TOKEN_SCALING:
					return AnimationMap.KTAS;
			}
		} else if (container instanceof GeosetAnim) {
			switch (name) {
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KGAO;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KGAC;
			}
		} else if (container instanceof Light) {
			switch (name) {
				case MdlUtils.TOKEN_ATTENUATION_START:
					return AnimationMap.KLAS;
				case MdlUtils.TOKEN_ATTENUATION_END:
					return AnimationMap.KLAE;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KLAC;
				case MdlUtils.TOKEN_INTENSITY:
					return AnimationMap.KLAI;
				case MdlUtils.TOKEN_AMB_INTENSITY:
					return AnimationMap.KLBI;
				case MdlUtils.TOKEN_AMB_COLOR:
					return AnimationMap.KLBC;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KLAV;
			}
		} else if (container instanceof Attachment) {
			switch (name) {
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KATV;
			}
		} else if (container instanceof ParticleEmitter) {
			switch (name) {
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KPEE;
				case MdlUtils.TOKEN_GRAVITY:
					return AnimationMap.KPEG;
				case MdlUtils.TOKEN_LONGITUDE:
					return AnimationMap.KPLN;
				case MdlUtils.TOKEN_LATITUDE:
					return AnimationMap.KPLT;
				case MdlUtils.TOKEN_LIFE_SPAN:
					return AnimationMap.KPEL;
				case MdlUtils.TOKEN_INIT_VELOCITY:
					return AnimationMap.KPES;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KPEV;
			}
		} else if (container instanceof ParticleEmitter2) {
			switch (name) {
				case MdlUtils.TOKEN_SPEED:
					return AnimationMap.KP2S;
				case MdlUtils.TOKEN_VARIATION:
					return AnimationMap.KP2R;
				case MdlUtils.TOKEN_LATITUDE:
					return AnimationMap.KP2L;
				case MdlUtils.TOKEN_GRAVITY:
					return AnimationMap.KP2G;
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KP2E;
				case MdlUtils.TOKEN_LENGTH:
					return AnimationMap.KP2N;
				case MdlUtils.TOKEN_WIDTH:
					return AnimationMap.KP2W;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KP2V;
			}
		} else if (container instanceof ParticleEmitterPopcorn) {
			switch (name) {
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KPPA;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KPPC;
				case MdlUtils.TOKEN_EMISSION_RATE:
					return AnimationMap.KPPE;
				case MdlUtils.TOKEN_LIFE_SPAN:
					return AnimationMap.KPPL;
				case MdlUtils.TOKEN_SPEED:
					return AnimationMap.KPPS;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KPPV;
			}
		} else if (container instanceof RibbonEmitter) {
			switch (name) {
				case MdlUtils.TOKEN_HEIGHT_ABOVE:
					return AnimationMap.KRHA;
				case MdlUtils.TOKEN_HEIGHT_BELOW:
					return AnimationMap.KRHB;
				case MdlUtils.TOKEN_ALPHA:
					return AnimationMap.KRAL;
				case MdlUtils.TOKEN_COLOR:
					return AnimationMap.KRCO;
				case MdlUtils.TOKEN_TEXTURE_SLOT:
					return AnimationMap.KRTX;
				case MdlUtils.TOKEN_VISIBILITY:
					return AnimationMap.KRVS;
			}
		} else if (container instanceof Camera.SourceNode) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KCTR;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KCRL;
			}
		} else if (container instanceof Camera.TargetNode) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KTTR;
			}
		}

		if (container instanceof IdObject) {
			switch (name) {
				case MdlUtils.TOKEN_TRANSLATION:
					return AnimationMap.KGTR;
				case MdlUtils.TOKEN_ROTATION:
					return AnimationMap.KGRT;
				case MdlUtils.TOKEN_SCALING:
					return AnimationMap.KGSC;
			}
		}

		return null;
	}

	protected void setVectorSize(Object value) {
		if (value instanceof float[]) {
			vectorSize = ((float[]) value).length;
		} else if (value instanceof Vec3) {
			vectorSize = 3;
		} else if (value instanceof Vec4) {
			vectorSize = 4;
		} else if (value.getClass().getName().equals("java.lang.Float") || value.getClass().getName().equals("java.lang.Integer")) {
			vectorSize = 1;
		} else {
			isFloat = false;
//			System.out.println("value class: " + value.getClass().getName());
//			System.out.println("long[].class: " + long[].class.getName());
//			System.out.println("long[].class: " + long[].class.getName());
			vectorSize = ((long[]) value).length;
		}
	}

	public void setInterpType(final InterpolationType interpolationType) {
		if (interpolationType.tangential() && inTans.isEmpty()) {
			unLinearize();
		} else if (!interpolationType.tangential()) {
			linearize();
		}
		this.interpolationType = interpolationType;
	}

	public InterpolationType getInterpolationType() {
		return interpolationType;
	}

	public int size() {
		return times.size();
	}

	public abstract MdlxTimeline<?> toMdlx(final TimelineContainer container);

	public int getGlobalSeqId() {
		return globalSeqId;
	}

	public void setGlobalSeqId(final int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean hasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(final boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public void addEntry(final Integer time, final T value) {
		times.add(time);
		values.add(value);
	}

	public void addEntry(final Integer time, final T value, final T inTan, final T outTan) {
		times.add(time);
		values.add(value);

		if (inTan != null && outTan != null) {
			inTans.add(inTan);
			outTans.add(outTan);
		}
	}

	public void addEntry(Entry<T> entry) {
		int keyframeIndex = getKeyframeCeilIndex(entry.time);
		times.add(keyframeIndex, entry.time);
		values.add(keyframeIndex, entry.value);

		if (entry.inTan != null && entry.outTan != null) {
			inTans.add(keyframeIndex, entry.inTan);
			outTans.add(keyframeIndex, entry.outTan);
		}
	}

//	public abstract T cloneValue(final T value);

	public void setEntry(final Integer time, final T value) {
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
				if (tans()) {
					inTans.set(index, value);
					outTans.set(index, value);
				}
			}
		}
	}

	/**
	 * To set an Entry with an Entry
	 *
	 * @param time  the time of the entry to be changed
	 * @param entry the entry to replace the old entry
	 */
	public void setEntryT(final Integer time, Entry<?> entry) {
		if (entry.value instanceof Integer && this instanceof IntAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Float && this instanceof FloatAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Vec3 && this instanceof Vec3AnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Quat && this instanceof QuatAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		}
	}

	public void setEntry(final Integer time, Entry<T> entry) {
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				times.set(index, entry.time);
				values.set(index, entry.value);
				if (tans()) {
					inTans.set(index, entry.inTan);
					outTans.set(index, entry.outTan);
				}
			}
		}
	}

	public void setOrAddEntryT(final Integer time, Entry<?> entry) {
		if (entry.value instanceof Integer && this instanceof IntAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Float && this instanceof FloatAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Vec3 && this instanceof Vec3AnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Quat && this instanceof QuatAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		}
	}

	public void setOrAddEntry(final Integer time, Entry<T> entry) {
		System.out.println("vec3 set entry");
		int index = floorIndex(time);
		if (!times.get(index).equals(time)) {
			times.add(index + 1, time);
			values.add(index + 1, entry.value);
			if (tans()) {
				inTans.add(index + 1, entry.inTan);
				outTans.add(index + 1, entry.outTan);
			}
		} else {
			times.set(index, time);
			values.set(index, entry.value);
			if (tans()) {
				inTans.set(index, entry.inTan);
				outTans.set(index, entry.outTan);
			}
		}
	}

	public void setEntry(Entry<T> entry) {
		setEntry(entry.time, entry);
	}

	public Entry<T> getEntry(final int index) {
		if (tans()) {
			return new Entry<T>(times.get(index), values.get(index), inTans.get(index), outTans.get(index));
		} else {
			return new Entry<T>(times.get(index), values.get(index));
		}
	}

	public Entry<T> getEntryAt(final int time) {
		int index = ceilIndex(time);
		if (times.get(index) == time) {
			return getEntry(index);
		}
		return null;
	}

	public T valueAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return values.get(i);
			}
		}
		return null;
	}

	public T inTanAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return inTans.get(i);
			}
		}
		return null;
	}

	public T outTanAt(final Integer time) {
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i).equals(time)) {
				return outTans.get(i);
			}
		}
		return null;
	}

	public void setValuesTo(final AnimFlag<?> af) {
		if (af instanceof IntAnimFlag && this instanceof IntAnimFlag) {
			((IntAnimFlag) this).setValuesTo((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag && this instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).setValuesTo((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag && this instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).setValuesTo((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag && this instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).setValuesTo((QuatAnimFlag) af);
		}
	}


	public abstract void setValuesTo2(final AnimFlag<T> af);

	public void setValuesTo3(final AnimFlag<T> af) {
		name = af.name;
		globalSeq = af.globalSeq;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;
		times = new ArrayList<>(af.times);
		values = deepCopy(af.values);
		inTans = deepCopy(af.inTans);
		outTans = deepCopy(af.outTans);
	}

	List<T> deepCopy(final List<T> source) {

		final List<T> copy = new ArrayList<>();
		for (final T item : source) {
			T toAdd = item;
			if (item instanceof Vec3) {
				final Vec3 v = (Vec3) item;
				toAdd = (T) v;
			} else if (item instanceof Quat) {
				final Quat r = (Quat) item;
				toAdd = (T) r;
			}
			copy.add(toAdd);
		}
		return copy;
	}

	public String getName() {
		return name;
	}

	public void setName(final String title) {
		name = title;
	}

	public int getTypeId() {
		return typeid;
	}

	public void updateGlobalSeqRef(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeq = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeq);
		}
	}

	public void generateTypeId() {
		typeid = switch (name) {
			case "Scaling" -> 1;
			case "Rotation" -> 2;
			case "Translation" -> 3;
			case "TextureID" -> 5; // aflg.title.equals("Visibility") || -- 100.088% visible in UndeadCampaign3D OutTans! Golook!
			case "Color" -> 4;
			default -> 0;
		};
	}

	public void flipOver(final byte axis) {
		if (typeid == 2) {
			// Rotation
			flipAll(axis, values);
			flipAll(axis, inTans);
			flipAll(axis, outTans);
		} else if (typeid == 3) {
			// Translation
			for (final Object value : values) {
				final Vec3 trans = (Vec3) value;

				trans.setCoord(axis, -trans.getCoord(axis));
			}

			for (final Object inTan : inTans) {
				final Vec3 trans = (Vec3) inTan;

				trans.setCoord(axis, -trans.getCoord(axis));
			}

			for (final Object outTan : outTans) {
				final Vec3 trans = (Vec3) outTan;

				trans.setCoord(axis, -trans.getCoord(axis));
			}
		}
	}

	private void flipAll(byte axis, List<T> values) {
		for (final T value : values) {
			final Quat rot = (Quat) value;
			final Vec3 euler = rot.toEuler();
			switch (axis) {
				case 0 -> {
					euler.x = -euler.x;
					euler.y = -euler.y;
				}
				case 1 -> {
					euler.x = -euler.x;
					euler.z = -euler.z;
				}
				case 2 -> {
					euler.y = -euler.y;
					euler.z = -euler.z;
				}
			}
			rot.set(euler);
		}
	}

	public AnimFlag<T> getMostVisible(final AnimFlag<T> partner) {
		if (partner != null) {
			if ((typeid == 0) && (partner.typeid == 0)) {
				final List<Integer> atimes = new ArrayList<>(times);
				final List<Integer> btimes = new ArrayList<>(partner.times);
				final List<T> avalues = new ArrayList<>(values);
				final List<T> bvalues = new ArrayList<>(partner.values);

				AnimFlag<T> mostVisible = null;
				mostVisible = getMostVissibleAnimFlag(atimes, btimes, avalues, bvalues, mostVisible, partner, this);
				if (mostVisible == null) return null;

				mostVisible = getMostVissibleAnimFlag(btimes, atimes, bvalues, avalues, mostVisible, this, partner);
				if (mostVisible == null) return null;

				// partner has priority!
				return Objects.requireNonNullElse(mostVisible, partner);
			} else {
				JOptionPane.showMessageDialog(null,
						"Error: Program attempted to compare visibility with non-visibility animation component.\nThis... probably means something is horribly wrong. Save your work, if you can.");
			}
		}
		return null;
	}

	private AnimFlag<T> getMostVissibleAnimFlag(List<Integer> atimes, List<Integer> btimes, List<T> avalues, List<T> bvalues, AnimFlag<T> mostVisible, AnimFlag<T> flag1, AnimFlag<T> flag2) {
		for (int i = atimes.size() - 1; i >= 0; i--)
		// count down from top, meaning that removing the current value causes no harm
		{
		}
		return mostVisible;
	}

	public boolean tans() {
		return interpolationType.tangential() && inTans.size() > 0;
	}

	public void linearize() {
		if (interpolationType.tangential()) {
			interpolationType = InterpolationType.LINEAR;
			inTans.clear();
			outTans.clear();
		}
	}

	public void unLinearize() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.BEZIER;

			inTans.addAll(deepCopy(values));
			outTans.addAll(deepCopy(values));
		}
	}

	public void setInterpolationType(InterpolationType interpolationType) {
		this.interpolationType = interpolationType;
		if (interpolationType.tangential()) {
			inTans.addAll(values);
			outTans.addAll(values);
		} else {
			linearize();
		}
	}

	public void deleteAnim(final Animation anim) {
		if (!hasGlobalSeq) {
			for (int index = times.size() - 1; index >= 0; index--) {
				final int time = times.get(index);
				if ((time >= anim.getStart()) && (time <= anim.getEnd())) {
					// If this "time" is a part of the anim being removed
					deleteAt(index);
				}
			}
		} else {
			System.out.println("KeyFrame deleting was blocked by a GlobalSequence");
		}
	}

	public void deleteAt(final int index) {
		times.remove(index);
		values.remove(index);
		if (tans()) {
			inTans.remove(index);
			outTans.remove(index);
		}
	}

	public void clear() {
		times.clear();
		values.clear();
		inTans.clear();
		outTans.clear();
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 * Does not check that the destination interval is empty!
	 *
	 * @param source      the AnimFlag which values will be copied
	 * @param sourceStart the start time for the interval to be copied, inclusive
	 * @param sourceEnd   the end time for the interval to be copied, inclusive
	 * @param newStart    the start time for the interval in the destination AnimFlag, inclusive
	 * @param newEnd      the end time for the interval in the destination AnimFlag, inclusive
	 */
	public void copyFrom(final AnimFlag<?> source, final int sourceStart, final int sourceEnd, final int newStart, final int newEnd) {
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag) {
			((IntAnimFlag) this).copyFrom((IntAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof FloatAnimFlag && source instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).copyFrom((FloatAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).copyFrom((Vec3AnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).copyFrom((QuatAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
	}

	public void copyFrom(final AnimFlag<?> source) {
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag) {
			((IntAnimFlag) this).copyFrom((IntAnimFlag) source);
		}
		if (this instanceof FloatAnimFlag && source instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).copyFrom((FloatAnimFlag) source);
		}
		if (this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).copyFrom((Vec3AnimFlag) source);
		}
		if (this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).copyFrom((QuatAnimFlag) source);
		}
	}

	public void timeScale(final int start, final int end, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
		for (int index = 0; index < times.size(); index++) {
			final int time = times.get(index);
			if ((time >= start) && (time <= end)) {
				// If this "time" is a part of the anim being rescaled
				final double ratio = (double) (time - start) / (double) (end - start);
				times.set(index, (int) (newStart + (ratio * (newEnd - newStart))));
			}
		}
		sort();
	}

	public void sort() {
		final int low = 0;
		final int high = times.size() - 1;
		if (size() > 1) {
			quicksort(low, high);
		}
	}

	private void quicksort(final int low, final int high) {
		// Thanks to Lars Vogel for the quicksort concept code (something to look at), found on google
		// (re-written by Eric "Retera" for use in AnimFlags)
		int i = low, j = high;
		final Integer pivot = times.get(low + ((high - low) / 2));

		while (i <= j) {
			while (times.get(i) < pivot) {
				i++;
			}
			while (times.get(j) > pivot) {
				j--;
			}
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j) {
			quicksort(low, j);
		}
		if (i < high) {
			quicksort(i, high);
		}
	}

	private void exchange(final int i, final int j) {
		final Integer iTime = times.get(i);
		final T iValue = values.get(i);

		times.set(i, times.get(j));
		try {
			values.set(i, values.get(j));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		times.set(j, iTime);
		values.set(j, iValue);

		if (inTans.size() > 0)// if we have to mess with Tans
		{
			final T iInTan = inTans.get(i);
			final T iOutTan = outTans.get(i);

			inTans.set(i, inTans.get(j));
			outTans.set(i, outTans.get(j));

			inTans.set(j, iInTan);
			outTans.set(j, iOutTan);
		}
	}

	public List<Integer> getTimes() {
		return times;
	}

	public List<T> getValues() {
		return values;
	}

	public List<T> getInTans() {
		return inTans;
	}

	public List<T> getOutTans() {
		return outTans;
	}

	public int ceilIndex(final int time) {
		if (times.size() == 0) {
			return 0;
		}
		final int ceilIndex = ceilIndex(time, 0, times.size() - 1);
		if (ceilIndex == -1) {
			return times.size() - 1;
		}
		return ceilIndex;
	}

	/*
	 * Rather than spending time visualizing corner cases for these, I borrowed
	 * logic from: https://www.geeksforgeeks.org/ceiling-in-a-sorted-array/
	 */
	private int ceilIndex(final int time, final int timeStartIndex, final int timeEndIndex) {
		if (time <= times.get(timeStartIndex)) {
			return timeStartIndex;
		}
		if (time > times.get(timeEndIndex)) {
			return -1;
		}

		if (timeEndIndex - timeStartIndex < 10) {
			for (int i = timeStartIndex; i <= timeEndIndex; i++) {
				if (times.get(i) == time || time < times.get(i) && (i > 0) && time > (times.get(i - 1))) {
					return i;
				}
			}
		}

		final int midIndex = (timeStartIndex + timeEndIndex) / 2;
		final Integer midTime = times.get(midIndex);
		if (midTime < time) {
			if (((midIndex + 1) <= timeEndIndex) && (time <= times.get(midIndex + 1))) {
				return midIndex + 1;
			} else {
				return ceilIndex(time, midIndex + 1, timeEndIndex);
			}
		} else {
			if (((midIndex - 1) >= timeStartIndex) && (time > times.get(midIndex - 1))) {
				return midIndex;
			} else {
				return ceilIndex(time, timeStartIndex, midIndex - 1);
			}
		}
	}

	public int floorIndex(final int time) {
		if (times.size() == 0) {
			return -1;
		}
		return floorIndex(time, 0, times.size() - 1);
	}

	/*
	 * Rather than spending time visualizing corner cases for these, I borrowed
	 * logic from: https://www.geeksforgeeks.org/floor-in-a-sorted-array/
	 */
	private int floorIndex(final int time, final int timeStartIndex, final int timeEndIndex) {
		if (timeStartIndex > timeEndIndex) {
			return -1;
		} else if (time >= times.get(timeEndIndex)) {
			return timeEndIndex;
		}
		if (timeEndIndex - timeStartIndex < 10) {
			for (int i = timeStartIndex; i <= timeEndIndex; i++) {
				if (times.get(i) == time) {
					return i;
				} else if (time < times.get(i) && (i > 0) && (times.get(i - 1) <= time)) {
					return i - 1;
				}
			}
		}
		final int mid = (timeStartIndex + timeEndIndex) / 2;
		final Integer midTime = times.get(mid);
		if (times.get(mid) == time) {
			return mid;
		}
		if ((time < midTime) && (mid > 0) && (time >= times.get(mid - 1))) {
			return mid - 1;
		}
		if (time > midTime) {
			return floorIndex(time, mid + 1, timeEndIndex);
		} else {
			return floorIndex(time, timeStartIndex, mid - 1);
		}
	}

	protected Object identity(final int typeid) {
		return switch (typeid) {
			case ALPHA -> 1.f;
			case TRANSLATION -> TRANSLATE_IDENTITY;
			case SCALING, COLOR -> SCALE_IDENTITY;
			case ROTATION -> ROTATE_IDENTITY;
			case TEXTUREID -> {
				System.err.println("Texture identity used in renderer... TODO make this function more intelligent.");
				yield 0;
			}
			default -> throw new IllegalStateException("Unexpected value: " + typeid);
		};
	}

	/**
	 * Interpolates at a given time. The lack of generics on this function is
	 * abysmal, but currently this is how the codebase is.
	 */
	public Object interpolateAt(final AnimatedRenderEnvironment animatedRenderEnvironment) {
//		System.out.println(name + ", interpolateAt");
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
//			System.out.println("~~ animatedRenderEnvironment == null");
			if (values.size() > 0) {
				return values.get(0);
			}
			return identity(typeid);
		}
		int localTypeId = typeid;
//		System.out.println("typeId 1: " + typeid);
		if ((localTypeId == ROTATION) && (size() > 0) && (values.get(0) instanceof Float)) {
			localTypeId = ALPHA; // magic Camera rotation!
		}
		if (times.isEmpty()) {
//			System.out.println(name + ", ~~ no times");
			return identity(localTypeId);
		}
		// TODO ghostwolf says to stop using binary search, because linear walking is faster for the small MDL case
		final int time;
		int ceilIndex;
		final int floorIndex;
		Object floorInTan;
		Object floorOutTan;
		Object floorValue;
		Object ceilValue;
		Integer floorIndexTime;
		Integer ceilIndexTime;
		final float timeBetweenFrames;
		if (hasGlobalSeq() && (getGlobalSeq() >= 0)) {
//			System.out.println(name + ", ~~ hasGlobalSeq");
			time = animatedRenderEnvironment.getGlobalSeqTime(getGlobalSeq());
			final int floorAnimStartIndex = Math.max(0, floorIndex(1));
			final int floorAnimEndIndex = Math.max(0, floorIndex(getGlobalSeq()));
			floorIndex = Math.max(0, floorIndex(time));

			ceilIndex = Math.max(floorIndex, ceilIndex(time)); // retarded repeated keyframes issue, see Peasant's Bone_Chest at time 18300

			floorValue = values.get(floorIndex);
			floorInTan = tans() ? inTans.get(floorIndex) : null;
			floorOutTan = tans() ? outTans.get(floorIndex) : null;
			ceilValue = values.get(ceilIndex);
			floorIndexTime = times.get(floorIndex);
			ceilIndexTime = times.get(ceilIndex);
			timeBetweenFrames = ceilIndexTime - floorIndexTime;
			if (ceilIndexTime < 0) {
				return identity(localTypeId);
			}
			if (floorIndexTime > getGlobalSeq()) {
				if (values.size() > 0) {
					// out of range global sequences end up just using the higher value keyframe
					return values.get(floorIndex);
				}
				return identity(localTypeId);
			}
			if ((floorIndexTime < 0) && (ceilIndexTime > getGlobalSeq())) {
				return identity(localTypeId);
			} else if (floorIndexTime < 0) {
				floorValue = identity(localTypeId);
				floorInTan = floorOutTan = identity(localTypeId);
			} else if (ceilIndexTime > getGlobalSeq()) {
				ceilValue = values.get(floorAnimStartIndex);
				ceilIndex = floorAnimStartIndex;
			}
			if (floorIndex == ceilIndex) {
				return floorValue;
			}
		} else {
//			System.out.println(name + ", ~~ no global seq");
			final TimeBoundProvider animation = animatedRenderEnvironment.getCurrentAnimation();
			int animationStart = animation.getStart();
			time = animationStart + animatedRenderEnvironment.getAnimationTime();
			final int floorAnimStartIndex = Math.max(0, floorIndex(animationStart + 1));
			int animationEnd = animation.getEnd();
			final int floorAnimEndIndex = Math.max(0, floorIndex(animationEnd));
			floorIndex = floorIndex(time);
			ceilIndex = Math.max(floorIndex, ceilIndex(time)); // retarded repeated keyframes issue, see Peasant's Bone_Chest at time 18300

			ceilIndexTime = times.get(ceilIndex);
			final int lookupFloorIndex = Math.max(0, floorIndex);
			floorIndexTime = times.get(lookupFloorIndex);
			if (ceilIndexTime < animationStart || floorIndexTime > animationEnd) {
//				System.out.println(name + ", ~~~~ identity(localTypeId)1 " + localTypeId + " id: " + identity(localTypeId));
				return identity(localTypeId);
			}
			ceilValue = values.get(ceilIndex);
			floorValue = values.get(lookupFloorIndex);
			floorInTan = tans() ? inTans.get(lookupFloorIndex) : null;
			floorOutTan = tans() ? outTans.get(lookupFloorIndex) : null;
			if ((floorIndexTime < animationStart) && (ceilIndexTime > animationEnd)) {
//				System.out.println(name + ", ~~~~ identity(localTypeId)3");
				return identity(localTypeId);
			} else if ((floorIndex == -1) || (floorIndexTime < animationStart)) {
				floorValue = values.get(floorAnimEndIndex);
				floorIndexTime = times.get(floorAnimStartIndex);
				if (tans()) {
					floorInTan = inTans.get(floorAnimEndIndex);
					floorOutTan = inTans.get(floorAnimEndIndex);
//					floorIndexTime = times.get(floorAnimEndIndex);
				}
				timeBetweenFrames = times.get(floorAnimEndIndex) - animationStart;
			} else if ((ceilIndexTime > animationEnd)
					|| ((ceilIndexTime < time) && (times.get(floorAnimEndIndex) < time))) {
				if (times.get(floorAnimStartIndex) == animationStart) {
					ceilValue = values.get(floorAnimStartIndex);
					ceilIndex = floorAnimStartIndex;
					ceilIndexTime = animationEnd;
					timeBetweenFrames = ceilIndexTime - floorIndexTime;
				} else {
					ceilIndex = ceilIndex(animationStart);
					ceilValue = values.get(ceilIndex);
					ceilIndexTime = animationEnd;
					timeBetweenFrames = animationEnd - animationStart;
				}
				// NOTE: we just let it be in this case, based on Water Elemental's birth
			} else {
				timeBetweenFrames = ceilIndexTime - floorIndexTime;
			}
			if (floorIndex == ceilIndex) {
//				System.out.println(name + ", ~~~~ floorValue");
				return floorValue;
			}
		}
//		System.out.println(name + ", ~~ Something");

		final Integer floorTime = floorIndexTime;
		final Integer ceilTime = ceilIndexTime;
		final float timeFactor = (time - floorTime) / timeBetweenFrames;

		// Integer
		switch (localTypeId) {
			case ALPHA | OTHER_TYPE -> {
				final Float previous = (Float) floorValue;
				final Float next = (Float) ceilValue;
				return switch (interpolationType) {
					case BEZIER -> MathUtils.bezier(previous, (Float) floorOutTan, (Float) inTans.get(ceilIndex), next, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> MathUtils.hermite(previous, (Float) floorOutTan, (Float) inTans.get(ceilIndex), next, timeFactor);
					case LINEAR -> MathUtils.lerp(previous, next, timeFactor);
				};
			}
			case TRANSLATION, SCALING, COLOR -> {
				// Vertex
				final Vec3 previous = (Vec3) floorValue;
				final Vec3 next = (Vec3) ceilValue;

				return switch (interpolationType) {
					case BEZIER -> Vec3.getBezier(previous, (Vec3) floorOutTan, (Vec3) inTans.get(ceilIndex), next, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> Vec3.getHermite(previous, (Vec3) floorOutTan, (Vec3) inTans.get(ceilIndex), next, timeFactor);
					case LINEAR -> Vec3.getLerped(previous, next, timeFactor);
				};
			}
			case ROTATION -> {
				// Quat
				final Quat previous = (Quat) floorValue;
				final Quat next = (Quat) ceilValue;

				return switch (interpolationType) {
					case BEZIER -> Quat.getSquad(previous, (Quat) floorOutTan, (Quat) inTans.get(ceilIndex), next, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> Quat.getSquad(previous, (Quat) floorOutTan, (Quat) inTans.get(ceilIndex), next, timeFactor);
					case LINEAR -> Quat.getSlerped(previous, next, timeFactor);
				};
			}
			case TEXTUREID -> {
				final Integer previous = (Integer) floorValue;
				return switch (interpolationType) {
					// dont use linear on these, does that even make any sense?
					// dont use hermite on these, does that even make any sense?
					// dont use bezier on these, does that even make any sense?
					case DONT_INTERP, BEZIER, HERMITE, LINEAR -> previous;
				};
			}
		}
		throw new IllegalStateException();
	}

	public void removeKeyframe(final int trackTime) {
		final int keyframeIndex = floorIndex(trackTime);
		if (keyframeIndex == -1 || (keyframeIndex >= size()) || (times.get(keyframeIndex) != trackTime)) {
			System.out.println("Attempted to remove keyframe, but no keyframe was found (" + keyframeIndex
					+ " @ time " + trackTime + ")");
//			throw new IllegalStateException("Attempted to remove keyframe, but no keyframe was found (" + keyframeIndex
//					+ " @ time " + trackTime + ")");
		} else {
			deleteAt(keyframeIndex);
		}
	}

	public void addKeyframe(final Entry<T> entry) {
		addEntry(entry);
	}

	private int getKeyframeCeilIndex(int trackTime) {
		int keyframeIndex = ceilIndex(trackTime);
		if (keyframeIndex == (times.size() - 1)) {
			if (times.isEmpty()) {
				keyframeIndex = 0;
			} else if (trackTime > times.get(times.size() - 1)) {
				keyframeIndex = times.size();
			}
		}
		return keyframeIndex;
	}

	public void addKeyframe(final int trackTime, final T value) {
		int keyframeIndex = getKeyframeCeilIndex(trackTime);
		times.add(keyframeIndex, trackTime);
		values.add(keyframeIndex, value);
	}

	public void addKeyframe(final int trackTime, final T value, final T inTan, final T outTan) {
		int keyframeIndex = getKeyframeCeilIndex(trackTime);
		times.add(keyframeIndex, trackTime);
		values.add(keyframeIndex, value);
		inTans.add(keyframeIndex, inTan);
		outTans.add(keyframeIndex, outTan);
	}

	public void setKeyframe(final Integer time, final T value) {
		if (tans()) {
			throw new IllegalStateException();
		}
		// TODO maybe binary search, ghostwolf says it's not worth it
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
			}
		}
	}

	public void setKeyframe(final Integer time, final T value, final T inTan, final T outTan) {
		if (!tans()) {
			throw new IllegalStateException();
		}
		for (int index = 0; index < times.size(); index++) {
			if (times.get(index).equals(time)) {
				values.set(index, value);
				inTans.set(index, inTan);
				outTans.set(index, outTan);
			}
		}
	}

	public void slideKeyframe(final int startTrackTime, final int endTrackTime) {
		if (times.size() < 1) {
			throw new IllegalStateException("Unable to slide keyframe: no frames exist");
		}
		final int startIndex = floorIndex(startTrackTime);
		final int endIndex = floorIndex(endTrackTime);
		if (endIndex != -1 && startIndex != -1) {
			if (times.get(endIndex) == endTrackTime) {
				throw new IllegalStateException("Sliding this keyframe would create duplicate entries at one time!");
			} else {
				times.set(startIndex, endTrackTime);
				sort();
			}
		}
	}

	public AnimFlag<T> setTimes(List<Integer> times) {
		this.times = times;
		return this;
	}

	public AnimFlag<T> setValues(List<T> values) {
		this.values = values;
		return this;
	}

	public AnimFlag<T> setInTans(List<T> inTans) {
		this.inTans = inTans;
		return this;
	}

	public AnimFlag<T> setOutTans(List<T> outTans) {
		this.outTans = outTans;
		return this;
	}

	/**
	 * This class is a small shell of an example for how my "AnimFlag" class
	 * should've been implemented. It's currently only used for the
	 * {@link AnimFlag#getEntry(int)} function.
	 *
	 * @author Eric
	 */
	public static class Entry<T> {
		public Integer time;
		public T value, inTan, outTan;

		public Entry(final Integer time, final T value, final T inTan, final T outTan) {
			super();
			this.time = time;
			this.value = value;
			this.inTan = inTan;
			this.outTan = outTan;
		}

		public Entry(final Integer time, final T value) {
			super();
			this.time = time;
			this.value = value;
		}

		public Entry(final Entry<T> other) {
			super();
			this.time = other.time;
			this.value = cloneEntryValue(other.value);
			this.inTan = cloneEntryValue(other.inTan);
			this.outTan = cloneEntryValue(other.outTan);
		}

		public void set(final Entry<T> other) {
			time = other.time;
			value = other.value;
			inTan = other.inTan;
			outTan = other.outTan;
		}

		private T cloneEntryValue(T value) {
			if (value == null) {
				return null;
			}

			if (value instanceof Integer || value instanceof Float) {
				return value;
			} else if (value instanceof Vec3) {
				return (T) new Vec3((Vec3) value);
			} else if (value instanceof Quat) {
				return (T) new Quat((Quat) value);
			} else {
				throw new IllegalStateException(value.getClass().getName());
			}
		}
	}
}
