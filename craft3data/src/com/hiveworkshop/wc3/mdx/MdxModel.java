package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;
import com.hiveworkshop.wc3.mdx.SequenceChunk.Sequence;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class MdxModel {
	public VersionChunk versionChunk;
	public ModelChunk modelChunk;
	public SequenceChunk sequenceChunk;
	public GlobalSequenceChunk globalSequenceChunk;
	public MaterialChunk materialChunk;
	public TextureChunk textureChunk;
	public TextureAnimationChunk textureAnimationChunk;
	public GeosetChunk geosetChunk;
	public GeosetAnimationChunk geosetAnimationChunk;
	public BoneChunk boneChunk;
	public LightChunk lightChunk;
	public HelperChunk helperChunk;
	public AttachmentChunk attachmentChunk;
	public PivotPointChunk pivotPointChunk;
	public ParticleEmitterChunk particleEmitterChunk;
	public ParticleEmitter2Chunk particleEmitter2Chunk;
	public RibbonEmitterChunk ribbonEmitterChunk;
	public EventObjectChunk eventObjectChunk;
	public CameraChunk cameraChunk;
	public CollisionShapeChunk collisionShapeChunk;
	public CornChunk cornChunk;
	public FaceEffectsChunk faceEffectsChunk;
	public BindPoseChunk bindPoseChunk;

	public static final String key = "MDLX";

	// Conversion helpers:
	private static Vertex vertex3f(final float[] array) {
		if ((array == null) || (array.length < 3)) {
			return null;
		}
		return new Vertex(array[0], array[1], array[2]);
	}

	private static Vertex vertex3f(final byte[] array) {
		if ((array == null) || (array.length < 3)) {
			return null;
		}
		return new Vertex((256 + array[0]) % 256, (256 + array[1]) % 256, (256 + array[2]) % 256);
	}

	private static QuaternionRotation vertex4f(final float[] array) {
		if ((array == null) || (array.length < 3)) {
			return null;
		}
		return new QuaternionRotation(array[0], array[1], array[2], array[3]);
	}

	private static ExtLog extents(final float[] minExt, final float[] maxExt, final float boundsRadius) {
		return new ExtLog(vertex3f(minExt), vertex3f(maxExt), boundsRadius);
	}

	public static boolean hasFlag(final int flags, final int mask) {
		return (flags & mask) != 0;
	}

	// end conversion helpers
	public EditableModel toMDL() {
		return new EditableModel(this);
	}

	public MdxModel() {

	}

	public MdxModel(final EditableModel mdl) {
		mdl.doSavePreps(); // restores all GeosetID, ObjectID, TextureID,
							// MaterialID stuff all based on object references
							// in the Java
		// (this is so that you can write a program that does something like
		// "mdl.add(new Bone())" without
		// a problem, or even "mdl.add(otherMdl.getGeoset(5))" and have the
		// geoset's textures and materials
		// all be carried over with it via object references in java

		// also this re-creates all matrices, which are consumed by the
		// MatrixEater at runtime in doPostRead()
		// in favor of each vertex having its own attachments list, no vertex
		// groups)
		for (final AnimFlag animFlag : mdl.getAllAnimFlags()) {
			animFlag.sort();
			// apparently this normally only gets sorted
			// on AnimFlag.printTo which is dumb
		}

		versionChunk = new VersionChunk();
		versionChunk.version = mdl.getFormatVersion();
		modelChunk = new ModelChunk();
		modelChunk.blendTime = mdl.getBlendTime();
		modelChunk.name = mdl.getHeaderName();
		if (mdl.getExtents() != null) {
			modelChunk.boundsRadius = (float) mdl.getExtents().getBoundsRadius();
			if (mdl.getExtents().getMaximumExtent() != null) {
				modelChunk.maximumExtent = mdl.getExtents().getMaximumExtent().toFloatArray();
			}
			if (mdl.getExtents().getMinimumExtent() != null) {
				modelChunk.minimumExtent = mdl.getExtents().getMinimumExtent().toFloatArray();
			}
		}
		if (mdl.getAnims().size() > 0) {
			sequenceChunk = new SequenceChunk();
			sequenceChunk.sequence = new Sequence[mdl.getAnims().size()];
			for (int i = 0; i < mdl.getAnims().size(); i++) {
				final SequenceChunk.Sequence seq = sequenceChunk.new Sequence(mdl.getAnim(i));
				sequenceChunk.sequence[i] = seq;
			}
		}
		if (mdl.getGlobalSeqs().size() > 0) {
			globalSequenceChunk = new GlobalSequenceChunk();
			globalSequenceChunk.globalSequences = new int[mdl.getGlobalSeqs().size()];
			for (int i = 0; i < mdl.getGlobalSeqs().size(); i++) {
				globalSequenceChunk.globalSequences[i] = mdl.getGlobalSeq(i).intValue();
			}
		}
		if (mdl.getMaterials().size() > 0) {
			materialChunk = new MaterialChunk();
			materialChunk.material = new MaterialChunk.Material[mdl.getMaterials().size()];
			for (int i = 0; i < mdl.getMaterials().size(); i++) {
				materialChunk.material[i] = materialChunk.new Material(mdl.getMaterial(i), versionChunk.version);
			}
		}
		if (mdl.getTextures().size() > 0) {
			textureChunk = new TextureChunk();
			textureChunk.texture = new TextureChunk.Texture[mdl.getTextures().size()];
			for (int i = 0; i < mdl.getTextures().size(); i++) {
				textureChunk.texture[i] = textureChunk.new Texture(mdl.getTexture(i));
			}
		}
		if (mdl.getTexAnims().size() > 0) {
			textureAnimationChunk = new TextureAnimationChunk();
			textureAnimationChunk.textureAnimation = new TextureAnimationChunk.TextureAnimation[mdl.getTexAnims()
					.size()];
			for (int i = 0; i < mdl.getTexAnims().size(); i++) {
				textureAnimationChunk.textureAnimation[i] = textureAnimationChunk.new TextureAnimation(
						mdl.getTexAnims().get(i));
			}
		}
		if (mdl.getGeosets().size() > 0) {
			geosetChunk = new GeosetChunk();
			geosetChunk.geoset = new GeosetChunk.Geoset[mdl.getGeosets().size()];
			for (int i = 0; i < mdl.getGeosets().size(); i++) {
				geosetChunk.geoset[i] = geosetChunk.new Geoset(mdl.getGeoset(i));
			}
		}
		if (mdl.getGeosetAnims().size() > 0) {
			geosetAnimationChunk = new GeosetAnimationChunk();
			// Shave off GeosetAnims that are just totally empty and stuff
			final List<GeosetAnim> nonEmptyGeosetAnimations = new ArrayList<>();
			for (int i = 0; i < mdl.getGeosetAnims().size(); i++) {
				final GeosetAnim geosetAnim = mdl.getGeosetAnim(i);
				if (!geosetAnim.getAnimFlags().isEmpty() || !geosetAnim.isDropShadow()
						|| (Math.abs(geosetAnim.getStaticAlpha() - 1.0f) > 0.0001f)
						|| (geosetAnim.getStaticColor() != null)) {
					nonEmptyGeosetAnimations.add(geosetAnim);
				}
			}
			geosetAnimationChunk.geosetAnimation = new GeosetAnimationChunk.GeosetAnimation[nonEmptyGeosetAnimations
					.size()];
			for (int i = 0; i < nonEmptyGeosetAnimations.size(); i++) {
				geosetAnimationChunk.geosetAnimation[i] = geosetAnimationChunk.new GeosetAnimation(
						nonEmptyGeosetAnimations.get(i));
			}
		}
		if (mdl.sortedIdObjects(Bone.class).size() > 0) {
			boneChunk = new BoneChunk();
			final List<Bone> nodes = mdl.sortedIdObjects(Bone.class);
			boneChunk.bone = new BoneChunk.Bone[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				boneChunk.bone[i] = boneChunk.new Bone(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(Light.class).size() > 0) {
			lightChunk = new LightChunk();
			final List<Light> nodes = mdl.sortedIdObjects(Light.class);
			lightChunk.light = new LightChunk.Light[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				lightChunk.light[i] = lightChunk.new Light(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(Helper.class).size() > 0) {
			helperChunk = new HelperChunk();
			final List<Helper> nodes = mdl.sortedIdObjects(Helper.class);
			helperChunk.helper = new HelperChunk.Helper[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				helperChunk.helper[i] = helperChunk.new Helper(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(Attachment.class).size() > 0) {
			attachmentChunk = new AttachmentChunk();
			final List<Attachment> nodes = mdl.sortedIdObjects(Attachment.class);
			attachmentChunk.attachment = new AttachmentChunk.Attachment[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).setAttachmentID(i);
				attachmentChunk.attachment[i] = attachmentChunk.new Attachment(nodes.get(i));
			}
		}
		if (mdl.getPivots().size() > 0) {
			pivotPointChunk = new PivotPointChunk();
			pivotPointChunk.pivotPoints = new float[mdl.getPivots().size() * 3];
			int i = 0;
			for (final Vertex pivot : mdl.getPivots()) {
				pivotPointChunk.pivotPoints[i++] = (float) pivot.getX();
				pivotPointChunk.pivotPoints[i++] = (float) pivot.getY();
				pivotPointChunk.pivotPoints[i++] = (float) pivot.getZ();
			}
		}
		if (mdl.sortedIdObjects(ParticleEmitter.class).size() > 0) {
			particleEmitterChunk = new ParticleEmitterChunk();
			final List<ParticleEmitter> nodes = mdl.sortedIdObjects(ParticleEmitter.class);
			particleEmitterChunk.particleEmitter = new ParticleEmitterChunk.ParticleEmitter[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				particleEmitterChunk.particleEmitter[i] = particleEmitterChunk.new ParticleEmitter(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(ParticleEmitter2.class).size() > 0) {
			particleEmitter2Chunk = new ParticleEmitter2Chunk();
			final List<ParticleEmitter2> nodes = mdl.sortedIdObjects(ParticleEmitter2.class);
			particleEmitter2Chunk.particleEmitter2 = new ParticleEmitter2Chunk.ParticleEmitter2[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				particleEmitter2Chunk.particleEmitter2[i] = particleEmitter2Chunk.new ParticleEmitter2(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(RibbonEmitter.class).size() > 0) {
			ribbonEmitterChunk = new RibbonEmitterChunk();
			final List<RibbonEmitter> nodes = mdl.sortedIdObjects(RibbonEmitter.class);
			ribbonEmitterChunk.ribbonEmitter = new RibbonEmitterChunk.RibbonEmitter[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				ribbonEmitterChunk.ribbonEmitter[i] = ribbonEmitterChunk.new RibbonEmitter(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(ParticleEmitterPopcorn.class).size() > 0) {
			cornChunk = new CornChunk();
			final List<ParticleEmitterPopcorn> nodes = mdl.sortedIdObjects(ParticleEmitterPopcorn.class);
			cornChunk.corns = new CornChunk.ParticleEmitterPopcorn[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				cornChunk.corns[i] = cornChunk.new ParticleEmitterPopcorn(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(EventObject.class).size() > 0) {
			eventObjectChunk = new EventObjectChunk();
			final List<EventObject> nodes = mdl.sortedIdObjects(EventObject.class);
			eventObjectChunk.eventObject = new EventObjectChunk.EventObject[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				eventObjectChunk.eventObject[i] = eventObjectChunk.new EventObject(nodes.get(i));
			}
		}
		if (mdl.getCameras().size() > 0) {
			cameraChunk = new CameraChunk();
			final List<Camera> nodes = mdl.getCameras();
			cameraChunk.camera = new CameraChunk.Camera[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				cameraChunk.camera[i] = cameraChunk.new Camera(nodes.get(i));
			}
		}
		if (mdl.sortedIdObjects(CollisionShape.class).size() > 0) {
			collisionShapeChunk = new CollisionShapeChunk();
			final List<CollisionShape> nodes = mdl.sortedIdObjects(CollisionShape.class);
			collisionShapeChunk.collisionShape = new CollisionShapeChunk.CollisionShape[nodes.size()];
			for (int i = 0; i < nodes.size(); i++) {
				collisionShapeChunk.collisionShape[i] = collisionShapeChunk.new CollisionShape(nodes.get(i));
			}
		}
		if (ModelUtils.isBindPoseSupported(versionChunk.version)) {
			bindPoseChunk = mdl.getBindPoseChunk();
			if (!mdl.getFaceEffects().isEmpty()) {
				faceEffectsChunk = new FaceEffectsChunk();
				faceEffectsChunk.faceEffects = new FaceEffect[mdl.getFaceEffects().size()];
				for (int i = 0; i < mdl.getFaceEffects().size(); i++) {
					faceEffectsChunk.faceEffects[i] = mdl.getFaceEffects().get(i);
				}
			}
		}
	}

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "MDLX");
		int version = 800;
		for (int i = 0; i < 23; i++) {
			in.mark(8);
			final String chunk = in.readCharsAsString(4);
			in.reset();
			if (MdxUtils.checkOptionalId(in, VersionChunk.key)) {
				versionChunk = new VersionChunk();
				versionChunk.load(in);
				version = versionChunk.version;
			}
			else if (MdxUtils.checkOptionalId(in, ModelChunk.key)) {
				modelChunk = new ModelChunk();
				modelChunk.load(in, version);
			}
			else if (MdxUtils.checkOptionalId(in, SequenceChunk.key)) {
				sequenceChunk = new SequenceChunk();
				sequenceChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, GlobalSequenceChunk.key)) {
				globalSequenceChunk = new GlobalSequenceChunk();
				globalSequenceChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, MaterialChunk.key)) {
				materialChunk = new MaterialChunk();
				materialChunk.load(in, version);
			}
			else if (MdxUtils.checkOptionalId(in, TextureChunk.key)) {
				textureChunk = new TextureChunk();
				textureChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, TextureAnimationChunk.key)) {
				textureAnimationChunk = new TextureAnimationChunk();
				textureAnimationChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, GeosetChunk.key)) {
				geosetChunk = new GeosetChunk();
				geosetChunk.load(in, version);
			}
			else if (MdxUtils.checkOptionalId(in, GeosetAnimationChunk.key)) {
				geosetAnimationChunk = new GeosetAnimationChunk();
				geosetAnimationChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, BoneChunk.key)) {
				boneChunk = new BoneChunk();
				boneChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, LightChunk.key)) {
				lightChunk = new LightChunk();
				lightChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, HelperChunk.key)) {
				helperChunk = new HelperChunk();
				helperChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, AttachmentChunk.key)) {
				attachmentChunk = new AttachmentChunk();
				attachmentChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, PivotPointChunk.key)) {
				pivotPointChunk = new PivotPointChunk();
				pivotPointChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, ParticleEmitterChunk.key)) {
				particleEmitterChunk = new ParticleEmitterChunk();
				particleEmitterChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Chunk.key)) {
				particleEmitter2Chunk = new ParticleEmitter2Chunk();
				particleEmitter2Chunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, CornChunk.key)) {
				cornChunk = new CornChunk();
				cornChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, RibbonEmitterChunk.key)) {
				ribbonEmitterChunk = new RibbonEmitterChunk();
				ribbonEmitterChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, EventObjectChunk.key)) {
				eventObjectChunk = new EventObjectChunk();
				eventObjectChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, CameraChunk.key)) {
				cameraChunk = new CameraChunk();
				cameraChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, CollisionShapeChunk.key)) {
				collisionShapeChunk = new CollisionShapeChunk();
				collisionShapeChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, FaceEffectsChunk.key)) {
				faceEffectsChunk = new FaceEffectsChunk();
				faceEffectsChunk.load(in);
			}
			else if (MdxUtils.checkOptionalId(in, BindPoseChunk.key)) {
				bindPoseChunk = new BindPoseChunk();
				bindPoseChunk.load(in);
			}
			else {
				final int available = in.available();
				if (available > 0) {
					boolean alpha = true;
					for (final byte b : chunk.getBytes()) {
						if (!Character.isAlphabetic(b)) {
							alpha = false;
						}
					}
					if (alpha) {
						final String skipChunk = in.readCharsAsString(4);
						final int bytesToSkip = in.readInt();
						in.skip(bytesToSkip);
					}
				}
			}

		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("MDLX", 4);
		if (versionChunk != null) {
			versionChunk.save(out);
		}
		if (modelChunk != null) {
			modelChunk.save(out);
		}
		if (sequenceChunk != null) {
			sequenceChunk.save(out);
		}
		if (globalSequenceChunk != null) {
			globalSequenceChunk.save(out);
		}
		if (materialChunk != null) {
			materialChunk.save(out, versionChunk.version);
		}
		if (textureChunk != null) {
			textureChunk.save(out);
		}
		if (textureAnimationChunk != null) {
			textureAnimationChunk.save(out);
		}
		if (geosetChunk != null) {
			geosetChunk.save(out, versionChunk.version);
		}
		if (geosetAnimationChunk != null) {
			geosetAnimationChunk.save(out);
		}
		if (boneChunk != null) {
			boneChunk.save(out);
		}
		if (lightChunk != null) {
			lightChunk.save(out);
		}
		if (helperChunk != null) {
			helperChunk.save(out);
		}
		if (attachmentChunk != null) {
			attachmentChunk.save(out);
		}
		if (pivotPointChunk != null) {
			pivotPointChunk.save(out);
		}
		if (particleEmitterChunk != null) {
			particleEmitterChunk.save(out);
		}
		if (particleEmitter2Chunk != null) {
			particleEmitter2Chunk.save(out);
		}
		if (ModelUtils.isCornSupported(versionChunk.version)) {
			if (cornChunk != null) {
				cornChunk.save(out);
			}
		}
		if (ribbonEmitterChunk != null) {
			ribbonEmitterChunk.save(out);
		}
		if (eventObjectChunk != null) {
			eventObjectChunk.save(out);
		}
		if (cameraChunk != null) {
			cameraChunk.save(out);
		}
		if (collisionShapeChunk != null) {
			collisionShapeChunk.save(out);
		}
		if (ModelUtils.isBindPoseSupported(versionChunk.version)) {
			if (faceEffectsChunk != null) {
				faceEffectsChunk.save(out);
			}
			if (bindPoseChunk != null) {
				bindPoseChunk.save(out);
			}
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		if (versionChunk != null) {
			a += versionChunk.getSize();
		}
		if (modelChunk != null) {
			a += modelChunk.getSize();
		}
		if (sequenceChunk != null) {
			a += sequenceChunk.getSize();
		}
		if (globalSequenceChunk != null) {
			a += globalSequenceChunk.getSize();
		}
		if (materialChunk != null) {
			a += materialChunk.getSize(versionChunk.version,
					LayerChunk.WRITE_JANK_REFORGED_2022_FORMAT_FILE_FIXES_NAGA_WATER);
		}
		if (textureChunk != null) {
			a += textureChunk.getSize();
		}
		if (textureAnimationChunk != null) {
			a += textureAnimationChunk.getSize();
		}
		if (geosetChunk != null) {
			a += geosetChunk.getSize(versionChunk.version);
		}
		if (geosetAnimationChunk != null) {
			a += geosetAnimationChunk.getSize();
		}
		if (boneChunk != null) {
			a += boneChunk.getSize();
		}
		if (lightChunk != null) {
			a += lightChunk.getSize();
		}
		if (helperChunk != null) {
			a += helperChunk.getSize();
		}
		if (attachmentChunk != null) {
			a += attachmentChunk.getSize();
		}
		if (pivotPointChunk != null) {
			a += pivotPointChunk.getSize();
		}
		if (particleEmitterChunk != null) {
			a += particleEmitterChunk.getSize();
		}
		if (particleEmitter2Chunk != null) {
			a += particleEmitter2Chunk.getSize();
		}
		if (ribbonEmitterChunk != null) {
			a += ribbonEmitterChunk.getSize();
		}
		if (eventObjectChunk != null) {
			a += eventObjectChunk.getSize();
		}
		if (ModelUtils.isCornSupported(versionChunk.version)) {
			if (cornChunk != null) {
				a += cornChunk.getSize();
			}
		}
		if (cameraChunk != null) {
			a += cameraChunk.getSize();
		}
		if (collisionShapeChunk != null) {
			a += collisionShapeChunk.getSize();
		}
		if (ModelUtils.isBindPoseSupported(versionChunk.version)) {
			if (faceEffectsChunk != null) {
				a += faceEffectsChunk.getSize();
			}
			if (bindPoseChunk != null) {
				a += bindPoseChunk.getSize();
			}
		}

		return a;
	}
}
