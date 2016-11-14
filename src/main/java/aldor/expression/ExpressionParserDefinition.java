package aldor.expression;

import aldor.lexer.AldorLexerAdapter;
import aldor.lexer.AldorTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static aldor.parser.AldorParserDefinition.WHITE_SPACES;

public class ExpressionParserDefinition implements ParserDefinition {
    private static final TokenSet STRING_LITERALS = TokenSet.create(AldorTokenTypes.TK_String);
    private static final TokenSet COMMENT_TOKENS = TokenSet.create(AldorTokenTypes.TK_Comment);

    private static final IFileElementType FILE = (IFileElementType) ExpressionTypeFactory.createElement("FILE");

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new AldorLexerAdapter();
    }

    @Override
    @NotNull
    public PsiElement createElement(ASTNode node) {
        return ExpressionTypes.Factory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new ExpressionFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @Override
    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENT_TOKENS;
    }

    @Override
    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRING_LITERALS;
    }

    @Override
    @NotNull
    public PsiParser createParser(final Project project) {
        return new ExpressionParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }
}
