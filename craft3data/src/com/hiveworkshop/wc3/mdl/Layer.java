package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.LayerView;
import com.hiveworkshop.wc3.mdl.v2.timelines.Animatable;
import com.hiveworkshop.wc3.mdx.LayerChunk;
import com.hiveworkshop.wc3.util.ModelUtils;

/**
 * Layers for MDLToolkit/MatrixEater.
 * <p>
 * Eric Theller 3/8/2012
 */
public class Layer implements Named, VisibilitySource, LayerView {
	// 0: none
	// 1: transparent
	// 2: blend
	// 3: additive
	// 4: add alpha
	// 5: modulate
	// 6: modulate 2x
	public static enum FilterMode {
		NONE("None"), TRANSPARENT("Transparent"), BLEND("Blend"), ADDITIVE("Additive"), ADDALPHA("AddAlpha"),
		MODULATE("Modulate"), MODULATE2X("Modulate2x");

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
	int textureId = -1;
	int TVertexAnimId = -1;
	private int CoordId = 0;
	Bitmap texture;
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

	public String getFilterModeString() {
		return filterMode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + CoordId;
		result = (prime * result) + TVertexAnimId;
		result = (prime * result) + ((anims == null) ? 0 : anims.hashCode());
		long temp;
		temp = Double.doubleToLongBits(emissiveGain);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelOpacity);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelTeamColor);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((filterMode == null) ? 0 : filterMode.hashCode());
		result = (prime * result) + ((flags == null) ? 0 : flags.hashCode());
		temp = Double.doubleToLongBits(staticAlpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((texture == null) ? 0 : texture.hashCode());
		result = (prime * result) + ((textureAnim == null) ? 0 : textureAnim.hashCode());
		result = (prime * result) + textureId;
		result = (prime * result) + ((textures == null) ? 0 : textures.hashCode());
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
		} else if (!anims.equals(other.anims)) {
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
		} else {
			if (!fresnelColor.equalLocs(other.fresnelColor)) {
				return false;
			}
		}
		if (filterMode == null) {
			if (other.filterMode != null) {
				return false;
			}
		} else if (!filterMode.equals(other.filterMode)) {
			return false;
		}
		if (flags == null) {
			if (other.flags != null) {
				return false;
			}
		} else if (!flags.equals(other.flags)) {
			return false;
		}
		if (Double.doubleToLongBits(staticAlpha) != Double.doubleToLongBits(other.staticAlpha)) {
			return false;
		}
		if (texture == null) {
			if (other.texture != null) {
				return false;
			}
		} else if (!texture.equals(other.texture)) {
			return false;
		}
		if (textureAnim == null) {
			if (other.textureAnim != null) {
				return false;
			}
		} else if (!textureAnim.equals(other.textureAnim)) {
			return false;
		}
		if (textureId != other.textureId) {
			return false;
		}
		if (textures == null) {
			if (other.textures != null) {
				return false;
			}
		} else if (!textures.equals(other.textures)) {
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
		this.textureId = textureId;
	}

	public Layer(final String filterMode, final Bitmap texture) {
		this.filterMode = filterMode;
		this.texture = texture;
	}

