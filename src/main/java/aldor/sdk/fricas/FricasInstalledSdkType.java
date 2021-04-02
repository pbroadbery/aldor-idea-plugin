
package aldor.sdk.fricas;

import aldor.sdk.AxiomInstalledSdkType;
import aldor.sdk.aldor.AldorVersionQuery;
import com.google.common.collect.Streams;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FricasInstalledSdkType extends SdkType implements FricasSdkType, AxiomInstalledSdkType {
    private static final String[] homeBasePaths = {
            "/home/pab/Work/fricas/opt/lib/fricas/target/",
            "/usr/local/lib/fricas/target/",
            "/usr/lib/fricas/target/",
            "/opt/lib/fricas/target/"
    };

    private static final String[] directPaths = {
            "C:/Fricas/"
    };

    private static final Logger LOG = Logger.getInstance(FricasInstalledSdkType.class);
    private final AldorVersionQuery versionQuery = new AldorVersionQuery();
    private static final Set<OrderRootType> applicableRootTypes = Collections.singleton(OrderRootType.SOURCES);

    private static final FricasInstalledSdkType instance = new FricasInstalledSdkType();

    @Contract(pure = true)
    public static FricasInstalledSdkType instance() {
        return instance;
    }

    public FricasInstalledSdkType() {
        super("Fricas SDK");
    }

    @Override
    public boolean isRootTypeApplicable(@NotNull OrderRootType type) {
        return applicableRootTypes.contains(type);
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return suggestHomePathsStream().findFirst().orElse(null);
    }

    @Override
    public @NotNull Collection<String> suggestHomePaths() {
        return Streams.concat(suggestHomePathsStream(),
                             suggestDirectPaths()).collect(Collectors.toList());
    }

    private Stream<String> suggestDirectPaths() {
        Predicate<String> isValidSdkHome = subpath -> isValidSdkHome(new File(subpath).getPath());
        return Arrays.stream(directPaths).filter(isValidSdkHome);
    }

    private @NotNull Stream<String> suggestHomePathsStream() {
        Predicate<File> isValidSdkHome = subpath -> isValidSdkHome(subpath.getPath());
        return Arrays.stream(homeBasePaths).map(File::new).filter(File::exists).map(File::listFiles).flatMap(Arrays::stream)
                .filter(isValidSdkHome)
                .map(File::getAbsolutePath);
    }

    @Override
    public @Nullable String fricasPath(Sdk sdk) {
        return versionQuery.fricasPath(sdk.getHomePath());
    }

    @Override
    @Nonnull
    public String fricasSysName(Sdk sdk) {
        return "FRICASsys";
    }

    @Override
    public String fricasEnvVar() {
        return "FRICAS";
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return versionQuery.fricasVersion(path).isPresent();
    }

    @Override
    public @NotNull String suggestSdkName(String currentSdkName, String sdkHome) {
        return "Fricas - " + sdkHome;
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        return versionQuery.fricasVersion(sdkHome).orElse(msg -> "[unable to determine version - " + msg +"]");
    }

    @Nullable
    @Override
    public FricasSdkDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return new FricasSdkDataConfigurable();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return getName();
    }

    @Override
    public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element additional) {

    }

    @Override
    public void setupSdkPaths(@NotNull Sdk sdk) {
        String homePath = sdk.getHomePath();
        assert homePath != null : sdk;
        File fricasHome = new File(homePath);
        SdkModificator sdkModificator = sdk.getSdkModificator();

        setupRootType(fricasHome, sdkModificator, OrderRootType.CLASSES);
        setupRootType(fricasHome, sdkModificator, OrderRootType.SOURCES);

        sdkModificator.commitChanges();
    }

    private void setupRootType(File fricasHome, SdkModificator sdkModificator, OrderRootType rootType) {
        LOG.info("Setting up root type for " + sdkModificator.getName() + " " + fricasHome + " " + rootType);
        List<VirtualFile> sources = findSources(fricasHome);
        Collection<VirtualFile> previousRoots = new LinkedHashSet<>(Arrays.asList(sdkModificator.getRoots(rootType)));
        sdkModificator.removeRoots(rootType);
        previousRoots.removeAll(new HashSet<>(sources));
        for (VirtualFile aClass : sources) {
            sdkModificator.addRoot(aClass, rootType);
        }
        for (VirtualFile root : previousRoots) {
            sdkModificator.addRoot(root, rootType);
        }
        LOG.info("finished root type " + sdkModificator.getName() + " " + rootType);
    }

    @NotNull
    private List<VirtualFile> findSources(File fricasHome) {
        // FIXME: Can do better than this
        File algebraPath = new File(fricasHome.getAbsolutePath() + "/src");
        VirtualFile root = LocalFileSystem.getInstance().findFileByPath(algebraPath.getAbsolutePath());
        return Collections.singletonList(root);
    }

    @Override
    public boolean isLocalInstall() {
        return false;
    }

    @Override
    public @NotNull String librarySuffix() {
        return " (interpreter)";
    }

}
