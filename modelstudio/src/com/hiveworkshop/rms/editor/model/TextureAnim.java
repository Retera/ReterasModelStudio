package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
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
	public TextureAnim(final AnimFlag<?> flag) {
		add(flag);
	}

	public TextureAnim(final List<AnimFlag<?>> flags) {
		setAnimFlags(flags);
	}

	public TextureAnim(final TextureAnim other) {
		Collection<AnimFlag<?>> flags = new ArrayList<>();
		for (AnimFlag<?> animFlag : other.getAnimFlags()) {
			flags.add(animFlag.deepCopy());
		}
		setAnimFlags(flags);
	}

	public TextureAnim(final MdlxTextureAnimation animation, EditableModel model) {
		loadTimelines(animation, model);
	}

	public String getFlagNames() {
		Map<String, AnimFlag<?>> flags = this.animFlags;
		//TODO figure out what this should return
//		System.out.println("TextureAnim flags: " + flags.keySet());
		return flags.keySet().toString();
	}

	public TextureAnim deepCopy() {
		return new TextureAnim(this);
	}

	@Override
	public String toString() {
		return getFlagNames();
	}
}
