package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas5 {

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Rigborn Rotator");

		for (int ang = 0; ang < 36; ang++) {
			final InputStream footman = MpqCodebase.get()
					.getResourceAsStream("Units\\Human\\TheCaptain\\TheCaptain.mdx");
			try {
				final EditableModel model = new EditableModel(MdxUtils.loadMdlx(footman));

				final Helper rootRotation = new Helper("Bone_Rotation");
				rootRotation.setPivotPoint(new Vertex(0, 0, 0));
				final AnimFlag rotationAnimation = new AnimFlag("Rotation");
				rotationAnimation.setInterpType(InterpolationType.LINEAR);
				for (final Animation anim : model.getAnims()) {
					rotationAnimation.addKeyframe(anim.getIntervalStart(),
							new QuaternionRotation(new Vertex(0, 0, 1), ang * ((2 * Math.PI) / 36)));
				}

				for (final IdObject node : model.getIdObjects()) {
					if (node.getParent() == null) {
						node.setParent(rootRotation);
					}
				}
				rootRotation.add(rotationAnimation);

				model.add(rootRotation);

				MdxUtils.saveMdx(model, new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\CaptainOutput" + ang + ".mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

}
