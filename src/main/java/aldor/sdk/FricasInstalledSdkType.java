package aldor.sdk;

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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FricasInstalledSdkType extends SdkType implements FricasSdkType {
    private final AldorVersionQuery versionQuery = new AldorVersionQuery();
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<OrderRootType> applicableRootTypes = Collections.singleton(OrderRootType.SOURCES);

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
        return "/home/pab/Work/fricas/opt/lib/fricas/target/x86_64-unknown-linux";
    }

    @Override
    public boolean isValidSdkHome(String path) {
        return versionQuery.fricasVersion(path + "/bin/fricas").isPresent();
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        return "Fricas - " + sdkHome;
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        return versionQuery.fricasVersion(sdkHome+"/bin/fricas").orElse(msg -> "[unable to determine version - " + msg +"]");
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
    }

    @NotNull
    private List<VirtualFile> findSources(File fricasHome) {
        // FIXME: Can do better than this
        File algebraPath = new File(fricasHome.getAbsolutePath() + "/src");
        VirtualFile root = LocalFileSystem.getInstance().findFileByPath(algebraPath.getAbsolutePath());
        return Collections.singletonList(root);
    }

    @Nullable
    @Override
    public String fricasPath(Sdk sdk) {
        return sdk.getHomePath() + "/bin/fricas";
    }

    @Override
    public boolean isLocalInstall() {
        return false;
    }
}
