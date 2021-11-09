package com.hiveworkshop.rms.parsers.mdlx;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A Warcraft 3 model. Supports loading from and saving to both the binary MDX
 * and text MDL file formats.
 */
public class MdlxModel {

	public List<String> comments = new ArrayList<>();
	public int version = 800;
	public String name = "";
	/**
	 * (Comment copied from Ghostwolf JS)
	 * To the best of my knowledge, this should always be left empty.
	 * This is probably a leftover from the Warcraft 3 beta.
	 * (WS game note: No, I never saw any animation files in the RoC 2001-2002 Beta. So it must be from the Alpha)
	 */
	public String animationFile = "";
	public MdlxExtent extent = new MdlxExtent();
	public long blendTime = 0;
	public List<MdlxSequence> sequences = new ArrayList<>();
	public List<Long /* UInt32 */> globalSequences = new ArrayList<>();
	public List<MdlxMaterial> materials = new ArrayList<>();
	public List<MdlxTexture> textures = new ArrayList<>();
	public List<MdlxTextureAnimation> textureAnimations = new ArrayList<>();
	public List<MdlxGeoset> geosets = new ArrayList<>();
	public List<MdlxGeosetAnimation> geosetAnimations = new ArrayList<>();
	public List<MdlxBone> bones = new ArrayList<>();
	public List<MdlxLight> lights = new ArrayList<>();
	public List<MdlxHelper> helpers = new ArrayList<>();
	public List<MdlxAttachment> attachments = new ArrayList<>();
	public List<float[]> pivotPoints = new ArrayList<>();
	public List<MdlxParticleEmitter> particleEmitters = new ArrayList<>();
	public List<MdlxParticleEmitter2> particleEmitters2 = new ArrayList<>();
	public List<MdlxParticleEmitterPopcorn> particleEmittersPopcorn = new ArrayList<>();
	public List<MdlxRibbonEmitter> ribbonEmitters = new ArrayList<>();
	public List<MdlxCamera> cameras = new ArrayList<>();
	public List<MdlxEventObject> eventObjects = new ArrayList<>();
	public List<MdlxCollisionShape> collisionShapes = new ArrayList<>();
	/**
	 * @since 900
	 */
	public List<MdlxFaceEffect> faceEffects = new ArrayList<>();
	/**
	 * @since 900
	 */
	public List<float[]> bindPose = new ArrayList<>();
	public List<MdlxUnknownChunk> unknownChunks = new ArrayList<>();

	public MdlxModel() {

	}

	public MdlxModel(final ByteBuffer buffer) {
		load(buffer);
	}

	public void load(final ByteBuffer buffer) {
		// MDX files start with "MDLX".
		if (buffer.get(0) == 77 && buffer.get(1) == 68 && buffer.get(2) == 76 && buffer.get(3) == 88) {
			MdxLoadSave.loadMdx(this, buffer);
		} else {
			MdlLoadSave.loadMdl(this, buffer);
		}
	}

}
