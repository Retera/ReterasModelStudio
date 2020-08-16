package com.matrixeater.hacks;

import java.nio.ByteBuffer;

public class PrintOutBinary {

	public static void main(final String[] args) {
//		System.out.println(Integer.toBinaryString(0x1000));
		final ByteBuffer bf = ByteBuffer.allocate(1000);
		bf.put((byte) 0);
		bf.put((byte) 0);
		bf.put((byte) 0);
		bf.put((byte) 0);
		bf.clear();
		final float float1 = bf.getFloat();
		System.out.println(float1);
//		try (BlizzardDataInputStream stream = new BlizzardDataInputStream(
//				MpqCodebase.get().getResourceAsStream("units\\human\\heropaladin\\heropaladin.mdx"))) {
//			final MdxModel model = MdxUtils.loadModel(stream);
//
//			for (final TextureChunk.Texture texture : model.textureChunk.texture) {
//				System.out.println(texture.fileName);
//			}
//		} catch (final IOException e) {
//			e.printStackTrace();
//		}
	}

}
