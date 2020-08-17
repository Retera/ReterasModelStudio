package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import com.etheller.warsmash.parsers.mdlx.MdlxLayer;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

import com.hiveworkshop.wc3.mdl.v2.LayerView;
import com.hiveworkshop.wc3.mdl.v2.timelines.Animatable;

import com.hiveworkshop.wc3.util.ModelUtils;

/**
 * Layers for MDLToolkit/MatrixEater.
 * <p>
 * Eric Theller 3/8/2012
 */
public class Layer extends TimelineContainer implements Named, VisibilitySource, LayerView {
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
	private double staticAlpha = 1;// Amount of static alpha (opacity)
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
		result = (prime * result) + ((animFlags == null) ? 0 : animFlags.hashCode());
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
		if (animFlags == null) {
			if (other.animFlags != null) {
				return false;
			}
		} else if (!animFlags.equals(other.animFlags)) {
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
		fresnelColor = new Vertex(other.fresnelColor);
		fresnelOpacity = other.fresnelOpacity;
		fresnelTeamColor = other.fresnelTeamColor;
		flags = new ArrayList<>(other.flags);
		animFlags = new ArrayList<>();
		textures = new ArrayList<>();
		for (final AnimFlag af : other.animFlags) {
			animFlags.add(new AnimFlag(af));
		}
		if (other.textures != null) {
			for (final Bitmap bmp : other.textures) {
				textures.add(new Bitmap(bmp));
			}
		} else {
			textures = null;
		}
	}

	public Layer(final MdlxLayer layer) {
		this(layer.filterMode.getMdlText(), layer.textureId);

		final int shadingFlags = layer.flags;
		// 0x1: unshaded
		// 0x2: sphere environment map
		// 0x4: ?
		// 0x8: ?
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

		setTVertexAnimId(layer.textureAnimationId);
		setCoordId((int)layer.coordId);
		setStaticAlpha(layer.alpha);

		// > 800
		emissiveGain = layer.emissiveGain;
		// > 900
		setFresnelColor(new Vertex(MdlxUtils.flipRGBtoBGR(layer.fresnelColor)));
		fresnelOpacity = layer.fresnelOpacity;
		fresnelTeamColor = layer.fresnelTeamColor;

		loadTimelines(layer);
	}

	public MdlxLayer toMdlx() {
		final MdlxLayer layer = new MdlxLayer();

		layer.filterMode = MdlxLayer.FilterMode.fromId(com.hiveworkshop.wc3.mdl.Layer.FilterMode.nameToId(getFilterModeString()));

		for (final String flag : getFlags()) {
			switch (flag) {
			case "Unshaded":
				layer.flags |= 0x1;
				break;
			case "SphereEnvironmentMap":
			case "SphereEnvMap":
				layer.flags |= 0x2;
				break;
			case "TwoSided":
				layer.flags |= 0x10;
				break;
			case "Unfogged":
				layer.flags |= 0x20;
				break;
			case "NoDepthTest":
				layer.flags |= 0x40;
				break;
			case "NoDepthSet":
				layer.flags |= 0x80;
				break;
			case "Unlit":
				layer.flags |= 0x100;
				break;
			}
		}

		layer.textureId = getTextureId();
		layer.textureAnimationId = getTVertexAnimId();
		layer.coordId = getCoordId();
		layer.alpha = (float)getStaticAlpha();

		// > 800
		layer.emissiveGain = layer.emissiveGain;
		// > 900
		layer.fresnelColor = MdlxUtils.flipRGBtoBGR(fresnelColor.toFloatArray());
		layer.fresnelOpacity = (float)fresnelOpacity;
		layer.fresnelTeamColor = (float)fresnelTeamColor;

		timelinesToMdlx(layer);

		return layer;
	}

	private Layer() {
		flags = new ArrayList<>();
		animFlags = new ArrayList<>();
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

	public Bitmap getRenderTexture(final AnimatedRenderEnvironment animatedRenderEnvironment, final EditableModel model) {
		final AnimFlag textureFlag = AnimFlag.find(animFlags, "TextureID");
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

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float)staticAlpha);
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

	public void buildTextureList(final EditableModel mdlr) {
		textures = new ArrayList<>();
		final AnimFlag txFlag = getFlag("TextureID");
		for (int i = 0; i < txFlag.values.size(); i++) {
			final int txId = ((Integer) txFlag.values.get(i)).intValue();
			final Bitmap texture2 = mdlr.getTexture(txId);
			textures.add(texture2);
			ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
		}
	}

	public void updateIds(final EditableModel mdlr) {
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

	public void updateRefs(final EditableModel mdlr) {
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
		for (final AnimFlag af : animFlags) {
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
}
