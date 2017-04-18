package aldor.build.module;

import aldor.lexer.IndentWidthCalculator;
import aldor.psi.AldorIdentifier;
import aldor.symbolfile.SrcPos;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharSequenceSubSequence;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

class LineNumberMap {
    private final NavigableMap<Integer, Integer> lineNumberForOffset;
    private final Map<Integer, Integer> offsetForLineNumber;
    private final IndentWidthCalculator widthCalculator; // Should really be per-project or something.

    LineNumberMap(@SuppressWarnings("TypeMayBeWeakened") PsiFile file) {
        this.lineNumberForOffset = scanLines(file);
        widthCalculator = new IndentWidthCalculator();
        offsetForLineNumber = lineNumberForOffset.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public int offsetForLine(int lineNumber) {
        return offsetForLineNumber.get(lineNumber);
    }

    private NavigableMap<Integer, Integer> scanLines(PsiElement file) {
        NavigableMap<Integer, Integer> lineForOffset = new TreeMap<>();
        String text = file.getText();
        lineForOffset.put(0, 0);
        int line = 1;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            if (text.charAt(i) == '\n') {
                // +1 is because we want the offset to be at the start of the line, not the newline char itself.
                lineForOffset.put(i + 1, line);
                line++;
            }
        }
        return lineForOffset;
    }

    public SrcPos findSrcPosForElement(PsiElement element) {
        int textOffset = element.getTextOffset();
        Integer lineOffset = lineNumberForOffset.headMap(textOffset, true).lastKey();
        int column = widthCalculator.width(element.getContainingFile().getText().subSequence(lineOffset, textOffset));
        return new SrcPos(StringUtil.trimExtension(element.getContainingFile().getName()), 1 + lineNumberForOffset.get(lineOffset), 1 + column);
    }

    @Nullable
    public AldorIdentifier findPsiElementForSrcPos(PsiFile file, int line, int col) {
        int lineOffset = offsetForLine(line - 1);
        int colOffset = lineOffset + widthCalculator.offsetForWidth(new CharSequenceSubSequence(file.getText(), lineOffset, file.getTextLength()), col);
        return PsiTreeUtil.findElementOfClassAtOffset(file, colOffset, AldorIdentifier.class, true);
    }
}
