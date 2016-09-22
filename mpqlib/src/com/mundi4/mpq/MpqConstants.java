package com.mundi4.mpq;

public interface MpqConstants {

    /*
     * Signature
     */
    int MPQSIG = 0x1a51504d;

    /*
     * Header sizes in bytes (including signatures)
     */
    int HEADER = 32;
    int EXTENDED_HEADER = 16;
    int HASH_TABLE = 16;
    int BLOCK_TABLE = 16;

    /*
     * Encryption keys
     */
    String HASH_TABLE_KEY = "(hash table)";
    String BLOCK_TABLE_KEY = "(block table)";

    /*
     * Internal list file name
     */
    String INTERNAL_LIST_FILE = "(listfile)";

    /*
     * MPQ file flags
     */
    int IMPLODED = 0x100;
    int COMPRESSED = 0x00000200;
    int ENCRYPTED = 0x00010000;
    int FIXSEED = 0x00020000;
    int SINGLE_UNIT = 0x01000000;
    int DUMMY_FILE = 0x02000000;
    int HAS_EXTRA = 0x04000000;
    int EXISTS = 0x80000000;

}
