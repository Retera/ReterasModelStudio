package com.hiveworkshop.rms.parsers.w3o;

import com.hiveworkshop.rms.util.War3ID;
import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Inspired by PitzerMike's obj.h, without a lot of immediate focus on Java
 * conventions. I will probably get it converted over to Java conventions once I
 * have a working replica of his obj.h code.
 *
 * @author Eric
 *
 */
public final class War3ObjectDataChangeset {
	public static final int VAR_TYPE_INT = 0;
	public static final int VAR_TYPE_REAL = 1;
	public static final int VAR_TYPE_UNREAL = 2;
	public static final int VAR_TYPE_STRING = 3;
	public static final int VAR_TYPE_BOOLEAN = 4;
	public static final int MAX_STR_LEN = 1024;
	private static final Set<War3ID> UNIT_ID_SET;
	private static final Set<War3ID> ABILITY_ID_SET;
	static {
		final Set<War3ID> unitHashSet = new HashSet<>();
		String[] unitStrings = {"ubpx", "ubpy", "ides", "uhot", "unam", "ureq", "urqa", "utip", "utub"};
		addFromStringArray(unitHashSet, unitStrings);
		UNIT_ID_SET = unitHashSet;

		final Set<War3ID> abilHashSet = new HashSet<>();
		String[] abilStrings = {
				"irc2", "irc3",
				"bsk1", "bsk2", "bsk3",
				"coau", "coa1", "coa2", "cyc1",
				"dcp1", "dcp2",
				"dvm1", "dvm2", "dvm3", "dvm4", "dvm5",
				"exh1", "exhu",
				"fak1", "fak2", "fak3",
				"hwdu",
				"inv1", "inv2", "inv3", "inv4", "inv5",
				"liq1", "liq2", "liq3", "liq4",
				"mim1",
				"mfl1", "mfl2", "mfl3", "mfl4", "mfl5",
				"tpi1", "tpi2",
				"spl1", "spl2",
				"irl1", "irl2", "irl3", "irl4", "irl5",
				"idc1", "idc2", "idc3",
				"imo1", "imo2", "imo3", "imou",
				"ict1", "ict2",
				"isr1", "isr2",
				"ipv1", "ipv2", "ipv3",
				"mec1",
				"spb1", "spb2", "spb3", "spb4", "spb5",
				"gra1", "gra2", "gra3", "gra4", "gra5",
				"ipmu",
				"flk1", "flk2", "flk3", "flk4", "flk5",
				"fbk1", "fbk2", "fbk3", "fbk4",
				"nca1",
				"pxf1", "pxf2",
				"mls1",
				"sla1", "sla2"};
		addFromStringArray(abilHashSet, abilStrings);
		ABILITY_ID_SET = abilHashSet;
	}

	private static void addFromStringArray(Set<War3ID> hashSet, String[] hashStrings) {
		for(String hashString : hashStrings){
			hashSet.add(War3ID.fromString(hashString));
		}
	}

	private int version;
	private ObjectMap original = new ObjectMap();
	private final ObjectMap custom = new ObjectMap();
	private char expected;
	private War3ID lastused;

	public char kind;
	public boolean detected;

	public War3ID nameField;

	public War3ObjectDataChangeset() {
		version = 2;
		kind = 'u';
		expected = 'u';
		detected = false;
		lastused = War3ID.fromString("u~~~");
	}

	public War3ObjectDataChangeset(final char expectedkind) {
		version = 2;
		kind = 'u';
		expected = expectedkind;
		detected = false;
		lastused = War3ID.fromString("u~~~");
	}

	public boolean detectKind(final War3ID chid) {
		if (UNIT_ID_SET.contains(chid)) {
			kind = 'u';
			return false;
		} else if (ABILITY_ID_SET.contains(chid)) {
			kind = 'a';
		} else {
			switch (chid.asStringValue().charAt(0)) {
				case 'f' -> kind = 'h';
				case 'i' -> kind = 't';
				case 'g' -> kind = 'q';
				case 'a', 'u', 'b', 'd' -> kind = chid.asStringValue().charAt(0);
				default -> kind = 'a';
			}
		}
		return true;
	}

	public char getExpectedKind() {
		return expected;
	}

