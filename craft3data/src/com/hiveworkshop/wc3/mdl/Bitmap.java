package com.hiveworkshop.wc3.mdl;

import com.etheller.warsmash.parsers.mdlx.MdlxTexture;

/**
 * A class to represent MDL texture references. (Not materials)
 *
 * Eric Theller 11/5/2011
 */
public class Bitmap {
	private String imagePath = "";
	private int replaceableId = 0;
	private int wrapStyle;// 0 = nothing, 1 = WrapWidth, 2 = WrapHeight, 3 =
							// both

	public String getPath() {
		return imagePath;
	}

	public String getName() {
		if (!imagePath.equals("")) {
			try {
				final String[] bits = imagePath.split("\\\\");
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

	public int getReplaceableId() {
		return replaceableId;
	}

	public void setReplaceableId(final int replaceableId) {
		this.replaceableId = replaceableId;
	}

	public Bitmap(final String imagePath, final int replaceableId) {
		this.imagePath = imagePath;
		this.replaceableId = replaceableId;
	}

	public Bitmap(final Bitmap other) {
		imagePath = other.imagePath;
		replaceableId = other.replaceableId;
		wrapStyle = other.wrapStyle;
	}

	public Bitmap(final MdlxTexture texture) {
		imagePath = texture.path;
		replaceableId = texture.replaceableId;
		setWrapStyle(texture.flags);
	}

	public MdlxTexture toMdlx() {
		MdlxTexture texture = new MdlxTexture();

		texture.path = imagePath;
		texture.replaceableId = replaceableId;
		texture.flags = wrapStyle;

		return texture;
	}
	
	public Bitmap(final String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((imagePath == null) ? 0 : imagePath.hashCode());
		result = (prime * result) + replaceableId;
		result = (prime * result) + wrapStyle;
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
		final Bitmap other = (Bitmap) obj;
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
		if (wrapStyle != other.wrapStyle) {
			return false;
		}
		return true;
	}

	public boolean isWrapHeight() {
		return (wrapStyle == 2) || (wrapStyle == 3);
	}

	public void setWrapHeight(final boolean flag) {
		if (flag) {
			if (wrapStyle == 1) {
				wrapStyle = 3;
			} else if (wrapStyle == 0) {
				wrapStyle = 2;
			}
		} else {
			if (wrapStyle == 3) {
				wrapStyle = 1;
			} else if (wrapStyle == 2) {
				wrapStyle = 0;
			}
		}
	}

	public boolean isWrapWidth() {
		return (wrapStyle == 1) || (wrapStyle == 3);
	}

	public void setWrapWidth(final boolean flag) {
		if (flag) {
			if (wrapStyle == 2) {
				wrapStyle = 3;
			} else if (wrapStyle == 0) {
				wrapStyle = 1;
			}
		} else {
			if (wrapStyle == 3) {
				wrapStyle = 2;
			} else if (wrapStyle == 1) {
				wrapStyle = 0;
			}
		}
	}

	public int getWrapStyle() {
		return wrapStyle;
	}

	public void setWrapStyle(final int wrapStyle) {
		this.wrapStyle = wrapStyle;
	}

	public void setPath(final String imagePath) {
		this.imagePath = imagePath;
	}
}
