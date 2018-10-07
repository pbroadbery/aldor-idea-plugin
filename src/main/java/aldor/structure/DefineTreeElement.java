package aldor.structure;

import aldor.psi.AldorDefine;
import aldor.psi.AldorPsiUtils;
import aldor.ui.AldorIcons;
import aldor.ui.AldorTextAttributes;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

final class DefineTreeElement extends PsiTreeElementBase<AldorDefine> {
    DefineTreeElement(AldorDefine o) {
        super(o);
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        if (getElement() == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(getElement().getChildren()).flatMap(elt -> DirectChildVisitor.getDirectChildren(elt).stream()).collect(Collectors.toList());
    }

    @Override
    public String getPresentableText() {
        if ((getElement() != null) && !getElement().isValid()) {
            return "<invalid>";
        }
        if (getElement() == null) {
            return "<missing>";
        }
        return getElement().defineIdentifier().map(PsiElement::getText).orElse("<missing id>");
    }

    @Override
    public Icon getIcon(boolean open) {
        return AldorIcons.IDENTIFIER;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        final AldorDefine element = getValue();

        return new MyColoredItemPresentation(element);

    }

    private final class MyColoredItemPresentation implements ColoredItemPresentation {
        @NotNull
        private final AldorDefine element;
        @Nullable
        private final ItemPresentation presentation;

        private MyColoredItemPresentation(@NotNull AldorDefine element) {
            this.element = element;
            presentation = element.getPresentation();
        }

        @Override
        public TextAttributesKey getTextAttributesKey() {
            AldorPsiUtils.DefinitionClass defClass = AldorPsiUtils.definitionClassForDefine(element);

            return textAttributesForDefinitionClass(defClass);
        }

        @Override
        public String getPresentableText() {
            return ((presentation == null) ? DefineTreeElement.this.getPresentableText() : presentation.getPresentableText()) + "-"+AldorPsiUtils.definitionClassForDefine(element);
        }

        @Nullable
        @Override
        public String getLocationString() {
            return null;
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
            return DefineTreeElement.this.getIcon(false);
        }
    }

    public static TextAttributesKey textAttributesForDefinitionClass(AldorPsiUtils.DefinitionClass defClass) {
        switch (defClass) {
            case CATEGORY:
                return AldorTextAttributes.ALDOR_CATEGORY_ATTRIBUTES;
            case DOMAIN:
                return AldorTextAttributes.ALDOR_DOMAIN_ATTRIBUTES;
            case VALUE:
                return AldorTextAttributes.ALDOR_VALUE_ATTRIBUTES;
        }
        throw new IllegalArgumentException("Missing " + defClass);
    }

}
