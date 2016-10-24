package aldor.expression;

import aldor.parser.AldorParserDefinition;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class ExpressionParserDefinition extends AldorParserDefinition {

    @NotNull
    @Override
    public PsiParser createParser(Project project) {
        return new ExpressionParser();
    }

    @Override
    @NotNull
    public PsiElement createElement(ASTNode node) {
        return ExpressionTypes.Factory.createElement(node);
    }
}
