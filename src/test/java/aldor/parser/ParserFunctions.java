package aldor.parser;

import aldor.psi.elements.AldorTypes;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings("ClassWithTooManyDependents")
public final class ParserFunctions {
    public static final int NOT_INTERACTIVE_MILLIS = 1000;
    private static final Logger LOG = Logger.getInstance(ParserFunctions.class);

    public static PsiElement parseAldorText(Project project, CharSequence text) {
        return parseAldorText(project, text, AldorTypes.CURLY_CONTENTS_LABELLED);
    }

    public static PsiElement parseAldorText(Project project, CharSequence text, IElementType elementType) {
        ParserDefinition aldorParserDefinition = new AldorParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(aldorParserDefinition, aldorParserDefinition.createLexer(project),
                text);

        PsiParser parser = aldorParserDefinition.createParser(project);
        ASTNode parsed = parser.parse(elementType, psiBuilder);

        return parsed.getPsi();
    }

    public static PsiElement parseSpadText(Project project, CharSequence text) {
        return parseSpadText(project, text, AldorTypes.SPAD_TOP_LEVEL);
    }

    public static PsiElement parseSpadText(Project project, CharSequence text, IElementType elementType) {
        ParserDefinition spadParserDefinition = new SpadParserDefinition();
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(spadParserDefinition, spadParserDefinition.createLexer(project),
                text);

        PsiParser parser = spadParserDefinition.createParser(project);
        ASTNode parsed = parser.parse(elementType, psiBuilder);

        return parsed.getPsi();
    }

    @NotNull
    public static List<PsiErrorElement> getPsiErrorElements(PsiElement psi) {
        final List<PsiErrorElement> errors = new ArrayList<>();

        psi.accept(new PsiRecursiveElementVisitor() {

            @Override
            public void visitErrorElement(PsiErrorElement element) {
                errors.add(element);
                super.visitErrorElement(element);
            }
        });
        return errors;
    }

    public static Collection<PsiElement> find(PsiElement elt, Predicate<PsiElement> predicate) {
        List<PsiElement> subElements = Lists.newArrayList();
        elt.accept(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (predicate.test(element)) {
                    subElements.add(element);
                }
                element.acceptChildren(this);
            }
        });
        return subElements;
    }


    @NotNull
    public static Multimap<FailReason, File> parseLibrary(Project project, File base, FileType fileType, Collection<String> blackList) {
        List<File> files = findAllSource(base);
        Multimap<FailReason, File> badFiles = ArrayListMultimap.create();
        for (File file: files) {
            long start = System.currentTimeMillis();
            LOG.info("Reading: " + file);
            if (blackList.contains(file.getName())) {
                continue;
            }
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
            assertNotNull(vf);
            if ((fileType != null) && (!Objects.equals(vf.getFileType(), fileType))) {
                continue;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
            assertNotNull(psiFile);
            String text = psiFile.getText();

            final PsiElement psi;
            if (file.getName().endsWith(".spad")) {
                psi = parseSpadText(project, text);
            }
            else {
                psi = parseAldorText(project, text);
            }

            final List<PsiErrorElement> errors = getPsiErrorElements(psi);
            long duration = System.currentTimeMillis() - start;
            //noinspection StringConcatenationMissingWhitespace
            LOG.info("... File " + file + " took " + duration + "ms");
            if (!errors.isEmpty()) {
                badFiles.put(FailReason.NoCompile, file);
            }
            if (duration > NOT_INTERACTIVE_MILLIS) {
                badFiles.put(FailReason.Slow, file);
            }
        }
        LOG.info("Compiled: " + files.size() + " " + badFiles.size() + " failed");
        return badFiles;
    }

    private static List<File> findAllSource(File base) {
        List<File> files = Lists.newArrayList();
        //noinspection ConstantConditions // list files => NPE?
        for (File file: base.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(findAllSource(file));
            }
            if (file.getName().endsWith(".as")) {
                files.add(file);
            }
            if (file.getName().endsWith(".spad")) {
                files.add(file);
            }
        }
        return files;
    }


    public enum FailReason {
        Slow, NoCompile
    }


}
