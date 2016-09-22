package com.mundi4.mpq;

import com.mundi4.mpq.bzip2.BZip2CompressorInputStream;
import com.mundi4.mpq.helper.ByteBufferInputStream;
import com.mundi4.mpq.helper.ReadOnlyIterator;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.*;
import java.util.zip.InflaterInputStream;

import static com.mundi4.mpq.helper.MpqUtils.*;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class MpqFile implements MpqConstants, Closeable {

    private String name;
    private FileChannel fc;
    private File listfile;
    private boolean usesInternalListfile = false;
    private boolean closeRequested;

    private long headerPosition;
    private MpqHeader header;
    private int blockSize;
    private Map<Hash, MpqEntry> entries;

    public MpqFile(String name) throws IOException {
        this(new File(name));
    }

    public MpqFile(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }

        name = file.getPath();
        usesInternalListfile = true;
        open(file);
    }

    public MpqFile(File file, File listfile) throws IOException {
        if (file == null || listfile == null) {
            throw new NullPointerException();
        }

        this.listfile = listfile;
        name = file.getPath();
        open(file);
    }

    private void open(File file) throws IOException {
        try {
            fc = new FileInputStream(file).getChannel();
            locateHeaderOrMpqException();
            this.blockSize = 0x200 << header.blockSize;

            int numHashes = header.numHashEntries;
            int hashTableSize = numHashes * HASH_TABLE;
            long hashtablePosition = header.hashTablePosition + headerPosition;
            ByteBuffer hashTable = fc.map(MapMode.READ_ONLY, hashtablePosition,
                    hashTableSize);
            byte[] b = new byte[hashTableSize];
            hashTable.get(b);

            decryptTable(b, HASH_TABLE_KEY);
            hashTable = ByteBuffer.wrap(b).order(LITTLE_ENDIAN);

            long end = headerPosition + header.archiveSize;
            int numBlocks = header.numBlockEntries;
            int blockTableSize = numBlocks * BLOCK_TABLE;
            long blocktablePosition = header.blockTablePosition
                    + headerPosition;
            if ((blocktablePosition + blockTableSize) > end) {
                blockTableSize = (int) (end - blocktablePosition);
                numBlocks = blockTableSize / BLOCK_TABLE;
            }
            ByteBuffer blockTable = fc.map(MapMode.READ_ONLY,
                    blocktablePosition, blockTableSize);
            b = new byte[blockTableSize];
            blockTable.get(b);
            decryptTable(b, BLOCK_TABLE_KEY);
            blockTable = ByteBuffer.wrap(b).order(LITTLE_ENDIAN);

            entries = new LinkedHashMap<Hash, MpqEntry>(numBlocks);
            for (int i = 0; i < numHashes; i++) {
                int code1 = hashTable.getInt();
                int code2 = hashTable.getInt();
                hashTable.position(hashTable.position() + 4);
                int blockIndex = hashTable.getInt();

                if (blockIndex < 0) {
                    continue;
                }
                int pos = blockIndex * BLOCK_TABLE;
                if (pos > blockTableSize - BLOCK_TABLE) {
                    continue;
                }
                blockTable.position(pos);
                long position = toUnsignedInt(blockTable.getInt());
                long compressedSize = toUnsignedInt(blockTable.getInt());
                long fileSize = blockTable.getInt();
                int flags = blockTable.getInt();
                Hash key = new Hash(code1, code2);
                MpqEntry entry = new MpqEntry(position, compressedSize,
                        fileSize, flags);
                entries.put(key, entry);
            }

            if (usesInternalListfile) {
                MpqEntry listfileEntry = entries.get(new Hash(
                        INTERNAL_LIST_FILE));
                if (listfileEntry != null) {
                    InputStream is = getInputStream(listfileEntry);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    for (String name = reader.readLine(); name != null; name = reader
                            .readLine()) {
                        Hash key = new Hash(name);
                        MpqEntry entry = entries.get(key);
                        if (entry != null) {
                            entry.name = name;
                        }
                    }
                } else {
                    throw new MpqException("(listfile) not found");
                }
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(
                        listfile));
                try {
                    for (String name = reader.readLine(); name != null; name = reader
                            .readLine()) {
                        Hash key = new Hash(name);
                        MpqEntry entry = entries.get(key);
                        if (entry != null) {
                            entry.name = name;
                        }
                    }
                } finally {
                    try {
                        reader.close();
                    } catch (Exception e) {
                    }
                }
            }

        } catch (IOException e) {
            try {
                close();
            } catch (Exception e2) {
            }
            throw e;
        } catch (RuntimeException e) {
            try {
                close();
            } catch (Exception e2) {
            }
            throw e;
        }
    }

    private void locateHeaderOrMpqException() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8).order(LITTLE_ENDIAN);
        for (long i = 0; i < (fc.size() - HEADER); i += 0x200) {
            buf.clear();
            fc.read(buf, i);
            buf.flip();

            if (buf.getInt() == MPQSIG) {
                headerPosition = i;
                int headerLength = buf.getInt();
                buf = ByteBuffer.allocate(headerLength - 8)
                        .order(LITTLE_ENDIAN);
                fc.read(buf, i + 8);
                buf.flip();

                header = new MpqHeader(buf, headerLength);
                return;
            }
        }

        throw new MpqException("Unable to locate MPQ header");
    }

    public MpqEntry getEntry(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        ensureOpen();
        return entries.get(new Hash(name));
    }

    public InputStream getInputStream(MpqEntry entry) throws IOException {
        if (entry == null) {
            throw new NullPointerException("entry");
        }

        if (entry.isEncrypted()) {
            throw new MpqException("encrypted MPQ entry not supported yet");
        }

        ensureOpen();
        long positon = headerPosition + entry.position;
        long size = entry.compressedSize;
        ByteBuffer buf = fc.map(MapMode.READ_ONLY, positon, size).order(
                LITTLE_ENDIAN);

        InputStream in = new ByteBufferInputStream(buf);
        if (!entry.isCompressed() && !entry.isEncrypted()) {
            return in;
        }

        if (entry.isSingleUnit()) {
            return new MpqFileInputStream(in, entry);
        }

        return new MultiBlockMpqFileInputStream(buf, entry);
    }

    public String getName() {
        return name;
    }

    public int getTotal() {
        return entries.size();
    }

    public Enumeration<MpqEntry> entries() {
        ensureOpen();
        return Collections.enumeration(entries.values());
    }

    public Iterator<MpqEntry> iterator() {
        ensureOpen();
        return new ReadOnlyIterator<MpqEntry>(entries.values().iterator());
    }

    @Override
    public void close() throws IOException {
        if (!closeRequested) {
            synchronized (this) {
                if (!closeRequested) {
                    if (fc != null) {
                        if (fc.isOpen()) {
                            fc.close();
                        }
                        fc = null;
                    }
                    closeRequested = true;
                }
            }
        }
    }

    private void ensureOpen() {
        if (closeRequested) {
            throw new IllegalStateException("mpq file closed");
        }
        if (fc == null) {
            throw new IllegalStateException("The object is not initialized.");
        }
    }

    @Override
    protected void finalize() throws IOException {
        close();
    }

    class MpqHeader {

        int headerLength;
        long archiveSize;
        short format;
        short blockSize; // Size of file block is 0x200 << BlockSize
        long hashTablePosition;
        long blockTablePosition;
        int numHashEntries;
        int numBlockEntries;
        long extendedBlockTablePosition;
        short hashTablePositionHigh;
        short blockTablePositionHigh;

        MpqHeader(ByteBuffer buf, int length) {
            this.headerLength = length;
            archiveSize = toUnsignedInt(buf.getInt());
            format = buf.getShort();
            blockSize = buf.getShort();
            hashTablePosition = toUnsignedInt(buf.getInt());
            blockTablePosition = toUnsignedInt(buf.getInt());
            numHashEntries = buf.getInt();
            numBlockEntries = buf.getInt();

            if (format == 1) {
                extendedBlockTablePosition = buf.getLong();
                hashTablePositionHigh = buf.getShort();
                blockTablePositionHigh = buf.getShort();
                // so what?
                assert extendedBlockTablePosition == 0;
                assert hashTablePositionHigh == 0;
                assert blockTablePositionHigh == 0;
            }
        }

    }

    private static class Hash {
        private int code1;
        private int code2;

        Hash(int code1, int code2) {
            this.code1 = code1;
            this.code2 = code2;
        }

        Hash(String str) {
            this(hashString(str, 0x100), hashString(str, 0x200));
        }

        @Override
        public int hashCode() {
            return code2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof Hash) {
                Hash that = (Hash) o;
                return this.code1 == that.code1 && this.code2 == that.code2;
            }
            return false;
        }
    }

    class MpqFileInputStream extends FilterInputStream {

        MpqFileInputStream(InputStream in, MpqEntry entry) throws IOException {
            this(in, entry, entry.getCompressedSize(), entry.getSize());
        }

        MpqFileInputStream(InputStream in, MpqEntry entry, long inSize,
                           long outSize) throws IOException {
            super(null);
            init(in, entry, inSize, outSize);
        }

        private void init(InputStream in, MpqEntry entry, long inSize,
                          long outSize) throws IOException {
            if (entry.isEncrypted() && inSize > 3) {
                in = initDecryptorInputStream(in);
            }
            if (entry.isCompressed() && inSize != outSize) {
                in = initInflaterInputStream(in);
            }

            super.in = in;
        }

        private InputStream initDecryptorInputStream(InputStream in) {
            // TODO
            return in;
        }

        private InputStream initInflaterInputStream(InputStream in)
                throws IOException {
            in = new PushbackInputStream(in);
            PushbackInputStream pin = (PushbackInputStream) in;
            int formats = in.read();
            int b = in.read();
            pin.unread(b);

            if (formats == -1) {
                throw new EOFException();
            }

            if ((0x10 & formats) != 0) {
                in.skip(2);
                in = new BZip2CompressorInputStream(in);
                formats &= 0xEF;
            }

            if ((0x08 & formats) != 0) {
                in = new InflaterInputStream(in);
                formats &= 0xF7;
            }

            if ((0x02 & formats) != 0) {
                in = new InflaterInputStream(in);
                formats &= 0xFD;
            }

            if ((0x01 & formats) == 0x01) {
                in = new InflaterInputStream(in);
                formats &= 0xFE;
            }

            if ((formats & 0x80) != 0) {
                // compressionType &= 0x7f;
                // throw new RuntimeException("wav0x80");
            }

            if ((formats & 0x40) != 0) {
                // compressionType &= 0xbf;
                // throw new RuntimeException("wav0x40");
            }

            if (formats != 0) {
                String msg = "unhandled compression formats: 0x"
                        + Integer.toHexString(formats);
                throw new MpqException(msg);
            }

            return in;
        }

    }

    class MultiBlockMpqFileInputStream extends InputStream {

        private ByteBuffer buf;
        private int pos;
        private int count;
        private int blockIndex = -1;
        private int blockCount;
        private int[] positions;
        private MpqEntry entry;
        private MpqFileInputStream in;

        MultiBlockMpqFileInputStream(ByteBuffer buf, MpqEntry entry)
                throws IOException {
            this.buf = buf;
            this.entry = entry;
            this.blockCount = (int) (((blockSize - 1) + entry.getSize()) / blockSize);
            loadBlockPositions();
        }

        private void loadBlockPositions() throws IOException {
            positions = new int[blockCount + 1];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = buf.getInt();
            }

            int bytesRead = 4 * positions.length;
            if (!entry.hasExtra() && positions[0] != bytesRead) {
                entry.flags |= ENCRYPTED;
            }
            if (bytesRead < positions[0]) {
                buf.position(positions[0]);
            }
            assert buf.position() == positions[0];
        }

        private int readNextBlock() throws IOException {
            assert count - pos <= 0;
            if (++blockIndex >= blockCount) {
                return -1;
            }
            if (in != null) {
                in.close();
            }
            pos = 0;
            count = (int) Math.min(entry.getSize() - (blockIndex * blockSize),
                    blockSize);

            int offset = positions[blockIndex];
            int length = positions[blockIndex + 1] - offset;
            InputStream bbin = new ByteBufferInputStream(buf, offset, length);
            in = new MpqFileInputStream(bbin, entry, length, count);
            return blockIndex;
        }

        @Override
        public int read() throws IOException {
            if (pos >= count) {
                readNextBlock();
                if (pos >= count) {
                    return -1;
                }
            }
            int b = in.read();
            pos++;
            return b;
        }

        @Override
        public long skip(long n) throws IOException {
            long i = 0;
            for (; i < n; i++) {
                if (read() == -1) {
                    break;
                }
            }
            return i;
        }

        @Override
        public int available() {
            int avail = count - pos;
            if (avail <= 0) {
                if (blockIndex + 1 < blockCount) {
                    return 1;
                }
                return 0;
            }
            return Math.max(0, avail);
        }

    }

}
