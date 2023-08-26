package aldor.builder.files;

import aldor.make.MakeOption;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompileRunner {
    String id();

    default boolean compileOneFile(@NotNull File buildDirectory, @NotNull String targetName) {
        return compileOneFile(buildDirectory, targetName, Collections.emptySet());
    }

    boolean compileOneFile(@NotNull File buildDirectory, @NotNull String targetName, Set<MakeOption> options);

    boolean runAutogen(@NotNull File buildDirectory, @NotNull String targetName);
    boolean runConfigure(@NotNull File buildDirectory, File sourceDirectory, String targetName, List<String> args);

    static CompileRunner logged(@NotNull CompileRunner c) {
        return new Logged(c);
    }

    @SuppressWarnings({"OverlyBroadCatchBlock", "ProhibitedExceptionThrown"})
    class Logged implements CompileRunner {
        private static final Logger LOG = Logger.getInstance(Logged.class);

        private final CompileRunner delegate;

        public Logged(@NotNull CompileRunner delegate) {
            this.delegate = delegate;
        }

        @Override
        public String id() {
            return delegate.id();
        }

        @Override
        public boolean compileOneFile(File buildDirectory, String targetName, Set<MakeOption> options) {
            try {
                LOG.info("Starting compile " + this.id() + ": " + buildDirectory + " target: " + targetName + (options.isEmpty()? "" : " options: " + options));
                return delegate.compileOneFile(buildDirectory, targetName, options);
            }
            catch (Exception e) {
                LOG.error("Failed " + this.id());
                throw e;
            }
            finally {
                LOG.info("Finished compilation");
            }
        }

        @Override
        public boolean runAutogen(@NotNull File buildDirectory, @NotNull String targetName) {
            try {
                LOG.info("Starting autogen " + this.id() + ": " + buildDirectory + " target: " + targetName);
                return delegate.runAutogen(buildDirectory, targetName);
            }
            catch (Exception e) {
                LOG.error("Failed " + this.id());
                LOG.error(e);
                throw e;
            }
            finally {
                LOG.info("Finished compilation");
            }
        }

        @Override
        public boolean runConfigure(@NotNull File buildDirectory, File sourceDirectory, String targetName, List<String> args) {
            try {
                LOG.info("Starting configure " + this.id() + ": " + buildDirectory + " target: " + targetName + " " + args);
                return delegate.runConfigure(buildDirectory, sourceDirectory, targetName, args);
            }
            catch (Exception e) {
                LOG.error("Failed " + this.id());
                LOG.error(e);
                throw e;
            }
            finally {
                LOG.info("Finished compilation");
            }
        }
    }

}
