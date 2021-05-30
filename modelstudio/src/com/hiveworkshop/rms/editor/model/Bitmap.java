package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTexture.WrapMode;

/**
 * A class to represent MDL texture references. (Not materials)
 *
 * Eric Theller 11/5/2011
 */
public class Bitmap {
	private String imagePath = "";
	private int replaceableId = 0;
	WrapMode wrapMode = WrapMode.REPEAT_BOTH;

	public String getPath() {
		return imagePath;
	}

	public Bitmap(String imagePath, int replaceableId) {
		this.imagePath = imagePath;
		this.replaceableId = replaceableId;
	}

	public int getReplaceableId() {
		return replaceableId;
	}

	public Bitmap(Bitmap other) {
		imagePath = other.imagePath;
		replaceableId = other.replaceableId;
		wrapMode = other.wrapMode;
	}

	public Bitmap() {

	}

	public Bitmap(MdlxTexture texture) {
		imagePath = texture.path;
		replaceableId = texture.replaceableId;
		wrapMode = texture.wrapMode;
	}

	public Bitmap(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getName() {
		if (!imagePath.equals("")) {
			try {
				String[] bits = imagePath.split("\\\\");
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

	public Bitmap setReplaceableId(int replaceableId) {
		this.replaceableId = replaceableId;
		return this;
	}

	public MdlxTexture toMdlx() {
		MdlxTexture texture = new MdlxTexture();

		texture.path = imagePath;
		texture.replaceableId = replaceableId;
		texture.wrapMode = wrapMode;

		return texture;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + ((imagePath == null) ? 0 : imagePath.hashCode());
		result = (prime * result) + replaceableId;
		result = (prime * result) + wrapMode.ordinal();
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
		return wrapMode == other.wrapMode;
	}

	public boolean isWrapHeight() {
		return (wrapMode == WrapMode.WRAP_HEIGHT) || (wrapMode == WrapMode.WRAP_BOTH);
	}

	public Bitmap setWrapHeight(boolean flag) {
		if (flag) {
			if (wrapMode == WrapMode.REPEAT_BOTH) {
				wrapMode = WrapMode.WRAP_HEIGHT;
			} else if (wrapMode == WrapMode.WRAP_WIDTH) {
				wrapMode = WrapMode.WRAP_BOTH;
			}
		} else {
			if (wrapMode == WrapMode.WRAP_BOTH) {
				wrapMode = WrapMode.WRAP_WIDTH;
			} else if (wrapMode == WrapMode.WRAP_HEIGHT) {
				wrapMode = WrapMode.REPEAT_BOTH;
			}
		}
		return this;
	}

	public boolean isWrapWidth() {
		return (wrapMode == WrapMode.WRAP_WIDTH) || (wrapMode == WrapMode.WRAP_BOTH);
	}

	public Bitmap setWrapWidth(boolean flag) {
		if (flag) {
			if (wrapMode == WrapMode.REPEAT_BOTH) {
				wrapMode = WrapMode.WRAP_WIDTH;
			} else if (wrapMode == WrapMode.WRAP_HEIGHT) {
				wrapMode = WrapMode.WRAP_BOTH;
			}
		} else {
			if (wrapMode == WrapMode.WRAP_BOTH) {
				wrapMode = WrapMode.WRAP_HEIGHT;
			} else if (wrapMode == WrapMode.WRAP_WIDTH) {
				wrapMode = WrapMode.REPEAT_BOTH;
			}
		}
		return this;
	}

	public WrapMode getWrapMode() {
		return wrapMode;
	}

	public Bitmap setWrapMode(WrapMode wrapMode) {
		this.wrapMode = wrapMode;
		return this;
	}

	public Bitmap setPath(String imagePath) {
		this.imagePath = imagePath;
		return this;
	}
}
