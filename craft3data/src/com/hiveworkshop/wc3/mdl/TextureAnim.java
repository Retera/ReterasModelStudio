package com.hiveworkshop.wc3.mdl;

import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxTextureAnimation;

/**
 * TextureAnims, inside them called TVertexAnims
 *
 * Eric Theller 3/9/2012
 */
public class TextureAnim extends TimelineContainer {
	/**
	 * Constructor for objects of class TextureAnim
	 */
	public TextureAnim(final AnimFlag flag) {
		add(flag);
	}

	public TextureAnim(final List<AnimFlag> flags) {
		setAnimFlags(flags);
	}

	public TextureAnim(final TextureAnim other) {
		setAnimFlags(other.getAnimFlags());
	}

	public TextureAnim(final MdlxTextureAnimation animation) {
		loadTimelines(animation);
	}

	public MdlxTextureAnimation toMdlx() {
		MdlxTextureAnimation animation = new MdlxTextureAnimation();

		timelinesToMdlx(animation);

		return animation;
	}
}
