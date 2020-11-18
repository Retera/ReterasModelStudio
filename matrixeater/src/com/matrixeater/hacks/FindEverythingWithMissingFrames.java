package com.matrixeater.hacks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Named;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class FindEverythingWithMissingFrames {
	public static void main(final String[] args) {
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		final Collection<String> listfile = mpqCodebase.getListfile();
		final int n = listfile.size();
		int m = 0;
		try (final PrintWriter writer = new PrintWriter("C:/Temp/MissingFrames.log")) {
			for (final String path : listfile) {
				if (path.toLowerCase().endsWith(".mdx")) {
					try (BlizzardDataInputStream stream = new BlizzardDataInputStream(
							mpqCodebase.getResourceAsStream(path))) {
						final EditableModel model = new EditableModel(MdxUtils.loadModel(stream));
						final List<AnimFlag> allFlags = model.getAllAnimFlags();
						for (final AnimFlag flag : allFlags) {
							if (flag.getGlobalSeq() != null) {
								boolean hasStart = false;
								boolean hasEnd = false;
								boolean hasSequence = false;
								int inSequence = 0;
								for (int i = 0; i < flag.size(); i++) {
									final Integer time = flag.getTimes().get(i);
									if ((time >= 0) && (time <= flag.getGlobalSeq())) {
										hasSequence = true;
										inSequence++;
										if (time == 0) {
											hasStart = true;
										}
										if (time == flag.getGlobalSeq().intValue()) {
											hasEnd = true;
										}
									}
								}
								if (hasSequence) {
									if (!hasStart) {
										writer.println(path + ":" + getSource(model, flag) + ":GlobalSeqId"
												+ flag.getGlobalSeqId() + ":Missing start at " + 0 + " for track "
												+ flag.getName());
									}
									if (!hasEnd) {
										if (!hasStart || (inSequence > 1)) {
											writer.println(path + ":" + getSource(model, flag) + ":GlobalSeqId"
													+ flag.getGlobalSeqId() + ":Missing end at " + flag.getGlobalSeq()
													+ " for track " + flag.getName());
										}
									}
								}
							} else {
								for (final Animation sequence : model.getAnims()) {
									final int start = sequence.getStart();
									final int end = sequence.getEnd();
									boolean hasStart = false;
									boolean hasEnd = false;
									boolean hasSequence = false;
									int inSequence = 0;
									for (int i = 0; i < flag.size(); i++) {
										final Integer time = flag.getTimes().get(i);
										if ((time >= start) && (time <= end)) {
											hasSequence = true;
											inSequence++;
											if (time == start) {
												hasStart = true;
											}
											if (time == end) {
												hasEnd = true;
											}
										}
									}
									if (hasSequence) {
										if (!hasStart) {
											writer.println(path + ":" + getSource(model, flag) + ":"
													+ sequence.getName() + ":Missing start at " + start + " for track "
													+ flag.getName());
										}
										if (!hasEnd) {
											if (!hasStart || (inSequence > 1)) {
												writer.println(path + ":" + getSource(model, flag) + ":"
														+ sequence.getName() + ":Missing end at " + end + " for track "
														+ flag.getName());
											}
										}
									}
								}
							}
						}
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				m++;
				if ((m % 100) == 0) {
					System.out.println(m + " / " + n);
				}
			}
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.println("done");

	}

	private static Object getSource(final EditableModel model, final AnimFlag flag) {
		final Object animFlagSource = model.getAnimFlagSource(flag);
		if (animFlagSource instanceof Named) {
			return ((Named) animFlagSource).getName();
		}
		return animFlagSource;
	}
}
