package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.hiveworkshop.wc3.mdx.BindPoseChunk;

/**
 * Read-only view of the bind pose matrices; they are regenerated on save.
 */
public class ComponentBindPoseCard extends ComponentCard<BindPoseChunk> {
	private final JTextArea matrices = new JTextArea();

	public ComponentBindPoseCard() {
		addInfo("Matrices", chunk -> chunk.bindPose == null ? "0" : Integer.toString(chunk.bindPose.length));
		matrices.setEditable(false);
		matrices.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
		addWide(new JScrollPane(matrices), "growx, growy, hmin 200");
	}

	@Override
	protected void onReload() {
		final StringBuilder sb = new StringBuilder();
		if (item.bindPose != null) {
			for (int i = 0; i < item.bindPose.length; i++) {
				sb.append(i).append(": ");
				final float[] m = item.bindPose[i];
				if (m == null) {
					sb.append("null");
				} else {
					for (int j = 0; j < m.length; j++) {
						if (j > 0) {
							sb.append(j % 3 == 0 ? " | " : ", ");
						}
						sb.append(String.format("%.3f", m[j]));
					}
				}
				sb.append('\n');
			}
		}
		matrices.setText(sb.toString());
		matrices.setCaretPosition(0);
	}
}
