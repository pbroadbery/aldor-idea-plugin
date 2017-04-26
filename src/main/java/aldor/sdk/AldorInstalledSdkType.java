package aldor.sdk;

import aldor.util.AnnotatedOptional;
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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class AldorInstalledSdkType extends SdkType implements AldorSdkType {
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<OrderRootType> applicableRootTypes = Collections.singleton(OrderRootType.SOURCES);
    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]+.[0-9]+.[0-9]+\\([0-9a-zA-z]*\\) for [^ ]+( \\(debug version\\))?");
    private final AldorVersionQuery versionQuery = new AldorVersionQuery();

    private static final AldorInstalledSdkType instance = new AldorInstalledSdkType();
    @Contract(pure = true)
    public static AldorInstalledSdkType instance() {
        return instance;
    }

    public AldorInstalledSdkType() {
        super(  "Aldor SDK");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return "/home/pab/Work/aldorgit/opt";
    }

    @Override
    public boolean isValidSdkHome(String path) {
        File file = new File(path + "/bin/aldor");
        return file.canExecute();
    }

    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        AnnotatedOptional<String, String> version = versionQuery.aldorVersion(sdkHome);
        final String newSdkName;
        if ((currentSdkName == null) || currentSdkName.isEmpty()) {
            newSdkName =  "Aldor - " + sdkHome;
        }
        else if (version.isPresent()) {
            Matcher matcher = VERSION_PATTERN.matcher(currentSdkName);
            newSdkName = matcher.replaceFirst(version.get());
        }
        else {
            newSdkName = currentSdkName;
        }
        return newSdkName;
    }

    @Override
    @Nullable
    public String getVersionString(String sdkHome){
        return versionQuery.aldorVersion(sdkHome + "/bin/aldor").orElseConstant(null);
    }

    @Nullable
    @Override
    public AldorSdkDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
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
    public boolean isRootTypeApplicable(@NotNull OrderRootType type) {
        return applicableRootTypes.contains(type);
    }

    @Override
    public void setupSdkPaths(@NotNull Sdk sdk) {
        String homePath = sdk.getHomePath();
        assert homePath != null : sdk;
        File aldorHome = new File(homePath);
        SdkModificator sdkModificator = sdk.getSdkModificator();

        setupRootType(aldorHome, sdkModificator, OrderRootType.CLASSES);
        setupRootType(aldorHome, sdkModificator, OrderRootType.SOURCES);

        sdkModificator.commitChanges();
    }

    private void setupRootType(File aldorHome, SdkModificator sdkModificator, OrderRootType rootType) {
        List<VirtualFile> sources = findSources(aldorHome);
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

    private static final List<String> LIBS = Arrays.asList("aldor", "algebra");

    private List<VirtualFile> findSources(File aldorHome) {
        Optional<VirtualFile> vf = ofNullable(LocalFileSystem.getInstance().findFileByIoFile(aldorHome));
        vf = vf.flatMap(f -> ofNullable(f.findChild("share")));
        vf = vf.flatMap(f -> ofNullable(f.findChild("aldor")));
        vf = vf.flatMap(f -> ofNullable(f.findChild("lib")));
        Optional<VirtualFile> base = vf;
        if (vf.isPresent()) {
            return LIBS.stream()
                        .map(libName -> base.flatMap(f -> ofNullable(f.findChild(libName))))
                                                      .flatMap(f -> f.map(Stream::of).orElse(Stream.empty()))
                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public String aldorPath(Sdk sdk) {
        return sdk.getHomePath() + "/bin/aldor";
    }

    @Override
    public boolean isLocalInstall() {
        return false;
    }
}
