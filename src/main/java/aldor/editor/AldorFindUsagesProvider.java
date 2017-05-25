package aldor.editor;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AldorFindUsagesProvider implements FindUsagesProvider {
    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return (psiElement instanceof AldorDefine) || (psiElement instanceof AldorDeclare);
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        return "Constant";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof AldorDefine) {
            AldorDefine def = (AldorDefine) element;
            Syntax lhs = SyntaxPsiParser.parse(def.lhs());
            return SyntaxPrinter.instance().toString(SyntaxUtils.typeName(lhs));
        }
        else if (element instanceof AldorDeclare) {
            AldorDeclare decl = (AldorDeclare) element;
            Syntax syntax = SyntaxUtils.typeName(SyntaxPsiParser.parse(decl));
            return SyntaxPrinter.instance().toString(syntax);
        }
        return element.getText();
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return element.getText();
    }
}
