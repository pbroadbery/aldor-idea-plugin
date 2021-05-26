package aldor.util;

import com.intellij.jarRepository.JarRepositoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.ui.OrderRoot;
import org.jetbrains.idea.maven.utils.library.RepositoryLibraryProperties;

import java.util.Collection;

public final class  Mavens {
    public static final String JUNIT_VERSION = "4.12";

    public static Collection<OrderRoot> downloadDependenciesWhenRequired(Project project,
                                                                         RepositoryLibraryProperties properties) throws MavenDownloadException {
        Collection<OrderRoot> roots =
                JarRepositoryManager.loadDependenciesModal(project, properties, false, false, null, null);
        if (roots.isEmpty()) {
            throw new MavenDownloadException("Failed to resolve " + properties.getMavenId());
        }
        return roots;
    }

    @SuppressWarnings("serial")
    public static class MavenDownloadException extends Exception {
        MavenDownloadException(String message) {
            super(message);
        }
    }

}
