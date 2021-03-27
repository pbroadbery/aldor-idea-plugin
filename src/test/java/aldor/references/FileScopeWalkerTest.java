package aldor.references;

import aldor.psi.AldorId;
import aldor.test_util.AssumptionAware;
import aldor.test_util.ExecutablePresentRule;
import aldor.test_util.SdkProjectDescriptors;
import aldor.util.VirtualFileTests;
import aldor.util.sexpr.SExpression;
import aldor.util.sexpr.SymbolPolicy;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Assert;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Collection;

import static aldor.symbolfile.AnnotationFileTests.lib;
import static aldor.symbolfile.AnnotationFileTests.name;
import static aldor.symbolfile.AnnotationFileTests.original;
import static aldor.symbolfile.AnnotationFileTests.srcpos;
import static aldor.symbolfile.AnnotationFileTests.syme;
import static aldor.symbolfile.AnnotationFileTests.type;
import static aldor.symbolfile.AnnotationFileTests.typeCode;
import static aldor.symbolfile.SymbolFileSymbols.Exporter;
import static aldor.symbolfile.SymbolFileSymbols.Id;
import static aldor.util.VirtualFileTests.createChildDirectory;
import static aldor.util.VirtualFileTests.createFile;
import static aldor.util.sexpr.SExpressions.list;

@SuppressWarnings("MagicNumber")
@Ignore("Too much hardcoding.. maybe the round trip tests are better")
public class FileScopeWalkerTest extends AssumptionAware.BasePlatformTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //JUnits.setLogToDebug();
    }

    @SuppressWarnings("unused")
    public void testFileReference() {
        VirtualFile root = VirtualFileTests.getProjectRoot(getProject());
        VirtualFile srcDir = createChildDirectory(root, "src");
        VirtualFile buildDir = createChildDirectory(root, "build");
        VirtualFile markerFile = createFile(srcDir, "configure.ac", "");
        VirtualFile defFile = createFile(srcDir, "def.as", "a == 2");
        VirtualFile useFile = createFile(srcDir, "use.as", "never;\nreturn a");

        VirtualFile defAnnotationFile = createFile(buildDir, "def.abn", createDefAnnotation().toString(SymbolPolicy.ALLCAPS));
        VirtualFile useAnnotationFile = createFile(buildDir, "use.abn", createUseAnnotation().toString(SymbolPolicy.ALLCAPS));
        PsiFile file = getPsiManager().findFile(useFile);

        Collection<AldorId> ids = PsiTreeUtil.findChildrenOfType(file, AldorId.class);

        PsiElement ref = FileScopeWalker.lookupBySymbolFile(ids.iterator().next());

        Assert.assertNotNull(ref);
        Assert.assertEquals(defFile.getPath(), ref.getContainingFile().getVirtualFile().getPath());
    }

    SExpression createDefAnnotation() {
        String file = "def";
        return list(
                list(//Syntax
                        list(Id, srcpos(file, 1, 1), syme(0))
                ),
                list(//Symbols
                        list(name("a"), srcpos(file, 1, 1), type(0), typeCode(888)),
                        list(name("T"))
                ),
                list(//Types
                        list(Id, syme(1)),
                        list(Id, syme(1))));
    }

    SExpression createUseAnnotation() {
        String file = "use";
        return list(
                list(//Syntax
                        list(Id, srcpos(file, 2, "return a".length()), syme(0))
                ),
                list(//Symbols
                        list(name("a"), type(0), typeCode(888), original(1)),
                        list(name("a"), type(0), typeCode(888), lib("def.ao")),
                        list(name("T"))
                ),
                list(//Types
                        list(Id, syme(2)),
                        list(Id, syme(2))));

    }


    @SuppressWarnings("unused")
    public void testTopLevelReference() {
        VirtualFile root = VirtualFileTests.getProjectRoot(getProject());
        VirtualFile srcDir = createChildDirectory(root, "src");
        VirtualFile buildDir = createChildDirectory(root, "build");
        VirtualFile markerFile = createFile(srcDir, "configure.ac", "");
        VirtualFile defFile = createFile(srcDir, "def.as", "\nD: with { f: () -> ()}");
        VirtualFile useFile = createFile(srcDir, "use.as", "\nf()$D");

        VirtualFile defAnnotationFile = createFile(buildDir, "def.abn", createTopLevelDefAnnotation().toString(SymbolPolicy.ALLCAPS));
        VirtualFile useAnnotationFile = createFile(buildDir, "use.abn", createTopLevelUseAnnotation().toString(SymbolPolicy.ALLCAPS));
        PsiFile file = getPsiManager().findFile(useFile);

        Collection<AldorId> ids = PsiTreeUtil.findChildrenOfType(file, AldorId.class);
        AldorId theDomainId = ids.stream().filter(x -> "D".equals(x.getName())).findFirst().orElseThrow(RuntimeException::new);
        AldorId theFunctionId = ids.stream().filter(x -> "f".equals(x.getName())).findFirst().orElseThrow(RuntimeException::new);

        PsiElement theDomain = FileScopeWalker.lookupBySymbolFile(theDomainId);
        PsiElement theFunction = FileScopeWalker.lookupBySymbolFile(theFunctionId);
        Assert.assertNotNull(theDomain);
        Assert.assertEquals(defFile.getPath(), theDomain.getContainingFile().getVirtualFile().getPath());
        Assert.assertNotNull(theFunction);
        Assert.assertEquals(defFile.getPath(), theFunction.getContainingFile().getVirtualFile().getPath());
    }

    SExpression createTopLevelDefAnnotation() {
        String file = "def";
        return list(
                list(//Syntax (no need)
                ),
                list(//Symbols
                        list(name("D"), srcpos(file, 2, 1), type(0), typeCode(0xDDD)),
                        list(name("f"), srcpos(file, 2, 11), type(1), typeCode(0xFFF)),
                        list(name("T"))
                ),
                list(//Types
                        list(Id, syme(2)),
                        list(Id, syme(2)),
                        list(Id, syme(2))
                ));
    }


    SExpression createTopLevelUseAnnotation() {
        String file = "use";
        return list(
                list(
                        list(Id, srcpos(file, 2, 1), syme(0)),
                        list(Id, srcpos(file, 2, 5), syme(1))
          /*      list(//Syntax
                        (|Apply|
                                (|Qualify|
                                (|Id| (|srcpos| "use" 2 1) (|syme| |ref| . 3))
        (|Id| (|srcpos| "use" 2 5) (|syme| |ref| . 5)))))
        (((|name| . |AldorLib|) (|type| |ref| . 0))
        */
                ),
                list(//Symbols
                        list(name("f"), type(1), original(3), typeCode(0xFFF)),
                        list(name("D"), type(1), list(Exporter, lib("def.ao"), typeCode(0xDDD)), typeCode(0xDDD)),
                        list(name("T"), type(1), lib("lang.ao"), typeCode(0x111)),
                        list(name("f"), type(1), lib("def.ao"), typeCode(0xFFF))
                ),
                list(//Types
                        list(Id, syme(2)),
                        list(Id, syme(2)),
                        list(Id, syme(2))
                ));
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return SdkProjectDescriptors.aldorSdkProjectDescriptor(ExecutablePresentRule.Aldor.INSTANCE);
    }
}
