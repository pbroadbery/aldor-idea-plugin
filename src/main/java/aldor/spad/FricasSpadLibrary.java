package aldor.spad;

import aldor.syntax.AnnotatedSyntax;
import aldor.syntax.Syntax;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldor.typelib.SymbolDatabase;
import aldor.typelib.SymbolMeaning;
import aldor.typelib.TForm;
import aldor.typelib.TypePackage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static aldor.syntax.AnnotatedSyntax.fromSyntax;

public class FricasSpadLibrary implements SpadLibrary, Disposable {
    private final String directory;
    private final AxiomInterface iface;
    private final GlobalSearchScope scope;
    private final AldorExecutor aldorExecutor;

    public FricasSpadLibrary(Project project, VirtualFile directory) {
        this.directory = directory.getCanonicalPath() + "/algebra";
        scope = GlobalSearchScopesCore.directoriesScope(project, true, directory);

        aldorExecutor = new AldorExecutor();
        try {
            iface = aldorExecutor.compute(this::initExecutor);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private AxiomInterface initExecutor() {
        Clos fn = aldorExecutor.createLoadFn("axiomshell");
        fn.call();
        return AxiomInterface.create(SymbolDatabase.daases(directory));
    }

    @Override
    public List<Syntax> parentCategories(@NotNull Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doParentCategories(syntax));
        } catch (InterruptedException e) {
            throw new RuntimeException("failed to create library: " + directory, e);
        }
    }

    private List<Syntax> doParentCategories(@NotNull Syntax syntax) {
        Env env = iface.env();
        AnnotatedAbSyn absyn = fromSyntax(env, syntax);
        TForm tf = iface.asTForm(absyn);
        Collection<TForm> parents = iface.directParents(tf);
        return parents.stream()
                .map(ptf -> TypePackage.asAbSyn(iface.env(), ptf))
                .map(ab -> AnnotatedSyntax.toSyntax(scope, ab))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> operations(Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doOperations(syntax));
        } catch (InterruptedException e) {
            throw new RuntimeException("failed to create library: " + directory, e);
        }
    }

    private List<Operation> doOperations(Syntax syntax) {
        Env env = iface.env();
        AnnotatedAbSyn absyn = fromSyntax(env, syntax);
        TForm tf = iface.infer(absyn);
        Collection<SymbolMeaning> operations = iface.directOperations(tf);
        for (SymbolMeaning syme: operations) {
            System.out.println("Operation: " + syme.name() + " " + syme.type());
        }

        return operations.stream().map(syme -> new Operation(syme.name().name(),
                                                            AnnotatedSyntax.toSyntax(scope, TypePackage.asAbSyn(iface.env(), syme.type())), null, null))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Syntax normalise(@NotNull Syntax syntax) {
        return syntax;
    }

    @Override
    public void dispose() {
    }

    public static class AldorExecutor {
        private final FoamContext context;
        private Lock lock = new ReentrantLock();

        public AldorExecutor() {
            this.context = new FoamContext();
        }

        public void run(Runnable r) throws InterruptedException {
            lock.tryLock(5, TimeUnit.SECONDS);
            try {
                FoamHelper.setContext(context);
                r.run();
            }
            finally {
                lock.unlock();
            }
        }

        public <T, E extends Throwable> T compute(@NotNull ThrowableComputable<T, E> action) throws E, InterruptedException {
            lock.tryLock(5, TimeUnit.SECONDS);
            try {
                FoamHelper.setContext(context);
                return action.compute();
            }
            finally {
                lock.unlock();
            }
        }

        public Clos createLoadFn(String className) {
            return context.createLoadFn(className);
        }
    }
}
