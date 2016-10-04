package aldor.syntax.components;

import aldor.psi.AldorIdentifier;
import aldor.syntax.Syntax;
import com.intellij.psi.PsiElement;

import java.util.Collections;

/**
 * Created by pab on 04/10/16.
 */
public class Id extends Syntax {
    private final AldorIdentifier id;
    private final String text;

    public Id(AldorIdentifier id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public String name() {
        return "Id";
    }

    @Override
    public PsiElement psiElement() {
        return id;
    }

    @Override
    public Iterable<Syntax> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return text;
    }

}
