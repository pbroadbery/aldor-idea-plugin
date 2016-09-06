package aldor.annotator;

import aldor.AldorPsiUtils;
import aldor.AldorTypes;
import aldor.psi.AldorType;
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
 *    - Potential quickfixes
 */
public class AldorAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof AldorType) {
            annotateType((AldorType) element, holder);
        }
    }

    private void annotateType(@SuppressWarnings("TypeMayBeWeakened") AldorType element, AnnotationHolder holder) {
        if (AldorPsiUtils.containsElement(element, AldorTypes.WITH_PART) || AldorPsiUtils.containsElement(element, AldorTypes.ADD_PART)) {
            return;
        }
        TextRange range = element.getTextRange();
        Annotation typeAnnotation = holder.createInfoAnnotation(range, "");
        typeAnnotation.setTextAttributes(JavaHighlightingColors.ANNOTATION_NAME_ATTRIBUTES);
        typeAnnotation.setTooltip(AldorPsiUtils.parse(element).toString());
    }

}
