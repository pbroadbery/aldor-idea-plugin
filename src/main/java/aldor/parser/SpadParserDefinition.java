package aldor.parser;

import aldor.file.SpadFile;
import aldor.language.SpadLanguage;
import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

public class SpadParserDefinition extends AldorParserDefinition {

    private static final IFileElementType FILE =
            new IFileElementType(Language.findInstance(SpadLanguage.class));

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new AldorIndentLexer(new AldorLexerAdapter(AldorLexerAdapter.LexMode.Spad, null));
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new SpadFile(viewProvider);
    }

}
