package aldor.build.module;

import aldor.build.facet.SpadFacet;
import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.fricas.FricasFacet;
import aldor.builder.jps.SpadFacetProperties;
import aldor.file.AldorFileType;
import aldor.file.SpadFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.Icon;

/**
 * Represents an aldor module.
 */
public class AldorModuleType extends ModuleType<AldorModuleBuilder> {
    public static final String NAME = "Aldor Module";
    public static final String ID = "ALDOR-MODULE";

    public static AldorModuleType instance() {
        return (AldorModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    public AldorModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public AldorModuleBuilder createModuleBuilder() {
        return new AldorModuleBuilder(this);
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Module containing spad/aldor sources";
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return AldorIcons.MODULE;
    }

    @Override
    public boolean isSupportedRootType(JpsModuleSourceRootType type) {
        return true;
    }

    public boolean is(Module module) {
        return ModuleType.is(module, this);
    }

    public @Nullable SpadFacet<? extends SpadFacetProperties> facetModuleType(Module module, FileType fileType) {
        if (fileType.equals(AldorFileType.INSTANCE)) {
            return AldorFacet.forModule(module);
        }
        else if (fileType.equals(SpadFileType.INSTANCE)) {
            return FricasFacet.forModule(module);
        }
        return null;
    }
}
