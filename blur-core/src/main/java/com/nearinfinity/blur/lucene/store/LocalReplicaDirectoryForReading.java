package com.nearinfinity.blur.lucene.store;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LockFactory;

public class LocalReplicaDirectoryForReading extends Directory implements Closeable {

    private static final Log LOG = LogFactory.getLog(LocalReplicaDirectoryForReading.class);

    private static final int BUFFER_SIZE = 16384;

    private Directory remoteDirectory;
    private Directory localDirectory;

    public LocalReplicaDirectoryForReading(Directory localDirectory, Directory remoteDirectory, LockFactory lockFactory) throws IOException {
        this.localDirectory = localDirectory;
        this.remoteDirectory = remoteDirectory;
        setLockFactory(lockFactory);
    }

    @Override
    public void close() throws IOException {
        safeClose(localDirectory);
        safeClose(remoteDirectory);
    }

    @Override
    public IndexInput openInput(String name) throws IOException {
        return localDirectory.openInput(name);
    }

    @Override
    public IndexOutput createOutput(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteFile(String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        return remoteDirectory.fileExists(name);
    }

    @Override
    public long fileLength(String name) throws IOException {
        return remoteDirectory.fileLength(name);
    }

    @Override
    public long fileModified(String name) throws IOException {
        return remoteDirectory.fileModified(name);
    }

    @Override
    public String[] listAll() throws IOException {
        return remoteDirectory.listAll();
    }

    @Override
    public void touchFile(String name) throws IOException {
        // do nothing
    }

    @Override
    public String getLockID() {
        return remoteDirectory.getLockID();
    }

    @Override
    public void sync(String name) throws IOException {
        LOG.info("sync [" + name + "]");
        if (!remoteDirectory.fileExists(name)) {
            throw new IOException(name);
        }
        if (localDirectory.fileExists(name)) {
            if (localDirectory.fileLength(name) != remoteDirectory.fileLength(name)) {
                localDirectory.deleteFile(name);
            } else {
                LOG.info("Skipping sync [" + name + "]");
                return;
            }
        }
        copyFile(name,remoteDirectory,localDirectory);
    }

    private void copyFile(String name, Directory src, Directory dest) throws IOException {
        IndexOutput os = null;
        IndexInput is = null;
        byte[] buf = new byte[BUFFER_SIZE];
        try {
            os = dest.createOutput(name);
            is = src.openInput(name);
            long len = is.length();
            long readCount = 0;
            while (readCount < len) {
                int toRead = readCount + BUFFER_SIZE > len ? (int) (len - readCount) : BUFFER_SIZE;
                is.readBytes(buf, 0, toRead);
                os.writeBytes(buf, toRead);
                readCount += toRead;
            }
        } finally {
            try {
                if (os != null)
                    os.close();
            } finally {
                if (is != null)
                    is.close();
            }
        }
    }

    private static void safeClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            LOG.error("Error closing [" + closeable + "]", e);
        }
    }
}