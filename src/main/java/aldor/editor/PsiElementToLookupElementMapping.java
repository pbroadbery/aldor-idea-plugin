package aldor.editor;

import aldor.psi.AldorDeclare;
import aldor.psi.AldorDefine;
import aldor.psi.AldorIdentifier;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.Nullable;

public interface PsiElementToLookupElementMapping {
    @Nullable
    LookupElement forMacro(AldorDefine define);
    @Nullable
    LookupElement forConstant(AldorDefine define);
    @Nullable
    LookupElement forDeclare(AldorDeclare define);
    @Nullable
    LookupElement forId(AldorIdentifier define);

}
