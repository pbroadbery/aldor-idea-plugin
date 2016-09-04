package aldor.lexer;

import aldor.SysCmd;
import aldor.SysCmd.SysCommandType;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import static aldor.lexer.AldorTokenTypes.KW_Indent;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.TK_SysCmd;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;

/**
 * Given a lexer, try to find a linear form of the structure
 */
public class Linearise {
    private final IndentWidthCalculator widthCalculator = new IndentWidthCalculator();
    private List<PiledSection> sections = null;

    public void linearise(AldorLexerAdapter lexer) {
        sections = scanForPiledSections(lexer);
        for (PiledSection section : sections) {
            blockMarkers(section);
            section.showIndents();
        }
    }

    public List<PiledSection> scanForPiledSections(AldorLexerAdapter lexer) {
        List<PiledSection> piles = new ArrayList<>();
        while (lexer.getTokenStart() < lexer.getBufferEnd()) {
            if (isSysCommand(SysCommandType.Pile, lexer)) {
                advanceLexerToLineStart(lexer);
                PiledSection ps = parsePiledSection(lexer);
                if (!ps.lines().isEmpty()) {
                    piles.add(ps);
                }

            } else {
                lexer.advance();
            }
        }
        return piles;
    }

    private void advanceLexerToLineStart(AldorLexerAdapter lexer) {
        while ((lexer.getTokenType() != null) && (!Objects.equals(lexer.getTokenType(), KW_NewLine))) {
            lexer.advance();
        }
        lexer.advance();
    }

    private static boolean isSysCommand(SysCommandType command, AldorLexerAdapter lexer) {
        IElementType eltType = lexer.getTokenType();
        if (Objects.equals(eltType, TK_SysCmd)) {
            SysCmd sysCmd = SysCmd.parse(lexer.getTokenText());
            if (sysCmd.type() == command) {
                return true;
            }
        }
        return false;
    }

    public void blockMarkers(PiledSection section) {
        Deque<Integer> indents = new ArrayDeque<>();
        scanBlock(indents, section, 0);
    }

    private int scanBlock(Deque<Integer> indents, PiledSection section, final int startIndex) {
        indents.push(section.indentForLine(startIndex));
        int index = startIndex+1;
        while (index < section.lines().size()) {
            if (isBackSetRequired(section, index - 1)) {
                index = scanBlock(indents, section, index);
            }
            else {
                index++;
            }
        }
        section.addBlock(startIndex, index);
        return index;
    }


    /* Typically, we want a block boundary here, except:
    *  "isBackSetRequired" decides whether a BackSet is needed between tl1 and tl2.
    *  Normally a BackSet is needed.   The exceptions are:
    *
    * 1. tl1 contains only ++ comments (-- comments already deleted)
    *    This allows
    *
    *	++ xxx
    *	f: T -> Y
    *
    * 2. tl1 ends with "," or an opener ("(" "[" etc).
    *    This allows
    *
    *	f(x,		  a := [
    *	  y,		     1, 2, 3,
    *	  z)		     4, 5, 6 ]
    *
    * 3. tl2 begins with "in", "then", "else" or a closer (")" "]" "}" etc)
    *    (i.e. words which CANNOT start an expression).
    *    This allows
    *
    *	if aaa		  let		     f(x,	  a := [1, 2, 3,
    *	then bbb	     f == 1	       y		4, 5, 6
    *	else ccc	  in x := f+f	     )		       ]
    *
    */
    private boolean isBackSetRequired(PiledSection section, int index) {
        if (section.lines().size() == (index + 1)) {
            return false;
        }

        SrcLine thisLine = section.lines().get(index);
        SrcLine nextLine = section.lines().get(index + 1);
        if (thisLine.indent() >= nextLine.indent()) {
            return false;
        }
        /* 1. */
        if (thisLine.isBlank() || nextLine.isBlank()) {
            return false;
        }

        /* 2. */
        if (AldorTokenTypes.isOpener(thisLine.lastToken()) || AldorTokenTypes.KW_Comma.equals(thisLine.lastToken())) {
            return false;
        }

        /* 3. */
        if (AldorTokenTypes.isFollower(nextLine.firstToken()) || AldorTokenTypes.isCloser(nextLine.firstToken())) {
            return false;
        }
        return true;
    }


