package aldor.editor.documentation;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.Syme;
import aldor.syntax.Syntax;
import aldor.syntax.SyntaxPrinter;
import aldor.syntax.components.Other;
import aldor.util.AnnotatedOptional;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class IdentifierDocumentationProvider extends TypedDocumentationProvider<AldorIdentifier> {
    // NB: Switch to a manager if we need per-project docs
    private static final DocumentationUtils docUtils = new DocumentationUtils();

    public IdentifierDocumentationProvider() {

    }

    @Override
    public String generateDoc(AldorIdentifier element, @Nullable PsiElement originalElement) {
        AnnotatedOptional<Syme, String> symeMaybe = docUtils.symeForElement(originalElement);

        if (symeMaybe.isPresent()) {
            SyntaxPrinter printer = SyntaxPrinter.instance();
            Syme syme = symeMaybe.get();
            Syntax exporter = syme.exporter();
            Syntax type = syme.type();

            String typeText = "<p><b>exporter:</b> " + printer.toString(exporter)
                    + "</b></p>" + "\n<p><b>type:</b> " + printer.toString(type) + "</p>";

            String docString = docUtils.aldorDocString(syme, element, originalElement);
            if (docString == null) {
                docString = docUtils.aldorDocStringFromIndex(element, originalElement);
            }
            return typeText + (docString == null ? "" : "<code>" + docString + "</code>");
        } else {
            String docString = "File lookup failed: " + symeMaybe.failInfo();
            String indexDoc = docUtils.aldorDocStringFromIndex(element, originalElement);
            if (indexDoc != null) {
                docString = docString + "<hr>\n" + indexDoc;
            }
            return docString;
        }
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(AldorIdentifier element, AldorIdentifier originalElement) {
        AnnotatedOptional<Syme, String> symeMaybe = docUtils.symeForElement(originalElement);

        if (!symeMaybe.isPresent()) {
            return null;
        }

        SyntaxPrinter printer = SyntaxPrinter.instance();
        Syme syme = symeMaybe.get();
        Syntax type = syme.type();
        Optional<Syntax> exporterMaybe = Optional.ofNullable(syme.exporter().is(Other.class) ? null : syme.exporter());
        Optional<String> exporterText = exporterMaybe.map(exporter -> " from <code>" + printer.toString(syme.exporter()) + "</code>");
        return syme.name() + ": <code>" + printer.toString(type) + "</code>" + exporterText.orElse("");
    }
}
