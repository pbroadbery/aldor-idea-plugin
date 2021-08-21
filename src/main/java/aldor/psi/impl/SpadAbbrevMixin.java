package aldor.psi.impl;

import aldor.lexer.AldorTokenTypes;
import aldor.lexer.SysCmd;
import aldor.psi.AbbrevClassifier;
import aldor.psi.AldorElementFactory;
import aldor.psi.AldorVisitor;
import aldor.psi.SpadAbbrev;
import aldor.psi.stub.AbbrevInfo;
import aldor.psi.stub.SpadAbbrevStub;
import aldor.references.SpadAbbrevReference;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpadAbbrevMixin extends StubBasedPsiElementBase<SpadAbbrevStub> implements SpadAbbrev {
    // Must be public - parser generator insists on it.
    public SpadAbbrevMixin(@NotNull ASTNode node) {
        super(node);
    }

    public SpadAbbrevMixin(SpadAbbrevStub stub, @SuppressWarnings("rawtypes") IStubElementType type) {
        super(stub, type);
    }

    SysCmd sysCmd() {
        return SysCmd.parse(this.getText());
    }

    private static final Pattern pattern = Pattern.compile("\\)abbrev\\s+(\\w+)\\s+(\\w+)\\s+(\\w+)");

    @Override
    public AbbrevInfo abbrevInfo() {
        if (this.getStub() != null) {
            return getStub().info();
        }

        String content = this.getText();
        Matcher match = pattern.matcher(content);
        if (!match.matches()) {
            return new AbbrevInfo();
        } else {
            String classifierText = match.group(1);
            AbbrevClassifier classifier = AbbrevClassifier.forText(classifierText);
            if (classifier == null) {
                return new AbbrevInfo();
            }
            String abbrev = match.group(2);
            String name = match.group(3);
            int nameIndex = match.start(3);
            return new AbbrevInfo(classifier, abbrev, name, nameIndex);
        }
    }

    @Override
    public PsiElement setName(String newElementName) {
        ASTNode ref = getNode().findChildByType(AldorTokenTypes.TK_SysCmdAbbrev);
        if (ref == null) {
            return this;
        }
        String text = ref.getText().substring(0, abbrevInfo().nameIndex()) + newElementName;
        PsiElement newAbbrev = AldorElementFactory.createIdentifier(getProject(), text);
        ASTNode newRef = newAbbrev.getNode().findChildByType(AldorTokenTypes.TK_SysCmdAbbrev);
        if ((newRef != null)) {
            getNode().replaceChild(ref, newRef);
        }
        return this;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        final PsiReference[] fromProviders = ReferenceProvidersRegistry.getReferencesFromProviders(this);

        PsiReference ref = new SpadAbbrevReference(this);
        return ArrayUtil.prepend(ref, fromProviders);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof AldorVisitor) {
            accept((AldorVisitor)visitor);
        } else {
            super.accept(visitor);
        }
    }

    public void accept(@NotNull AldorVisitor aldorVisitor) {
        aldorVisitor.visitSpadAbbrev(this);
    }


}