    private PiledSection parsePiledSection(AldorLexerAdapter lexer) {

        if (lexer.getTokenType() == null) {
            return new PiledSection(Collections.emptyList());
        }

        SrcLine currentLine = new SrcLine(currentIndent(lexer), lexer.getTokenStart());
        if (Objects.equals(lexer.getTokenType(), KW_Indent)) {
            lexer.advance();
        }

        List<SrcLine> lines = new ArrayList<>();
        while (lexer.getTokenStart() < lexer.getBufferEnd()) {
            IElementType eltType = lexer.getTokenType();
            if (isSysCommand(SysCommandType.EndPile, lexer)) {
                advanceLexerToLineStart(lexer);
                break;
            }

            if (eltType.equals(KW_NewLine)) {
                if (!currentLine.tokens().isEmpty()) {
                    lines.add(currentLine);
                }
                lexer.advance();
                currentLine = new SrcLine(currentIndent(lexer), lexer.getTokenStart());
                if (!Objects.equals(lexer.getTokenType(), KW_Indent)) {
                    currentLine.add(lexer.getTokenType());
                }
            } else if (!eltType.equals(WHITE_SPACE)) {
                currentLine.add(eltType);
            }
            lexer.advance();
        }
        if (!currentLine.tokens().isEmpty()) {
            lines.add(currentLine);
        }

        return new PiledSection(lines);
    }

    private int currentIndent(AldorLexerAdapter lexer) {
        if (!Objects.equals(lexer.getTokenType(), KW_Indent)) {
            return 0;
        } else {
            return widthCalculator.width(lexer.getTokenSequence());
        }
    }

    public boolean isBlockStart(int tokenStart) {
        for (PiledSection section : sections) {
            if (section.start() > tokenStart) {
                return false;
            }
            if (section.isBlockStart(tokenStart)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtBlockNewLine(int tokenStart) {
        for (PiledSection section : sections) {
            if (section.start() > tokenStart) {
                return false;
            }
            if (section.isBlockNewline(tokenStart)) {
                return true;
            }
        }
        return false;
    }


    public static class PiledSection {
        private final List<SrcLine> lines;
        private final SortedMap<Integer, Integer> blocks;

        public PiledSection(List<SrcLine> lines) {
            this.blocks = new TreeMap<>();
            this.lines = lines;
        }

        @Override
        public String toString() {
            return "{PS: " + lines.size() + "}";
        }

        public void showIndents() {
            for (SrcLine line : lines) {
                if (line.tokens().isEmpty()) {
                    System.out.format("%3d .. blank\n", line.indent());
                }
                if (line.tokens().size() == 1) {
                    System.out.format("%3d %s\n", line.indent(), line.tokens().get(0));
                }
                if (line.tokens().size() > 1) {
                    System.out.format("%3d %s...%s\n", line.indent(), line.tokens().get(0), line.lastToken());
                }
            }
        }

        public List<SrcLine> lines() {
            return lines;
        }

        public void addBlock(int startIndex, int endIndex) {
            blocks.put(lines.get(startIndex).startPosition(), endIndex);
        }

        public SortedMap<Integer, Integer> blockMarkers() {
            return blocks;
        }

        public int start() {
            return lines.get(0).startPosition();
        }

        public boolean isBlockStart(int tokenStart) {
            return blocks.containsKey(tokenStart);
        }

        public boolean isBlockNewline(int tokenStart) {
            for (SrcLine line : lines()) {
                if (tokenStart == line.startPosition()) {
                    return true;
                }
            }
            return false;
        }

        public Integer indentForLine(int startIndex) {
            return lines.get(startIndex).indent();
        }
    }


    private static class SrcLine {
        private final List<IElementType> tokens = new ArrayList<>();
        private final int indent;
        private final int startPosition;

        SrcLine(int indent, int startPosition) {
            this.indent = indent;
            this.startPosition = startPosition;
        }

        public void add(IElementType eltType) {
            tokens.add(eltType);
        }

        public int indent() {
            return indent;
        }

        public List<IElementType> tokens() {
            return tokens;
        }

        public IElementType lastToken() {
            return tokens.get(tokens.size() - 1);
        }

        public int startPosition() {
            return startPosition;
        }

        public boolean isBlank() {
            return (tokens.size() == 1) && AldorTokenTypes.DOC_TOKENS.contains(firstToken());
        }

        public IElementType firstToken() {
            return tokens.get(0);
        }

        @Override
        public String toString() {
            //noinspection StringConcatenationMissingWhitespace
            return "{SL: " + startPosition + " indent: " + indent + " " + tokens.get(0) + (tokens.size() ==  1 ? "" : " .. " + lastToken()) + "}";
        }
    }

}
