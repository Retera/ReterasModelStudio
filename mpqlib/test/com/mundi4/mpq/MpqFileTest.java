package com.mundi4.mpq;

import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class MpqFileTest {

    int nThreads = 5;

    String filename = "C:\\Program Files (x86)\\Warcraft III\\war3.mpq";

    @Test
    public void testReadFilesUsingThreads() throws IOException {
        System.gc();
        final long start = System.currentTimeMillis();
        final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        final MpqFile mpq = new MpqFile(filename);
        final Iterator<MpqEntry> iter = mpq.iterator();
        while (iter.hasNext()) {
            final MpqEntry entry = iter.next();
            executor.execute(new ReadFileCommand(mpq, entry));
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } finally {
            try {
                mpq.close();
            } catch (final Exception e2) {
            }
        }
        final long end = System.currentTimeMillis();
        System.out.println("elapsed(using thread):" + (end - start));
    }

    @Test
    public void testReadFiles() throws IOException {
        System.gc();
        final long start = System.currentTimeMillis();
        final MpqFile mpq = new MpqFile(filename);
        try {
            final Iterator<MpqEntry> iter = mpq.iterator();
            while (iter.hasNext()) {
                final MpqEntry entry = iter.next();
                readFile(mpq, entry);
            }
        } finally {
            try {
                mpq.close();
            } catch (final Exception e2) {
            }
        }
        final long end = System.currentTimeMillis();
        System.out.println("elapsed:" + (end - start));
    }

    private void readFile(final MpqFile mpq, final MpqEntry entry) {
        InputStream is = null;
        try {
            is = mpq.getInputStream(entry);
            int read = 0;
            while (skipOrRead(is)) {
                read++;
            }
        } catch (final Exception e) {
            fail(e.toString());
        } finally {
            try {
                is.close();
            } catch (final Exception e) {
            }
        }
    }

    private class ReadFileCommand implements Runnable {

        MpqFile mpq;
        MpqEntry entry;

        public ReadFileCommand(final MpqFile mpq, final MpqEntry entry) {
            this.mpq = mpq;
            this.entry = entry;
        }

        @Override
        public void run() {
            readFile(mpq, entry);
        }
    }

    private final Random random = new Random(System.currentTimeMillis());

    private boolean skipOrRead(final InputStream in) throws IOException {
        if (random.nextBoolean()) {
            final long skipped = in.skip(1);
            return skipped == 1;
        } else {
            return in.read() != -1;
        }
    }

}