	public War3ID getNameField() {
		final War3ID field = War3ID.fromString("unam");
		char cmp = kind;
		if (!detected) {
			cmp = expected;
		}
		switch (cmp) {
			case 'h' -> nameField = field.set(0, 'f');
			case 't' -> nameField = field.set(0, 'u');
			case 'q' -> nameField = field.set(0, 'g');
			default -> nameField = field.set(0, cmp);
		}
		return nameField;
	}

	public boolean extended() {
		char cmp = kind;
		if (!detected) {
			cmp = expected;
		}
		return switch (cmp) {
			case 'u', 'h', 'b', 't' -> false;
			default -> true;
		};
	}

	public void renameIds(final ObjectMap map, final boolean isOriginal) {
		final War3ID nameId = getNameField();
		final List<War3ID> idsToRemoveFromMap = new ArrayList<>();
		final Map<War3ID, ObjectDataChangeEntry> idsToObjectsForAddingToMap = new HashMap<>();

        for (final Map.Entry<War3ID, ObjectDataChangeEntry> entry : map) {
            final ObjectDataChangeEntry current = entry.getValue();
            final List<Change> nameEntry = current.getChanges().get(nameId);

            if ((nameEntry != null) && nameEntry.size() > 0) {
                final Change firstNameChange = nameEntry.get(0);
                int pos = firstNameChange.getStrval().lastIndexOf("::");

                if ((pos != -1) && (firstNameChange.getStrval().length() > (pos + 2))) {
                    String rest = firstNameChange.getStrval().substring(pos + 2);

                    if (rest.length() == 4) {
                        final War3ID newId = War3ID.fromString(rest);
                        final ObjectDataChangeEntry existingObjectWithMatchingId = map.get(newId);

                        if (isOriginal) {// obj.cpp: update id and name
                            current.setOldId(newId);
                        } else {
                            current.setNewId(newId);
                        }
                        firstNameChange.setStrval(firstNameChange.getStrval().substring(0, pos));

                        if (existingObjectWithMatchingId != null) {
							carryOverChanges(nameId, current, existingObjectWithMatchingId);

						} else { // obj.cpp: an object with that id didn't exist
                            idsToRemoveFromMap.add(entry.getKey());
                            idsToObjectsForAddingToMap.put(newId, current.clone());
                        }
                    } else if ("REMOVE".equals(rest)) { // obj.cpp: want to remove the object
                        idsToRemoveFromMap.add(entry.getKey());
                    } // obj.cpp: in all other cases keep it untouched
                }
            }

        }
		for (final War3ID id : idsToRemoveFromMap) {
			map.remove(id);
		}
		for (final Map.Entry<War3ID, ObjectDataChangeEntry> entry : idsToObjectsForAddingToMap.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
	}

	private void carryOverChanges(War3ID nameId, ObjectDataChangeEntry current, ObjectDataChangeEntry existingObjectWithMatchingId) {
		// obj.cpp: carry over all changes
		for (Map.Entry<War3ID, List<Change>> changeIteratorNext : current.getChanges()) {
			final War3ID copiedChangeId = changeIteratorNext.getKey();
			List<Change> changeListForFieldToOverwrite = existingObjectWithMatchingId.getChanges()
					.get(copiedChangeId);
			if (changeListForFieldToOverwrite == null) {
				changeListForFieldToOverwrite = new ArrayList<>();
			}
			for (final Change changeToCopy : changeIteratorNext.getValue()) {
				final Iterator<Change> replaceIterator = changeListForFieldToOverwrite.iterator();

				boolean didOverwrite = overwrite(nameId, copiedChangeId, changeToCopy, replaceIterator);
				if (!didOverwrite) {
					changeListForFieldToOverwrite.add(changeToCopy);
					if (changeListForFieldToOverwrite.size() == 1) {
						existingObjectWithMatchingId.getChanges().add(copiedChangeId,
								changeListForFieldToOverwrite);
					}
				}
			}
		}
	}

	private boolean overwrite(War3ID nameId, War3ID copiedChangeId, Change changeToCopy, Iterator<Change> replaceIterator) {
		boolean didOverwrite = false;
		int pos;
		String rest;
		while (replaceIterator.hasNext()) {
			final Change changeToOverwrite = replaceIterator.next();
			if (changeToOverwrite.getLevel() != changeToCopy.getLevel()) {
				// obj.cpp: we can only replace changes with the same level/variation
				continue;
			}
			if (copiedChangeId.equals(nameId)) {
				// obj.cpp: carry over further references
				pos = changeToOverwrite.getStrval().lastIndexOf("::");
				if ((pos != -1) && (changeToOverwrite.getStrval().length() > (pos + 2))) {
					rest = changeToOverwrite.getStrval().substring(pos + 2);
					if ((rest.length() == 4) || "REMOVE".equals(rest)) {
						changeToCopy.setStrval(changeToCopy.getStrval() + "::" + rest);
						// so if this is a peasant, whose name was "Peasant::hfoo" and when we
						// copied his data onto the footman, we found that the footman was named
						// "Footman::hkni", then at that point we set the peasant's name to be
						// "Peasant::hkni" because we are about to copy it onto the footman.
						// And, we already set it to just "Peasant", so appending the "::" and
						// the 'rest' variable is enough.
						// Then, on a further loop iteration, in theory we will copy the
						// footman who is named Peasant onto the knight.
						//
						// TODO but what if we already copied the footman onto the knight?
						// did PitzerMike consider this in obj.cpp?
					}
				}
			}
			changeToOverwrite.copyFrom(changeToCopy);
			didOverwrite = true;
			break;
		}
		return didOverwrite;
	}

	public void renameIds() {
		renameIds(original, true);
		renameIds(custom, false);
	}

	// ' ' - '/'
	// ':' - '@'
	// '[' - '`'
	// '{' - '~'
	public char nextchar(final char cur) {
		return switch (cur) {
			// skip ' because often jass parsers don't handle escaped rawcodes like '\''
			case '&' -> '(';
			// skip digits
			case '/' -> ':';
			// skip capital letters
			// skip \ for the sam reason like ' ('\\')
			case '@' -> '[';
			case '[' -> ']';
			// skip � and lower case letters (� can't be seen very well)
			case '_' -> '{';
			// close circle and restart at !
			case '~' -> '!';
			default -> (char) ((short) cur + 1);
		};
	}

	// we use only special characters to avoid collisions with existing objects
	// the first character must remain unchanged though because it can have a
	// special meaning
	public War3ID getUnusedId(final War3ID substituteFor) {
		lastused = lastused.set(0, substituteFor.charAt(0));
		lastused = lastused.set(3, nextchar(substituteFor.charAt(3)));
		if (lastused.charAt(3) == '!') {
			lastused = lastused.set(2, nextchar(substituteFor.charAt(2)));
			if (lastused.charAt(2) == '!') {
				lastused = lastused.set(1, nextchar(substituteFor.charAt(1)));
			}
		}
		return lastused;
	}

	public void mergeTable(final ObjectMap target, final ObjectMap targetCustom, final ObjectMap source,
						   final CollisionHandling collisionHandling) {
		for (Map.Entry<War3ID, ObjectDataChangeEntry> sourceObject : source) {
			if (target.containsKey(sourceObject.getKey())) {
				// obj.cpp: we have a collision
				War3ID oldId;
				War3ID replacementId;

				// obj.cpp: get new id until we finally have one that isn't used yet, or we're
				// out of ids
				// final ObjectDataChangeEntry deleteObject = target.get(sourceObject.getKey());
				switch (collisionHandling) {
					case CREATE_NEW_ID -> {
						oldId = sourceObject.getKey();
						replacementId = getUnusedId(oldId);
						while (!((oldId.charAt(1) == '~')
								&& (oldId.charAt(2) == '~')
								&& (oldId.charAt(3) == '~'))
								&& targetCustom.containsKey(replacementId)) {
							oldId = replacementId;
							replacementId = getUnusedId(oldId);
						}
						if (!((oldId.charAt(1) == '~') && (oldId.charAt(2) == '~') && (oldId.charAt(3) == '~'))) {
							sourceObject.getValue().setNewId(replacementId);
							targetCustom.put(replacementId, sourceObject.getValue().clone());
						}
					}
					case REPLACE -> target.put(sourceObject.getKey(), sourceObject.getValue().clone());
// merge
					default -> {
						final ObjectDataChangeEntry targetObject = target.get(sourceObject.getKey());
						for (final Map.Entry<War3ID, List<Change>> sourceUnitField : sourceObject.getValue()
								.getChanges()) {
							for (final Change sourceChange : sourceUnitField.getValue()) {
								List<Change> targetChanges = targetObject.getChanges().get(sourceUnitField.getKey());
								findBestSource(targetObject, sourceChange, targetChanges, sourceUnitField.getKey());
							}
						}
					}
				}
			} else {
				targetCustom.put(sourceObject.getKey(), sourceObject.getValue().clone());
			}
		}
	}

	private void findBestSource(ObjectDataChangeEntry targetObject, Change sourceChange, List<Change> targetChanges, War3ID key) {
		if (targetChanges == null) {
			targetChanges = new ArrayList<>();
		}
		Change bestTargetChange = null;
		for (final Change targetChange : targetChanges) {
			if (targetChange.getLevel() == sourceChange.getLevel()) {
				bestTargetChange = targetChange;
				break;
			}
		}
		if (bestTargetChange != null) {
			bestTargetChange.copyFrom(sourceChange);
		} else {
			targetChanges.add(sourceChange.clone());
			if (targetChanges.size() == 1) {
				targetObject.getChanges().add(key, targetChanges);
			}
		}
	}

	public enum CollisionHandling {
		CREATE_NEW_ID, REPLACE, MERGE
	}

	public void merge(final War3ObjectDataChangeset obj, final CollisionHandling collisionHandling) {
		mergeTable(original, custom, obj.original, collisionHandling);
		mergeTable(original, custom, obj.custom, collisionHandling);
	}

	public int getvartype(final String name) {
		if ("int".equals(name) || "bool".equals(name)) {
			return 0;
		} else if ("real".equals(name)) {
			return 1;
		} else if ("unreal".equals(name)) {
			return 2;
		}
		return 3; // string
	}

	public boolean loadtable(final BlizzardDataInputStream stream, final ObjectMap map, final boolean isOriginal,
			final WTS wts, final boolean inlineWTS) throws IOException {
		final War3ID noId = new War3ID(0);
		final ByteBuffer stringByteBuffer = ByteBuffer.allocate(1024); // TODO check max len?
		final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		int ptr;
		final int count = stream.readInt();
		for (int i = 0; i < count; i++) {
			final long nanoTime = System.nanoTime();
			final War3ID origId;
			War3ID newid = null;
			origId = readWar3ID(stream);
			ObjectDataChangeEntry existingObject;
			checkId(noId, origId);
			if (isOriginal) {
				existingObject = map.get(origId);
				if (existingObject == null) {
					existingObject = new ObjectDataChangeEntry(origId, noId);
				}
				existingObject.setNewId(readWar3ID(stream));
			} else {
				newid = readWar3ID(stream);
				checkId(noId, newid);
				existingObject = map.get(newid);
				if (existingObject == null) {
					existingObject = new ObjectDataChangeEntry(origId, newid);
				}
			}
			final int ccount = stream.readInt();// Retera: I assume this is change count?
			if ((ccount == 0) && isOriginal) {
				// throw new IOException("we seem to have reached the end of the stream and get
				// zeroes");
				System.err.println("we seem to have reached the end of the stream and get zeroes");
			}
			if (isOriginal) {
				debugprint("StandardUnit \"" + origId + "\" " + ccount + " {");
			} else {
				debugprint("CustomUnit \"" + origId + ":" + newid + "\" " + ccount + " {");
			}
			for (int j = 0; j < ccount; j++) {
				final War3ID chid = readWar3ID(stream);
				checkId(chid, noId);
				if (!detected) {
					detected = detectKind(chid);
				}

				final Change newlyReadChange = new Change();
				newlyReadChange.setId(chid);
				newlyReadChange.setVartype(stream.readInt());
				debugprint("\t\"" + chid + "\" {");
				debugprint("\t\tType " + newlyReadChange.getVartype() + ",");
				if (extended()) {
					newlyReadChange.setLevel(stream.readInt());
					newlyReadChange.setDataptr(stream.readInt());
					debugprint("\t\tLevel " + newlyReadChange.getLevel() + ",");
					debugprint("\t\tData " + newlyReadChange.getDataptr() + ",");
				}

				switch (newlyReadChange.getVartype()) {
					case 0 -> {
						newlyReadChange.setLongval(stream.readInt());
						debugprint("\t\tValue " + newlyReadChange.getLongval() + ",");
					}
					case 3 -> {
						ptr = 0;
						stringByteBuffer.clear();
						byte charRead;
						while ((charRead = (byte) stream.read()) != 0) {
							stringByteBuffer.put(charRead);
						}
						stringByteBuffer.flip();
						newlyReadChange.setStrval(decoder.decode(stringByteBuffer).toString());
						if (inlineWTS
								&& (newlyReadChange.getStrval().length() > 8)
								&& "TRIGSTR_".equals(newlyReadChange.getStrval().substring(0, 8))) {
							final int key = getWTSValue(newlyReadChange);
							newlyReadChange.setStrval(wts.get(key));

							if ((newlyReadChange.getStrval() != null)
									&& (newlyReadChange.getStrval().length() > MAX_STR_LEN)) {
								newlyReadChange.setStrval(newlyReadChange.getStrval().substring(0, MAX_STR_LEN - 1));
							}
						}
						debugprint("\t\tValue \"" + newlyReadChange.getStrval() + "\",");
					}
					case 4 -> {
						newlyReadChange.setBoolval(stream.readInt() == 1);
						debugprint("\t\tValue " + newlyReadChange.isBoolval() + ",");
					}
					default -> {
						newlyReadChange.setRealval(stream.readFloat());
						debugprint("\t\tValue " + newlyReadChange.getRealval() + ",");
					}
				}
				final War3ID crap = readWar3ID(stream);
				debugprint("\t\tExtra \"" + crap + "\",");
				newlyReadChange.setJunkDNA(crap);
				List<Change> existingChanges = existingObject.getChanges().get(chid);

				findBestSource(existingObject, newlyReadChange, existingChanges, chid);

				if (!crap.equals(existingObject.getOldId())
						&& !crap.equals(existingObject.getNewId())
						&& !crap.equals(noId)) {
					for (int charIndex = 0; charIndex < 4; charIndex++) {
						if ((crap.charAt(charIndex) < 32) || (crap.charAt(charIndex) > 126)) {
							return false;
						}
					}
				}
				debugprint("\t}");
			}
			debugprint("}");
			if ((newid == null) && !isOriginal) {
				throw new IllegalStateException("custom unit has no ID!");
			}
			map.put(isOriginal ? origId : newid, existingObject);
			final long endNanoTime = System.nanoTime();
			final long deltaNanoTime = endNanoTime - nanoTime;
		}
		return true;
	}

	private void checkId(War3ID noId, War3ID id) throws IOException {
		if (id.equals(noId)) {
			throw new IOException("the input stream might be screwed");
		}
	}

	private War3ID readWar3ID(final BlizzardDataInputStream stream) throws IOException {
		return War3ID.fromString(new String(stream.readChars(4)));
	}

	private static int getWTSValue(final Change change) {
		String numberAsText = change.getStrval().substring(8);
		while ((numberAsText.length() > 0) && (numberAsText.charAt(0) == '0')) {
			numberAsText = numberAsText.substring(1);
		}
		if (numberAsText.length() == 0) {
			return 0;
		}
		while (!Character.isDigit(numberAsText.charAt(numberAsText.length() - 1))) {
			numberAsText = numberAsText.substring(0, numberAsText.length() - 1);
		}
		return Integer.parseInt(numberAsText);
	}

	public boolean load(final BlizzardDataInputStream stream, final WTS wts, final boolean inlineWTS)
			throws IOException {
		detected = false;
		version = stream.readInt();
		if ((version != 1) && (version != 2)) {
			return false;
		}
		ObjectMap backup = original.clone();
		if (!loadtable(stream, original, true, wts, inlineWTS)) {
			original = backup;
			return false;
		}
		backup = custom.clone();
		if (!loadtable(stream, custom, false, wts, inlineWTS)) {
			original = backup;
			return false;
		}
		return true;
	}

	public boolean load(final File file, final WTS wts, final boolean inlineWTS) throws IOException {
		try (BlizzardDataInputStream inputStream = new BlizzardDataInputStream(new FileInputStream(file))) {
			return load(inputStream, wts, inlineWTS);
		}
	}

	public static void inlineWTSTable(final ObjectMap map, final WTS wts) {
		for (final Map.Entry<War3ID, ObjectDataChangeEntry> entry : map) {
			for (final Map.Entry<War3ID, List<Change>> changes : entry.getValue().getChanges()) {
				for (final Change change : changes.getValue()) {
					if ((change.getStrval().length() > 8) && "TRIGSTR_".equals(change.getStrval().substring(0, 8))) {
						final int key = getWTSValue(change);
						change.setStrval(wts.get(key));
						if (change.getStrval().length() > MAX_STR_LEN) {
							change.setStrval(change.getStrval().substring(0, MAX_STR_LEN - 1));
						}
					}
				}
			}
		}
	}

	public void inlineWTS(final WTS wts) {
		inlineWTSTable(original, wts);
		inlineWTSTable(custom, wts);
	}

	public void reset() {
		reset('u');
	}

	public void reset(final char expectedkind) {
		detected = false;
		kind = 'u';
		lastused = War3ID.fromString("u~~~");
		expected = expectedkind;
		original.clear();
		custom.clear();
	}

	public boolean saveTable(final BlizzardDataOutputStream outputStream, final ObjectMap map, final boolean isOriginal)
			throws IOException {
		final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		final CharBuffer charBuffer = CharBuffer.allocate(1024);
		final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		final War3ID noid = new War3ID(0);
		int count;
		count = map.size();
		outputStream.writeInt(count);
		for (final Map.Entry<War3ID, ObjectDataChangeEntry> entry : map) {
			final ObjectDataChangeEntry cl = entry.getValue();
			int totalSize = 0;
			for (final Map.Entry<War3ID, List<Change>> changeEntry : cl.getChanges()) {
				totalSize += changeEntry.getValue().size();
			}
			if ((totalSize > 0) || !isOriginal) {
				saveWriteChars(outputStream, cl.getOldId().asStringValue().toCharArray());
				saveWriteChars(outputStream, cl.getNewId().asStringValue().toCharArray());
				count = totalSize;// cl.getChanges().size();
				outputStream.writeInt(count);
				for (final Map.Entry<War3ID, List<Change>> changes : entry.getValue().getChanges()) {
					for (final Change change : changes.getValue()) {
						saveWriteChars(outputStream, change.getId().asStringValue().toCharArray());
						outputStream.writeInt(change.getVartype());
						if (extended()) {
							outputStream.writeInt(change.getLevel());
							outputStream.writeInt(change.getDataptr());
						}
						switch (change.getVartype()) {
							case 0 -> outputStream.writeInt(change.getLongval());
							case 3 -> {
								charBuffer.clear();
								byteBuffer.clear();
								charBuffer.put(change.getStrval());
								charBuffer.flip();
								encoder.encode(charBuffer, byteBuffer, false);
								byteBuffer.flip();
								final byte[] stringBytes = new byte[byteBuffer.remaining() + 1];
								int i = 0;
								while (byteBuffer.hasRemaining()) {
									stringBytes[i++] = byteBuffer.get();
								}
								stringBytes[i] = 0;
								outputStream.write(stringBytes);
							}
							case 4 -> outputStream.writeInt(change.isBoolval() ? 1 : 0);
							default -> outputStream.writeFloat(change.getRealval());
						}
						// if (change.getJunkDNA() == null) {
						// saveWriteChars(outputStream, cl.getNewId().asStringValue().toCharArray());
						// } else {
						// saveWriteChars(outputStream,
						// change.getJunkDNA().asStringValue().toCharArray());
						// }
						// saveWriteChars(outputStream, cl.getNewId().asStringValue().toCharArray());
						saveWriteChars(outputStream, noid.asStringValue().toCharArray());
					}
				}
			}
		}
		return true;
	}

	private void saveWriteChars(final BlizzardDataOutputStream outputStream, final char[] charArray)
			throws IOException {
		// TODO Auto-generated method stub
		outputStream.writeChars(charArray);
		for (int i = charArray.length; i < 4; i++) {
			outputStream.writeByte(0);
		}
	}

	public boolean save(final BlizzardDataOutputStream outputStream, final boolean generateWTS) throws IOException {
		if (generateWTS) {
			throw new UnsupportedOperationException("FAIL cannot generate WTS, needs more code");
		}
		version = 2;
		outputStream.writeInt(version);
		if (!saveTable(outputStream, original, true)) {
			throw new RuntimeException("Failed to save standard unit custom data");
		}
		if (!saveTable(outputStream, custom, false)) {
			throw new RuntimeException("Failed to save custom unit custom data");
		}
		return true;
	}

	public ObjectMap getOriginal() {
		return original;
	}

	public ObjectMap getCustom() {
		return custom;
	}

	private static void debugprint(final String s) {

	}
}
