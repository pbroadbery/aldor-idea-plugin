package aldor.build.module;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Represents an aldor module.
 */
public class AldorModuleType extends ModuleType<ModuleBuilder> {
    public static final String NAME = "Aldor Module";
    public static final String ID = "ALDOR-MODULE";
    private static final AldorModuleType instance = new AldorModuleType();

    public AldorModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
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
        return "Module containing aldor sources";
    }

    @Override
    public Icon getBigIcon() {
        return AllIcons.FileTypes.Custom;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return AllIcons.FileTypes.Custom;
    }

    public static ModuleType<ModuleBuilder> instance() {
        return instance;
    }

    public static class AldorModuleBuilder extends ModuleBuilder {

        private final ModuleType<?> type;

        AldorModuleBuilder(ModuleType<?> type) {
            this.type = type;
        }

        @Override
        public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
            doAddContentEntry(modifiableRootModel);
        }


        @Override
        public ModuleType<?> getModuleType() {
            return type;
        }
    }
}
