package aldor.build.module;

import aldor.psi.AldorIdentifier;
import aldor.symbolfile.SrcPos;
import aldor.symbolfile.Syme;
import aldor.util.AnnotatedOptional;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AnnotationFileNavigator {
    @Nullable
    AldorIdentifier lookupReference(@NotNull PsiElement element);

    @Nullable
    SrcPos findSrcPosForElement(PsiElement element);

    @Nullable
    AldorIdentifier findElementForSrcPos(PsiFile file, SrcPos srcPos);

    AnnotatedOptional<Syme,String> symeForElement(PsiElement element);
}
