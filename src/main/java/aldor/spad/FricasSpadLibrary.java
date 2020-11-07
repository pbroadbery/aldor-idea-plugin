package aldor.spad;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.index.AldorDeclareTopIndex;
import aldor.psi.index.AldorDefineTopLevelIndex;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.DeclareNode;
import aldor.syntax.components.Id;
import aldor.typelib.AnnotatedAbSyn;
import aldor.typelib.AxiomInterface;
import aldor.typelib.Env;
import aldor.typelib.NamedExport;
import aldor.typelib.SymbolMeaning;
import aldor.typelib.TForm;
import aldor.typelib.TypePackage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private final Project project;
    private final AxiomInterfaceContainer axiomInterfaceContainer;
    private final SpadEnvironment environment;
    private final AldorExecutor aldorExecutor;

    public FricasSpadLibrary(Project project, SpadEnvironment fricasEnvironment) {
        assert !project.isDisposed();
        this.name = fricasEnvironment.name();
        this.scope = fricasEnvironment.scope(project);
        this.environment = fricasEnvironment;
        this.project = project;
        this.axiomInterfaceContainer = new AxiomInterfaceContainer(fricasEnvironment);
        this.aldorExecutor = ApplicationManager.getApplication().getComponent(AldorExecutor.class);
        this.initialiseFileListener();
    }

    @Override
    public void addDependant(SpadLibrary other) {
        axiomInterfaceContainer.addDependant(other);
    }

    @Override
    public void needsReload() {
        axiomInterfaceContainer.needsReload();
    }

    public void removeDependant(SpadLibrary other) {
        axiomInterfaceContainer.removeDependant(other);
    }

    @NotNull
    @Override
    public Env environment() {
        return axiomInterfaceContainer.value().env();
    }

    private void initialiseFileListener() {
        VirtualFileListener listener = createListener();
        VirtualFileManager.getInstance().addVirtualFileListener(listener, this);
    }

    private VirtualFileListener createListener() {
        return new VirtualFileContentsChangedAdapter() {
            @Override
            protected void onFileChange(@NotNull VirtualFile file) {
                LOG.info("File changed: " + file);
                if (environment.containsFile(file)) {
                    axiomInterfaceContainer.needsReload();
                }
            }

            @Override
            protected void onBeforeFileChange(@NotNull VirtualFile file) {

            }
        };
    }

    @Override
    public List<Syntax> parentCategories(@NotNull Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doParentCategories(syntax));
        } catch (InterruptedException e) {
            throw new FricasSpadLibraryException("failed to create parents: " + name + " " + syntax, e);
        }
        catch (RuntimeException e) {
            throw new FricasSpadLibraryException("Failed to create parents: " + name + " " + syntax, e);
        }
    }

    private List<Syntax> doParentCategories(@NotNull Syntax syntax) {
        assert !project.isDisposed();
        AxiomInterface iface = axiomInterfaceContainer.value();
        AnnotatedAbSyn absyn = fromSyntax(iface.env(), syntax);
        TForm tf = iface.asTForm(absyn);
        Collection<TForm> parents = iface.directParents(tf);
        return parents.stream()
                .map(ptf -> TypePackage.asAbSyn(iface.env(), ptf))
                .map(ab -> toSyntax(project, scope, ab))
                .collect(Collectors.toList());
    }

    @Override
    public List<Operation> operations(Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doOperations(syntax));
        } catch (InterruptedException e) {
            throw new FricasSpadLibraryException("failed find operations: " + name + " " + syntax, e);
        }
        catch (RuntimeException e) {
            throw new FricasSpadLibraryException("Failed to list operations: " + name + " " + syntax, e);
        }
    }

    private List<Operation> doOperations(Syntax syntax) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        Env env = iface.env();
        AnnotatedAbSyn absyn = fromSyntax(env, syntax);
        Collection<NamedExport> operations = iface.directOperations(absyn);

        return operations.stream().map(namedExport -> createOperation(syntax, iface, namedExport))
                .collect(Collectors.toList());
    }

    @NotNull
    private Operation createOperation(Syntax syntax, AxiomInterface iface, NamedExport namedExport) {
        try {
            return new Operation(namedExport.name().name(),
                    toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), namedExport.type())),
                    null,
                    syntax,
                    declarationFor(syntax, namedExport),
                    containingForm(SyntaxUtils.leadingId(syntax)));
        }
        catch (RuntimeException e) {
            throw new FricasSpadLibraryException("Creating operation: " + namedExport.name() + " " + namedExport.type(), e);
        }
    }

    /** This is, well, guesswork. If we had the declaration line number it would be easier.  Might be more
     * possible via aldor .abn files */
    private @Nullable
    PsiNamedElement declarationFor(Syntax exporter, NamedExport namedExport) {
        Syntax leadingExporter = SyntaxUtils.leadingId(exporter);
        // Find all definitions of "namedExport" in the file containing the exporter
        AldorDefine exportingDefinition = containingForm(leadingExporter);
        if (exportingDefinition == null) {
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

        return null; // More than one definition matches.. odd
    }

    @Nullable
    private AldorDefine containingForm(Syntax leadingExporter) {
        Collection<AldorDefine> exportingDefinitionCandidates = AldorDefineTopLevelIndex.instance.get(leadingExporter.as(Id.class).symbol(), project, scope);
        if (exportingDefinitionCandidates.size() != 1) {
            return null;
        }
        AldorDefine exportingDefinition = exportingDefinitionCandidates.iterator().next();
        if (exportingDefinition.getContainingFile() == null) {
            return null;
        }
        return exportingDefinition;
    }

    @NotNull
    private Predicate<AldorDeclare> filterBySignature(NamedExport namedExport) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        Syntax librarySyntax = toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), namedExport.original()));
        return decl -> matchDeclaration(librarySyntax, decl);
    }

    private boolean matchDeclaration(Syntax librarySyntax, AldorDeclare decl) {
        Optional<AldorDeclareStub> stub = Optional.ofNullable(decl.getGreenStub());
        if (!stub.isPresent()) {
            stub = Optional.ofNullable(decl.getStub());
        }
        Syntax sourceSyntax = stub
                .map(s -> s.syntax().as(DeclareNode.class).rhs())
                .orElse(SyntaxPsiParser.parse(decl.rhs()));

        LOG.info("Lib syntax: " + librarySyntax + " Source: " + sourceSyntax);
        return SyntaxUtils.match(sourceSyntax, librarySyntax);
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
    @NotNull
    public List<Syntax> allTypes() {
        try {
            AxiomInterface iface = axiomInterfaceContainer.value();
            return this.aldorExecutor.compute(() -> iface.allTypes().stream().map(absyn -> toSyntax(project, scope, absyn)).collect(Collectors.toList()));
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

    @Override
    public GlobalSearchScope scope(Project project) {
        //noinspection ObjectEquality
        assert project == this.project;
        return this.scope;
    }

    @SuppressWarnings("serial")
    public static class FricasSpadLibraryException extends RuntimeException {

        public FricasSpadLibraryException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
