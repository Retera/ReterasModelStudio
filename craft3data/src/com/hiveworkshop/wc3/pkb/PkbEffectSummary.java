package com.hiveworkshop.wc3.pkb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the editor can read off a baked effect without running its bytecode:
 * the layers and, per layer, the renderers with their geometry class, blend
 * mode, billboarding mode and diffuse texture. Enough to preview an emitter
 * with the right textures and blending; the actual particle behaviour lives
 * in the compiled blobs and needs the VM (see the roadmap, W12).
 */
public final class PkbEffectSummary {
	/** PopcornFX renderer classes ({@code CLayerCompileCacheRenderer.RendererClass}). */
	public enum RendererClass {
		BILLBOARD, RIBBON, MESH, LIGHT, UNKNOWN;

		static RendererClass of(final int raw) {
			switch (raw) {
			case 0:
				return BILLBOARD;
			case 1:
				return RIBBON;
			case 2:
				return MESH;
			case 3:
				return LIGHT;
			default:
				return UNKNOWN;
			}
		}
	}

	/** PopcornFX transparency modes ({@code Transparent.Type}). */
	public enum BlendMode {
		ADDITIVE, ADDITIVE_NO_ALPHA, ALPHA_BLEND, PREMULTIPLIED, OPAQUE, ALPHA_KEY;

		static BlendMode of(final int raw) {
			switch (raw) {
			case 0:
				return ADDITIVE;
			case 1:
				return ADDITIVE_NO_ALPHA;
			case 2:
				return ALPHA_BLEND;
			case 3:
				return PREMULTIPLIED;
			case 5:
				return ALPHA_KEY;
			default:
				return OPAQUE;
			}
		}
	}

	public static final class Renderer {
		public final RendererClass rendererClass;
		public final BlendMode blendMode;
		/** 0 screen aligned, 1 viewpos aligned, 2 axis quad, 3 spheroid, 4 capsule, 5 plane aligned. */
		public final int billboardingMode;
		/** Texture path as written in the effect ({@code _HD.w3mod/Textures/...tif}); may be empty. */
		public final String diffuseTexturePath;
		public final int atlasSubDivX;
		public final int atlasSubDivY;
		public final boolean renderingEnabled;
		public final boolean size2D;

		Renderer(final RendererClass rendererClass, final BlendMode blendMode, final int billboardingMode,
				final String diffuseTexturePath, final int atlasSubDivX, final int atlasSubDivY,
				final boolean renderingEnabled, final boolean size2D) {
			this.rendererClass = rendererClass;
			this.blendMode = blendMode;
			this.billboardingMode = billboardingMode;
			this.diffuseTexturePath = diffuseTexturePath;
			this.atlasSubDivX = atlasSubDivX;
			this.atlasSubDivY = atlasSubDivY;
			this.renderingEnabled = renderingEnabled;
			this.size2D = size2D;
		}

		/**
		 * The texture path relative to the game data root, with the
		 * {@code _HD.w3mod/} style mod prefix removed and backslashes, so the
		 * usual texture loader (with its .tif/.blp to .dds fallback) can find it.
		 */
		public String gameTexturePath() {
			if (diffuseTexturePath.isEmpty()) {
				return "";
			}
			String path = diffuseTexturePath.replace('/', '\\');
			final int mod = path.toLowerCase().lastIndexOf(".w3mod\\");
			if (mod >= 0) {
				path = path.substring(mod + ".w3mod\\".length());
			}
			return path;
		}

		@Override
		public String toString() {
			return rendererClass + " " + blendMode + " billboarding=" + billboardingMode + " texture="
					+ diffuseTexturePath + ((atlasSubDivX > 1) || (atlasSubDivY > 1) ? " atlas " + atlasSubDivX + "x" + atlasSubDivY : "");
		}
	}

	public static final class Layer {
		public final int index;
		public final List<Renderer> renderers;
		public final int blobBytes;
		public final int samplerCount;
		/** True when the layer is spawned by the root entry, false for child/event layers. */
		public final boolean rootLayer;

		Layer(final int index, final List<Renderer> renderers, final int blobBytes, final int samplerCount,
				final boolean rootLayer) {
			this.index = index;
			this.renderers = Collections.unmodifiableList(renderers);
			this.blobBytes = blobBytes;
			this.samplerCount = samplerCount;
			this.rootLayer = rootLayer;
		}
	}

	private final List<Layer> layers;
	private final List<String> attributeNames;

	private PkbEffectSummary(final List<Layer> layers, final List<String> attributeNames) {
		this.layers = Collections.unmodifiableList(layers);
		this.attributeNames = Collections.unmodifiableList(attributeNames);
	}

	public List<Layer> getLayers() {
		return layers;
	}

	/** Names such as {@code Game.TeamColor} the effect reads from the game. */
	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public List<Renderer> allRenderers() {
		final List<Renderer> out = new ArrayList<>();
		for (final Layer layer : layers) {
			out.addAll(layer.renderers);
		}
		return out;
	}

