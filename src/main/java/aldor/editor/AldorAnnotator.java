package aldor.editor;

import aldor.parser.AldorTypes;
import aldor.psi.AldorPsiUtils;
import aldor.psi.AldorType;
import aldor.psi.AldorTypeE12;
import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Annotations - setting text to be funny colours/fonts based on parsed tree.
 * Supported: Types
 * On the list:
 *    - Macros
 *    - System commands (actually, this should be in the highlighter)
 *    - Errors (we'd need to compile the thing first)
 *    - Type tooltips
 *    - Potential quick fixes
 */
public class AldorAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof AldorType) {
            annotateType(element, holder);
        }
        else if (element instanceof AldorTypeE12) {
            annotateType(element, holder);
        }
    }

    private void annotateType(PsiElement element, AnnotationHolder holder) {
        if (AldorPsiUtils.containsElement(element, AldorTypes.WITH_PART) || AldorPsiUtils.containsElement(element, AldorTypes.ADD_PART)) {
            return;
        }
        TextRange range = element.getTextRange();
        Annotation typeAnnotation = holder.createInfoAnnotation(range, "");
        typeAnnotation.setTextAttributes(JavaHighlightingColors.ANNOTATION_NAME_ATTRIBUTES); //FIXME: Not Java
    }

}
