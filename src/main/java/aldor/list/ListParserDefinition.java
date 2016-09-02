package aldor.list;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class ListParserDefinition implements ParserDefinition {
  private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
  //public static final TokenSet COMMENTS = TokenSet.create(ListTypes.COMMENT);

  private static final IFileElementType FILE =
      new IFileElementType(Language.findInstance(ListLanguage.class));

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    System.out.println("Creating lexer for " + project.getName());
    return new ListLexerAdapter();
  }

  @Override
  @NotNull
  public TokenSet getWhitespaceTokens() {
    return WHITE_SPACES;
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public PsiParser createParser(final Project project) {
    System.out.println("Creating parser for " + project.getName());
    return new ListParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public PsiFile createFile(FileViewProvider viewProvider) {
    return new ListFile(viewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
    return SpaceRequirements.MAY;
  }

  @Override
  @NotNull
  public PsiElement createElement(ASTNode node) {
    return ListTypes.Factory.createElement(node);
  }
}