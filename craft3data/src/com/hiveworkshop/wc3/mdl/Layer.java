package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.LayerView;
import com.hiveworkshop.wc3.mdl.v2.timelines.Animatable;
import com.hiveworkshop.wc3.mdx.LayerChunk;
import com.hiveworkshop.wc3.mdx.MaterialTextureId;
import com.hiveworkshop.wc3.util.ModelUtils;

/**
 * Layers for MDLToolkit/MatrixEater.
 * <p>
 * Eric Theller 3/8/2012
 */
public class Layer implements Named, VisibilitySource, LayerView, TimelineContainer {
	// 0: none
	// 1: transparent
	// 2: blend
	// 3: additive
	// 4: add alpha
	// 5: modulate
	// 6: modulate 2x
	public static enum FilterMode {
		NONE("None"),
		TRANSPARENT("Transparent"),
		BLEND("Blend"),
		ADDITIVE("Additive"),
		ADDALPHA("AddAlpha"),
		MODULATE("Modulate"),
		MODULATE2X("Modulate2x");

		String mdlText;

		FilterMode(final String str) {
			this.mdlText = str;
		}

		public String getMdlText() {
			return mdlText;
		}

		public static FilterMode fromId(final int id) {
			return values()[id];
		}

		public static int nameToId(final String name) {
			for (final FilterMode mode : values()) {
				if (mode.getMdlText().equals(name)) {
					return mode.ordinal();
				}
			}
			return -1;
		}

		@Override
		public String toString() {
			return getMdlText();
		}
	}

	private String filterMode = "None";//
	int TVertexAnimId = -1;
	private int CoordId = 0;
	TextureAnim textureAnim;
	private double emissiveGain = Double.NaN;
	private Vertex fresnelColor;
	private double fresnelOpacity;
	private double fresnelTeamColor;
	private double staticAlpha = -1;// Amount of static alpha (opacity)
	private ArrayList<String> flags = new ArrayList<>();// My way of
	// dealing with
	// all the stuff
	// that I
	// forget/don't
	// bother with:
	// "Unshaded,"
	// "Unfogged,"
	// "TwoSided,"
	// "CoordId X,"
	// actually
	// CoordId was
	// moved into
	// its own field
	ArrayList<AnimFlag> anims = new ArrayList<>();// Used instead of
	// static alpha for
	// animated alpha
	ArrayList<Bitmap> textures;
	private final EnumMap<ShaderTextureTypeHD, Integer> shaderTextureIds = new EnumMap<>(ShaderTextureTypeHD.class);
	private final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = new EnumMap<>(ShaderTextureTypeHD.class);
	private LayerShader layerShader;
	/**
	 * Shader type ids other than 0 (SD) and 1 (HD) exist in Forsaken Kingdom data (2 on the Forsaken Paladin
	 * portrait, 24 on some cinematic ice props). They are preserved here for round-tripping and rendered with the
	 * closest known shader.
	 */
	private int unknownShaderTypeId = -1;

