package aldor.build.module;

import com.intellij.openapi.project.Project;

import java.util.function.Function;

public class AnnotationFileNavigatorManager {
    public static AnnotationFileNavigatorManager instance = new AnnotationFileNavigatorManager();

    private Function<Project, AnnotationFileNavigator> getManager = null;

    public AnnotationFileNavigator getInstance(Project project) {
        return getManager.apply(project);
    }

    public void registerAnnotationNavigator(Function<Project, AnnotationFileNavigator> fn) {
        this.getManager = fn;
    }

    public static void register(Function<Project, AnnotationFileNavigator> fn) {
        instance().registerAnnotationNavigator(fn);
    }

    public static AnnotationFileNavigatorManager instance() {
        return instance;
    }

}