	/** The first enabled billboard renderer with a texture, or null. */
	public Renderer firstBillboard() {
		for (final Renderer renderer : allRenderers()) {
			if ((renderer.rendererClass == RendererClass.BILLBOARD) && renderer.renderingEnabled
					&& !renderer.diffuseTexturePath.isEmpty()) {
				return renderer;
			}
		}
		return null;
	}

	public static PkbEffectSummary of(final PkbEffect effect) {
		final List<Layer> layers = new ArrayList<>();
		final PkbObject root = effect.getRootEffect();
		final PkbObject graph = effect.follow(root, "LayerGraphCompileCache");
		final List<Integer> rootTargets = new ArrayList<>();
		if (graph != null) {
			// the entry event slot lists which layer slots the effect spawns directly
			final List<PkbObject> eventSlots = effect.followAll(graph, "EventSlots");
			for (final PkbObject slot : eventSlots) {
				if (slot.getInt("ParentLayerSlot", -1) == -1) {
					for (final int target : slot.get("LayerTargets") == null ? new int[0]
							: slot.get("LayerTargets").asIntArray()) {
						rootTargets.add(target);
					}
				}
			}
			final List<PkbObject> layerSlots = effect.followAll(graph, "LayerSlots");
			for (int i = 0; i < layerSlots.size(); i++) {
				final PkbObject cache = effect.follow(layerSlots.get(i), "LayerCache");
				if (cache == null) {
					continue;
				}
				final List<Renderer> renderers = new ArrayList<>();
				for (final PkbObject rendererObject : effect.followAll(cache, "Renderers")) {
					renderers.add(readRenderer(effect, rendererObject));
				}
				int blobBytes = 0;
				final PkbObject varying = effect.follow(cache, "BlobCache_IR_TimeVarying");
				if ((varying != null) && (varying.get("Blob") != null)) {
					blobBytes += varying.get("Blob").asBlobBytes().length;
				}
				for (final PkbObject fixed : effect.followAll(cache, "BlobCache_IR_TimeFixed")) {
					if (fixed.get("Blob") != null) {
						blobBytes += fixed.get("Blob").asBlobBytes().length;
					}
				}
				layers.add(new Layer(i, renderers, blobBytes, cache.getLinks("Samplers").length,
						rootTargets.contains(i)));
			}
		}
		final List<String> attributes = new ArrayList<>();
		for (final PkbObject external : effect.allOfType("CCompilerBlobCacheExternal")) {
			final String name = external.getString("NameGUID", "");
			if (name.startsWith("Game.") && !attributes.contains(name)) {
				attributes.add(name);
			}
		}
		return new PkbEffectSummary(layers, attributes);
	}

	private static Renderer readRenderer(final PkbEffect effect, final PkbObject rendererObject) {
		final RendererClass rendererClass = RendererClass.of(rendererObject.getInt("RendererClass", 0));
		boolean transparent = false;
		int transparentType = 0;
		boolean opaque = false;
		int opaqueType = 0;
		int billboardingMode = 0;
		String texture = "";
		int subX = 0;
		int subY = 0;
		boolean enabled = true;
		boolean size2D = false;
		for (final PkbObject property : effect.followAll(rendererObject, "Properties")) {
			final String name = property.getString("PropertyName", "");
			final PkbValue numeric = property.get("PropertyValueNumeric");
			final int firstWord = numeric == null ? 0 : numeric.asInt();
			final boolean toggle = firstWord != 0;
			switch (name) {
			case "Transparent":
				transparent = toggle;
				break;
			case "Transparent.Type":
				transparentType = firstWord;
				break;
			case "Opaque":
				opaque = toggle;
				break;
			case "Opaque.Type":
				opaqueType = firstWord;
				break;
			case "BillboardingMode":
				billboardingMode = firstWord;
				break;
			case "EnableRendering":
				enabled = toggle;
				break;
			case "EnableSize2D":
				size2D = toggle;
				break;
			case "Diffuse.DiffuseMap":
			case "Blend.TextureBase":
				if (!property.getString("PropertyValueStr", "").isEmpty()) {
					texture = property.getString("PropertyValueStr", "");
				}
				break;
			case "Atlas.SubDiv":
				if (numeric != null) {
					final float[] words = numeric.asFloats();
					subX = (int) words[0];
					subY = words.length > 1 ? (int) words[1] : 0;
				}
				break;
			default:
				break;
			}
		}
		BlendMode blend = BlendMode.OPAQUE;
		if (transparent) {
			blend = BlendMode.of(transparentType <= 3 ? transparentType : 4);
		} else if (opaque) {
			blend = opaqueType == 1 ? BlendMode.ALPHA_KEY : BlendMode.OPAQUE;
		}
		return new Renderer(rendererClass, blend, billboardingMode, texture, subX, subY, enabled, size2D);
	}
}
