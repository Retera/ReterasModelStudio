package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.GeosetVertexBoneLink;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Deep copies of model components, bound to a target model.
 * <p>
 * Within one model a copy shares everything it references (bones, materials,
 * textures, texture anims, global sequences) with the original, which is what
 * "duplicate this thing" means to a user. Across models those references are
 * remapped by name or equality to the target model's own components, and
 * anything with no counterpart is copied too and reported in
 * {@link CopyResult#getExtras()} so the paste action can add it.
 */
public final class ModelComponentCopier {
	public static final class CopyResult {
		private final Object item;
		private final List<Object> extras = new ArrayList<>();
		private Animation keyframeSource;

		private CopyResult(final Object item) {
			this.item = item;
		}

		public Object getItem() {
			return item;
		}

		/** Additional components the target model needs for the item to resolve. */
		public List<Object> getExtras() {
			return extras;
		}

		/**
		 * For sequences duplicated inside one model: the animation whose keyframes
		 * should be copied onto the new interval.
		 */
		public Animation getKeyframeSource() {
			return keyframeSource;
		}
	}

	private ModelComponentCopier() {
	}

	public static boolean canCopy(final Object item) {
		final ComponentKind kind = ComponentKind.of(item);
		if (kind == null) {
			return false;
		}
		// never call IdObject.copy() here: the copy constructors register the copy
		// as a child of the original's parent as a side effect
		return kind != ComponentKind.GEOSET_ANIM;
	}

	/**
	 * @return null if the component cannot be copied
	 */
	public static CopyResult copy(final Object item, final EditableModel source, final EditableModel target) {
		final ComponentKind kind = ComponentKind.of(item);
		if (kind == null) {
			return null;
		}
		final boolean sameModel = source == target;
		switch (kind) {
		case SEQUENCE:
			return copyAnimation((Animation) item, target, sameModel);
		case GLOBAL_SEQUENCE:
			return new CopyResult(newGlobalSequence(((Integer) item).intValue()));
		case TEXTURE:
			return new CopyResult(new Bitmap((Bitmap) item));
		case MATERIAL:
			return copyMaterial((Material) item, target, sameModel);
		case TEXTURE_ANIM:
			return new CopyResult(new TextureAnim((TextureAnim) item));
		case GEOSET:
			return copyGeoset((Geoset) item, target, sameModel);
		case GEOSET_ANIM:
			return null;
		case CAMERA:
			return new CopyResult(copyCamera((Camera) item));
		default:
			return copyNode((IdObject) item, target, sameModel);
		}
	}

	private static CopyResult copyAnimation(final Animation animation, final EditableModel target,
			final boolean sameModel) {
		final Animation copy = new Animation(animation);
		final int start = nextFreeSequenceStart(target);
		final int length = Math.max(1, animation.length());
		copy.setInterval(start, start + length);
		if (sameModel) {
			copy.setName(animation.getName() + " Copy");
		}
		final CopyResult result = new CopyResult(copy);
		if (sameModel) {
			result.keyframeSource = animation;
		}
		return result;
	}

	/** First start time after every existing sequence, on a 1000 ms boundary. */
	public static int nextFreeSequenceStart(final EditableModel model) {
		int greatest = -1;
		for (final Animation anim : model.getAnims()) {
			greatest = Math.max(greatest, Math.max(anim.getIntervalStart(), anim.getIntervalEnd()));
		}
		if (greatest < 0) {
			return 0;
		}
		return ((greatest / 1000) + 1) * 1000 + 1000;
	}

	/**
	 * A global sequence is an Integer whose identity matters (tracks point at the
	 * list element), so make a new object even for small values.
	 */
	@SuppressWarnings({ "deprecation", "removal" })
	public static Integer newGlobalSequence(final int length) {
		return new Integer(length);
	}

	private static CopyResult copyMaterial(final Material material, final EditableModel target,
			final boolean sameModel) {
		final Material copy = new Material(material);
		final CopyResult result = new CopyResult(copy);
		final Map<Integer, Integer> globalSeqs = new HashMap<>();
		for (int i = 0; i < copy.getLayers().size(); i++) {
			final Layer copyLayer = copy.getLayers().get(i);
			final Layer origLayer = material.getLayers().get(i);
			bindLayer(copyLayer, origLayer, target, sameModel, result, globalSeqs);
		}
		return result;
	}

	/**
	 * Layer(Layer) copies the texture anim and the animated-texture bitmaps, which
	 * is wrong for an in-model duplicate (they should be shared) and incomplete
	 * for a cross-model one (shader textures are shared by reference). Fix both.
	 */
	private static void bindLayer(final Layer copyLayer, final Layer origLayer, final EditableModel target,
			final boolean sameModel, final CopyResult result, final Map<Integer, Integer> globalSeqs) {
		if (sameModel) {
			copyLayer.setTextureAnim(origLayer.getTextureAnim());
			if ((origLayer.getTextures() != null) && (copyLayer.getTextures() != null)) {
				for (int t = 0; t < origLayer.getTextures().size(); t++) {
					copyLayer.getTextures().set(t, origLayer.getTextures().get(t));
				}
			}
			return;
		}
		if (copyLayer.getTextureAnim() != null) {
			result.extras.add(copyLayer.getTextureAnim());
		}
		for (final ShaderTextureTypeHD slot : ShaderTextureTypeHD.VALUES) {
			final Bitmap bitmap = copyLayer.getShaderTextures().get(slot);
			if (bitmap != null) {
				copyLayer.getShaderTextures().put(slot, remapBitmap(bitmap, target, result));
			}
		}
		if (copyLayer.getTextures() != null) {
			for (int t = 0; t < copyLayer.getTextures().size(); t++) {
				copyLayer.getTextures().set(t, remapBitmap(copyLayer.getTextures().get(t), target, result));
			}
		}
		for (final AnimFlag flag : copyLayer.getAnims()) {
			remapGlobalSeq(flag, target, result, globalSeqs);
		}
	}

	private static Bitmap remapBitmap(final Bitmap bitmap, final EditableModel target, final CopyResult result) {
		for (final Bitmap existing : target.getTextures()) {
			if (existing.equals(bitmap)) {
				return existing;
			}
		}
		for (final Object extra : result.extras) {
			if ((extra instanceof Bitmap) && extra.equals(bitmap)) {
				return (Bitmap) extra;
			}
		}
		final Bitmap copy = new Bitmap(bitmap);
		result.extras.add(copy);
		return copy;
	}

	private static Material remapMaterial(final Material material, final EditableModel target,
			final CopyResult result) {
		for (final Material existing : target.getMaterials()) {
			if (existing.equals(material)) {
				return existing;
			}
		}
		for (final Object extra : result.extras) {
			if ((extra instanceof Material) && extra.equals(material)) {
				return (Material) extra;
			}
		}
		final CopyResult materialCopy = copyMaterial(material, target, false);
		result.extras.addAll(materialCopy.extras);
		result.extras.add(materialCopy.item);
		return (Material) materialCopy.item;
	}

	private static void remapGlobalSeq(final AnimFlag flag, final EditableModel target, final CopyResult result,
			final Map<Integer, Integer> alreadyMapped) {
		final Integer globalSeq = flag.getGlobalSeq();
		if (globalSeq == null) {
			return;
		}
		flag.setGlobalSeq(remapGlobalSeq(globalSeq, target, result, alreadyMapped));
		flag.setHasGlobalSeq(true);
	}

	private static Integer remapGlobalSeq(final Integer globalSeq, final EditableModel target,
			final CopyResult result, final Map<Integer, Integer> alreadyMapped) {
		for (final Integer existing : target.getGlobalSeqs()) {
			if (existing.equals(globalSeq)) {
				return existing;
			}
		}
		Integer mapped = alreadyMapped.get(globalSeq);
		if (mapped == null) {
			mapped = newGlobalSequence(globalSeq.intValue());
			alreadyMapped.put(globalSeq, mapped);
			result.extras.add(mapped);
		}
		return mapped;
	}

	private static CopyResult copyGeoset(final Geoset geoset, final EditableModel target, final boolean sameModel) {
		final Geoset copy = new Geoset();
		final CopyResult result = new CopyResult(copy);
		final Map<GeosetVertex, GeosetVertex> vertexMap = new HashMap<>();
		Bone fallbackBone = null;
		for (final GeosetVertex vertex : geoset.getVertices()) {
			final GeosetVertex vertexCopy = new GeosetVertex(vertex);
			vertexCopy.setGeoset(copy);
			vertexCopy.setTriangles(new ArrayList<Triangle>());
			if (!sameModel) {
				final List<GeosetVertexBoneLink> links = vertexCopy.getLinks();
				for (int i = links.size() - 1; i >= 0; i--) {
					final GeosetVertexBoneLink link = links.get(i);
					final Bone counterpart = findBoneByName(target, link.bone == null ? null : link.bone.getName());
					if (counterpart == null) {
						links.remove(i);
					} else {
						link.bone = counterpart;
					}
				}
				if (links.isEmpty()) {
					if (fallbackBone == null) {
						fallbackBone = firstBone(target);
					}
					if (fallbackBone != null) {
						vertexCopy.addBoneAttachment((short) 255, fallbackBone);
					}
				}
			}
			vertexMap.put(vertex, vertexCopy);
			copy.add(vertexCopy);
		}
		for (final Triangle triangle : geoset.getTriangles()) {
			final GeosetVertex[] all = triangle.getAll();
			final Triangle triangleCopy = new Triangle(vertexMap.get(all[0]), vertexMap.get(all[1]),
					vertexMap.get(all[2]), copy);
			copy.add(triangleCopy);
			for (final GeosetVertex vertexCopy : triangleCopy.getAll()) {
				if (vertexCopy != null) {
					vertexCopy.getTriangles().add(triangleCopy);
				}
			}
		}
		if (geoset.getMaterial() != null) {
			copy.setMaterial(sameModel ? geoset.getMaterial() : remapMaterial(geoset.getMaterial(), target, result));
		}
		copy.setSelectionGroup(geoset.getSelectionGroup());
		copy.setLevelOfDetail(geoset.getLevelOfDetail());
		copy.setLevelOfDetailName(geoset.getLevelOfDetailName());
		if (geoset.getExtents() != null) {
			copy.setExtents(new ExtLog(geoset.getExtents()));
		}
		for (final Animation extentEntry : geoset.getAnims()) {
			copy.add(new Animation(extentEntry));
		}
		for (final String flag : geoset.getFlags()) {
			copy.addFlag(flag);
		}
		copy.setParentModel(target);
		if (geoset.getGeosetAnim() != null) {
			final GeosetAnim animCopy = new GeosetAnim(copy, geoset.getGeosetAnim());
			if (!sameModel) {
				final Map<Integer, Integer> globalSeqs = new HashMap<>();
				for (final AnimFlag flag : animCopy.getAnimFlags()) {
					remapGlobalSeq(flag, target, result, globalSeqs);
				}
			}
			copy.setGeosetAnim(animCopy);
			result.extras.add(animCopy);
		}
		return result;
	}

	private static CopyResult copyNode(final IdObject node, final EditableModel target, final boolean sameModel) {
		final IdObject copy = node.copy();
		if (copy == null) {
			return null;
		}
		final CopyResult result = new CopyResult(copy);
		if (sameModel) {
			copy.setName(node.getName() + " Copy");
			return result;
		}
		final IdObject parent = node.getParent();
		copy.setParent(parent == null ? null : findNodeByName(target, parent.getName()));
		final Map<Integer, Integer> globalSeqs = new HashMap<>();
		for (final AnimFlag flag : copy.getAnimFlags()) {
			remapGlobalSeq(flag, target, result, globalSeqs);
		}
		if (copy instanceof ParticleEmitter2) {
			final ParticleEmitter2 emitter = (ParticleEmitter2) copy;
			if (emitter.getTexture() != null) {
				emitter.setTexture(remapBitmap(emitter.getTexture(), target, result));
			}
		}
		if (copy instanceof RibbonEmitter) {
			final RibbonEmitter emitter = (RibbonEmitter) copy;
			if (emitter.getMaterial() != null) {
				emitter.setMaterial(remapMaterial(emitter.getMaterial(), target, result));
			}
		}
		if (copy instanceof EventObject) {
			final EventObject eventObject = (EventObject) copy;
			if (eventObject.getGlobalSeq() != null) {
				eventObject.setGlobalSeq(remapGlobalSeq(eventObject.getGlobalSeq(), target, result, globalSeqs));
				eventObject.setHasGlobalSeq(true);
			}
		}
		if (copy instanceof Bone) {
			// geoset and geoset anim back-references belong to the source model
			((Bone) copy).setGeoset(null);
			((Bone) copy).setGeosetAnim(null);
		}
		return result;
	}

	public static Camera copyCamera(final Camera camera) {
		final Camera copy = new Camera(camera.getName(), new Vertex(camera.getPosition()),
				new Vertex(camera.getTargetPosition()), camera.getFieldOfView(), camera.getFarClip(),
				camera.getNearClip());
		final ArrayList<AnimFlag> sourceFlags = new ArrayList<>();
		for (final AnimFlag flag : camera.getAnimFlags()) {
			sourceFlags.add(new AnimFlag(flag));
		}
		copy.setAnimFlags(sourceFlags);
		final ArrayList<AnimFlag> targetFlags = new ArrayList<>();
		for (final AnimFlag flag : camera.getTargetAnimFlags()) {
			targetFlags.add(new AnimFlag(flag));
		}
		copy.setTargetAnimFlags(targetFlags);
		copy.setHeaderByte(camera.getHeaderByte());
		if (camera.getBindPose() != null) {
			copy.setBindPose(camera.getBindPose().clone());
		}
		return copy;
	}

	public static IdObject findNodeByName(final EditableModel model, final String name) {
		if (name == null) {
			return null;
		}
		for (final IdObject candidate : model.getIdObjects()) {
			if (name.equals(candidate.getName())) {
				return candidate;
			}
		}
		return null;
	}

	public static Bone findBoneByName(final EditableModel model, final String name) {
		final IdObject node = findNodeByName(model, name);
		return node instanceof Bone ? (Bone) node : null;
	}

	public static Bone firstBone(final EditableModel model) {
		for (final IdObject candidate : model.getIdObjects()) {
			if (candidate instanceof Bone) {
				return (Bone) candidate;
			}
		}
		return null;
	}
}
