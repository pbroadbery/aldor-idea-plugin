package aldor.editor.completion;

import aldor.language.SpadLanguage;
import aldor.psi.AldorId;
import aldor.psi.elements.AldorTypes;
import aldor.spad.SpadLibrary;
import aldor.spad.SpadLibraryManager;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxUtils;
import aldor.syntax.components.Apply;
import aldor.syntax.components.Id;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import icons.AldorIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.object;
import static com.intellij.patterns.StandardPatterns.or;

public class AldorCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(AldorCompletionContributor.class);

    public AldorCompletionContributor() {

        ElementPattern<PsiElement> typeElement = psiElement().withElementType(or(object(AldorTypes.TYPE), object(AldorTypes.TYPE_E_12)));
        ElementPattern<PsiElement> typeElementStopPattern = psiElement().withParent(StandardPatterns.not(AldorPatterns.isFirstChild()));

        ElementPattern<? extends PsiElement> insideTypeElementPattern = psiElement(AldorId.class).withLanguage(SpadLanguage.INSTANCE).inside(true, typeElement, typeElementStopPattern);

        extend(CompletionType.BASIC, psiElement(), idCompletion());
    }

    private CompletionProvider<CompletionParameters> idCompletion() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                List<LookupElement> element = allTypes(parameters);
                result.addAllElements(element);
            }
        };
    }

    public List<LookupElement> allTypes(CompletionParameters parameters) {
        PsiElement elt = parameters.getPosition();
        SpadLibrary spadLibrary = SpadLibraryManager.getInstance(parameters.getOriginalFile().getProject()).spadLibraryForElement(elt);
        return (spadLibrary == null) ? Collections.emptyList() : allTypes_withCancel(spadLibrary);
    }

    @VisibleForTesting
    public static List<LookupElement> allTypes_withCancel(SpadLibrary spadLibrary) {
        try {
            ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            return asElements(spadLibrary, ApplicationUtil.runWithCheckCanceled(spadLibrary::allTypes, progressIndicator));
        }
        catch (ProcessCanceledException e) {
            LOG.info("Cancelled completion");
        } catch (Exception e) {
            LOG.error("Failed to collect all types", e);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    public static List<LookupElement> asElements(SpadLibrary spadLibrary, Collection<Syntax> allTypes) {
        return allTypes.stream()
                .flatMap(e -> createLookupElement(spadLibrary, e).stream())
                .collect(Collectors.toList());
    }

    private static Optional<LookupElement> createLookupElement(SpadLibrary spadLibrary, Syntax syntax) {
        Optional<Id> id = SyntaxUtils.leadingId(syntax).maybeAs(Id.class);
        if (id.isEmpty()) {
            return Optional.empty();
        }
        else {
            Optional<Apply> isApply = syntax.maybeAs(Apply.class);
            if (isApply.filter(apply -> apply.arguments().size() > 1).isPresent()) {
                return Optional.of(LookupElementBuilder.create(id.get().symbol())
                        .withInsertHandler(ParenthesesInsertHandler.WITH_PARAMETERS)
                        .withIcon(AldorIcons.IDENTIFIER)
                        .withTailText(tailTextForElement(spadLibrary, id.get()), true)
                );
            }
            else if (isApply.filter(apply -> apply.arguments().size() == 1).isPresent()) {
                return Optional.of(LookupElementBuilder.create(id.get().symbol())
                        .withIcon(AldorIcons.IDENTIFIER)
                        .withTailText(tailTextForElement(spadLibrary, id.get()), true)
                        .withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
            }
            else {
                return Optional.of(LookupElementBuilder.create(id.get().symbol())
                        .withIcon(AldorIcons.IDENTIFIER)
                        .withTailText(tailTextForElement(spadLibrary, id.get()), true));
            }
        }
    }

    private static String tailTextForElement(SpadLibrary spadLibrary, Id symbol) {
        return " (" + spadLibrary.definingFile(symbol) + ")";
    }

    private static final class AldorPatterns {
        static ElementPattern<PsiElement> isFirstChild() {
            //noinspection InnerClassTooDeeplyNested
            return psiElement().with(new PatternCondition<>("firstChild") {
                @Override
                public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
                    return (psiElement.getParent() != null) && psiElement.getParent().getFirstChild().equals(psiElement);
                }
            });
        }
    }
}
