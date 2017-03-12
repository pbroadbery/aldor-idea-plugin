package aldor.editor.documentation;

import aldor.psi.AldorDeclare;
import aldor.psi.stub.AldorDeclareStub;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import com.intellij.psi.PsiElement;

class DeclareDocumentationProvider extends TypedDocumentationProvider<AldorDeclare> {
    // NB: Switch to a manager if we need per-project docs
    private static final DocumentationUtils docUtils = new DocumentationUtils();

    @Override
    public String generateDoc(AldorDeclare o, PsiElement originalElement) {
        AldorDeclareStub greenStub = o.getGreenStub();
        String type1 = declareTypeText(o, greenStub);
        String header = "<b>Type:</b> " + type1;
        String docco = docUtils.aldorDocStringFromContainingElement(o);
        return header + "<hr/>" + docco;
    }

    private String declareTypeText(AldorDeclare o, AldorDeclareStub greenStub) {
        String type;
        if (greenStub == null) {
            Syntax parsed = SyntaxPsiParser.parse(o.rhs());
            type = SyntaxPrinter.instance().toString(parsed);
        }
        else {
            type = SyntaxPrinter.instance().toString(greenStub.rhsSyntax());
        }
        return type;
    }


}
