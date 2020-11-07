package aldor.editor;

import aldor.psi.AldorIdentifier;
import aldor.psi.AldorVisitor;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Annotations - setting text to be funny colours/fonts based on parsed tree.
 * Supported: Known identifiers; they show up underlined
 * On the list:
 *    - Macros
 *    - System commands (actually, this should be in the highlighter)
 *    - Errors (we'd need to compile the thing first)
 *    - Potential quick fixes
 */

public class SpadAnnotator implements Annotator {
    // TODO: This is mostly to test that lookup works ok
    private static final TextAttributes attributes = new TextAttributes();
    static {
        attributes.setForegroundColor(JBColor.GREEN.darker());
        attributes.setEffectType(EffectType.BOLD_LINE_UNDERSCORE);
    }

    private static final TextAttributesKey key = TextAttributesKey.createTextAttributesKey("SPAD_KNOWN", attributes);
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (holder.isBatchMode()) {
            return;
        }

        element.accept(new SpadAnnotationVisitor(holder));
    }

    private static final class SpadAnnotationVisitor extends AldorVisitor {
        private final AnnotationHolder holder;

        private SpadAnnotationVisitor(AnnotationHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitIdentifier(@NotNull AldorIdentifier id) {
            @Nullable PsiReference ref = id.getReference();
            if (ref != null) {
                Annotation annotation = holder.createInfoAnnotation(id.getTextRange(), "");
                annotation.setTooltip(ref.getElement().getContainingFile().getName());
                annotation.setTextAttributes(key);
            }
        }
    }

}
