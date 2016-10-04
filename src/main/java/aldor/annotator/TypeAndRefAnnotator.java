package aldor.annotator;

import com.intellij.ide.highlighter.JavaHighlightingColors;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by pab on 03/10/16.
 */
public class TypeAndRefAnnotator extends ExternalAnnotator<TypeAndRefAnnotator.SomeInfo, TypeAndRefAnnotator.FullInfo> {

    public static class SomeInfo {
        private final Editor e;

        public SomeInfo(Editor e) {
            this.e = e;
        }
    }

    public static class FullInfo {
        private final SomeInfo someInfo;

        public FullInfo(SomeInfo someInfo) {
            this.someInfo = someInfo;
        }
    }


    @Nullable
    @Override
    public SomeInfo collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new SomeInfo(editor);
    }

    @Nullable
    @Override
    public FullInfo doAnnotate(SomeInfo collectedInfo) {
        return new FullInfo(collectedInfo);
    }

    @Override
    public void apply(@NotNull PsiFile file, FullInfo annotationResult, @NotNull AnnotationHolder holder) {
        Document doc = annotationResult.someInfo.e.getDocument();
        if (doc.getLineCount() < 4) {
            return;
        }
        int offs = doc.getLineStartOffset(3);
        PsiElement elt = PsiUtilCore.getElementAtOffset(file, offs);
        if (elt != null) {
            Annotation annotation = holder.createInfoAnnotation(elt, "");
            annotation.setTextAttributes(JavaHighlightingColors.ANNOTATION_NAME_ATTRIBUTES);
        }
    }
}

