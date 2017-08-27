package com.hiveworkshop.wc3.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.etheller.collections.HashSet;
import com.etheller.collections.Set;
import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.user.SaveProfile;

import mpq.ArchivedFile;
import mpq.ArchivedFileExtractor;
import mpq.ArchivedFileStream;
import mpq.HashLookup;
import mpq.MPQArchive;
import mpq.MPQException;

public class MpqCodebase implements Codebase {
	private final boolean isDebugMode = true;
	MpqGuy war3;
	MpqGuy war3x;
	MpqGuy war3xlocal;
	MpqGuy war3patch;
	MpqGuy hfmd;
	ArrayList<MpqGuy> mpqList = new ArrayList<>();
	ArchivedFileExtractor extractor = new ArchivedFileExtractor();

	private static final class MpqGuy {
		private final MPQArchive archive;
		private final SeekableByteChannel inputChannel;

		public MpqGuy(final MPQArchive archive, final SeekableByteChannel inputChannel) {
			this.archive = archive;
			this.inputChannel = inputChannel;
		}

		public MPQArchive getArchive() {
			return archive;
		}

		public SeekableByteChannel getInputChannel() {
			return inputChannel;
		}

		public boolean has(final String file) {
			try {
				archive.lookupPath(file);
				return true;
			} catch (final MPQException exc) {
				if (exc.getMessage().equals("lookup not found")) {
					return false;
				} else {
					throw new RuntimeException(exc);
				}
			}
		}
	}

	public MpqCodebase() {
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
		// if (isDebugMode) {
		// hfmd = loadMPQ("hfmd.exe");
		// }
	}

	Map<String, File> cache = new HashMap<>();