	public String getFilterModeString() {
		return filterMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + CoordId;
		result = (prime * result) + TVertexAnimId;
		result = (prime * result) + (anims == null ? 0 : anims.hashCode());
		long temp;
		temp = Double.doubleToLongBits(emissiveGain);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelOpacity);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelTeamColor);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (filterMode == null ? 0 : filterMode.hashCode());
		result = (prime * result) + (flags == null ? 0 : flags.hashCode());
		temp = Double.doubleToLongBits(staticAlpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (textureAnim == null ? 0 : textureAnim.hashCode());
		result = (prime * result) + (textures == null ? 0 : textures.hashCode());
		result = (prime * result) + (shaderTextures == null ? 0 : shaderTextures.hashCode());
		result = (prime * result) + (layerShader == null ? 0 : layerShader.hashCode());
		if (fresnelColor != null) {
			temp = Double.doubleToLongBits(fresnelColor.x);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(fresnelColor.y);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(fresnelColor.z);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
		}
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Layer other = (Layer) obj;
		if (CoordId != other.CoordId) {
			return false;
		}
		if (TVertexAnimId != other.TVertexAnimId) {
			return false;
		}
		if (anims == null) {
			if (other.anims != null) {
				return false;
			}
		}
		else if (!anims.equals(other.anims)) {
			return false;
		}
		if (Double.doubleToLongBits(emissiveGain) != Double.doubleToLongBits(other.emissiveGain)) {
			return false;
		}
		if (Double.doubleToLongBits(fresnelOpacity) != Double.doubleToLongBits(other.fresnelOpacity)) {
			return false;
		}
		if (Double.doubleToLongBits(fresnelTeamColor) != Double.doubleToLongBits(other.fresnelTeamColor)) {
			return false;
		}
		if (fresnelColor == null) {
			if (other.fresnelColor != null) {
				return false;
			}
		}
		else {
			if (other.fresnelColor == null) {
				return false;
			}
			if (!fresnelColor.equalLocs(other.fresnelColor)) {
				return false;
			}
		}
		if (filterMode == null) {
			if (other.filterMode != null) {
				return false;
			}
		}
		else if (!filterMode.equals(other.filterMode)) {
			return false;
		}
		if (flags == null) {
			if (other.flags != null) {
				return false;
			}
		}
		else if (!flags.equals(other.flags)) {
			return false;
		}
		if (Double.doubleToLongBits(staticAlpha) != Double.doubleToLongBits(other.staticAlpha)) {
			return false;
		}
		if (textureAnim == null) {
			if (other.textureAnim != null) {
				return false;
			}
		}
		else if (!textureAnim.equals(other.textureAnim)) {
			return false;
		}
		if (textures == null) {
			if (other.textures != null) {
				return false;
			}
		}
		else if (!textures.equals(other.textures)) {
			return false;
		}
		if (shaderTextures == null) {
			if (other.shaderTextures != null) {
				return false;
			}
		}
		else if (!shaderTextures.equals(other.shaderTextures)) {
			return false;
		}
		if (layerShader == null) {
			if (other.layerShader != null) {
				return false;
			}
		}
		else if (!layerShader.equals(other.layerShader)) {
			return false;
		}
		return true;
	}

	// @Override
	// public boolean equals( Object o )
	// {
	// if( !( o instanceof Layer ) )
	// {
	// return false;
	// }
	// Layer lay = (Layer)o;
	// boolean does =staticAlpha == lay.staticAlpha
	// && CoordId == lay.CoordId
	// && (texture == null ? lay.texture == null : texture.equals(lay.texture) )
	// && (textureAnim == null ? lay.textureAnim == null :
	// textureAnim.equals(lay.textureAnim) )
	// && (filterMode == null ? lay.filterMode == null :
	// filterMode.equals(lay.filterMode) )
	// && (textures == null ? lay.textures == null :
	// textures.equals(lay.textures) )
	// && (flags == null ? lay.flags == null : flags.equals(lay.flags) )
	// && (anims == null ? lay.anims == null : anims.equals(lay.anims) );
	// return does;
	// }
	public Layer(final String filterMode, final int textureId) {
		this.filterMode = filterMode;
		this.shaderTextureIds.put(ShaderTextureTypeHD.Diffuse, textureId);
		this.layerShader = LayerShader.SD;
	}

	public Layer(final String filterMode, final Bitmap texture) {
		this.filterMode = filterMode;
		this.shaderTextures.put(ShaderTextureTypeHD.Diffuse, texture);
		this.layerShader = LayerShader.SD;
	}

	public Layer(final FilterMode filterMode, final LayerShader layerShader) {
		setFilterMode(filterMode);
		this.layerShader = layerShader;
	}

	public Layer(final Layer other) {
		filterMode = other.filterMode;
		TVertexAnimId = other.TVertexAnimId;
		CoordId = other.CoordId;
		if (other.textureAnim != null) {
			textureAnim = new TextureAnim(other.textureAnim);
		}
		else {
			textureAnim = null;
		}
		staticAlpha = other.staticAlpha;
		emissiveGain = other.emissiveGain;
		fresnelColor = other.fresnelColor == null ? null : new Vertex(other.fresnelColor);
		fresnelOpacity = other.fresnelOpacity;
		fresnelTeamColor = other.fresnelTeamColor;
		layerShader = other.layerShader;
		unknownShaderTypeId = other.unknownShaderTypeId;
		shaderTextures.putAll(other.shaderTextures);
		flags = new ArrayList<>(other.flags);
		anims = new ArrayList<>();
		textures = new ArrayList<>();
		for (final AnimFlag af : other.anims) {
			anims.add(new AnimFlag(af));
		}
		if (other.textures != null) {
			for (final Bitmap bmp : other.textures) {
				textures.add(new Bitmap(bmp));
			}
		}
		else {
			textures = null;
		}
	}

	public Layer(final LayerChunk.Layer lay) {
		this(FilterMode.fromId(lay.filterMode).getMdlText(), lay.getSDSingleTextureId());
		final int shadingFlags = lay.shadingFlags;
		// 0x1: unshaded
		// 0x2: sphere environment map
		// 0x4: wrap width
		// 0x8: wrap height
		// 0x10: two sided
		// 0x20: unfogged
		// 0x30: no depth test
		// 0x40: no depth set
		if (EditableModel.hasFlag(shadingFlags, 0x1)) {
			add("Unshaded");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x2)) {
			add("SphereEnvMap");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x4)) {
			add("WrapWidth");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x8)) {
			add("WrapHeight");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x10)) {
			add("TwoSided");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x20)) {
			add("Unfogged");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x40)) {
			add("NoDepthTest");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x80)) {
			add("NoDepthSet");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x100)) {
			add("Unlit");
		}
		// MDX 1800 (Forsaken Kingdom) added two shading flag bits and two MDL keywords ("BackFacesForShadows",
		// "AmbientOcclusion"). The bit<->keyword pairing below is a best guess from the stock 3.0 data: 0x200 is
		// only used by the cinematic "shadowblocker" doodads, 0x400 by large opaque buildings and walls.
		if (EditableModel.hasFlag(shadingFlags, 0x200)) {
			add("BackFacesForShadows");
		}
		if (EditableModel.hasFlag(shadingFlags, 0x400)) {
			add("AmbientOcclusion");
		}
