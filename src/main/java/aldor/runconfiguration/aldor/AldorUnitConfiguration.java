package aldor.runconfiguration.aldor;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AldorModuleType;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.psi.index.AldorDefineTopLevelIndex;
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AldorUnitConfiguration extends ModuleBasedConfiguration<AldorRunConfigurationModule, Element>
                    implements SMRunnerConsolePropertiesProvider {
    private static final Logger LOG = Logger.getInstance(AldorUnitConfiguration.class);

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
        return bean.typeName + " (" + new File(bean.inputFile).getName() + ")";
    }

    @Override
    public boolean isGeneratedName() {
        return bean.isGeneratedName;
    }

    // FIXME: This should return all modules that make sense for this configuration
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
            other.packageName = packageName;
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
        @Nullable
        public PsiFile getPsiElement() {
            final String qualifiedName = bean.inputFile;
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + qualifiedName);
            if (file == null) {
                return null;
            }
            PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
            if (psiFile == null) {
                return null;
            }
            LOG.info("PsiElement for AldorUnit: " + getName() + " --> " + psiFile.getVirtualFile().getPath());
            return psiFile;
        }

        @Override
        public void setPsiElement(final PsiFile psiFile) {
            setName(psiFile.getVirtualFile().getPath());
        }
    };

    final RefactoringListeners.Accessor<AldorIdentifier> myTypeName = new RefactoringListeners.Accessor<AldorIdentifier>() {
        @Override
        public void setName(final String name) {
            final boolean generatedName = isGeneratedName();
            bean.typeName = name;
            if (generatedName)
                setGeneratedName();
        }

        @Override
        public AldorIdentifier getPsiElement() {
            final String qualifiedName = bean.inputFile;
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(qualifiedName);
            if (file == null) {
                return null;
            }
            PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
            if (psiFile == null) {
                return null;
            }
            Collection<AldorDefine> def = AldorDefineTopLevelIndex.instance.get(bean.typeName, getProject(), GlobalSearchScope.fileScope(getProject(), file));
            return def.stream().findFirst().flatMap(AldorDefine::defineIdentifier).orElse(null); // FIXME: Need to find the file PSI element for this configuration
        }

        @Override
        public void setPsiElement(final AldorIdentifier id) {
            setName(id.getName());
        }
    };
}
