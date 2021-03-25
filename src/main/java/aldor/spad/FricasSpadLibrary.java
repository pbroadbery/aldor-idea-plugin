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
import aldor.typelib.ExportSet;
import aldor.typelib.NamedExport;
import aldor.typelib.SymbolMeaning;
import aldor.typelib.TForm;
import aldor.typelib.TfGeneral;
import aldor.typelib.TypeExport;
import aldor.typelib.TypePackage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileContentsChangedAdapter;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
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
    private VirtualFileListener listener;

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
        listener = createListener();
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
    public Pair<List<ParentType>, List<Operation>> allParents(Syntax syntax) {
        try {
            return aldorExecutor.compute(() -> doAllParents(syntax));
        } catch (InterruptedException e) {
            throw new FricasSpadLibraryException("interrupt on create parents: " + name + " " + syntax, e);
        } catch (RuntimeException e) {
            throw new FricasSpadLibraryException("Failed to create parents: " + name + " " + syntax, e);
        }
    }

    private Pair<List<ParentType>, List<Operation>> doAllParents(@NotNull Syntax syntax) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        AnnotatedAbSyn absyn = fromSyntax(AxiomInterface.withUnknownForMissingSymbols(iface.env()), syntax);

        ExportSet exports = iface.expandParents(absyn);
        List<TypeExport> types = exports.parentTypes();
        List<NamedExport> sigs = exports.signatures();


        return Pair.create(
                types.stream().map(te -> new ParentType(toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), te.type())),
                                                        te.condition().isNone() ? null : toSyntax(project, scope, te.condition()), null))
                        .collect(Collectors.toList()),
                sigs.stream().map(ne ->  createOperation(iface, syntax, ne)).collect(Collectors.toList())
        );

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
        AnnotatedAbSyn absyn = fromSyntax(AxiomInterface.withUnknownForMissingSymbols(iface.env()), syntax);

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

    /*
    @Override
    public Pair<List<Syntax>, List<Operation>> allParents(Syntax syntax) {
        return aldorExecutor.compute(doAllParents(syntax));
    }


    private List<Syntax> doAllParents(Syntax syntax) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        Env env = iface.env();
        AnnotatedAbSyn absyn = fromSyntax(env, syntax);
        iface.expandParents(iface.infer(absyn));
    }
    */
    private List<Operation> doOperations(Syntax source) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        Env env = iface.env();
        AnnotatedAbSyn absyn = fromSyntax(env, source);
        Collection<NamedExport> operations = iface.directOperations(absyn);

        return operations.stream().map(namedExport -> createOperation(iface, source, namedExport))
                .peek(op -> LOG.info("Found operation " + op + " from " + source + " decl " + op.declaration()))
                .collect(Collectors.toList());
    }

    @NotNull
    private Operation createOperation(AxiomInterface iface, Syntax source, NamedExport namedExport) {
        try {
            return new Operation(namedExport.name().name(),
                    toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), namedExport.type())),
                    namedExport.condition().isNone() ? null : toSyntax(project, scope, namedExport.condition()),
                    source,
                    declarationFor(iface, source, namedExport),
                    containingForm(SyntaxUtils.leadingId(source)));
        }
        catch (RuntimeException e) {
            throw new FricasSpadLibraryException("Creating operation: " + namedExport.name() + " " + namedExport.type(), e);
        }
    }

    @Nullable
    private PsiNamedElement implementationFor(AxiomInterface iface, Syntax source, NamedExport export) {
        Syntax requestedName = SyntaxUtils.leadingId(source);
        AldorDefine exportingDefinition = containingForm(requestedName);
        if (exportingDefinition == null) {
            return null;
        }
        PsiElement implementation = exportingDefinition.implementation();
        throw new RuntimeException("to be continued");
    }

    /** This is, well, guesswork. If we had the declaration line number it would be easier.  Might be more
     * possible via aldor .abn files */
    private PsiNamedElement declarationFor(AxiomInterface iface, Syntax source, NamedExport export) {
        PsiNamedElement elt = declarationFor(iface, export.name().name(), export.original(), source);
        if ((elt == null) && TfGeneral.isGeneral(export.exporter())) {
            Syntax originalExporter = toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), export.exporter()));
            elt = declarationFor(iface, export.name().name(), export.original(), originalExporter);
        }
        return elt;
    }

    private @Nullable
    PsiNamedElement declarationFor(AxiomInterface iface, String name, TForm originalType, Syntax exporter) {
        Syntax leadingExporter = SyntaxUtils.leadingId(exporter);
        // Find all definitions of "namedExport" in the file containing the exporter
        AldorDefine exportingDefinition = containingForm(leadingExporter);
        if (exportingDefinition == null) {
            return null;
        }
        Collection<AldorDeclare> candidates = AldorDeclareTopIndex.instance.get(name, project,
                GlobalSearchScope.fileScope(exportingDefinition.getContainingFile()));
        Predicate<AldorDeclare> filterByExporter  = filterByExporter(leadingExporter);
        Predicate<AldorDeclare> filterBySignature = filterBySignature(originalType);

        List<Predicate<AldorDeclare>> filters = Arrays.asList(filterByExporter, filterBySignature);
        int filterIndex = 0;

        while (!candidates.isEmpty() && (filterIndex < filters.size())) {
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
    private Predicate<AldorDeclare> filterBySignature(TForm type) {
        AxiomInterface iface = axiomInterfaceContainer.value();
        Syntax librarySyntax = toSyntax(project, scope, TypePackage.asAbSyn(iface.env(), type));
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
                if (sourceExporter == null) {
                    LOG.warn("Missing exporter on stub: " + decl + " " + stub.getPsi().getContainingFile().getName());
                    return false;
                }
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
        VirtualFileManager.getInstance().removeVirtualFileListener(listener);
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
/*
NB: Memory leak: TODO Ensure that listener is cleaned up when reqd
	at com.intellij.openapi.util.ObjectTree.assertIsEmpty(ObjectTree.java:226)
	at com.intellij.openapi.util.Disposer.assertIsEmpty(Disposer.java:130)
	at com.intellij.openapi.util.Disposer.assertIsEmpty(Disposer.java:125)
	at com.intellij.openapi.application.impl.ApplicationImpl.disposeContainer(ApplicationImpl.java:183)
	at com.intellij.openapi.application.impl.ApplicationImpl.disposeSelf(ApplicationImpl.java:200)
	at com.intellij.openapi.application.impl.ApplicationImpl.doExit(ApplicationImpl.java:621)
	at com.intellij.openapi.application.impl.ApplicationImpl.exit(ApplicationImpl.java:589)
	at com.intellij.openapi.application.impl.ApplicationImpl.restart(ApplicationImpl.java:536)
	at com.intellij.diagnostic.hprof.action.HeapDumpSnapshotRunnable$CaptureHeapDumpTask.confirmRestart(HeapDumpSnapshotRunnable.kt:176)
	at com.intellij.diagnostic.hprof.action.HeapDumpSnapshotRunnable$CaptureHeapDumpTask.access$confirmRestart(HeapDumpSnapshotRunnable.kt:144)
	at com.intellij.diagnostic.hprof.action.HeapDumpSnapshotRunnable$CaptureHeapDumpTask$onSuccess$1.invoke(HeapDumpSnapshotRunnable.kt:154)
	at com.intellij.diagnostic.hprof.action.HeapDumpSnapshotRunnable$CaptureHeapDumpTask$onSuccess$1.invoke(HeapDumpSnapshotRunnable.kt:144)
	at com.intellij.diagnostic.hprof.action.HeapDumpSnapshotRunnableKt$sam$java_lang_Runnable$0.run(HeapDumpSnapshotRunnable.kt)
	at com.intellij.openapi.application.TransactionGuardImpl$2.run(TransactionGuardImpl.java:201)
	at com.intellij.openapi.application.impl.ApplicationImpl.runIntendedWriteActionOnCurrentThread(ApplicationImpl.java:831)
	at com.intellij.openapi.application.impl.ApplicationImpl.lambda$invokeLater$4(ApplicationImpl.java:310)
	at com.intellij.openapi.application.impl.FlushQueue.doRun(FlushQueue.java:80)
	at com.intellij.openapi.application.impl.FlushQueue.runNextEvent(FlushQueue.java:128)
	at com.intellij.openapi.application.impl.FlushQueue.flushNow(FlushQueue.java:46)
	at com.intellij.openapi.application.impl.FlushQueue$FlushNow.run(FlushQueue.java:184)
	at java.desktop/java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:313)
	at java.desktop/java.awt.EventQueue.dispatchEventImpl(EventQueue.java:776)
	at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:727)
	at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:721)
	at java.base/java.security.AccessController.doPrivileged(Native Method)
	at java.base/java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:85)
	at java.desktop/java.awt.EventQueue.dispatchEvent(EventQueue.java:746)
	at com.intellij.ide.IdeEventQueue.defaultDispatchEvent(IdeEventQueue.java:974)
	at com.intellij.ide.IdeEventQueue._dispatchEvent(IdeEventQueue.java:847)
	at com.intellij.ide.IdeEventQueue.lambda$null$8(IdeEventQueue.java:449)
	at com.intellij.openapi.progress.impl.CoreProgressManager.computePrioritized(CoreProgressManager.java:741)
	at com.intellij.ide.IdeEventQueue.lambda$dispatchEvent$9(IdeEventQueue.java:448)
	at com.intellij.openapi.application.impl.ApplicationImpl.runIntendedWriteActionOnCurrentThread(ApplicationImpl.java:831)
	at com.intellij.ide.IdeEventQueue.dispatchEvent(IdeEventQueue.java:496)
	at java.desktop/java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:203)
	at java.desktop/java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:124)
	at java.desktop/java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:113)
	at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:109)
	at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.desktop/java.awt.EventDispatchThread.run(EventDispatchThread.java:90)
Caused by: java.lang.Throwable
	at com.intellij.openapi.util.ObjectNode.<init>(ObjectNode.java:35)
	at com.intellij.openapi.util.ObjectTree.createNodeFor(ObjectTree.java:106)
	at com.intellij.openapi.util.ObjectTree.register(ObjectTree.java:70)
	at com.intellij.openapi.util.Disposer.register(Disposer.java:65)
	at com.intellij.util.containers.DisposableWrapperList.createDisposableWrapper(DisposableWrapperList.java:253)
	at com.intellij.util.containers.DisposableWrapperList.add(DisposableWrapperList.java:62)
	at com.intellij.util.EventDispatcher.addListener(EventDispatcher.java:152)
	at com.intellij.openapi.vfs.impl.VirtualFileManagerImpl.addVirtualFileListener(VirtualFileManagerImpl.java:185)
	at aldor.spad.FricasSpadLibrary.initialiseFileListener(FricasSpadLibrary.java:90)
	at aldor.spad.FricasSpadLibrary.<init>(FricasSpadLibrary.java:65)
	at aldor.spad.FricasSpadLibraryBuilder.createFricasSpadLibrary(FricasSpadLibraryBuilder.java:46)
	at aldor.spad.SpadLibraryManager.doForSdk(SpadLibraryManager.java:150)
	at aldor.spad.SpadLibraryManager.forSdk(SpadLibraryManager.java:129)
	at aldor.spad.SpadLibraryManager.forAldorModule(SpadLibraryManager.java:107)
	at aldor.spad.SpadLibraryManager.forModule(SpadLibraryManager.java:97)
	at aldor.spad.SpadLibraryManager.spadLibraryForElement(SpadLibraryManager.java:172)
	at aldor.editor.completion.AldorCompletionContributor.allTypes(AldorCompletionContributor.java:66)
	at aldor.editor.completion.AldorCompletionContributor$1.addCompletions(AldorCompletionContributor.java:58)
	at com.intellij.codeInsight.completion.CompletionProvider.addCompletionVariants(CompletionProvider.java:26)
	at com.intellij.codeInsight.completion.CompletionContributor.fillCompletionVariants(CompletionContributor.java:154)
	at com.intellij.codeInsight.completion.CompletionService.getVariantsFromContributors(CompletionService.java:76)
	at com.intellij.codeInsight.completion.CompletionResultSet.runRemainingContributors(CompletionResultSet.java:154)
	at com.intellij.codeInsight.completion.CompletionResultSet.runRemainingContributors(CompletionResultSet.java:146)
	at com.intellij.codeInsight.completion.CompletionResultSet.runRemainingContributors(CompletionResultSet.java:142)
	at com.intellij.codeInsight.template.impl.LiveTemplateCompletionContributor$1.addCompletions(LiveTemplateCompletionContributor.java:86)
	at com.intellij.codeInsight.completion.CompletionProvider.addCompletionVariants(CompletionProvider.java:26)
	at com.intellij.codeInsight.completion.CompletionContributor.fillCompletionVariants(CompletionContributor.java:154)
	at com.intellij.codeInsight.completion.CompletionService.getVariantsFromContributors(CompletionService.java:76)
	at com.intellij.codeInsight.completion.CompletionService.getVariantsFromContributors(CompletionService.java:59)
	at com.intellij.codeInsight.completion.CompletionService.performCompletion(CompletionService.java:132)
	at com.intellij.codeInsight.completion.BaseCompletionService.performCompletion(BaseCompletionService.kt:30)
	at com.intellij.codeInsight.completion.CompletionProgressIndicator.calculateItems(CompletionProgressIndicator.java:834)
	at com.intellij.codeInsight.completion.CompletionProgressIndicator.runContributors(CompletionProgressIndicator.java:819)
	at com.intellij.codeInsight.completion.CodeCompletionHandlerBase.lambda$null$6(CodeCompletionHandlerBase.java:332)
	at com.intellij.codeInsight.completion.AsyncCompletion.lambda$tryReadOrCancel$5(CompletionThreading.java:172)
	at com.intellij.openapi.application.impl.ApplicationImpl.tryRunReadAction(ApplicationImpl.java:1106)
	at com.intellij.codeInsight.completion.AsyncCompletion.tryReadOrCancel(CompletionThreading.java:170)
	at com.intellij.codeInsight.completion.CodeCompletionHandlerBase.lambda$startContributorThread$7(CodeCompletionHandlerBase.java:324)
	at com.intellij.codeInsight.completion.AsyncCompletion.lambda$null$0(CompletionThreading.java:95)
	at com.intellij.openapi.progress.impl.CoreProgressManager.lambda$runProcess$2(CoreProgressManager.java:166)
	at com.intellij.openapi.progress.impl.CoreProgressManager.registerIndicatorAndRun(CoreProgressManager.java:627)
	at com.intellij.openapi.progress.impl.CoreProgressManager.executeProcessUnderProgress(CoreProgressManager.java:572)
	at com.intellij.openapi.progress.impl.ProgressManagerImpl.executeProcessUnderProgress(ProgressManagerImpl.java:61)
	at com.intellij.openapi.progress.impl.CoreProgressManager.runProcess(CoreProgressManager.java:153)
	at com.intellij.codeInsight.completion.AsyncCompletion.lambda$startThread$1(CompletionThreading.java:91)
	at com.intellij.util.RunnableCallable.call(RunnableCallable.java:20)
	at com.intellij.util.RunnableCallable.call(RunnableCallable.java:11)
	at com.intellij.openapi.application.impl.ApplicationImpl$1.call(ApplicationImpl.java:255)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base/java.lang.Thread.run(Thread.java:834)

 */