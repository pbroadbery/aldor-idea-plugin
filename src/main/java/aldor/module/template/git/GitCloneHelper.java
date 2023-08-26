package aldor.module.template.git;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckoutProvider;
import com.intellij.openapi.vcs.VcsKey;
import git4idea.checkout.GitCheckoutProvider;
import git4idea.commands.Git;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class GitCloneHelper {

    public static boolean clone(Project project, String url, String parentDirectory, String directoryName) {
        System.out.println("Clone " + url + " " + parentDirectory + " " + directoryName);
        return GitCheckoutProvider.doClone(project, Git.getInstance(), directoryName, parentDirectory, url);
    }

    static class Listener implements CheckoutProvider.Listener {
        private final CompletableFuture<String> fut;

        public Listener(CompletableFuture<String> fut) {
            this.fut = fut;
        }

        @Override
        public void directoryCheckedOut(File directory, VcsKey vcs) {
            System.out.println("Checking out " + directory + " " + vcs);
        }

        @Override
        public void checkoutCompleted() {
            System.out.println("Done");
            fut.complete("done");
        }
    }

}
