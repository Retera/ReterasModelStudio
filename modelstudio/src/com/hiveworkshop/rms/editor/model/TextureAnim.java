package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxTextureAnimation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TextureAnims, inside them called TVertexAnims
 *
 * Eric Theller 3/9/2012
 */
public class TextureAnim extends TimelineContainer {
	/**
	 * Constructor for objects of class TextureAnim
	 */
	public TextureAnim(final AnimFlag<?> flag) {
		add(flag);
	}

	public TextureAnim(final List<AnimFlag<?>> flags) {
		setAnimFlags(flags);
	}

	public TextureAnim(final TextureAnim other) {
		Collection<AnimFlag<?>> flags = new ArrayList<>();
		for (AnimFlag<?> animFlag : other.getAnimFlags()) {
			flags.add(new IntAnimFlag((IntAnimFlag) animFlag));
		}
		setAnimFlags(flags);
	}

	public TextureAnim(final MdlxTextureAnimation animation) {
		loadTimelines(animation);
	}

	public MdlxTextureAnimation toMdlx() {
		final MdlxTextureAnimation animation = new MdlxTextureAnimation();

		timelinesToMdlx(animation);

		return animation;
	}

	public String getFlagNames() {
		Map<String, AnimFlag<?>> flags = this.animFlags;
		//TODO figure out what this should return
		System.out.println(flags.keySet());
		return flags.keySet().toString();
	}
}
