package aldor;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class ParserFunctions {

    public static PsiElement parseText(Project project, CharSequence text) {
        return parseText(project, text, AldorTypes.CURLY_CONTENTS_LABELLED);
    }

    public static PsiElement parseText(Project project, CharSequence text, IElementType elementType) {
        ParserDefinition aldorParserDefinition = new AldorParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(aldorParserDefinition, aldorParserDefinition.createLexer(null),
                text);

        PsiParser parser = aldorParserDefinition.createParser(project);
        ASTNode parsed = parser.parse(elementType, psiBuilder);

        return parsed.getPsi();
    }


    @NotNull
    public static List<PsiErrorElement> getPsiErrorElements(PsiElement psi) {
        final List<PsiErrorElement> errors = new ArrayList<>();

        psi.accept(new PsiRecursiveElementVisitor() {

            @Override
            public void visitErrorElement(PsiErrorElement element) {
                errors.add(element);
                super.visitErrorElement(element);
            }
        });
        return errors;
    }

    public static Collection<PsiElement> find(PsiElement elt, Predicate<PsiElement> predicate) {
        List<PsiElement> subElements = Lists.newArrayList();
        elt.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (predicate.test(element)) {
                    subElements.add(element);
                }
                element.acceptChildren(this);
            }
        });
        return subElements;
    }
}
