package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.mdx.TextureAnimationChunk;

/**
 * TextureAnims, inside them called TVertexAnims
 *
 * Eric Theller 3/9/2012
 */
public class TextureAnim {
	ArrayList<AnimFlag> animFlags = new ArrayList();// The flags of animation

	/**
	 * Constructor for objects of class TextureAnim
	 */
	public TextureAnim(final AnimFlag flag) {
		animFlags.add(flag);
	}

	public TextureAnim(final ArrayList<AnimFlag> flags) {
		animFlags = flags;
	}

	public TextureAnim(final TextureAnim other) {
		for (final AnimFlag af : other.animFlags) {
			animFlags.add(new AnimFlag(af));
		}
	}

	public TextureAnim(final TextureAnimationChunk.TextureAnimation txa) {
		if (txa.textureRotation != null) {
			final AnimFlag flag = new AnimFlag(txa.textureRotation);
			add(flag);
		}
		if (txa.textureScaling != null) {
			final AnimFlag flag = new AnimFlag(txa.textureScaling);
			add(flag);
		}
		if (txa.textureTranslation != null) {
			final AnimFlag flag = new AnimFlag(txa.textureTranslation);
			add(flag);
		}
	}

	private TextureAnim() {

	}

	public static TextureAnim read(final BufferedReader mdl) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("TVertexAnim")) {
			final TextureAnim tan = new TextureAnim();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				MDLReader.reset(mdl);
				tan.animFlags.add(AnimFlag.read(mdl));
				MDLReader.mark(mdl);
			}
			return tan;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse TextureAnim: Missing or unrecognized open statement.");
		}
		return null;
	}

	public static ArrayList<TextureAnim> readAll(final BufferedReader mdl) {
		String line = "";
		final ArrayList<TextureAnim> outputs = new ArrayList<TextureAnim>();
		MDLReader.mark(mdl);
		if ((line = MDLReader.nextLine(mdl)).contains("TextureAnims")) {
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
				MDLReader.reset(mdl);
				outputs.add(read(mdl));
				MDLReader.mark(mdl);
			}
			return outputs;
		} else {
			MDLReader.reset(mdl);
//             JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Unable to parse TextureAnims: Missing or unrecognized open statement.");
		}
		return outputs;
	}

	public void printTo(final PrintWriter writer, final int tabHeight) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		writer.println(tabs + "TVertexAnim {");
		for (int i = 0; i < animFlags.size(); i++) {
			final AnimFlag temp = animFlags.get(i);
			temp.printTo(writer, tabHeight + 1);
		}
		writer.println(tabs + "}");
	}

	public void add(final AnimFlag af) {
		animFlags.add(af);
	}

	public AnimFlag get(final int i) {
		return animFlags.get(i);
	}

	public ArrayList<AnimFlag> getAnimFlags() {
		return animFlags;
	}

	public void setAnimFlags(final ArrayList<AnimFlag> animFlags) {
		this.animFlags = animFlags;
	}
}
