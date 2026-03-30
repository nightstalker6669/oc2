/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.vm.fs;

import li.cil.sedna.fs.*;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LayeredFileSystem implements FileSystem {
    private final ArrayList<FileSystem> fileSystems = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////

    public void addLayer(final FileSystem fileSystem) {
        fileSystems.add(0, fileSystem);
    }

    public void clear() {
        fileSystems.clear();
    }

    @Override
    public FileSystemStats statfs() throws IOException {
        final FileSystemStats result = new FileSystemStats();
        for (final FileSystem fileSystem : fileSystems) {
            final FileSystemStats stats = fileSystem.statfs();
            result.blockCount += stats.blockCount; // not correct if blocksize differs, but whatever
            result.freeBlockCount += stats.freeBlockCount;
            result.availableBlockCount += stats.availableBlockCount;
            result.fileCount += stats.fileCount;
            result.freeFileCount += stats.freeFileCount;
        }
        return result;
    }

    @Override
    public long getUniqueId(@Nullable final Path path) throws IOException {
        if (path == null) {
            throw new FileNotFoundException();
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return fileSystem.getUniqueId(path);
            }
        }

        throw new FileNotFoundException();
    }

    @Override
    public boolean exists(@Nullable final Path path) {
        if (path == null) {
            return false;
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isDirectory(@Nullable final Path path) {
        if (path == null) {
            return false;
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return fileSystem.isDirectory(path);
            }
        }

        return false;
    }

    @Override
    public boolean isWritable(@Nullable final Path path) {
        return false;
    }

    @Override
    public boolean isReadable(@Nullable final Path path) {
        if (path == null) {
            return false;
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return fileSystem.isReadable(path);
            }
        }

        return false;
    }

    @Override
    public boolean isExecutable(@Nullable final Path path) {
        if (path == null) {
            return false;
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return fileSystem.isExecutable(path);
            }
        }

        return false;
    }

    @Override
    public BasicFileAttributes getAttributes(@Nullable final Path path) throws IOException {
        if (path == null) {
            throw new FileNotFoundException();
        }
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.exists(path)) {
                return fileSystem.getAttributes(path);
            }
        }

        throw new FileNotFoundException();
    }

    @Override
    public void mkdir(@Nullable final Path path) throws IOException {
        throw new IOException();
    }

    @Override
    public FileHandle open(@Nullable final Path path, final int flags) throws IOException {
        if (path == null) {
            throw new FileNotFoundException();
        }
        if ((flags & FileMode.WRITE) != 0) {
            throw new IOException();
        }

        if (isDirectory(path)) {
            final ArrayList<FileHandle> fileHandles = new ArrayList<>();
            for (final FileSystem fileSystem : fileSystems) {
                if (fileSystem.isDirectory(path)) {
                    fileHandles.add(fileSystem.open(path, flags));
                }
            }
            return new LayeredDirectoryFileHandle(fileHandles);
        } else {
            for (final FileSystem fileSystem : fileSystems) {
                if (fileSystem.exists(path)) {
                    return fileSystem.open(path, flags);
                }
            }
        }

        throw new FileNotFoundException();
    }

    @Override
    public FileHandle create(@Nullable final Path path, final int flags) throws IOException {
        throw new IOException();
    }

    @Override
    public void unlink(@Nullable final Path path) throws IOException {
        throw new IOException();
    }

    @Override
    public void rename(@Nullable final Path oldPath, @Nullable final Path newPath) throws IOException {
        throw new IOException();
    }

    ///////////////////////////////////////////////////////////////////

    private static final class LayeredDirectoryFileHandle implements FileHandle {
        private final ArrayList<DirectoryEntry> entries = new ArrayList<>();

        public LayeredDirectoryFileHandle(final ArrayList<FileHandle> fileHandles) {
            for (final FileHandle fileHandle : fileHandles) {
                try {
                    final List<DirectoryEntry> layer = fileHandle.readdir();
                    for (final DirectoryEntry entry : layer) {
                        if (entries.stream().noneMatch(e -> Objects.equals(e.name, entry.name))) {
                            entries.add(entry);
                        }
                    }
                } catch (final IOException ignored) {
                }
            }
        }

        @Override
        public int read(final long offset, @Nullable final ByteBuffer buffer) throws IOException {
            throw new IOException();
        }

        @Override
        public int write(final long offset, @Nullable final ByteBuffer buffer) throws IOException {
            throw new IOException();
        }

        @Override
        public List<DirectoryEntry> readdir() {
            return entries;
        }

        @Override
        public void close() {
        }
    }
}
