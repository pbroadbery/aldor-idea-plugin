package pab.aldor;

import aldor.AldorParserDefinition;
import aldor.AldorTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ParserFunctions {
    static void logPsi(PsiElement psi, int i) {
        String text = (psi.getChildren().length == 0) ? psi.getText(): "";
        System.out.println("(psi: " + psi + " " + text);
        for (PsiElement elt: psi.getChildren()) {
            logPsi(elt, i+1);
        }
        System.out.println(")");
    }

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



}
