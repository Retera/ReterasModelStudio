package com.hiveworkshop.wc3.mdlx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.etheller.warsmash.parsers.mdlx.MdlxModel;

import com.hiveworkshop.wc3.mdl.EditableModel;

import com.hiveworkshop.wc3.mdx.MdxUtils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
//		System.out.println(Integer.toBinaryString(0x1));
//		System.out.println(Integer.toBinaryString(0x2));
//		System.out.println(Integer.toBinaryString(0x4));
//		System.out.println(Integer.toBinaryString(0x8));
//		System.out.println(Integer.toBinaryString(0x10));
//		System.out.println(Integer.toBinaryString(0x20));
//		System.out.println(Integer.toBinaryString(0x40));
//		System.out.println(Integer.toBinaryString(0x80));
		try (InputStream bdis = new FileInputStream("PackHorse.mdx")) {
			final MdlxModel packHorse = MdxUtils.loadMdlx(bdis);
//			for( MaterialChunk.Material mat: packHorse.materialChunk.material ) {
//				mat.layerChunk.layer[0].shadingFlags |= 0x4;
//				mat.layerChunk.layer[0].shadingFlags |= 0x8;
//				mat.layerChunk.layer[0].shadingFlags |= 0x10;
//			}
			final OutputStream out = new FileOutputStream(new File("PackHorse_modified.mdx"));
			MdxUtils.saveMdx(packHorse, out);
			out.close();
			final MdlxModel packHorse2 = MdxUtils.loadMdlx(new FileInputStream("PackHorse_modified.mdx"));
			final EditableModel model = new EditableModel(packHorse);
			model.setName("PackHorse_MDLified");
			final MdlxModel packHorseMdx = model.toMdlx();
			final OutputStream out2 = new FileOutputStream(new File("PackHorse_MXfied.mdx"));
			MdxUtils.saveMdx(packHorseMdx, out2);
			out2.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
