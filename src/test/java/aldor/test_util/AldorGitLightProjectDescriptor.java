package aldor.test_util;

import aldor.module.template.git.GitCloneHelper;
import com.intellij.ide.util.importProject.DetectedRootData;
import com.intellij.ide.util.importProject.RootDetectionProcessor;
import com.intellij.ide.util.projectWizard.AbstractStepWithProgress;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.ide.util.projectWizard.importSources.impl.ProjectFromSourcesBuilderImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.ui.EmptyIcon;

import java.io.File;
import java.util.List;

public class AldorGitLightProjectDescriptor extends LightProjectDescriptor {

    private final ProjectFromSourcesBuilderImpl myBuilder;
    private final File myRootDir;

    AldorGitLightProjectDescriptor(File rootDir) {
        myBuilder = new ProjectFromSourcesBuilderImpl(new WizardContext(null, getTestRootDisposable()), ModulesProvider.EMPTY_MODULES_PROVIDER);
        myRootDir = rootDir;
    }

    void clone(Project project, String url, VirtualFile destination, String directoryName) {
        GitCloneHelper.clone(project, url, destination.getPath(), directoryName);
    }

    private Disposable getTestRootDisposable() {
        return null;
    }

    private void importFromSources(Project project, File dir) {
        myBuilder.setBaseProjectPath(dir.getAbsolutePath());
        List<DetectedRootData> list = RootDetectionProcessor.detectRoots(dir);
        MultiMap<ProjectStructureDetector, DetectedProjectRoot> map = RootDetectionProcessor.createRootsMap(list);
        myBuilder.setupProjectStructure(map);
        for (ProjectStructureDetector detector : map.keySet()) {
            List<ModuleWizardStep> steps = detector.createWizardSteps(myBuilder, myBuilder.getProjectDescriptor(detector), EmptyIcon.ICON_16);
            try {
                for (ModuleWizardStep step : steps) {
                    if (step instanceof AbstractStepWithProgress<?>) {
                        performStep((AbstractStepWithProgress<?>)step);
                    }
                }
            }
            finally {
                for (ModuleWizardStep step : steps) {
                    step.disposeUIResources();
                }
            }
        }
        myBuilder.commit(project, null, ModulesProvider.EMPTY_MODULES_PROVIDER);
    }

    private static <Result> void performStep(AbstractStepWithProgress<Result> step) {
        step.performStep();
    }

}
