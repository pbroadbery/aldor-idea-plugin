package aldor.editor.documentation;

import aldor.language.SpadLanguage;
import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.psi.elements.AldorDefineInfo;
import aldor.psi.stub.AldorDefineStub;
import com.intellij.psi.PsiElement;

import java.util.Optional;

class DefineDocumentationProvider extends TypedDocumentationProvider<AldorDefine> {
    // NB: Switch to a manager if we need per-project docs
    private static final DocumentationUtils docUtils = new DocumentationUtils();

    @Override
    public String generateDoc(AldorDefine o, PsiElement originalElement) {

        Optional<String> externalLink = (o.getContainingFile().getLanguage().equals(SpadLanguage.INSTANCE)) ? makeExternalLink(o) : Optional.empty();

        //noinspection StringConcatenationMissingWhitespace
        return externalLink.map(x -> x + "<hr/>\n").orElse("") + docUtils.aldorDocStringFromContainingElement(o);
    }

    private Optional<String> makeExternalLink(AldorDefine aldorDefine) {
        AldorDefineStub stub = aldorDefine.getStub();
        Optional<String> nameMaybe;

        if (stub == null) {
            Optional<Boolean> opt = AldorPsiUtils.isTopLevel(aldorDefine.getParent()) ? Optional.of(true) : Optional.empty();
            nameMaybe = opt.flatMap(f -> aldorDefine.defineIdentifier()).map(PsiElement::getText);
        }
        else {
            if ((aldorDefine.definitionType() == AldorDefine.DefinitionType.CONSTANT)
                && (stub.defineInfo().level() == AldorDefineInfo.Level.TOP)) {
                nameMaybe = Optional.ofNullable(stub.defineId());
            }
            else {
                nameMaybe = Optional.empty();
            }
        }
        return nameMaybe.map(name -> "External link: <a href=\"http://fricas.github.io/api/" + name + ".html\">"+ name + "</a>");
    }

}
