package aldor.editor.documentation;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.stub.AldorDeclareStub;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.SyntaxPsiParser;
import aldor.syntax.SyntaxUtils;
import aldor.util.AnnotatedOptional;
import com.intellij.psi.PsiElement;

import java.util.Optional;

import static aldor.psi.AldorPsiUtils.WITH;

class DeclareDocumentationProvider extends TypedDocumentationProvider<AldorDeclare> {
    // NB: Switch to a manager if we need per-project docs
    private static final DocumentationUtils docUtils = new DocumentationUtils();

    @Override
    public String generateDoc(AldorDeclare o, PsiElement originalElement) {
        String type1 = declareTypeText(o);
        AnnotatedOptional<Syntax, String> syme = AnnotatedOptional.ofNullable(originalElement, () -> (String) null)
                                                                .flatMap(docUtils::symeForElement)
                                                                .map(Syme::type);
        String exportType = declareExporterType(o).map(e -> "<b>Exporter:</b> " + e).orElse("");

        String header = "<b>Type:</b> " + type1;
        String importType = "<br/>" + syme.map(s -> "<b>Imported Type:</b> " + SyntaxPrinter.instance().toString(s)).orElse(msg -> "");
        String docco = docUtils.aldorDocStringFromContainingElement(o);
        return header + "<br/>" + exportType + importType + "<hr/>" + docco;
    }

    private String declareTypeText(AldorDeclare o) {
        String type;
        AldorDeclareStub greenStub = o.getGreenStub();
        if (greenStub == null) {
            Syntax parsed = SyntaxPsiParser.parse(o.rhs());
            type = SyntaxPrinter.instance().toString(parsed);
        }
        else {
            type = SyntaxPrinter.instance().toString(greenStub.rhsSyntax());
        }
        return type;
    }

    private Optional<String> declareExporterType(@SuppressWarnings("TypeMayBeWeakened") AldorDeclare o) {
        AldorPsiUtils.ContainingBlock<?> block = AldorPsiUtils.containingBlock(o);
        Optional<AldorDefine> definingForm = block.castTo(WITH)
                .map(AldorPsiUtils.ContainingBlock::element)
                .flatMap(AldorPsiUtils::definingForm);

        Optional<Syntax> syntax = definingForm.map(form -> SyntaxUtils.typeName(SyntaxPsiParser.parse(form.lhs())));
        return syntax.map(s -> SyntaxPrinter.instance().toString(s));
    }

}
