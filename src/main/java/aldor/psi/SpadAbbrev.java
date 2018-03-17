package aldor.psi;

import aldor.psi.stub.AbbrevInfo;
import aldor.psi.stub.SpadAbbrevStub;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;

@SuppressWarnings("CyclicClassDependency")
public interface SpadAbbrev extends StubBasedPsiElement<SpadAbbrevStub>, Navigatable {

    AbbrevInfo abbrevInfo();

    PsiElement setName(String newElementName);

}
