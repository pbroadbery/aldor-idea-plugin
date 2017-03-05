package aldor.editor.documentation;

import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AbbrevInfo;
import com.intellij.psi.PsiElement;

class SpadAbbrevDocumentationProvider extends TypedDocumentationProvider<SpadAbbrev> {

    @Override
    public String generateDoc(SpadAbbrev abbrev, PsiElement originalElement) {
        AbbrevInfo info = abbrev.abbrevInfo();
        if (info.isError()) {
            return "Badly formed abbrev expression: " + abbrev.getText();
        }
        return "Abbreviation for the " + info.kind().value() + " <b>" + info.name() + "</b>";
    }

}
