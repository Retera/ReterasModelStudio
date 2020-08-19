package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas4 {

	public static void main(final String[] args) {
		final Set<String> mergedListfile = MpqCodebase.get().getMergedListfile();
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

		final InputStream footman = MpqCodebase.get().getResourceAsStream("Units\\Orc\\Hellscream\\Hellscream.mdx");
		try {
//			final MdxModel footmanMdx = MdxUtils.loadModel(new BlizzardDataInputStream(footman));
//			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(
//					new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareGruntX.mdx"))) {
//				footmanMdx.save(out);
//			}

			final EditableModel model = new EditableModel(MdxUtils.loadMdlx(footman));
			MdxUtils.saveMdx(model, new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareHellscream.mdx"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
