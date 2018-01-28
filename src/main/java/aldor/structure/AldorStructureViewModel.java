package aldor.structure;

import aldor.file.AldorFile;
import aldor.file.AxiomFile;
import aldor.psi.AldorDefine;
import aldor.psi.impl.CollectingAldorVisitor;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class AldorStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    public AldorStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    @Override
    public Sorter[] getSorters() {
        return new Sorter [] { Sorter.ALPHA_SORTER };
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return new Filter[] { new MacroFilter() };
    }

    public static Collection<StructureViewTreeElement> getDirectChildren(PsiElement elt) {
        return Optional.ofNullable(new DirectChildVisitor().apply(elt)).orElse(Collections.emptyList());
    }

    @NotNull
    @Override
    protected Class<?>[] getSuitableClasses() {
        return new Class<?>[] { AldorFile.class, AldorDefine.class };
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return new AldorFileTreeElement((AxiomFile) getPsiFile());
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return element instanceof AldorFileTreeElement;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return false;
    }

    private static class AldorFileTreeElement extends PsiTreeElementBase<AxiomFile> {

        protected AldorFileTreeElement(AxiomFile psiElement) {
            super(psiElement);
        }

        @NotNull
        @Override
        public Collection<StructureViewTreeElement> getChildrenBase() {
            if (getElement() == null) {
                return Collections.emptyList();
            }
            return Arrays.stream(getElement().getChildren()).flatMap(elt -> getDirectChildren(elt).stream()).collect(Collectors.toList());
        }

        @Nullable
        @Override
        public String getPresentableText() {
            return (getElement() == null) ? "<deleted>" : getElement().getName();
        }
    }

    private static class DirectChildVisitor extends CollectingAldorVisitor<StructureViewTreeElement> {
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

    }


}
