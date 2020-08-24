package mpq;

import mpq.compression.Compression;
import mpq.data.*;
import mpq.util.Cryption;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class MPQArchive {	
	private long archiveOffset;
	private short blockShift;
	private HashTable hashTable;
	private BlockTable blockTable;
	private short version;
	private long archiveSize;
	
	// locates the archive header from within a SeekableByteChannel
	private ArchiveHeader locateArchive(final SeekableByteChannel in) throws IOException, MPQException{
		// *** find MPQ archive header
		// allocate a buffer and header interpreter
		ByteBuffer buffer = ByteBuffer.allocate(FileHeader.STRUCT_SIZE);
		final FileHeader header = new FileHeader(buffer);
		
		// look for the header positioned at 512 bytes
		archiveOffset = in.position() & ~(512 - 1);
		for(;;){
			// read in possible header data
			in.position(archiveOffset);
			while( buffer.hasRemaining() ) {
				if( in.read(buffer) == -1 ) {
					throw new MPQException("channel does not contain a MPQ archive");
				}
			}
			buffer.clear();
			
			// check header validity
			if( header.getIdentifierInt() == FileHeader.ARCHIVE_IDENTIFIER_INT ) {
				break;
			}
			// if user data header is found, extract archive header offset and continue
			else if( header.getIdentifierInt() == FileHeader.USERDATA_IDENTIFIER_INT ){
				final ByteBuffer temp = ByteBuffer.allocate(header.getHeaderSize());
				final UserDataHeader udheader = new UserDataHeader(temp);
				
				while( temp.hasRemaining() ) {
					if( in.read(temp) == -1 ) {
						break;
					}
				}
				
				archiveOffset+= udheader.getArchiveOffset();
				continue;
			}
			
			// prepare buffer for next operation and skip to next 512 bytes
			archiveOffset+= 512;
		}
		
		// *** load MPQ archive header
		// allocate a buffer and archive header interpreter
		int size = header.getHeaderSize() - FileHeader.STRUCT_SIZE;
		// place a reasonable 8KB limit to archive header size
		if( size > 1 << 13 || size < 0  ) {
			size = 1 << 13;
		}
		buffer = ByteBuffer.allocate(size);
		final ArchiveHeader archiveheader = new ArchiveHeader(buffer);
		
		// read in archive header bytes
		while( buffer.hasRemaining() ) {
			if( in.read(buffer) == -1 ) {
				break;
			}
		}
		
		return archiveheader;
	}
	
	// 
	private void deserializeHashTable(final SeekableByteChannel in, final ArchiveHeader archiveheader) throws IOException, MPQException{
		// *** deserialize header
		// version 1
		long htoffset = (long) archiveheader.getHashTablePosition() & 0xFFFFFFFFL;
		final int htsize = archiveheader.getHashTableSize();
		final int rsize = htsize * HashTableEntry.STRUCT_SIZE;
		// version 2
		if( version >= 1 ){
			htoffset|= ((long) archiveheader.getHashTablePositionHigh() & 0xFFFFL) << 32;
		}
		// version 3
		final int csize;
		if( version >= 3 ){
			csize = (int) archiveheader.getHashTableSizeCompressed();
		}else{
			csize = rsize;
		}
		
		// *** validate hashtable
		// no hashtable to load
		if( htoffset == 0 ){
			hashTable = null;
			return;
		// hashtable size not power of 2
		}else if( (htsize & htsize - 1) != 0 ) {
			throw new MPQException("hashtable was not power of two ( was " + htsize + " )");
		}
		
		// *** read MPQ archive hashtable
		ByteBuffer buffer = ByteBuffer.allocate(rsize);
		buffer.limit(csize);
		in.position(htoffset + archiveOffset);
		while( buffer.hasRemaining() ) {
			if( in.read(buffer) == -1 ) {
				break;
			}
		}
		buffer.rewind();
		
		// *** decrypt hashtable
		Cryption.decryptData(buffer, buffer, Cryption.KEY_HASH_TABLE);
		
		// *** decompress hashtable
		if( csize < rsize ){
			buffer = new Compression().blockDecompressAny(buffer, ByteBuffer.allocate(rsize));
			if( buffer.limit() != buffer.capacity() ) {
				System.err.println("hashtable decompressed size did not match expected size");
			}
		}
		
		// *** deserialize hashtable
		final HashTable.Entry[] entries = new HashTable.Entry[htsize];
		final HashTableEntry htentry = new HashTableEntry();
		for( int i = 0 ; i < htsize ; i+= 1 ){
			htentry.move(buffer);
			final HashTable.Entry tempentry = new HashTable.Entry();
			tempentry.hash = htentry.getHash();
			tempentry.locale = htentry.getLocale();
			tempentry.platform = htentry.getPlatform();
			tempentry.blockIndex = htentry.getBlockIndex();
			entries[i] = tempentry;
			buffer.position(buffer.position() + HashTableEntry.STRUCT_SIZE);
		}
		hashTable = new HashTable(entries);
	}
	
	private void deserializeBlockTable(final SeekableByteChannel in, final ArchiveHeader archiveheader) throws IOException, MPQException{
		// *** deserialize header
		// version 1
		long btoffset = (long) archiveheader.getBlockTablePosition() & 0xFFFFFFFFL;
		int btsize = archiveheader.getBlockTableSize();
		final int rsize = btsize * BlockTableEntry.STRUCT_SIZE;
		// version 2
		final long hbtoffset;
		final int rhsize;
		if( version >= 1 ){
			hbtoffset = archiveheader.getHighBlockTablePosition();
			rhsize = btsize * 2;
			btoffset|= ((long) archiveheader.getBlockTablePositionHigh() & 0xFFFFL) << 32;
		}else{
			hbtoffset = 0;
			rhsize = 0;
		}
		// version 4
		final int csize;
		final int chsize;
		if( version >= 3 ){
			csize = (int) archiveheader.getBlockTableSizeCompressed();
			chsize = (int) archiveheader.getHighBlockTableSizeCompressed();
		}else{
			csize = rsize;
			chsize = rhsize;
		}
		
		// *** validate blocktable
		// no blocktable to load
		if( btoffset == 0 ){
			blockTable = null;
			return;
		// blocktable size clamp
		}else if( btsize > 1 << 20 || btsize < 0 ){
			System.err.println("blocktable is stupidly large ( " + btsize + " ) so was clamped to " + (1 << 20));
			btsize = 1 << 20;
		}
		
		// *** read MPQ archive blocktable
		ByteBuffer buffer = ByteBuffer.allocate(rsize);
		buffer.limit(csize);
		in.position(btoffset + archiveOffset);
		while( buffer.hasRemaining() ) {
			if( in.read(buffer) == -1 ) {
				break;
			}
		}
		buffer.rewind();
		
		// *** decrypt blocktable
		Cryption.decryptData(buffer, buffer, Cryption.KEY_BLOCK_TABLE);
		
		// *** decompress blocktable
		if( csize < rsize ){
			buffer = new Compression().blockDecompressAny(buffer, ByteBuffer.allocate(rsize));
			if( buffer.limit() != buffer.capacity() ) {
				System.err.println("blocktable decompressed size did not match expected size");
			}
		}
		
		// *** deserialize blocktable
		final BlockTable.Entry[] entries = new BlockTable.Entry[btsize];
		final BlockTableEntry btentry = new BlockTableEntry();
		for( int i = 0 ; i < btsize ; i+= 1 ){
			btentry.move(buffer);
			final BlockTable.Entry tempentry = new BlockTable.Entry();
			tempentry.filePosition = (long) btentry.getFilePosition() & 0xFFFFFFFFL;
			tempentry.compressedSize = btentry.getCompressedSize();
			tempentry.fileSize = btentry.getFileSize();
			tempentry.flags = btentry.getFlags();
			entries[i] = tempentry;
			buffer.position(buffer.position() + BlockTableEntry.STRUCT_SIZE);
		}
		
		// *** add high blocktable
		if( hbtoffset > 0 ){
			// read MPQ archive high blocktable
			buffer = ByteBuffer.allocate(rhsize);
			buffer.limit(chsize);
			in.position(hbtoffset + archiveOffset);
			while( buffer.hasRemaining() ) {
				if( in.read(buffer) == -1 ) {
					break;
				}
			}
			buffer.rewind();
			
			// decompress high blocktable
			if( chsize < rhsize ){
				buffer = new Compression().blockDecompressAny(buffer, ByteBuffer.allocate(rhsize));
				if( buffer.limit() != buffer.capacity() ) {
					System.err.println("high blocktable decompressed size did not match expected size");
				}
			}
			
			// deserialize high blocktable
			final short[] highposarray = RawArrays.getShortArray(buffer);
			for( int i = 0 ; i < btsize ; i+= 1 ){
				entries[i].filePosition|= ((long) highposarray[i] & 0xFFFFL) << 32;
			}
		}
		blockTable = new BlockTable(entries);
	}
	
	public void loadArchive(final SeekableByteChannel in, final boolean fold) throws IOException, MPQException{
		// *** find archive header
		final ArchiveHeader archiveheader = locateArchive(in);
		
		// *** deserialize archive globals
		archiveSize = (long) archiveheader.getArchiveSize() & 0xFFFFFFFFL;
		// force old allows support for Warcraft III archives with corrupted version field (not 0)
		if( fold ) {
			version = 0;
		} else {
			version = archiveheader.getFormatVersion();
		}
		blockShift = archiveheader.getBlockSize();
		if( version >= 2 ) {
			archiveSize = archiveheader.getArchiveSizeLong();
		}
		
		// *** deserialize archive components
		deserializeHashTable(in, archiveheader);
		deserializeBlockTable(in, archiveheader);
		if( version >= 2 ) {
			System.err.println("het and bet tables not supported");
		}
	}
	
	public MPQArchive(final SeekableByteChannel in) throws MPQException, IOException{
		loadArchive(in, false);
	}
	
	public MPQArchive(){
	}
	
	public long getArchiveOffset() {
		return archiveOffset;
	}

	public short getBlockShift() {
		return blockShift;
	}

	public short getVersion() {
		return version;
	}

	public boolean isOffsetInArchive(final long offset){
		return offset >= 0 && offset <= archiveSize;
	}
	
	public boolean isPositionInArchive(final long position){
		return isOffsetInArchive(position - archiveOffset);
	}
	
	public int lookupPath(final String path) throws MPQException{
		return hashTable.lookupBlock(new HashLookup(path));
	}
	
	public BlockTable.Entry lookupHash(final HashLookup hash) throws MPQException{
		return blockTable.lookupEntry(hashTable.lookupBlock(hash));
	}
	
	public ArchivedFile lookupHash2(final HashLookup hash) throws MPQException{
		return new ArchivedFile(this, hash, blockTable.lookupEntry(hashTable.lookupBlock(hash)));
	}
}
