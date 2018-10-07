package aldor.parser;

import aldor.file.AldorFile;
import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import aldor.lexer.AldorTokenTypes;
import aldor.psi.elements.AldorElementTypeFactory;
import aldor.psi.elements.AldorTypes;
import aldor.psi.elements.PsiElementCreator;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

// ToDo: Split into "Aldor"-specific and "spad-like-language" parts
public class AldorParserDefinition implements ParserDefinition {
    private static final TokenSet STRING_LITERALS = TokenSet.create(AldorTokenTypes.TK_String);
    private static final TokenSet COMMENT_TOKENS = TokenSet.create(AldorTokenTypes.TK_Comment);
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE, AldorTokenTypes.KW_NewLine,
                                                                AldorTokenTypes.TK_Comment,
                                                                AldorTokenTypes.TK_SysCmdIf, AldorTokenTypes.TK_SysCmdEndIf,
                                                                AldorTokenTypes.TK_IfLine,
                                                                AldorTokenTypes.KW_Indent,
                                                                AldorTokenTypes.TK_SysCmd);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new AldorIndentLexer(new AldorLexerAdapter());
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
        return new AldorParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return AldorElementTypeFactory.ALDOR_FILE_ELEMENT_TYPE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new AldorFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    @NotNull
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();
        if (!(elementType instanceof PsiElementCreator)) {
            return AldorTypes.Factory.createElement(node);
        }
        else {
            return ((PsiElementCreator) elementType).createElement(node);
        }
    }

}
