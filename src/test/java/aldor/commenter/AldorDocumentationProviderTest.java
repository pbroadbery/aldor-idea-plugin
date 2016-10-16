package aldor.commenter;

import aldor.build.module.AldorModuleManager;
import aldor.build.module.AldorModuleType;
import aldor.psi.AldorIdentifier;
import aldor.util.SExpression;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static aldor.symbolfile.SymbolFileSymbols.Id;
import static aldor.symbolfile.SymbolFileSymbols.Name;
import static aldor.symbolfile.SymbolFileSymbols.Ref;
import static aldor.symbolfile.SymbolFileSymbols.SrcPos;
import static aldor.symbolfile.SymbolFileSymbols.Syme;
import static aldor.symbolfile.SymbolFileSymbols.Type;
import static aldor.util.SExpression.cons;
import static aldor.util.SExpression.integer;
import static aldor.util.SExpression.string;
import static aldor.util.SExpression.symbol;
import static aldor.util.SExpressions.list;

public class AldorDocumentationProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testDocProvider() throws IOException {
        AldorModuleManager mgr = AldorModuleManager.getInstance(getProject());
        List<VirtualFile> roots = mgr.aldorModules().stream().flatMap(mod -> Arrays.stream(ModuleRootManager.getInstance(mod).getContentRoots())).collect(Collectors.toList());
        System.out.println("Roots: " + roots);

        VirtualFile root = roots.get(0);

        VirtualFile virtualFile = createFile(root, "foo.as", "a; b; c; d;");
        createFile(root, "foo.abn", createMockTypeMarkup().toString());
        PsiFile file = getPsiManager().findFile(virtualFile);

        Collection<AldorIdentifier> ids = PsiTreeUtil.findChildrenOfType(file, AldorIdentifier.class);

        for (AldorIdentifier id : ids) {
            assertNotNull(id.getContainingFile());
            String docco = docForElement(id);
            System.out.println("Doc is: " + docco);
        }

        String firstDoc = docForElement(ids.iterator().next());
        assertTrue(firstDoc.contains("> T<"));

    }

    private String docForElement(PsiElement id) {
        return DocumentationManager.getProviderFromElement(id).generateDoc(id, null);
    }

    private SExpression createMockTypeMarkup() {
        String file = "foo";
        return list(
                list(//Syntax
                        list(Id, srcpos(file, 1, 1), syme(0)),
                        list(Id, srcpos(file, 1, 4), syme(1)),
                        list(Id, srcpos(file, 1, 7), syme(2))),
                list(//Symbols
                        list(cons(Name, symbol("a")), type(0)),
                        list(cons(Name, symbol("b")), type(1)),
                        list(cons(Name, symbol("c")), type(1)),
                        list(cons(Name, symbol("T")))),
                list(//Types
                        list(Id, syme(3)),
                        list(Id, syme(3))));

    }

    private SExpression type(int index) {
        return cons(Type, cons(Ref, integer(index)));
    }

    private SExpression srcpos(String file, int line, int column) {
        return list(SrcPos, string(file), integer(line), integer(column));
    }

    private SExpression syme(int ref) {
        return cons(Syme, cons(Ref, integer(ref)));
    }

    public VirtualFile createFile(VirtualFile dir, String name, String content) throws IOException {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                VirtualFile file = dir.createChildData(null, name);
                //noinspection NestedTryStatement
                try (OutputStream stream = file.getOutputStream(null)) {
                    stream.write(content.getBytes(Charset.defaultCharset()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        return dir.findChild(name);
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        //noinspection ReturnOfInnerClass
        return new LightProjectDescriptor() {

            @Override
            @NotNull
            public ModuleType<?> getModuleType() {
                return AldorModuleType.instance();
            }

        };
    }
}
