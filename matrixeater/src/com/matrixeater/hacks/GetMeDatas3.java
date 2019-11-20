package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas3 {

	public static void main(final String[] args) {
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
//		for (final String file : mergedListfile) {
//			System.out.println(file);
//		}
//		final InputStream slk = MpqCodebase.get().getResourceAsStream("Units\\UnitData.slk");
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(slk))) {
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
//			}
//		} catch (final IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		final BufferedImage gameTex = BLPHandler.get()
//				.getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp");
//		if (gameTex == null) {
//			throw new IllegalStateException("game tex is null");
//		}
//		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(gameTex)));

		final InputStream footman = MpqCodebase.get()
				.getResourceAsStream("D:\\NEEDS_ORGANIZING\\Scratch\\Gimli_by_Jhotam\\Gimli_LOTR_ByJhotam_900.mdx");
		try {
//			final MdxModel footmanMdx = MdxUtils.loadModel(new BlizzardDataInputStream(footman));
//			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(
//					new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareGruntX.mdx"))) {
//				footmanMdx.save(out);
//			}

			final MDL model = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));
			for (final Geoset geo : model.getGeosets()) {
				final ArrayList<GeosetVertex> vertices = geo.getVertices();
				for (final GeosetVertex gv : vertices) {
					final Normal normal = gv.getNormal();
					if (normal != null) {
						gv.initV900();
						final float[] tangent = gv.getTangent();
						for (int i = 0; i < 3; i++) {
							tangent[i] = (float) normal.getCoord((byte) i);
						}
						tangent[3] = 1;
					}
					final int bones = gv.getBoneAttachments().size();
					for (int i = 0; (i < bones) && (i < 4); i++) {
						gv.getSkinBones()[i] = gv.getBoneAttachments().get(i);
						gv.getSkinBoneWeights()[i] = (short) (255 / bones);
					}
				}
			}
//			for (final Material m : model.getMaterials()) {
//				m.setShaderString("Shader_HD_DefaultUnit");
//				if (m.getLayers().size() > 1) {
//					m.getLayers().add(m.getLayers().remove(0));
//				}
//				final Bitmap texture = new Bitmap("DeityCube.dds");
//				texture.setWrapHeight(true);
//				texture.setWrapWidth(true);
//				m.getLayers().add(1, new Layer("None", texture));
//				m.getLayers().add(2, new Layer("None", texture));
//				m.getLayers().add(3, new Layer("None", new Bitmap("Textures\\Black32.dds")));
//				m.getLayers().add(4, new Layer("None", new Bitmap("Textures\\Black32.dds")));
//				m.getLayers().add(5, new Layer("None", new Bitmap("ReplaceableTextures\\EnvironmentMap.dds")));
//				for (final Layer l : m.getLayers()) {
//					l.setEmissive(1.0);
//				}
//			}
			model.printTo(new File("D:\\NEEDS_ORGANIZING\\Scratch\\Gimli_by_Jhotam\\Gimli_LOTR_ByJhotam_900_bxd.mdx"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
