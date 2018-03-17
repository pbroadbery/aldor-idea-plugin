package aldor.editor;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.ui.AldorIcons;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AldorLookupElementFactory implements PsiElementToLookupElementMapping {

    @Override
    public LookupElement forMacro(AldorDefine define) {
        return LookupElementBuilder.create(define)
                .withTypeText("MACRO", true)
                .withIcon(AldorIcons.MACRO);
    }

    @Nullable
    @Override
    public LookupElement forConstant(AldorDefine define) {
        Syntax definitionSyntax = SyntaxPsiParser.parse(define.lhs());
        if (definitionSyntax == null) {
            return null;
        }
        Optional<Syntax> typeSyntax = SyntaxUtils.definitionToSignature(definitionSyntax);
        String tailText = typeSyntax.map(SyntaxPrinter.instance()::toString)
                .orElse("??");
        return LookupElementBuilder.create(define)
                .withTailText(": " + tailText, true)
                .withTypeText("<<local>>")
                .withIcon(AldorIcons.DECLARE_ICON);
    }

    @Override
    @Nullable
    public LookupElement forDeclare(AldorDeclare define) {
        Syntax typeSyntax = SyntaxPsiParser.parse(define.rhs());
        if (typeSyntax == null) {
            return null;
        }
        return LookupElementBuilder.create(define)
                .withTailText(": " + SyntaxPrinter.instance().toString(typeSyntax))
                .withTypeText("<<local>>")
                .withIcon(AldorIcons.DECLARE_ICON);
    }

    @Override
    public LookupElement forId(AldorIdentifier define) {
        return LookupElementBuilder.create(define)
                .withTypeText("<<var>>");
    }
}
