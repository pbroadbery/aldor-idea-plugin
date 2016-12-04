package aldor.psi.elements;

import aldor.parser.AldorParserDefinition;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;

public class SpadSysCmdLazyPsiElement extends ILazyParseableElementType{

    public SpadSysCmdLazyPsiElement() {
        super("SpadSysCmd");
    }


    @Override
    public PsiBuilder parseLight(ASTNode chameleon) {
        Project project = chameleon.getPsi().getProject();
        ParserDefinition parserDefinition = AldorParserDefinition.abbrevParserDefinition(project);
        Lexer lexer = parserDefinition.createLexer(project);

        return PsiBuilderFactory.getInstance().createBuilder(parserDefinition, lexer, chameleon.getChars());
    }

    /**
     * Parses the contents of the specified chameleon node and returns the AST tree
     * representing the parsed contents.
     *
     * @param chameleon the node to parse.
     * @return the parsed contents of the node.
     */
    @Override
    public ASTNode parseContents(ASTNode chameleon) {
        return parseLight(chameleon).getTreeBuilt().getFirstChildNode();
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        throw new UnsupportedOperationException("parsing..");
    }


}