//		System.err.println("Creating MDL layer from shadingFlags: " + Integer.toBinaryString(lay.shadingFlags));
		if (lay.materialEmissions != null) {
			final AnimFlag flag = new AnimFlag(lay.materialEmissions);
			anims.add(flag);
			emissiveGain = lay.emissiveGain;
		}
		else if (!Float.isNaN(lay.emissiveGain)) {
			emissiveGain = lay.emissiveGain;
		}
		setTVertexAnimId(lay.textureAnimationId);
		setCoordId(lay.coordID); // this isn't an unknown field!
		// it's coordID! don't be like
		// Magos and forget!
		// (breaks the volcano model, this is why War3ModelEditor can't open
		// volcano!)
		if (lay.materialAlpha != null) {
			final AnimFlag flag = new AnimFlag(lay.materialAlpha);
			anims.add(flag);
		}
		else if (lay.alpha != 1.0f) {
			setStaticAlpha(lay.alpha);
		}
		if (lay.materialTextureId != null) {
			final AnimFlag flag = new AnimFlag(lay.materialTextureId);
			anims.add(flag);
		}
		if (lay.materialFresnelColor != null) {
			final AnimFlag flag = new AnimFlag(lay.materialFresnelColor);
			anims.add(flag);
		}
		else if (lay.fresnelColor != null) {
			final Vertex coloring = new Vertex(MdlxUtils.flipRGBtoBGR(lay.fresnelColor));
			if ((coloring.x != 1.0) || (coloring.y != 1.0) || (coloring.z != 1.0)) {
				setFresnelColor(coloring);
			}
		}
		if (lay.materialFresnelOpacity != null) {
			final AnimFlag flag = new AnimFlag(lay.materialFresnelOpacity);
			anims.add(flag);
		}
		else if (lay.fresnelOpacity != 0.0f) {
			fresnelOpacity = lay.fresnelOpacity;
		}
		if (lay.materialFresnelTeamColor != null) {
			final AnimFlag flag = new AnimFlag(lay.materialFresnelTeamColor);
			anims.add(flag);
		}
		else if (lay.fresnelTeamColor != 0.0f) {
			fresnelTeamColor = lay.fresnelTeamColor;
		}
		if (lay.textureIdsMDLX1100 != null) {
			for (final ShaderTextureTypeHD textureTypeHD : ShaderTextureTypeHD.VALUES) {
				final int singleTextureId = lay.textureIdsMDLX1100[textureTypeHD.ordinal()];
				if (singleTextureId != LayerChunk.Layer.NO_TEXTURE_ID) {
					shaderTextureIds.put(textureTypeHD, singleTextureId);
				}
				final MaterialTextureId materialTextureId = lay.animatedTextureIdsMDLX1100[textureTypeHD.ordinal()];
				if (materialTextureId != null) {
					final AnimFlag flag = new AnimFlag(materialTextureId);
					if (textureTypeHD != ShaderTextureTypeHD.Diffuse) {
						flag.setName(textureTypeHD.name() + flag.getName());
					}
					anims.add(flag);
				}
			}
		}
		if (lay.shaderTypeId != LayerChunk.Layer.NO_SHADER_TYPE_ID) {
			setShaderTypeId(lay.shaderTypeId);
		}
		else {
			layerShader = LayerShader.SD; // TODO determine this later on, probably from MDLX1000 parser
		}
	}

	private Layer() {
		flags = new ArrayList<>();
		anims = new ArrayList<>();
		layerShader = LayerShader.SD;
	}

	public Bitmap firstTexture() {
		final Bitmap diffuse = shaderTextures.get(ShaderTextureTypeHD.Diffuse);
		if (diffuse != null) {
			return diffuse;
		}
		else {
			if ((textures != null) && (textures.size() > 0)) {
				return textures.get(0);
			}
			return null;
		}
	}

	public Bitmap getRenderTexture(final AnimatedRenderEnvironment animatedRenderEnvironment, final EditableModel model,
			final ShaderTextureTypeHD shaderTextureTypeHD) {
		final String flagKeyName = (shaderTextureTypeHD == ShaderTextureTypeHD.Diffuse ? ""
				: shaderTextureTypeHD.name()) + "TextureID";
		final AnimFlag textureFlag = AnimFlag.find(getAnims(), flagKeyName);
		if ((textureFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentAnimation() == null) {
				if (textures.size() > 0) {
					return textures.get(0);
				}
				else {
					return shaderTextures.get(shaderTextureTypeHD);
				}
			}
			final Integer textureIdAtTime = (Integer) textureFlag.interpolateAt(animatedRenderEnvironment, -1);
			if ((textureIdAtTime >= model.getTextures().size()) || (textureIdAtTime == -1)) {
				return shaderTextures.get(shaderTextureTypeHD);
			}
			final Bitmap textureAtTime = model.getTextures().get(textureIdAtTime);
			return textureAtTime;
		}
		else {
			return shaderTextures.get(shaderTextureTypeHD);
		}
	}

	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag visibilityFlag = getVisibilityFlag();
		if (visibilityFlag != null) {
			final Number alpha = (Number) visibilityFlag.interpolateAt(animatedRenderEnvironment);
			if (alpha == null) {
				return staticAlpha < 0 ? 1 : (float) staticAlpha;
			}
			final float alphaFloatValue = alpha.floatValue();
			return alphaFloatValue;
		}
		return staticAlpha < 0 ? 1 : (float) staticAlpha;
	}

	public Vertex getRenderFresnelColor(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag fresnelColorFlag = AnimFlag.find(getAnims(), "FresnelColor");
		if ((fresnelColorFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentAnimation() == null) {
				return fresnelColor;
			}
			final Vertex fresnelColorAtTime = (Vertex) fresnelColorFlag.interpolateAt(animatedRenderEnvironment);
			return fresnelColorAtTime;
		}
		else {
			return fresnelColor;
		}
	}

	public float getRenderFresnelTeamColor(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag visibilityFlag = AnimFlag.find(getAnims(), "FresnelTeamColor");
		if (visibilityFlag != null) {
			final Number alpha = (Number) visibilityFlag.interpolateAt(animatedRenderEnvironment);
			if (alpha == null) {
				return fresnelTeamColor < 0 ? 0 : (float) fresnelTeamColor;
			}
			final float alphaFloatValue = alpha.floatValue();
			return alphaFloatValue;
		}
		return fresnelTeamColor < 0 ? 0 : (float) fresnelTeamColor;
	}

	public float getRenderFresnelOpacity(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag visibilityFlag = AnimFlag.find(getAnims(), "FresnelOpacity");
		if (visibilityFlag != null) {
			final Number alpha = (Number) visibilityFlag.interpolateAt(animatedRenderEnvironment);
			if (alpha == null) {
				return fresnelOpacity < 0 ? 0 : (float) fresnelOpacity;
			}
			final float alphaFloatValue = alpha.floatValue();
			return alphaFloatValue;
		}
		return fresnelOpacity < 0 ? 0 : (float) fresnelOpacity;
	}

	public float getRenderEmissiveGain(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		final AnimFlag visibilityFlag = AnimFlag.find(getAnims(), "EmissiveGain");
		if (visibilityFlag != null) {
			final Number alpha = (Number) visibilityFlag.interpolateAt(animatedRenderEnvironment);
			if (alpha == null) {
				return emissiveGain < 0 ? 0 : (float) emissiveGain;
			}
			final float alphaFloatValue = alpha.floatValue();
			return alphaFloatValue;
		}
		return emissiveGain < 0 ? 0 : (float) emissiveGain;
	}

	public void setTextureAnim(final TextureAnim texa) {
		textureAnim = texa;
	}

	public void updateTextureAnim(final ArrayList<TextureAnim> list) { // Sets the
		// texture
		// anim
		// reference
		// to the
		// one from
		// the list
		// corresponding
		// to the
		// TVertexAnimId
		textureAnim = list.get(TVertexAnimId);
	}

	private transient Map<Integer, Bitmap> ridiculouslyWrongTextureIDToTexture = new HashMap<>();
	private transient EnumMap<ShaderTextureTypeHD, Map<Integer, Bitmap>> shaderTypeToRidiculouslyWrongTextureIDToTexture = new EnumMap<>(
			ShaderTextureTypeHD.class);;

	private Map<Integer, Bitmap> getRidiculouslyWrongTextureIDToTexture(final ShaderTextureTypeHD type) {
		Map<Integer, Bitmap> map = shaderTypeToRidiculouslyWrongTextureIDToTexture.get(type);
		if (map == null) {
			map = new HashMap<>();
			shaderTypeToRidiculouslyWrongTextureIDToTexture.put(type, map);
		}
		return map;
	}

	public void buildTextureList(final EditableModel mdlr) {
		final AnimFlag txFlag = getFlag("TextureID");
		if (txFlag != null) {
			if (textures == null) {
				textures = new ArrayList<>();
			}
			for (int i = 0; i < txFlag.values.size(); i++) {
				final int txId = ((Integer) txFlag.values.get(i)).intValue();
				final Bitmap texture2 = mdlr.getTexture(txId);
				textures.add(texture2);
				ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
			}
		}
		for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
			if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
				final AnimFlag shaderTextureIdFlag = getFlag(shaderTextureTypeHD.name() + "TextureID");
				if (shaderTextureIdFlag != null) {
					if (textures == null) {
						textures = new ArrayList<>();
					}
					for (int i = 0; i < shaderTextureIdFlag.values.size(); i++) {
						final int txId = ((Integer) shaderTextureIdFlag.values.get(i)).intValue();
						final Bitmap texture2 = mdlr.getTexture(txId);
						textures.add(texture2);
						getRidiculouslyWrongTextureIDToTexture(shaderTextureTypeHD).put(txId, texture2);
					}
				}
			}
		}
	}

	public void updateIds(final EditableModel mdlr) {
		TVertexAnimId = mdlr.getTextureAnimId(textureAnim);
		shaderTextureIds.clear();
		for (final Map.Entry<ShaderTextureTypeHD, Bitmap> entry : shaderTextures.entrySet()) {
			final int singleTextureId = mdlr.getTextureId(entry.getValue());
			if (singleTextureId != -1) {
				shaderTextureIds.put(entry.getKey(), singleTextureId);
			}
		}
		if (textures != null) {
			final AnimFlag txFlag = getFlag("TextureID");
			if (txFlag != null) {
				remapTextureIdTrack(txFlag, ridiculouslyWrongTextureIDToTexture, mdlr);
			}
			for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
				final AnimFlag shadTxFlag = getFlag(shaderTextureTypeHD.name() + "TextureID");
				if (shadTxFlag != null) {
					remapTextureIdTrack(shadTxFlag, getRidiculouslyWrongTextureIDToTexture(shaderTextureTypeHD), mdlr);
				}
			}
		}
	}

	/**
	 * Rewrites an animated texture id track from the old texture ids to the ids
	 * in the model's rebuilt texture list.
	 * <p>
	 * The old id to texture map must be read from a snapshot: the previous
	 * implementation wrote each new id back into the same map while still
	 * iterating, so when a new id collided with an old id that had not been
	 * processed yet, the later key resolved to the wrong texture. On a model
	 * whose water layer animated through 45 textures this collapsed the track
	 * into repeating triples on the first save and dropped 21 textures on the
	 * second.
	 */
	private static void remapTextureIdTrack(final AnimFlag track, final Map<Integer, Bitmap> oldIdToTexture,
			final EditableModel mdlr) {
		final Map<Integer, Bitmap> snapshot = new HashMap<>(oldIdToTexture);
		final Map<Integer, Bitmap> newIdToTexture = new HashMap<>();
		for (int i = 0; i < track.values.size(); i++) {
			final int oldId = ((Integer) track.values.get(i)).intValue();
			final Bitmap texture = snapshot.get(oldId);
			final int newId = mdlr.getTextureId(texture);
			track.values.set(i, newId);
			if (texture != null) {
				newIdToTexture.put(newId, texture);
			}
		}
		oldIdToTexture.clear();
		oldIdToTexture.putAll(newIdToTexture);
	}

	public void updateRefs(final EditableModel mdlr) {
		if ((TVertexAnimId >= 0) && (TVertexAnimId < mdlr.texAnims.size())) {
			textureAnim = mdlr.texAnims.get(TVertexAnimId);
		}
		shaderTextures.clear();
		for (final Map.Entry<ShaderTextureTypeHD, Integer> entry : shaderTextureIds.entrySet()) {
			final Integer textureId = entry.getValue();
			if ((textureId >= 0) && (textureId < mdlr.getTextures().size())) {
				shaderTextures.put(entry.getKey(), mdlr.getTexture(textureId));
			}
		}
		updateTextureListIfApplicable(mdlr);
	}

	protected void updateTextureListIfApplicable(final EditableModel mdlr) {
		buildTextureList(mdlr);
	}

	public static Layer read(final BufferedReader mdl, final EditableModel mdlr) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Layer")) {
			boolean hasAnimatedFresnelColor = false;
			final Layer lay = new Layer();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				if (line.contains("FilterMode")) {
					lay.filterMode = MDLReader.readField(line);
				}
				else if (line.contains("static TextureID")) {
					// `static TextureID id,` or the game's `static TextureID id <= slot,`
					final int[] ints = MDLReader.splitToInts(line);
					final int textureId = ints[0];
					final int slot = ints.length > 1 ? ints[1] : 0;
					final ShaderTextureTypeHD slotType = (slot >= 0) && (slot < ShaderTextureTypeHD.VALUES.length)
							? ShaderTextureTypeHD.VALUES[slot]
							: ShaderTextureTypeHD.Diffuse;
					lay.shaderTextureIds.put(slotType, textureId);
					lay.shaderTextures.put(slotType, mdlr.getTexture(textureId));
				}
				else if (line.contains("CoordId")) {
					lay.CoordId = MDLReader.readInt(line);
				}
				else if (line.contains("ShaderTypeId")) {
					lay.setShaderTypeId(MDLReader.readInt(line));
				}
				else if (line.trim().startsWith("Shader ")) {
					lay.setShaderByName(MDLReader.readName(line));
				}
				else if (line.contains("static Emissive") && !line.contains("TextureID")) {
					lay.emissiveGain = MDLReader.readDouble(line);
				}
				else if (line.contains("Emissive") && !line.contains("TextureID")) {
					MDLReader.reset(mdl);
					final AnimFlag emissiveGainAnimFlag = AnimFlag.read(mdl);
					if (emissiveGainAnimFlag.getName().equals("Emissive")) {
						emissiveGainAnimFlag.setName("EmissiveGain");
					}
					lay.anims.add(emissiveGainAnimFlag);
				}
				else if (line.contains("static FresnelOpacity")) {
					lay.fresnelOpacity = MDLReader.readDouble(line);
				}
				else if (line.contains("FresnelOpacity")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
				}
				else if (line.contains("static FresnelTeamColor")) {
					lay.fresnelTeamColor = MDLReader.readDouble(line);
				}
				else if (line.contains("FresnelTeamColor")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
				}
				else if (line.contains("static FresnelColor")) {
					lay.fresnelColor = Vertex.parseText(line);
				}
				else if (line.contains("FresnelColor")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
					hasAnimatedFresnelColor = true;
				}
				else if (line.contains("TVertexAnimId")) {
					lay.TVertexAnimId = MDLReader.readInt(line);
				}
				else if (line.contains("static Alpha")) {
					lay.staticAlpha = MDLReader.readDouble(line);
				}
				else if (line.contains("Alpha")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
				}
				else if (line.contains("TextureID") && line.contains("{")) {
					MDLReader.reset(mdl);
					final AnimFlag textureIdTrack = AnimFlag.read(mdl);
					if (line.contains("<=")) {
						// the game's designator names the slot: `TextureID 3 <= 2 { ... }`
						final int[] ints = MDLReader.splitToInts(line.substring(0, line.indexOf('{')));
						final int slot = ints.length > 1 ? ints[ints.length - 1] : 0;
						if ((slot > 0) && (slot < ShaderTextureTypeHD.VALUES.length)) {
							textureIdTrack.setName(getTextureIdTrackName(ShaderTextureTypeHD.VALUES[slot]));
						}
					}
					lay.anims.add(textureIdTrack);
					lay.buildTextureList(mdlr);
				}
				else {
					boolean foundMatch = false;
					for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
						if (line.contains("static " + shaderTextureTypeHD.name() + "TextureID")) {
							final int textureId = MDLReader.readInt(line);
							lay.shaderTextureIds.put(shaderTextureTypeHD, textureId);
							lay.shaderTextures.put(shaderTextureTypeHD, mdlr.getTexture(textureId));
							foundMatch = true;
						}
					}
					if (!foundMatch) {
						String flag = MDLReader.readFlag(line);
						if ("SphereEnvironmentMap".equals(flag)) {
							// some versions of this codebase were dumping out MDL models with the token
							// "SphereEnvironmentMap" which is not correct, for legacy support we need to
							// convert it to "SphereEnvMap" which is the official Blizzard token to my
							// knowledge
							flag = "SphereEnvMap";
						}
						lay.flags.add(flag);
					}
				}
				MDLReader.mark(mdl);
			}
			if (ModelUtils.isFresnelColorLayerSupported(mdlr.getFormatVersion()) && !hasAnimatedFresnelColor
					&& (lay.fresnelColor == null)) {
				lay.fresnelColor = new Vertex(1, 1, 1); // default value
			}
			if (ModelUtils.isEmissiveLayerSupported(mdlr.getFormatVersion()) && Double.isNaN(lay.emissiveGain)) {
				lay.emissiveGain = 1.0;
			}
			return lay;
		}
		else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Layer: Missing or unrecognized open statement.");
		}
		return null;
	}

	public boolean hasCoordId() {
		return CoordId != 0;
	}

	@Override
	public int getCoordId() {
		return CoordId;
	}

	public boolean hasTexAnim() {
		return TVertexAnimId != -1;
	}

	/** The track name this program uses for a texture slot: {@code TextureID}, {@code NormalTextureID}, ... */
	public static String getTextureIdTrackName(final ShaderTextureTypeHD shaderTextureTypeHD) {
		return (shaderTextureTypeHD == ShaderTextureTypeHD.Diffuse ? "" : shaderTextureTypeHD.name()) + "TextureID";
	}

	/** The slot index of one of this program's texture track names; 0 for the plain {@code TextureID}. */
	public static int getTextureSlot(final String trackName) {
		for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
			if (getTextureIdTrackName(shaderTextureTypeHD).equals(trackName)) {
				return shaderTextureTypeHD.ordinal();
			}
		}
		return 0;
	}

	/**
	 * The shader name the game registers for this layer's shader id, or null for a plain SD layer
	 * (the game's writer omits the line there). Ids the editor does not model keep their registered
	 * names so they survive a text round trip.
	 */
	public String getWarcraftShaderName() {
		if (unknownShaderTypeId == 2) {
			return Material.SHADER_SD_FIXED_FUNCTION;
		}
		if (unknownShaderTypeId == 24) {
			return Material.SHADER_HD_CRYSTAL;
		}
		if ((unknownShaderTypeId == -1) && (layerShader == LayerShader.HD)) {
			return Material.SHADER_HD_DEFAULT_UNIT;
		}
		return null;
	}

	/** Applies a `Shader "name",` line: the game matches the four registered names case-insensitively. */
	public void setShaderByName(final String shaderName) {
		if (Material.SHADER_HD_DEFAULT_UNIT.equalsIgnoreCase(shaderName)) {
			setLayerShader(LayerShader.HD);
		}
		else if (Material.SHADER_HD_CRYSTAL.equalsIgnoreCase(shaderName)) {
			setShaderTypeId(24);
		}
		else if (Material.SHADER_SD_FIXED_FUNCTION.equalsIgnoreCase(shaderName)) {
			setShaderTypeId(2);
		}
		else {
			setLayerShader(LayerShader.SD);
		}
	}

	public void printTo(final PrintWriter writer, final int tabHeight, final boolean useCoords, final int version) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		writer.println(tabs + "Layer {");
		writer.println(tabs + "\tFilterMode " + filterMode + ",");
		for (int i = 0; i < flags.size(); i++) {
			writer.println(tabs + "\t" + flags.get(i) + ",");
		}
		// Warcraft III dialect (the form the game's own MDL reader accepts): from version 1100 the
		// shader is a per-layer `Shader "name",` line (omitted for SD layers), every texture slot is
		// bound with `static TextureID id <= slot,`, and an animated slot is its track with the same
		// designator (`TextureID 3 <= 2 { ... }`, no designator for slot 0). A slot that is animated
		// is written as its track only, as the game does.
		if (ModelUtils.isCombinedHDLayerSupported(version)) {
			final String shaderName = getWarcraftShaderName();
			if (shaderName != null) {
				writer.println(tabs + "\tShader \"" + shaderName + "\",");
			}
			for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
				final AnimFlag track = getFlag(getTextureIdTrackName(shaderTextureTypeHD));
				if ((track != null) && (track.size() > 0)) {
					continue;
				}
				final Integer textureId = shaderTextureIds.get(shaderTextureTypeHD);
				if ((textureId != null) && (textureId != -1)) {
					writer.println(tabs + "\tstatic TextureID " + textureId + " <= " + shaderTextureTypeHD.ordinal()
							+ ",");
				}
			}
		}
		else {
			final AnimFlag track = getFlag("TextureID");
			final Integer textureId = shaderTextureIds.get(ShaderTextureTypeHD.Diffuse);
			if ((textureId != null) && (textureId != -1) && ((track == null) || (track.size() == 0))) {
				writer.println(tabs + "\tstatic TextureID " + textureId + ",");
			}
		}
		for (int i = 0; i < anims.size(); i++) {
			final AnimFlag temp = anims.get(i);
			if (temp.getName().endsWith("TextureID")) {
				if (ModelUtils.isCombinedHDLayerSupported(version)) {
					final int slot = getTextureSlot(temp.getName());
					temp.printTo(writer, tabHeight + 1, "TextureID", slot == 0 ? "" : (" <= " + slot));
				}
				else if (temp.getName().equals("TextureID")) {
					temp.printTo(writer, tabHeight + 1);
				}
			}
		}
		if (hasTexAnim()) {
			writer.println(tabs + "\tTVertexAnimId " + TVertexAnimId + ",");
		}
		if (useCoords) {
			writer.println(tabs + "\tCoordId " + CoordId + ",");
		}
		boolean foundAlpha = false;
		for (int i = 0; i < anims.size(); i++) {
			final AnimFlag temp = anims.get(i);
			if (temp.getName().equals("Alpha")) {
				temp.printTo(writer, tabHeight + 1);
				foundAlpha = true;
			}
		}
		if ((staticAlpha != -1) && !foundAlpha) {
			writer.println(tabs + "\tstatic Alpha " + staticAlpha + ",");
		}
		if (ModelUtils.isEmissiveLayerSupported(version)) {
			boolean foundEmissive = false;
			for (int i = 0; i < anims.size(); i++) {
				final AnimFlag temp = anims.get(i);
				if (temp.getName().startsWith("Emissive")) {
					temp.printTo(writer, tabHeight + 1);
					foundEmissive = true;
				}
			}
			if (!Double.isNaN(emissiveGain) && (emissiveGain != 1.0) && !foundEmissive) {
				writer.println(tabs + "\tstatic EmissiveGain " + MDLReader.doubleToString(emissiveGain) + ",");
			}
		}
		if (ModelUtils.isFresnelColorLayerSupported(version)) {
			boolean foundFresnelColor = false;
			for (int i = 0; i < anims.size(); i++) {
				final AnimFlag temp = anims.get(i);
				if (temp.getName().startsWith("FresnelColor")) {
					temp.printTo(writer, tabHeight + 1);
					foundFresnelColor = true;
				}
			}
			if ((fresnelColor != null)
					&& ((fresnelColor.x != 1.0) || (fresnelColor.y != 1.0) || (fresnelColor.z != 1.0))
					&& !foundFresnelColor) {
				writer.println(tabs + "\tstatic FresnelColor " + fresnelColor + ",");
			}
			boolean foundFresnelOpacity = false;
			for (int i = 0; i < anims.size(); i++) {
				final AnimFlag temp = anims.get(i);
				if (temp.getName().startsWith("FresnelOpacity")) {
					temp.printTo(writer, tabHeight + 1);
					foundFresnelOpacity = true;
				}
			}
			if (!Double.isNaN(fresnelOpacity) && (fresnelOpacity != 0) && !foundFresnelOpacity) {
				writer.println(tabs + "\tstatic FresnelOpacity " + MDLReader.doubleToString(fresnelOpacity) + ",");
			}
			boolean foundFresnelTeamColor = false;
			for (int i = 0; i < anims.size(); i++) {
				final AnimFlag temp = anims.get(i);
				if (temp.getName().startsWith("FresnelTeamColor")) {
					temp.printTo(writer, tabHeight + 1);
					foundFresnelTeamColor = true;
				}
			}
			if (!Double.isNaN(fresnelTeamColor) && (fresnelTeamColor != 0) && !foundFresnelTeamColor) {
				writer.println(tabs + "\tstatic FresnelTeamColor " + MDLReader.doubleToString(fresnelTeamColor) + ",");
			}
		}
		writer.println(tabs + "}");
	}

	@Override
	public String getName() {
		final Bitmap texture = shaderTextures.get(ShaderTextureTypeHD.Diffuse);
		return getTextureName(texture, "multi-textured layer (mode " + filterMode + ") ");
	}

	public String getTextureName(final Bitmap texture, final String nullText) {
		if (texture != null) {
			return texture.getName() + " layer (mode " + filterMode + ") ";
		}
		return nullText;
	}

	public AnimFlag getFlag(final String what) {
		int count = 0;
		AnimFlag output = null;
		for (final AnimFlag af : anims) {
			if (af.getName().equals(what)) {
				count++;
				output = af;
			}
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some " + what + " animation data was lost unexpectedly during retrieval in " + getName() + ".");
		}
		return output;
	}

	@Override
	public void setVisibilityFlag(final AnimFlag flag) {
		int count = 0;
		int index = 0;
		for (int i = 0; i < anims.size(); i++) {
			final AnimFlag af = anims.get(i);
			if (af.getName().equals("Visibility") || af.getName().equals("Alpha")) {
				count++;
				index = i;
				anims.remove(af);
			}
		}
		if (flag != null) {
			anims.add(index, flag);
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some visiblity animation data was lost unexpectedly during overwrite in " + getName() + ".");
		}
	}

	@Override
	public AnimFlag getVisibilityFlag() {
		int count = 0;
		AnimFlag output = null;
		for (final AnimFlag af : anims) {
			if (af.getName().equals("Visibility") || af.getName().equals("Alpha")) {
				count++;
				output = af;
			}
		}
		if (count > 1) {
			JOptionPane.showMessageDialog(null,
					"Some visiblity animation data was lost unexpectedly during retrieval in " + getName() + ".");
		}
		return output;
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public int getTVertexAnimId() {
		return TVertexAnimId;
	}

	public void setTVertexAnimId(final int tVertexAnimId) {
		TVertexAnimId = tVertexAnimId;
	}

//	public Bitmap getTextureBitmap() {
//		return texture;
//	}

	public EnumMap<ShaderTextureTypeHD, Bitmap> getShaderTextures() {
		return shaderTextures;
	}

	@Deprecated
	public EnumMap<ShaderTextureTypeHD, Integer> getShaderTextureIds() {
		return shaderTextureIds;
	}

	public LayerShader getLayerShader() {
		return layerShader;
	}

	public int getUnknownShaderTypeId() {
		return unknownShaderTypeId;
	}

	public void setShaderTypeId(final int shaderTypeId) {
		if ((shaderTypeId >= 0) && (shaderTypeId < LayerShader.values().length)) {
			layerShader = LayerShader.fromId(shaderTypeId);
			unknownShaderTypeId = -1;
		}
		else {
			// unknown shader from a newer game version: keep the id, render as HD when more than one texture is set
			unknownShaderTypeId = shaderTypeId;
			layerShader = shaderTextures.size() > 1 ? LayerShader.HD : LayerShader.SD;
		}
	}

	public void setLayerShader(final LayerShader layerShader) {
		this.layerShader = layerShader;
		this.unknownShaderTypeId = -1;
	}

//	public void setTexture(final Bitmap texture) {
//		this.texture = texture;
//	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(final double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public ArrayList<String> getFlags() {
		return flags;
	}

	public void setFlags(final ArrayList<String> flags) {
		this.flags = flags;
	}

	public void add(final String flag) {
		flags.add(flag);
	}

	public ArrayList<AnimFlag> getAnims() {
		return anims;
	}

	public void setAnims(final ArrayList<AnimFlag> anims) {
		this.anims = anims;
	}

	public ArrayList<Bitmap> getTextures() {
		return textures;
	}

	public void setTextures(final ArrayList<Bitmap> textures) {
		this.textures = textures;
	}

	public TextureAnim getTextureAnim() {
		return textureAnim;
	}

	public void setFilterMode(final String filterMode) {
		this.filterMode = filterMode;
	}

	public void setFilterMode(final FilterMode mode) {
		this.filterMode = mode.getMdlText();
	}

	public void setCoordId(final int coordId) {
		CoordId = coordId;
	}

	@Override
	public FilterMode getFilterMode() {
		return FilterMode.fromId(FilterMode.nameToId(filterMode));
	}

	@Override
	public boolean isUnshaded() {
		return flags.contains("Unshaded");
	}

	@Override
	public boolean isUnfogged() {
		return flags.contains("Unfogged");
	}

	@Override
	public boolean isTwoSided() {
		return flags.contains("TwoSided");
	}

	@Override
	public boolean isSphereEnvironmentMap() {
		return flags.contains("SphereEnvMap");
	}

	@Override
	public boolean isNoDepthTest() {
		return flags.contains("NoDepthTest");
	}

	@Override
	public boolean isNoDepthSet() {
		return flags.contains("NoDepthSet");
	}

	public boolean isUnlit() {
		return flags.contains("Unlit");
	}

	public double getEmissive() {
		return emissiveGain;
	}

	public void setEmissive(final double emissive) {
		this.emissiveGain = emissive;
	}

	@Override
	public Animatable<Bitmap> getTexture() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Animatable<Double> getAlpha() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public Vertex getFresnelColor() {
		return fresnelColor;
	}

	public void setFresnelColor(final Vertex fresnelColor) {
		this.fresnelColor = fresnelColor;
	}

	public double getFresnelOpacity() {
		return fresnelOpacity;
	}

	public void setFresnelOpacity(final double fresnelOpacity) {
		this.fresnelOpacity = fresnelOpacity;
	}

	public double getFresnelTeamColor() {
		return fresnelTeamColor;
	}

	public void setFresnelTeamColor(final double fresnelTeamColor) {
		this.fresnelTeamColor = fresnelTeamColor;
	}

	public Map<Integer, Bitmap> getRidiculouslyWrongTextureIDToTexture() {
		return ridiculouslyWrongTextureIDToTexture;
	}

	@Override
	public void add(final AnimFlag timeline) {
		anims.add(timeline);
	}

	@Override
	public void remove(final AnimFlag timeline) {
		anims.remove(timeline);
	}
}
