package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.filesystem.sources.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

public class DataSourceTracker {
	private final List<DataSourceDescriptor> dataSources;

	public DataSourceTracker(Collection<DataSourceDescriptor> dataSources) {
		this.dataSources = new ArrayList<>(dataSources);
	}
	public DataSourceTracker() {
		this.dataSources = new ArrayList<>();
	}

	public boolean contains(DataSourceDescriptor descriptor) {
		return dataSources.contains(descriptor);
	}

	public boolean add(DataSourceDescriptor descriptor) {
		return dataSources.add(descriptor);
	}
	public boolean addAll(Collection<DataSourceDescriptor> dataSources) {
		return this.dataSources.addAll(dataSources);
	}

	public boolean remove(DataSourceDescriptor descriptor) {
		return dataSources.remove(descriptor);
	}

	public boolean removeFirst() {
		DataSourceDescriptor descriptor = dataSources.stream().findFirst().orElse(null);
		return remove(descriptor);
	}

	public boolean remove(int i) {
		DataSourceDescriptor descriptor = dataSources.stream().skip(i).findFirst().orElse(null);
		return remove(descriptor);
	}

	public List<DataSourceDescriptor> getDataSourceDescriptors() {
		return dataSources;
	}

	public DataSourceTracker clear() {
		dataSources.clear();
		return this;
	}

	public int size() {
		return dataSources.size();
	}

	public DataSourceTracker sort() {
		TreeSet<DataSourceDescriptor> sorted = new TreeSet<>(dataSources);
		dataSources.clear();
		dataSources.addAll(sorted);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		for (DataSourceDescriptor desc : dataSources) {
			sb.append("\t")
					.append(DataSourceType.getNameFrom(desc)).append(": ")
					.append(desc).append(",\n");
		}
		sb.append("]");
		return sb.toString();
	}

	public DataSourceTracker fromString(String string) {
		System.out.println("DST - Full String:\n" + string);
		System.out.println("\nDST - parsedBits:\n");


		String match = "("
				+  "(" + DataSourceType.CASC.name()     + ")"
				+ "|(" + DataSourceType.MPQ.name()      + ")"
				+ "|(" + DataSourceType.FOLDER.name()   + ")"
				+ "|(" + DataSourceType.JAR.name()      + ")"
				+ "|(" + DataSourceType.MULTI.name()    + ")"
				+ ")";

		String splitString = "(,?\\[?\\n?\\t("
				+ "(?=" + match + ": )"
				+ "))|(,?\\n])";
		String splitString2 = "((?<=" + match + ")): (?=.+;\\s$)";

		String[] split = string.split(splitString);
		int i = 0;
		for (String s : split) {
			if(!s.isBlank()) {
				String[] type_content = s.split(splitString2);
//				System.out.println(i++ + ": {" + s + "}" + type_content[0]);
				String type = type_content[0];
				String content = type_content[1];
//				System.out.println(i++ + ": {" + type + "}" + "{" + content + "}");

				if(type.matches(match)) {
					String[] folder_prefixes = content.split("prefixes = ");
					String folder = folder_prefixes[0].replaceAll("^\\w+Path = \"", "").replaceAll("\";\\s*$", "");
					String prefixes = 1 < folder_prefixes.length ? folder_prefixes[1].replaceAll(";\\s*$", "") : "";
//					System.out.println(i++ + ": {" + type + "}" + "{" + folder + "}" + "{" + prefixes + "}");
					DataSourceDescriptor desc = switch (DataSourceType.valueOf(type)) {
						case CASC -> ((CascDataSourceDescriptor) DataSourceType.CASC.get(folder)).parsePrefixes(prefixes);
						case MPQ -> DataSourceType.MPQ.get(folder);
						case FOLDER -> DataSourceType.FOLDER.get(folder);
						case JAR -> DataSourceType.JAR.get(folder);
						case MULTI -> DataSourceType.MULTI.get(folder);
						case NONE -> null;
					};
					if (desc != null) {
						add(desc);
					}
				}
			}
		}
		System.out.println("datasource size: " + size());
		System.out.println("splitString2: \"" + splitString2 + "\"");
		System.out.println("DST end \n");
		return this;
	}

	private enum DataSourceType {
		CASC(s -> new CascDataSourceDescriptor(s)),
		MPQ(s -> new MpqDataSourceDescriptor(s)),
		FOLDER(s -> new FolderDataSourceDescriptor(s)),
		JAR(s -> new JavaJarDataSourceDescriptor()),
		MULTI(s -> new CompoundDataSourceDescriptor(new ArrayList<>())),
		NONE(s -> null);
		final Function<String, DataSourceDescriptor> function;
		DataSourceType(Function<String, DataSourceDescriptor> function) {
			this.function = function;
		}
		public DataSourceDescriptor get(String s) {
			return function.apply(s);
		}

		public static DataSourceType getFrom(DataSourceDescriptor descriptor) {
			if (descriptor instanceof CascDataSourceDescriptor) {
				return CASC;
			} else if (descriptor instanceof MpqDataSourceDescriptor) {
				return MPQ;
			} else if (descriptor instanceof FolderDataSourceDescriptor) {
				return FOLDER;
			} else if (descriptor instanceof JavaJarDataSourceDescriptor) {
				return JAR;
			} else if (descriptor instanceof CompoundDataSourceDescriptor) {
				return MULTI;
			}
			return NONE;
		}

		public static String getNameFrom(DataSourceDescriptor descriptor) {
			return getFrom(descriptor).name();
		}
	}



	public boolean isHd() {
		return isHd(dataSources);
	}
	private boolean isHd(final Iterable<DataSourceDescriptor> dataSources) {
		for (final DataSourceDescriptor desc : dataSources) {
			if (desc instanceof FolderDataSourceDescriptor fDesc && fDesc.getPath().contains("_hd.w3mod")
					|| desc instanceof MpqDataSourceDescriptor mDesc && mDesc.getPath().contains("_hd")
					|| desc instanceof CompoundDataSourceDescriptor compDesc && isHd(compDesc.getDataSourceDescriptors())) {
				return true;
			} else if (desc instanceof CascDataSourceDescriptor cDesc) {
				for (final String prefix : cDesc.getPrefixes()) {
					if (prefix.contains("_hd.w3mod")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
