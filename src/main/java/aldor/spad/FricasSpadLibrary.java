package aldor.spad;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.index.AldorDeclareTopIndex;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.AnnotatedSyntax;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldor.typelib.NamedExport;
import aldor.typelib.SymbolDatabase;
import aldor.typelib.SymbolDatabaseHelper;
import aldor.typelib.SymbolMeaning;
import aldor.typelib.TForm;
import aldor.typelib.TypePackage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import foamj.Clos;
import foamj.FoamContext;
import foamj.FoamHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aldor.syntax.AnnotatedSyntax.fromSyntax;
import static aldor.syntax.AnnotatedSyntax.toSyntax;

public class FricasSpadLibrary implements SpadLibrary, Disposable {
    private static final Logger LOG = Logger.getInstance(FricasSpadLibrary.class);
    public static final GlobalSearchScope[] EMPTY_SCOPE_ARRAY = new GlobalSearchScope[0];

    private final String name;
    private final GlobalSearchScope scope;
    private final AldorExecutor aldorExecutor;
    private final Project project;
    private final FricasSpadLibrary.AxiomInterfaceContainer axiomInterfaceContainer;
    private final FricasEnvironment environment;

    public FricasSpadLibrary(Project project, FricasEnvironment fricasEnvironment) {
        //this.directory = daaseDirectory.getCanonicalPath() + "/algebra";
        //this.scope = GlobalSearchScopesCore.directoriesScope(project, true, daaseDirectory);
        this.name = fricasEnvironment.name();
        this.scope = fricasEnvironment.scope(project);
        this.environment = fricasEnvironment;
        this.project = project;
        this.aldorExecutor = new AldorExecutor();
        this.axiomInterfaceContainer = new AxiomInterfaceContainer(fricasEnvironment);
    }


    void initialiseFileListener() {
        VirtualFileListener listener = createListener();
        VirtualFileManager.getInstance().addVirtualFileListener(listener, this);
    }

