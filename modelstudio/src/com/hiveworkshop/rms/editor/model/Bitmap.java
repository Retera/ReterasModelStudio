package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;

import java.util.EnumSet;

/**
 * A class to represent MDL texture references. (Not materials)
 *
 * Eric Theller 11/5/2011
 */
public class Bitmap implements Named {
	private String imagePath = "";
	private int replaceableId = 0;
	private final EnumSet<WrapFlag> wrapFlags = EnumSet.noneOf(WrapFlag.class);

	public String getPath() {
		return imagePath;
	}

	public Bitmap(String imagePath, int replaceableId) {
		this.imagePath = imagePath;
		this.replaceableId = replaceableId;
		if(imagePath == null){
			System.err.println("Bitmap Path is null!");
		}
	}

	public Bitmap(String imagePath) {
		this(imagePath, 0);
	}

	public Bitmap(Bitmap other) {
		imagePath = other.imagePath;
		replaceableId = other.replaceableId;
		wrapFlags.addAll(other.wrapFlags);
	}

	public Bitmap() {
		this("", 0);
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public String getName() {
		if (!imagePath.equals("")) {
			try {
				String[] bits = imagePath.split("[\\\\/]");
				return bits[bits.length - 1].split("\\.")[0];
			} catch (final Exception e) {
				return "bad blp path";
			}
		} else {
			if (replaceableId == 1) {
				return "Team Color";
			} else if (replaceableId == 2) {
				return "Team Glow";
			} else {
				return "Replaceable" + replaceableId;
			}
		}
	}
	@Override
	public void setName(String text) {
	}

	public Bitmap setReplaceableId(int replaceableId) {
		this.replaceableId = replaceableId;
		return this;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + ((imagePath == null) ? 0 : imagePath.hashCode());
		result = (prime * result) + replaceableId;
		result = (prime * result) + wrapFlags.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Bitmap other = (Bitmap) obj;
		if (imagePath == null) {
			if (other.imagePath != null) {
				return false;
			}
		} else if (!imagePath.equals(other.imagePath)) {
			return false;
		}
		if (replaceableId != other.replaceableId) {
			return false;
		}
		return wrapFlags.equals(other.wrapFlags);
	}

	public boolean isWrapHeight() {
		return wrapFlags.contains(WrapFlag.HEIGHT);
	}

	public boolean isWrapWidth() {
		return wrapFlags.contains(WrapFlag.WIDTH);
	}

	public Bitmap setWrapHeight(boolean wrap) {
		return setFlag(WrapFlag.HEIGHT, wrap);
	}

	public Bitmap setWrapWidth(boolean wrap) {
		return setFlag(WrapFlag.WIDTH, wrap);
	}

	public boolean isFlagSet(WrapFlag flag){
		return wrapFlags.contains(flag);
	}
	public Bitmap setFlag(WrapFlag flag, boolean set){
		if(set){
			wrapFlags.add(flag);
		} else {
			wrapFlags.remove(flag);
		}
		return this;
	}
	public Bitmap toggleFlag(WrapFlag flag){
		return setFlag(flag, !isFlagSet(flag));
	}

	public Bitmap setPath(String imagePath) {
		this.imagePath = imagePath;
		return this;
	}
	public EnumSet<WrapFlag> getWrapFlags() {
		return wrapFlags;
	}

	public String getRenderableTexturePath() {
		if (imagePath.length() == 0) {
			String tcString = ("" + (100 + ProgramGlobals.getPrefs().getTeamColor())).substring(1);
			return switch (replaceableId) {
				case 0 -> "";
				case 1 -> "ReplaceableTextures\\TeamColor\\TeamColor" + tcString + ".blp";
				case 2 -> "ReplaceableTextures\\TeamGlow\\TeamGlow" + tcString + ".blp";
				case 11 -> "ReplaceableTextures\\Cliff\\Cliff0" + ".blp";
				// "ReplaceableTextures\Cliff\Cliff0.tga",
				// "ReplaceableTextures\Cliff\Cliff1.tga"
				case 31 -> "ReplaceableTextures\\LordaeronTree\\LordaeronSummerTree" + ".blp";
				// "ReplaceableTextures\LordaeronTree\LordaeronSummerTree",
				// "ReplaceableTextures\LordaeronTree\LordaeronFallTree",
				// "ReplaceableTextures\LordaeronTree\LordaeronWinterTree",
				// "ReplaceableTextures\LordaeronTree\LordaeronSnowTree",
				// "ReplaceableTextures\LordaeronTree\LordaeronFallTree"
				// "ReplaceableTextures\DalaranRuinsTree\DalaranRuinsTree"
				case 32 -> "ReplaceableTextures\\AshenvaleTree\\AshenTree" + ".blp";
				// "ReplaceableTextures\AshenvaleTree\FelwoodTree",
				// "ReplaceableTextures\AshenvaleTree\Ice_Tree",
				// "ReplaceableTextures\AshenvaleTree\AshenCanopyTree",
				// "ReplaceableTextures\BarrensTree\BarrensTree",
				// "ReplaceableTextures\NorthrendTree\NorthTree",
				// "ReplaceableTextures\RuinsTree\RuinsTree"
				case 33 -> "ReplaceableTextures\\BarrensTree\\BarrensTree" + ".blp";
				case 34 -> "ReplaceableTextures\\NorthrendTree\\NorthTree" + ".blp";
				case 35 -> "ReplaceableTextures\\Mushroom\\MushroomTree" + ".blp"; //tga?,
				// "ReplaceableTextures\Mushroom\MushroomTree.tga"
				// "ReplaceableTextures\UndergroundTree\UnderMushroomTree"
				case 36 -> "ReplaceableTextures\\RuinsTree\\RuinsTree" + ".blp";
				case 37 -> "ReplaceableTextures\\OutlandMushroomTree\\MushroomTree" + ".blp";
				default -> "replaceabletextures\\lordaerontree\\lordaeronsummertree" + ".blp";
			};
		}
		return imagePath;
	}

	public enum WrapFlag {
		WIDTH(MdlUtils.TOKEN_WRAP_WIDTH, 0x1),
		HEIGHT(MdlUtils.TOKEN_WRAP_HEIGHT, 0x2);
		final String name;
		final int flagBit;
		WrapFlag(String name, int flagBit){
			this.name = name;
			this.flagBit = flagBit;
		}

		public String getName() {
			return name;
		}

		public int getFlagBit() {
			return flagBit;
		}

		public static EnumSet<WrapFlag> fromBits(int bits){
			EnumSet<WrapFlag> flagSet = EnumSet.noneOf(WrapFlag.class);
			for (WrapFlag f : WrapFlag.values()){
				if ((f.flagBit & bits) == f.flagBit){
					flagSet.add(f);
				}
			}
			return flagSet;
		}
	}
}
