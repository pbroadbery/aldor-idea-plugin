package aldor.runconfiguration.aldor;

import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDefineNameIndex;
import aldor.psi.index.AldorDefineTopLevelIndex;
import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AldorTestLocator implements SMTestLocator {
    public static final AldorTestLocator INSTANCE = new AldorTestLocator();
    private static final Logger LOG = Logger.getInstance(AldorTestLocator.class);
    private static final String JAVA_TEST_PROTOCOL = "java:test";

    @SuppressWarnings("rawtypes")
    @NotNull
    @Override
    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        //java:test aldor.test.MyOtherTestSuite/testOne
        LOG.info("Looking for " + protocol + " " + path);

        if (!protocol.equals(JAVA_TEST_PROTOCOL)) {
            return Collections.emptyList();
        }

        int slash = path.indexOf('/');
        if (slash == -1) {
            return Collections.emptyList();
        }

        String className = path.substring(0, slash);
        String domainName = className.substring(className.lastIndexOf('.') + 1);
        String methodName = path.substring(slash + 1);

        Collection<AldorDefine> domains = AldorDefineTopLevelIndex.instance.get(domainName, project, scope);

        List<Location> locations = new ArrayList<>();
        for (AldorDefine domain : domains) {
            Collection<AldorDefine> methods = AldorDefineNameIndex.instance.get(methodName, project, GlobalSearchScope.fileScope(domain.getContainingFile()));

            for (AldorDefine method : methods) {
                if (PsiTreeUtil.isAncestor(domain, method, true)) {
                    locations.add(new AldorTestMethodLocation(domain, method));
                }

            }

        }
        return locations;
    }
}