    private VirtualFileListener createListener() {
        return new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                if (environment.containsFile(file)) {
                    axiomInterfaceContainer.needsReload();
                }
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        };
    }

    private class AxiomInterfaceContainer {
        private final FricasEnvironment environment;
        private AxiomInterface iface = null;
        private boolean needsReload = true;

        AxiomInterfaceContainer(FricasEnvironment fricasEnvironment) {
            this.environment = fricasEnvironment;
        }

        AxiomInterface value() {
            if (needsReload) {
                iface = load();
                needsReload = false;
            }
            return iface;
        }

        AxiomInterface load() {
            try {
                Clos fn = aldorExecutor.createLoadFn("axiomshell");
                fn.call();
                return aldorExecutor.compute(environment::create);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        public void needsReload() {
            needsReload = true;
        }

        public void dispose() {
        }
    }

    @Override
    public List<Syntax> parentCategories(@NotNull Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doParentCategories(syntax));
        } catch (InterruptedException e) {
            throw new RuntimeException("failed to create parents: " + name + " " + syntax, e);
        }
    }

    private List<Syntax> doParentCategories(@NotNull Syntax syntax) {
        AxiomInterface iface = axiomInterfaceContainer.value();
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
            throw new RuntimeException("failed find operations: " + name + " " + syntax, e);
        }
    }

    private List<Operation> doOperations(Syntax syntax) {
        AxiomInterface iface = axiomInterfaceContainer.value();
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
    private PsiNamedElement declarationFor(Syntax exporter, NamedExport namedExport) {
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
        AxiomInterface iface = axiomInterfaceContainer.value();
        return decl -> {
            Syntax librarySyntax = toSyntax(scope, TypePackage.asAbSyn(iface.env(), namedExport.original()));
            Syntax sourceSyntax = decl.getGreenStub().syntax().as(DeclareNode.class).rhs();

            LOG.info("Lib syntax: " + librarySyntax + " Source: " + sourceSyntax);
            return SyntaxUtils.match(sourceSyntax, librarySyntax);
        };
    }

    @NotNull
    private Predicate<AldorDeclare> filterByExporter(Syntax leadingExporter) {
        return decl -> {
            AldorDeclareStub stub = Optional.ofNullable(decl.getGreenStub()).orElse(decl.getStub());
            if (stub == null) {
                Optional<AldorDefine> definingForm = AldorPsiUtils.definingForm(decl);
                Optional<Syntax> exporter = definingForm.map(form -> SyntaxUtils.typeName(SyntaxPsiParser.parse(form.lhs())));
                return exporter.map(e -> SyntaxUtils.match(SyntaxUtils.leadingId(e), leadingExporter)).orElse(false);
            }
            else {
                Syntax sourceExporter = stub.exporter();
                return SyntaxUtils.match(SyntaxUtils.leadingId(sourceExporter), leadingExporter);
            }
        };
    }

    @NotNull
    @Override
    public Syntax normalise(@NotNull Syntax syntax) {
        return syntax;
    }

    @Override
    public List<Syntax> allTypes() {
        try {
            AxiomInterface iface = axiomInterfaceContainer.value();
            return this.aldorExecutor.compute(() -> iface.allTypes().stream().map(absyn -> toSyntax(scope, absyn)).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            LOG.error("failed to read types", e);
            return Collections.emptyList();
        }
    }

    @Override
    public String definingFile(Id id) {
        Collection<AldorDefine> defineCollection = AldorDefineTopLevelIndex.instance.get(id.symbol(), project, scope);
        if (defineCollection.isEmpty()) {
            LOG.warn("No file for: " + id);
            return "";
        }
        AldorDefine define = defineCollection.iterator().next();
        return define.getContainingFile().getVirtualFile().getPresentableName();
    }

    private Stream<AldorDefine> topLevelDefinition(SymbolMeaning symbolMeaning) {
        Collection<AldorDefine> definitions = AldorDefineTopLevelIndex.instance.get(symbolMeaning.name().name(), project, scope);
        return definitions.stream().limit(1);
    }

    @Override
    public void dispose() {
        axiomInterfaceContainer.dispose();
    }

    public static class AldorExecutor {
        private final FoamContext context;
        private final Lock lock = new ReentrantLock();

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

    public static class FricasEnvironment {
        private final VirtualFile daaseDirectory;
        private final List<VirtualFile> nrLibs;
        private final VirtualFile daaseSourceDirectory;
        private final List<VirtualFile> nrlibSourceDirectories;

        FricasEnvironment(VirtualFile daaseDirectory, VirtualFile daaseSourceDirectory, List<VirtualFile> nrLibs, List<VirtualFile> nrlibSourceDirectories) {
            this.daaseDirectory = daaseDirectory;
            this.nrLibs = new ArrayList<>(nrLibs);
            this.daaseSourceDirectory = daaseSourceDirectory;
            this.nrlibSourceDirectories = new ArrayList<>(nrlibSourceDirectories);
        }

        AxiomInterface create() {
            List<SymbolDatabase> databases = new ArrayList<>();
            if (daaseDirectory != null) {
                databases.add(SymbolDatabase.daases(daaseDirectory.getPath()));
            }
            databases.addAll(nrLibs.stream().map(dir -> SymbolDatabaseHelper.nrlib(dir.getPath())).collect(Collectors.toList()));
            if (databases.size() != 1) {
                throw new RuntimeException("Invalid fricas library state - can only support one lib at the moment");
            }
            return AxiomInterface.create(databases.get(0));
        }

        public String name() {
            return "FricasEnv: " + daaseDirectory + " " + nrLibs;
        }

        public GlobalSearchScope scope(Project project) {
            List<GlobalSearchScope> lst = new ArrayList<>();
            if (daaseSourceDirectory != null) {
                lst.add(GlobalSearchScopesCore.directoriesScope(project, true, daaseSourceDirectory));
            }
            for (VirtualFile nrlibDir: nrlibSourceDirectories) {
                lst.add(GlobalSearchScopesCore.directoriesScope(project, true, nrlibDir));
            }
            if (lst.isEmpty()) {
                return GlobalSearchScope.EMPTY_SCOPE;
            }
            return GlobalSearchScope.union(lst.toArray(new GlobalSearchScope[lst.size()]));
        }

        public boolean containsFile(VirtualFile file) {
            if (VfsUtilCore.isAncestor(daaseDirectory, file, true)) {
                return true;
            }
            if (nrLibs.stream().anyMatch(lib -> VfsUtilCore.isAncestor(lib, file, true))) {
                return true;
            }
            return false;
        }
    }

}
