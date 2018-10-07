package aldor.structure;

import aldor.file.AldorFile;
import aldor.file.AxiomFile;
import aldor.psi.AldorDefine;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

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


}
