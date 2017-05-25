package aldor.editor;

import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.Nullable;

public class AldorUsageTypeProvider implements UsageTypeProvider {
    public static final UsageType ALDOR_CODE = new UsageType("Aldor code");

    @Nullable
    @Override
    public UsageType getUsageType(PsiElement element) {
        // Really could be more specific: Used in import, type context
        return ALDOR_CODE;
    }
}
