package aldor.structure;

import aldor.psi.AldorDefine;
import aldor.psi.CollectingAldorVisitor;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

class DirectChildVisitor extends CollectingAldorVisitor<StructureViewTreeElement> {
    @Override
    public void visitDefine(@NotNull AldorDefine o) {
        this.add(createViewElement(o));
    }

    @Override
    public void visitPsiElement(@NotNull PsiElement o) {
        o.acceptChildren(this);
    }

    @NotNull
    private StructureViewTreeElement createViewElement(AldorDefine o) {
        return new DefineTreeElement(o);
    }

    public static Collection<StructureViewTreeElement> getDirectChildren(PsiElement elt) {
        return Optional.ofNullable(new DirectChildVisitor().apply(elt)).orElse(Collections.emptyList());
    }

}
