package aldor.runconfiguration.aldor;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AldorModuleType;
import aldor.psi.AldorId;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.junit.RefactoringListeners;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.sm.runner.SMRunnerConsolePropertiesProvider;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.psi.PsiFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AldorUnitConfiguration extends ModuleBasedConfiguration<AldorRunConfigurationModule, Element>
                    implements SMRunnerConsolePropertiesProvider {
    private final Bean bean = new Bean();

    public AldorUnitConfiguration(@NotNull AldorRunConfigurationModule configurationModule, @NotNull ConfigurationFactory factory) {
        super(configurationModule, factory);
    }

    public Bean bean() {
        return bean;
    }

    @Nullable
    @Override
    public String suggestedName() {
        return bean.typeName + " (" + bean.inputFile+ ")";
    }

    @Override
    public boolean isGeneratedName() {
        return bean.isGeneratedName;
    }

    @Override
    public Collection<Module> getValidModules() {
        AldorModuleManager.getInstance(getProject()).aldorModules(getProject());
        return Arrays.stream(ModuleManager.getInstance(getProject()).getModules())
                .filter(module -> AldorModuleType.instance().is(module))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<AldorUnitConfiguration> group = new SettingsEditorGroup<>();

        group.addEditor("Run details", new AldorUnitConfigurable(getProject()));

        return group;
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new AldorUnitRunnableState(this, environment);
    }

    @Override
    public SMTRunnerConsoleProperties createTestConsoleProperties(Executor executor) {
        return new AldorUnitRunnerConsoleProperties(this, executor);
    }

    public void inputFile(String path) {
        this.bean.inputFile = path;
    }

    public String inputFile() {
        return bean.inputFile;
    }

    public void setTypeName(String text) {
        this.bean.typeName = text;
    }

    public void setSuggestedName() {
        this.setName(suggestedName());
        bean.isGeneratedName = true;
    }

    public String javaClass() {
        return bean().packageName + "." + bean().typeName;
    }

    static class Bean {
        public String inputFile = "";
        public String typeName = "";
        public String packageName = "aldor.test"; // TODO: Discover package name
        public boolean isGeneratedName = false;

        void copyInto(Bean other) {
            other.inputFile = inputFile;
            other.typeName = typeName;

        }
    }


    // TODO: Wire these up properly - com.intellij.refactoring.elementListenerProvider
    final RefactoringListeners.Accessor<PsiFile> myInputFile = new RefactoringListeners.Accessor<PsiFile>() {
        @Override
        public void setName(final String name) {
            final boolean generatedName = isGeneratedName();
            bean.inputFile = name;
            if (generatedName)
                setGeneratedName();
        }

        @Override
        public PsiFile getPsiElement() {
            final String qualifiedName = bean.inputFile;
            return null; // FIXME: Need to find the file PSI element for this configuration
        }

        @Override
        public void setPsiElement(final PsiFile psiFile) {
            setName(psiFile.getName());
        }
    };

    final RefactoringListeners.Accessor<AldorId> myTypeName = new RefactoringListeners.Accessor<AldorId>() {
        @Override
        public void setName(final String name) {
            final boolean generatedName = isGeneratedName();
            bean.typeName = name;
            if (generatedName)
                setGeneratedName();
        }

        @Override
        public AldorId getPsiElement() {
            final String qualifiedName = bean.typeName;
            return null; // FIXME: Need to find the definition PSI element for this configuration
        }

        @Override
        public void setPsiElement(final AldorId id) {
            setName(id.getName());
        }
    };
}
