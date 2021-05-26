package aldor.build.module;

import aldor.build.facet.SpadFacet;
import aldor.build.facet.aldor.AldorFacet;
import aldor.build.facet.fricas.FricasFacet;
import aldor.builder.jps.SpadFacetProperties;
import aldor.builder.jps.module.JpsAldorModuleType;
import aldor.file.AldorFileType;
import aldor.file.SpadFileType;
import aldor.file.SpadInputFileType;
import com.intellij.ide.util.projectWizard.EmptyModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
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
 *
 * Note: Try to avoid using this class; module characteristics should be defined by facets
 */
public class AldorModuleType extends ModuleType<ModuleBuilder> {
    public static final String NAME = "Aldor Module";
    public static final String ID = JpsAldorModuleType.ID;

    public static AldorModuleType instance() {
        return (AldorModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    public AldorModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
        return new EmptyModuleBuilder() {
            @Override
            public boolean isAvailable() {
                return false;
            }
        };
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

    @NotNull
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

    // TODO: This should be by language - file type is a bit odd
    @Nullable
    public static SpadFacet<? extends SpadFacetProperties> facetModuleType(Module module, FileType fileType) {
        if (fileType.equals(AldorFileType.INSTANCE)) {
            return AldorFacet.forModule(module);
        }
        else if (fileType.equals(SpadFileType.INSTANCE)) {
            return FricasFacet.forModule(module);
        }
        else if (fileType.equals(SpadInputFileType.INSTANCE)) {
            return FricasFacet.forModule(module);
        }
        return null;
    }
}
