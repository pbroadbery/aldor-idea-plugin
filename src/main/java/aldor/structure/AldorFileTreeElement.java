package aldor.structure;

import aldor.file.AxiomFile;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

class AldorFileTreeElement extends PsiTreeElementBase<AxiomFile> {

    protected AldorFileTreeElement(AxiomFile psiElement) {
        super(psiElement);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        if (getElement() == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(getElement().getChildren()).flatMap(elt -> DirectChildVisitor.getDirectChildren(elt).stream()).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return (getElement() == null) ? "<deleted>" : getElement().getName();
    }
}