	@Override
	public File getFile(final String filepath) {
		if (cache.containsKey(filepath)) {
			return cache.get(filepath);
		}
		try {
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final MpqGuy mpqGuy = mpqList.get(i);
				final MPQArchive mpq = mpqGuy.getArchive();
				// System.out.println("getting it from the outside: " +
				// filepath);
				ArchivedFile file = null;
				try {
					file = mpq.lookupHash2(new HashLookup(filepath));
				} catch (final MPQException exc) {
					if (exc.getMessage().equals("lookup not found")) {
						continue;
					} else {
						throw new IOException(exc);
					}
				}
				final ArchivedFileStream stream = new ArchivedFileStream(mpqGuy.getInputChannel(), extractor, file);
				final InputStream newInputStream = Channels.newInputStream(stream);
				String tmpdir = System.getProperty("java.io.tmpdir");
				if (!tmpdir.endsWith(File.separator)) {
					tmpdir += File.separator;
				}
				final String tempDir = tmpdir + "MatrixEaterExtract/";
				final File tempProduct = new File(tempDir + filepath.replace('\\', File.separatorChar));
				tempProduct.delete();
				tempProduct.getParentFile().mkdirs();
				Files.copy(newInputStream, tempProduct.toPath());
				tempProduct.deleteOnExit();
				cache.put(filepath, tempProduct);
				return tempProduct;
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(final String filepath) {
		try {
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final MpqGuy mpqGuy = mpqList.get(i);
				final MPQArchive mpq = mpqGuy.getArchive();
				ArchivedFile file = null;
				try {
					file = mpq.lookupHash2(new HashLookup(filepath));
				} catch (final MPQException exc) {
					if (exc.getMessage().equals("lookup not found")) {
						continue;
					} else {
						throw new IOException(exc);
					}
				}
				final ArchivedFileStream stream = new ArchivedFileStream(mpqGuy.getInputChannel(), extractor, file);
				final InputStream newInputStream = Channels.newInputStream(stream);
				return newInputStream;
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean has(final String filepath) {
		if (cache.containsKey(filepath)) {
			return true;
		}
		for (int i = mpqList.size() - 1; i >= 0; i--) {
			final MpqGuy mpqGuy = mpqList.get(i);
			if (mpqGuy.has(filepath)) {
				return true;
			}
		}
		return false;
	}

	public void refresh() {
		try {
			war3.getInputChannel().close();
			// } catch (IOException e) {
			// e.printStackTrace();
		} catch (final NullPointerException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			war3x.getInputChannel().close();
			// } catch (IOException e) {
			// e.printStackTrace();
		} catch (final NullPointerException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			war3xlocal.getInputChannel().close();
			// } catch (IOException e) {
			// e.printStackTrace();
		} catch (final NullPointerException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			war3patch.getInputChannel().close();
			// } catch (IOException e) {
			// e.printStackTrace();
		} catch (final NullPointerException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isDebugMode) {
			try {
				hfmd.getInputChannel().close();
				// } catch (IOException e) {
				// e.printStackTrace();
			} catch (final NullPointerException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mpqList.clear();
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
		if (isDebugMode) {
			hfmd = loadMPQ("hfmd.exe");
		}
	}

	public SetView<String> getMergedListfile() {
		final Set<String> listfile = new HashSet<>();
		for (final MpqGuy mpqGuy : mpqList) {
			try {
				final ArchivedFile listfileContents = mpqGuy.getArchive().lookupHash2(new HashLookup("(listfile)"));
				final ArchivedFileStream stream = new ArchivedFileStream(mpqGuy.getInputChannel(), extractor,
						listfileContents);
				final InputStream newInputStream = Channels.newInputStream(stream);
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(newInputStream))) {
					String line;
					while ((line = reader.readLine()) != null) {
						listfile.add(line);
					}
				} catch (final IOException exc) {
					throw new RuntimeException(exc);
				}
			} catch (final MPQException exc) {
				if (exc.getMessage().equals("lookup not found")) {
					continue;
				} else {
					throw new RuntimeException(exc);
				}
			}
		}
		return listfile;
	}

	private MpqGuy loadMPQ(final String mpq) {
		MpqGuy temp;
		// try {
		try {
			final SeekableByteChannel sbc = Files.newByteChannel(Paths.get(getWarcraftDirectory(), mpq),
					EnumSet.of(StandardOpenOption.READ));
			temp = new MpqGuy(new MPQArchive(sbc), sbc);
			mpqList.add(temp);
			return temp;
		} catch (final MPQException e) {
			ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs.\n" + mpq, e);
			e.printStackTrace();
		} catch (final IOException e) {
			ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs.\n" + mpq, e);
			e.printStackTrace();
		}
		// } catch (MPQFormatException e) {
		// ExceptionPopup.display("Warcraft installation archive reading error
		// occurred. Check your MPQs."+mpq, e);
		// e.printStackTrace();
		// } catch (MPQIsAVIException e) {
		// ExceptionPopup.display("Warcraft installation archive reading error
		// occurred. Check your MPQs."+mpq, e);
		// e.printStackTrace();
		// }//MPQArchive.openArchive(new File(getWarcraftDirectory()+mpq));
		// catch (MPQArchiveNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return null;
	}

	public boolean isBaseGameFile(final String filepath) {
		try {
			for (int i = mpqList.size() - 1; i >= 0; i--) {
				final MpqGuy mpqGuy = mpqList.get(i);
				final MPQArchive mpq = mpqGuy.getArchive();
				try {
					mpq.lookupPath(filepath);
					return i <= 3;
				} catch (final MPQException exc) {
					if (exc.getMessage().equals("lookup not found")) {
						continue;
					} else {
						throw new IOException(exc);
					}
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public LoadedMPQ loadMPQ(final Path path) throws MPQException, IOException {
		final SeekableByteChannel sbc = Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ));
		final MpqGuy temp = new MpqGuy(new MPQArchive(sbc), sbc);
		mpqList.add(temp);
		cache.clear();
		return new LoadedMPQ() {
			@Override
			public void unload() {
				mpqList.remove(temp);
				cache.clear();
			}

			@Override
			public boolean hasListfile() {
				return temp.has("(listfile)");
			}
		};
	}

	public interface LoadedMPQ {
		void unload();

		boolean hasListfile();
	}

	private static MpqCodebase current;

	public static MpqCodebase get() {
		if (current == null) {
			current = new MpqCodebase();
		}
		return current;
	}

	/**
	 * @return The Warcraft directory used by this test.
	 */
	public static String getWarcraftDirectory() {
		// return "C:\\temp\\WC3Archives\\";
		return SaveProfile.getWarcraftDirectory();
	}
}
