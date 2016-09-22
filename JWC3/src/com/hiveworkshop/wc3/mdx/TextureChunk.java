package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Bitmap;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class TextureChunk {
	public Texture[] texture = new Texture[0];

	public static final String key = "TEXS";

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "TEXS");
		int chunkSize = in.readInt();
		List<Texture> textureList = new ArrayList();
		int textureCounter = chunkSize;
		while (textureCounter > 0) {
			Texture temptexture = new Texture();
			textureList.add(temptexture);
			temptexture.load(in);
			textureCounter -= temptexture.getSize();
		}
		texture = textureList.toArray(new Texture[textureList.size()]);
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		int nrOfTextures = texture.length;
		out.writeNByteString("TEXS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < texture.length; i++) {
			texture[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < texture.length; i++) {
			a += texture[i].getSize();
		}

		return a;
	}

	public class Texture {
		public int replaceableId;
		public String fileName = "";
		public int unknownNull;
		public int flags;

		public void load(BlizzardDataInputStream in) throws IOException {
			replaceableId = in.readInt();
			fileName = in.readCharsAsString(256);
			unknownNull = in.readInt();
			flags = in.readInt();
		}

		public void save(BlizzardDataOutputStream out) throws IOException {
			out.writeInt(replaceableId);
			out.writeNByteString(fileName, 256);
			out.writeInt(unknownNull);
			out.writeInt(flags);

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 256;
			a += 4;
			a += 4;

			return a;
		}
		
		public Texture() {
			
		}
		public Texture(Bitmap mdlTex) {
			fileName = mdlTex.getPath();
			replaceableId = mdlTex.getReplaceableId();
			if( replaceableId == -1 )
				replaceableId = 0;
			flags = mdlTex.getWrapStyle();
		}
	}
}