	public Layer(final Layer other) {
		filterMode = other.filterMode;
		textureId = other.textureId;
		TVertexAnimId = other.TVertexAnimId;
		CoordId = other.CoordId;
		texture = new Bitmap(other.texture);
		if (other.textureAnim != null) {
			textureAnim = new TextureAnim(other.textureAnim);
		} else {
			textureAnim = null;
		}
		staticAlpha = other.staticAlpha;
		emissiveGain = other.emissiveGain;
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
		} else {
			textures = null;
		}
	}

	public Layer(final LayerChunk.Layer lay) {
		this(FilterMode.fromId(lay.filterMode).getMdlText(), lay.textureId);
		final int shadingFlags = lay.shadingFlags;
		// 0x1: unshaded
		// 0x2: sphere environment map
		// 0x4: ?
		// 0x8: ?
		// 0x10: two sided
		// 0x20: unfogged
		// 0x30: no depth test
		// 0x40: no depth set
		if (MDL.hasFlag(shadingFlags, 0x1)) {
			add("Unshaded");
		}
		if (MDL.hasFlag(shadingFlags, 0x2)) {
			add("SphereEnvironmentMap");
		}
		if (MDL.hasFlag(shadingFlags, 0x10)) {
			add("TwoSided");
		}
		if (MDL.hasFlag(shadingFlags, 0x20)) {
			add("Unfogged");
		}
		if (MDL.hasFlag(shadingFlags, 0x40)) {
			add("NoDepthTest");
		}
		if (MDL.hasFlag(shadingFlags, 0x80)) {
			add("NoDepthSet");
		}
//		System.err.println("Creating MDL layer from shadingFlags: " + Integer.toBinaryString(lay.shadingFlags));
		if (lay.materialEmissions != null) {
			final AnimFlag flag = new AnimFlag(lay.materialEmissions);
			anims.add(flag);
		} else if (!Float.isNaN(lay.emissive)) {
			emissiveGain = lay.emissive;
		}
		setTVertexAnimId(lay.textureAnimationId);
		setCoordId(lay.unknownNull_CoordID); // this isn't an unknown field!
		// it's coordID! don't be like
		// Magos and forget!
		// (breaks the volcano model, this is why War3ModelEditor can't open
		// volcano!)
		if (lay.materialAlpha != null) {
			final AnimFlag flag = new AnimFlag(lay.materialAlpha);
			anims.add(flag);
		} else if (lay.alpha != 1.0f) {
			setStaticAlpha(lay.alpha);
		}
		if (lay.materialTextureId != null) {
			final AnimFlag flag = new AnimFlag(lay.materialTextureId);
			anims.add(flag);
		}
	}

	private Layer() {
		flags = new ArrayList<>();
		anims = new ArrayList<>();
	}

	public Bitmap firstTexture() {
		if (texture != null) {
			return texture;
		} else {
			if ((textures != null) && (textures.size() > 0)) {
				return textures.get(0);
			}
			return null;
		}
	}

	public Bitmap getRenderTexture(final AnimatedRenderEnvironment animatedRenderEnvironment, final MDL model) {
		final AnimFlag textureFlag = AnimFlag.find(getAnims(), "TextureID");
		if ((textureFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentAnimation() == null) {
				if (textures.size() > 0) {
					return textures.get(0);
				} else {
					return texture;
				}
			}
			final Integer textureIdAtTime = (Integer) textureFlag.interpolateAt(animatedRenderEnvironment);
			if (textureIdAtTime >= model.getTextures().size()) {
				return texture;
			}
			final Bitmap textureAtTime = model.getTextures().get(textureIdAtTime);
			return textureAtTime;
		} else {
			return texture;
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

	public void setTextureAnim(final TextureAnim texa) {
		textureAnim = texa;
	}

	public void setTextureAnim(final ArrayList<TextureAnim> list) { // Sets the
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

	public void buildTextureList(final MDL mdlr) {
		textures = new ArrayList<>();
		final AnimFlag txFlag = getFlag("TextureID");
		for (int i = 0; i < txFlag.values.size(); i++) {
			final int txId = ((Integer) txFlag.values.get(i)).intValue();
			final Bitmap texture2 = mdlr.getTexture(txId);
			textures.add(texture2);
			ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
		}
	}

	public void updateIds(final MDL mdlr) {
		textureId = mdlr.getTextureId(texture);
		TVertexAnimId = mdlr.getTextureAnimId(textureAnim);
		if (textures != null) {
			final AnimFlag txFlag = getFlag("TextureID");
			for (int i = 0; i < txFlag.values.size(); i++) {
				final Bitmap textureFoundFromDirtyId = ridiculouslyWrongTextureIDToTexture
						.get(((Integer) txFlag.values.get(i)).intValue());
				final int newerTextureId = mdlr.getTextureId(textureFoundFromDirtyId);
				txFlag.values.set(i, newerTextureId);
				ridiculouslyWrongTextureIDToTexture.put(newerTextureId, textureFoundFromDirtyId);
			}
		}
	}

	public void updateRefs(final MDL mdlr) {
		if ((textureId >= 0) && (textureId < mdlr.getTextures().size())) {
			texture = mdlr.getTexture(textureId);
		}
		if ((TVertexAnimId >= 0) && (TVertexAnimId < mdlr.texAnims.size())) {
			textureAnim = mdlr.texAnims.get(TVertexAnimId);
		}
		final AnimFlag txFlag = getFlag("TextureID");
		if (txFlag != null) {
			buildTextureList(mdlr);
		}
	}

	public static Layer read(final BufferedReader mdl, final MDL mdlr) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Layer")) {
			final Layer lay = new Layer();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				if (line.contains("FilterMode")) {
					lay.filterMode = MDLReader.readField(line);
				} else if (line.contains("static TextureID")) {
					lay.textureId = MDLReader.readInt(line);
					lay.texture = mdlr.getTexture(lay.textureId);
				} else if (line.contains("CoordId")) {
					lay.CoordId = MDLReader.readInt(line);
				} else if (line.contains("static Emissive")) {
					lay.emissiveGain = MDLReader.readDouble(line);
				} else if (line.contains("Emissive")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
				} else if (line.contains("TVertexAnimId")) {
					lay.TVertexAnimId = MDLReader.readInt(line);
				} else if (line.contains("static Alpha")) {
					lay.staticAlpha = MDLReader.readDouble(line);
				} else if (line.contains("Alpha")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
				} else if (line.contains("TextureID")) {
					MDLReader.reset(mdl);
					lay.anims.add(AnimFlag.read(mdl));
					lay.buildTextureList(mdlr);
				} else {
					lay.flags.add(MDLReader.readFlag(line));
					// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error
					// parsing Layer: Unrecognized statement '"+line[i]+"'.");
				}
				MDLReader.mark(mdl);
			}
			return lay;
		} else {
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
		if (textureId != -1) {
			writer.println(tabs + "\tstatic TextureID " + textureId + ",");
		}
		for (int i = 0; i < anims.size(); i++) {
			final AnimFlag temp = anims.get(i);
			if (temp.getName().equals("TextureID")) {
				temp.printTo(writer, tabHeight + 1);
			}
		}
		if (hasTexAnim()) {
			writer.println(tabs + "\tTVertexAnimId " + TVertexAnimId + ",");
		}
		if (useCoords) {
			writer.println(tabs + "\tCoordId " + CoordId + ",");
		}
		boolean foundEmissive = false;
		for (int i = 0; i < anims.size(); i++) {
			final AnimFlag temp = anims.get(i);
			if (temp.getName().equals("Emissive")) {
				temp.printTo(writer, tabHeight + 1);
				foundEmissive = true;
			}
		}
		if (!Double.isNaN(emissiveGain) && !foundEmissive && ModelUtils.isEmissiveLayerSupported(version)) {
			writer.println(tabs + "\tstatic Emissive " + MDLReader.doubleToString(emissiveGain) + ",");
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
		writer.println(tabs + "}");
	}

	@Override
	public String getName() {
		if (texture != null) {
			return texture.getName() + " layer (mode " + filterMode + ") ";
		}
		return "multi-textured layer (mode " + filterMode + ") ";
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
					"Some visiblity animation data was lost unexpectedly during retrieval in " + getName() + ".");
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

	public int getTextureId() {
		return textureId;
	}

	public void setTextureId(final int textureId) {
		this.textureId = textureId;
	}

	public int getTVertexAnimId() {
		return TVertexAnimId;
	}

	public void setTVertexAnimId(final int tVertexAnimId) {
		TVertexAnimId = tVertexAnimId;
	}

	public Bitmap getTextureBitmap() {
		return texture;
	}

	public void setTexture(final Bitmap texture) {
		this.texture = texture;
	}

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
		return flags.contains("SphereEnvironmentMap");
	}

	@Override
	public boolean isNoDepthTest() {
		return flags.contains("NoDepthTest");
	}

	@Override
	public boolean isNoDepthSet() {
		return flags.contains("NoDepthSet");
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
}
