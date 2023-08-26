package aldor.builder.jps.autoconf;

import aldor.builder.jps.autoconf.descriptors.BuildInstanceModelImpl;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class BuildInstanceModelImplTest {

    @Test
    public void test1() {
        BasicFileAttributes mock = new FakeFileAttributes();
        Assert.assertTrue(BuildInstanceModelImpl.isAldorFile(Path.of("/foo/bar/file.as"), mock));
    }

    private class FakeFileAttributes implements BasicFileAttributes {
        @Override
        public FileTime lastModifiedTime() {
            return null;
        }

        @Override
        public FileTime lastAccessTime() {
            return FileTime.from(Instant.now());
        }

        @Override
        public FileTime creationTime() {
            return null;
        }

        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }
}