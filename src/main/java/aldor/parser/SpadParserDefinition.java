package aldor.parser;

import aldor.file.SpadFile;
import aldor.lexer.AldorIndentLexer;
import aldor.lexer.AldorLexerAdapter;
import aldor.lexer.LexMode;
import aldor.psi.elements.AldorElementTypeFactory;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

public class SpadParserDefinition extends AldorParserDefinition {

    @Override
    public IFileElementType getFileNodeType() {
        return AldorElementTypeFactory.SPAD_FILE_ELEMENT_TYPE;
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new AldorIndentLexer(new AldorLexerAdapter(LexMode.Spad, null));
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new SpadFile(viewProvider);
    }

}
