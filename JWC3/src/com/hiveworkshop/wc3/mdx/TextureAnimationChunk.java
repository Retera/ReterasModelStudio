package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class TextureAnimationChunk {
	public TextureAnimation[] textureAnimation = new TextureAnimation[0];

	public static final String key = "TXAN";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "TXAN");
		int chunkSize = in.readInt();
		List<TextureAnimation> textureAnimationList = new ArrayList();
		int textureAnimationCounter = chunkSize;
		while (textureAnimationCounter > 0) {
			TextureAnimation temptextureAnimation = new TextureAnimation();
			textureAnimationList.add(temptextureAnimation);
			temptextureAnimation.load(in);
			textureAnimationCounter -= temptextureAnimation.getSize();
		}
		textureAnimation = textureAnimationList
				.toArray(new TextureAnimation[textureAnimationList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTextureAnimations = textureAnimation.length;
		out.writeNByteString("TXAN", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < textureAnimation.length; i++) {
			textureAnimation[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < textureAnimation.length; i++) {
			a += textureAnimation[i].getSize();
		}

		return a;
	}

	public class TextureAnimation {
		public TextureTranslation textureTranslation;
		public TextureRotation textureRotation;
		public TextureScaling textureScaling;

		public void load(BlizzardDataInputStream in) throws IOException {
			int inclusiveSize = in.readInt();
			for (int i = 0; i < 3; i++) {
				if (MdxUtils.checkOptionalId(in, TextureTranslation.key)) {
					textureTranslation = new TextureTranslation();
					textureTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, TextureRotation.key)) {
					textureRotation = new TextureRotation();
					textureRotation.load(in);
				} else if (MdxUtils.checkOptionalId(in, TextureScaling.key)) {
					textureScaling = new TextureScaling();
					textureScaling.load(in);
				}

			}
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			if (textureTranslation != null) {
				textureTranslation.save(out);
			}
			if (textureRotation != null) {
				textureRotation.save(out);
			}
			if (textureScaling != null) {
				textureScaling.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			if (textureTranslation != null) {
				a += textureTranslation.getSize();
			}
			if (textureRotation != null) {
				a += textureRotation.getSize();
			}
			if (textureScaling != null) {
				a += textureScaling.getSize();
			}

			return a;
		}
		public TextureAnimation() {
			
		}
		public TextureAnimation(TextureAnim txa) {
			for( AnimFlag af: txa.getAnimFlags() ) {
				if( af.getName().equals("Translation") ) {
					textureTranslation = new TextureTranslation();
					textureTranslation.globalSequenceId = af.getGlobalSeqId();
					textureTranslation.interpolationType = af.getInterpType();
					textureTranslation.translationTrack = new TextureTranslation.TranslationTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						TextureTranslation.TranslationTrack mdxEntry = textureTranslation.new TranslationTrack();
						textureTranslation.translationTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.translation = ((Vertex)mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((Vertex)mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex)mdlEntry.outTan).toFloatArray();
						}
					}
				} else if( af.getName().equals("Scaling") ) {
					textureScaling = new TextureScaling();
					textureScaling.globalSequenceId = af.getGlobalSeqId();
					textureScaling.interpolationType = af.getInterpType();
					textureScaling.translationTrack = new TextureScaling.TranslationTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						TextureScaling.TranslationTrack mdxEntry = textureScaling.new TranslationTrack();
						textureScaling.translationTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.scaling = ((Vertex)mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((Vertex)mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex)mdlEntry.outTan).toFloatArray();
						}
					}
				} else if( af.getName().equals("Rotation") ) {
					textureRotation = new TextureRotation();
					textureRotation.globalSequenceId = af.getGlobalSeqId();
					textureRotation.interpolationType = af.getInterpType();
					textureRotation.translationTrack = new TextureRotation.TranslationTrack[af.size()];
					boolean hasTans = af.tans();
					for( int i = 0; i < af.size(); i++ ) {
						TextureRotation.TranslationTrack mdxEntry = textureRotation.new TranslationTrack();
						textureRotation.translationTrack[i] = mdxEntry;
						AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.rotation = ((QuaternionRotation)mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if( hasTans ) {
							mdxEntry.inTan = ((QuaternionRotation)mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((QuaternionRotation)mdlEntry.outTan).toFloatArray();
						}
					}
				}
			}
		}
	}
}
