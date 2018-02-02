package aldor.spad;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.index.AldorDeclareTopIndex;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.syntax.AnnotatedSyntax;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldor.typelib.NamedExport;
import aldor.typelib.SymbolDatabase;
import aldor.typelib.TForm;
import aldor.typelib.TypePackage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static aldor.syntax.AnnotatedSyntax.fromSyntax;

public class FricasSpadLibrary implements SpadLibrary, Disposable {
    private static final Logger LOG = Logger.getInstance(FricasSpadLibrary.class);

    private final String directory;
    private final AxiomInterface iface;
    private final GlobalSearchScope scope;
    private final AldorExecutor aldorExecutor;
    private final Project project;

    public FricasSpadLibrary(Project project, VirtualFile directory) {
        this.directory = directory.getCanonicalPath() + "/algebra";
        this.scope = GlobalSearchScopesCore.directoriesScope(project, true, directory);
        this.project = project;
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
        Collection<NamedExport> operations = iface.directOperations(absyn);
        for (NamedExport namedExport: operations) {
            System.out.println("Operation: " + namedExport.name() + " " + namedExport.type());
        }

        return operations.stream().map(namedExport -> new Operation(namedExport.name().name(),
                                                             AnnotatedSyntax.toSyntax(scope, TypePackage.asAbSyn(iface.env(), namedExport.type())),
                                                            null,
                                                             syntax,
                                                             declarationFor(syntax, namedExport)))
                .collect(Collectors.toList());
    }

    /** This is, well, guesswork. If we had the declaration line number it would be easier. */
    private PsiElement declarationFor(Syntax exporter, NamedExport namedExport) {
        Syntax leadingExporter = SyntaxUtils.leadingId(exporter);
        // Find all definitions of "namedExport" in the file containing the exporter
        Collection<AldorDefine> exportingDefinitionCandidates = AldorDefineTopLevelIndex.instance.get(leadingExporter.as(Id.class).symbol(), project, scope);
        if (exportingDefinitionCandidates.size() != 1) {
            return null;
        }
        AldorDefine exportingDefinition = exportingDefinitionCandidates.iterator().next();
        if (exportingDefinition.getContainingFile() == null) {
            return null;
        }
        Collection<AldorDeclare> candidates = AldorDeclareTopIndex.instance.get(namedExport.name().name(), project, GlobalSearchScope.fileScope(exportingDefinition.getContainingFile()));
        Predicate<AldorDeclare> filterByExporter  = filterByExporter(leadingExporter);
        Predicate<AldorDeclare> filterBySignature = filterBySignature(namedExport);

        List<Predicate<AldorDeclare>> filters = Arrays.asList(filterByExporter, filterBySignature);
        int filterIndex = 0;

        while ((candidates.size() > 1) && (filterIndex < filters.size())) {
            candidates = candidates.stream().filter(filters.get(filterIndex)).collect(Collectors.toList());
            filterIndex++;
        }
        if (candidates.isEmpty()) {
            return null;
        }
        else if (candidates.size() == 1) {
            return candidates.iterator().next();
        }

        return null;
    }

    @NotNull
    private Predicate<AldorDeclare> filterBySignature(NamedExport namedExport) {
        return decl -> {
            Syntax librarySyntax = AnnotatedSyntax.toSyntax(scope, TypePackage.asAbSyn(iface.env(), namedExport.original()));
            Syntax sourceSyntax = decl.getGreenStub().syntax().as(DeclareNode.class).rhs();

            LOG.info("Lib syntax: " + librarySyntax + " Source: " + sourceSyntax);
            return SyntaxUtils.match(sourceSyntax, librarySyntax);
        };
    }

    @NotNull
    private Predicate<AldorDeclare> filterByExporter(Syntax leadingExporter) {
        return decl -> {
            Syntax sourceExporter = decl.getGreenStub().exporter();
            return SyntaxUtils.match(SyntaxUtils.leadingId(sourceExporter), leadingExporter);
        };
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
            boolean hasLock = lock.tryLock(5, TimeUnit.SECONDS);
            if (!hasLock) {
                throw new RuntimeException("Aldor operation not available");
            }
            try {
                FoamHelper.setContext(context);
                r.run();
            }
            finally {
                lock.unlock();
            }
        }

        public <T, E extends Throwable> T compute(@NotNull ThrowableComputable<T, E> action) throws E, InterruptedException {
            boolean hasLock = lock.tryLock(5, TimeUnit.SECONDS);
            if (!hasLock) {
                throw new RuntimeException("Aldor operation not available");
            }
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
