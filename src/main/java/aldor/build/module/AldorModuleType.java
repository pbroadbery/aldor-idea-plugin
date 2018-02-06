package aldor.build.module;

import aldor.ui.AldorIcons;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.Icon;

/**
 * Represents an aldor module.
 */
public class AldorModuleType extends ModuleType<AldorModuleBuilder> {
    public static final String NAME = "Aldor Module";
    public static final String ID = "ALDOR-MODULE";

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

    public static AldorModuleType instance() {
        return (AldorModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @Override
    public boolean isSupportedRootType(@SuppressWarnings("rawtypes") JpsModuleSourceRootType type) {
        return type == JavaSourceRootType.SOURCE;
    }

}
