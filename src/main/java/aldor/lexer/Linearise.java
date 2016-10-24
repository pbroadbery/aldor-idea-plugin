package aldor.lexer;

import aldor.lexer.SysCmd.SysCommandType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static aldor.lexer.AldorLexerAdapter.LexMode.Spad;
import static aldor.lexer.AldorTokenTypes.KW_Add;
import static aldor.lexer.AldorTokenTypes.KW_Always;
import static aldor.lexer.AldorTokenTypes.KW_But;
import static aldor.lexer.AldorTokenTypes.KW_CCurly;
import static aldor.lexer.AldorTokenTypes.KW_CParen;
import static aldor.lexer.AldorTokenTypes.KW_Catch;
import static aldor.lexer.AldorTokenTypes.KW_Else;
import static aldor.lexer.AldorTokenTypes.KW_Finally;
import static aldor.lexer.AldorTokenTypes.KW_Indent;
import static aldor.lexer.AldorTokenTypes.KW_NewLine;
import static aldor.lexer.AldorTokenTypes.KW_OCurly;
import static aldor.lexer.AldorTokenTypes.KW_OParen;
import static aldor.lexer.AldorTokenTypes.KW_Then;
import static aldor.lexer.AldorTokenTypes.KW_Try;
import static aldor.lexer.AldorTokenTypes.KW_With;
import static aldor.lexer.AldorTokenTypes.TK_PostDoc;
import static aldor.lexer.AldorTokenTypes.TK_SysCmd;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;

/**
 * Given a lexer, try to find a linear form of the structure
 */
public class Linearise {
    private static final TokenSet OpenBrackets = TokenSet.create(AldorTokenTypes.KW_OBrack, KW_OParen, KW_OCurly);
    private static final TokenSet CloseBrackets = TokenSet.create(AldorTokenTypes.KW_CBrack, KW_CParen, KW_CCurly);

    private final IndentWidthCalculator widthCalculator = new IndentWidthCalculator();
    private List<PiledSection> sections = null;

    public void linearise(AldorLexerAdapter lexer) {
        if (lexer.mode() == Spad) {
            sections = Collections.singletonList(parsePiledSection(lexer));
        }
        else {
            sections = scanForPiledSections(lexer);
        }
        for (PiledSection section : sections) {
            System.out.println("Scanning: " + section.lines());
            blockMarkers(section);
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

    @Override
    public String toString() {
        return sections.toString();
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
        (new BlockScanner()).scan(section);
    }

    private class BlockScanner {

        private void scan(PiledSection section) {
            scan(section, 0);
        }

        private int scan(PiledSection section, final int startIndex) {
            int index = startIndex + 1;
            while (index < section.lines().size()) {
                SrcLine prevLine = section.lines().get(index - 1);
                SrcLine thisLine = section.lines().get(index);
                if (prevLine.indent() < thisLine.indent()) {
                    System.out.println(" - New block");
                    index = scan(section, index);
                } else if (section.isPileRequired(prevLine, thisLine)) {
                    System.out.println(" - New block (forced) " + prevLine.startPosition());
                    index = scan(section, index);
                }
                if (prevLine.indent() > thisLine.indent()) {
                    System.out.println(" - End block");
                    break;
                } else {
                    index++;
                }
            }
            if (startIndex > 0) {
                System.out.println("Add block: " + startIndex + " " + index);
                section.addBlock(startIndex, index);
            }
            return index;
        }
    }

    private PiledSection parsePiledSection(AldorLexerAdapter lexer) {

        if (lexer.getTokenType() == null) {
            return new PiledSection(lexer.getCurrentPosition().getOffset(), Collections.emptyList());
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
                currentLine.lastPosition(lexer.getTokenEnd());
                currentLine.setType();
                lines.add(currentLine);
                lexer.advance();
                currentLine = new SrcLine(currentIndent(lexer), lexer.getTokenStart());
            } else {
                if (eltType.equals(WHITE_SPACE)
                        || eltType.equals(KW_Indent)) {
                    currentLine.lastPosition(lexer.getTokenEnd());
                } else {
                    currentLine.add(eltType, lexer.getTokenEnd());
                }
                lexer.advance();
            }
        }
        currentLine.setType();
        lines.add(currentLine);

        return new PiledSection(lexer.getCurrentPosition().getOffset(), lines);
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

    public boolean isBlockEnd(int tokenStart) {
        for (PiledSection section : sections) {
            if (section.start() > tokenStart) {
                return false;
            }
            if (section.isBlockEnd(tokenStart)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPileMode(int tokenStart) {
        for (PiledSection section : sections) {
            if (section.start() > tokenStart) {
                return false;
            }
            if (section.start() < tokenStart) {
                return true;
            }
        }
        return false;
    }

    public int indentLevel(int c) {
        for (PiledSection section : sections) {
            if (section.start() > c) {
                return -1;
            }
            if (section.endOffset() > c) {
                return section.indentLevel(c);
            }
        }
        return -1;
    }


    public static class PiledSection {
        private final NavigableMap<Integer, Integer> lineIndexForOffset;
        private final NavigableMap<Integer, Integer> lineEndForLineStart;
        private final SortedSet<Integer> blockEnds;
        private final List<SrcLine> lines;
        private final int endOffset;

        public PiledSection(int offset, List<SrcLine> lines) {
            this.lineIndexForOffset = new TreeMap<>();
            this.lineEndForLineStart = new TreeMap<>();
            this.blockEnds = new TreeSet<>();
            this.lines = joinContinuationLines(lines);
            int i = 0;
            for (SrcLine line : this.lines) {
                lineIndexForOffset.put(line.startPosition(), i);
                i++;
            }
            endOffset = offset;
        }

        private List<SrcLine> joinContinuationLines(@SuppressWarnings("TypeMayBeWeakened") List<SrcLine> lines) {
            List<SrcLine> joinedLines = new ArrayList<>(lines.size());
            SrcLine lastLine = null;
            int braceCount = 0;

            for (SrcLine line : lines) {
                //noinspection StatementWithEmptyBody
                if (line.isBlank() && (lastLine == null)) {
                    System.out.println("Skipping " + line);
                }
                else if (!line.isPreDocument() && line.isBlank() && (lastLine != null)) {
                    lastLine.join(line);
                } else if (lastLine == null) {
                    joinedLines.add(line);
                    lastLine = line;
                } else if (lastLine.isPreDocument() && line.isPreDocument()) {
                    lastLine.join(line);
                } else if (braceCount > 0) {
                    lastLine.joinTokens(line);
                } else if (!isBackSetRequired(lastLine, line)) {
                    lastLine.joinTokens(line);
                } else {
                    joinedLines.add(line);
                    lastLine = line;
                }
                // NB: Avoid having count fall below zero, as it's most likely a typo
                braceCount = line.bracketDelta(braceCount);
            }
            return joinedLines;
        }

        @Override
        public String toString() {
            return "{PS: " + lines + "}";
        }

        public int endOffset() {
            return endOffset;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public List<SrcLine> lines() {
            return lines;
        }

        public void addBlock(int startIndex, int endIndex) {
            lineEndForLineStart.put(startIndex, endIndex);
            blockEnds.add(endIndex);
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public NavigableMap<Integer, Integer> blockMarkers() {
            return lineEndForLineStart;
        }

        public int start() {
            return lineIndexForOffset.firstKey();
        }

        public boolean isBlockStart(int tokenStart) {
            if (!lineIndexForOffset.containsKey(tokenStart)) {
                return false;
            }
            int idx = lineIndexForOffset.get(tokenStart);
            return lineEndForLineStart.containsKey(idx);
        }

        public boolean isBlockEnd(int tokenEnd) {
            if (tokenEnd >= endOffset) {
                return false;
            }
            int idx = lineIndexForOffset.floorEntry(tokenEnd - 1).getValue();
            SrcLine line = lines.get(idx);
            if (line.lastPosition() != tokenEnd) {
                return false;
            }
            // block ends are the start of the following line.
            return blockEnds.contains(idx + 1);
        }

        public boolean isBlockNewline(int tokenEnd) {
            if (tokenEnd >= endOffset) {
                return false;
            }
            int idx = lineIndexForOffset.floorEntry(tokenEnd - 1).getValue();
            SrcLine line = lines.get(idx);
            SrcLine nextLine = (idx < (lines.size() - 1)) ? lines.get(idx + 1) : null;
            if (line.isPreDocument()) {
                return false;
            }
            if ((line.lastPosition() == tokenEnd) && AldorTokenTypes.isFollower(lines.get(idx).lastToken())) {
                return true;
            }
            if ((line.lastPosition() == tokenEnd) && (nextLine != null)
                    && !AldorTokenTypes.isFollower(nextLine.firstToken())
                    && (line.indent() == nextLine.indent())) {
                return true;
            }
            return false;
        }

        public Integer indentForLine(int startIndex) {
            return lines.get(startIndex).indent();
        }

        public int indentLevel(int c) {
            Map.Entry<Integer, Integer> ent = lineIndexForOffset.floorEntry(c);
            if (ent == null) {
                System.out.println("No line for offset " + c + " " + lineIndexForOffset);
                throw new IllegalStateException("Missing line offset for " + c);
            }
            return lines.get(ent.getValue()).indent();
        }


        /*
         * "isPileRequired" decides whether a SetTab/BackTab empiling is needed
         *  for the line which is to be joined added
         *
         *  A single line pile is formed whenever the previous word is an alphabetic
         *  language keyword, e.g.  "return", "then", "else", etc.
         *  (Note, this does not include user-definable operators such as "quo".)
         */
        private boolean isPileRequired(SrcLine lastLine, SrcLine thisLine) {
            IElementType tok = lastLine.lastToken();
            if (thisLine.indent() == lastLine.indent())
                return false;
            // TODO: Use a set!
            return Objects.equals(tok, KW_Then) || Objects.equals(tok, KW_Else) ||
                    Objects.equals(tok, KW_With) || Objects.equals(tok, KW_Add) ||
                    Objects.equals(tok, KW_Try) || Objects.equals(tok, KW_But) ||
                    Objects.equals(tok, KW_Catch) || Objects.equals(tok, KW_Finally) ||
                    Objects.equals(tok, KW_Always);
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
        private boolean isBackSetRequired(SrcLine thisLine, SrcLine nextLine) {
            boolean flg = isBackSetRequired1(thisLine, nextLine);
            System.out.println("Backset: " + thisLine + " "+ nextLine
                    + " Last open: " + AldorTokenTypes.isOpener(thisLine.lastToken())
                    + " Next follow: " + AldorTokenTypes.isFollower(nextLine.firstToken())
                    + " Next close: " + AldorTokenTypes.isCloser(nextLine.firstToken())
                    + " = " + flg);
            return flg;
        }
        private boolean isBackSetRequired1(SrcLine thisLine, SrcLine nextLine) {
            if (nextLine.isPreDocument()) {
                return true;
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
    }


    private static class SrcLine {
        private final List<IElementType> tokens = new ArrayList<>();
        private final List<IElementType> allTokens = new ArrayList<>(); // Probably don't need this.
        private int indent;
        private final int startPosition;
        private int lastPosition;
        private boolean isPreDocument = false;

        SrcLine(int indent, int startPosition) {
            this.indent = indent;
            this.startPosition = startPosition;
            this.lastPosition = startPosition;
        }

        public void add(IElementType eltType, int lastPosition) {
            tokens.add(eltType);
            allTokens.add(eltType);
            this.lastPosition = lastPosition;
        }

        public int indent() {
            return indent;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Collection<IElementType> tokens() {
            return tokens;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Collection<IElementType> allTokens() {
            return allTokens;
        }

        public IElementType lastToken() {
            return tokens.get(tokens.size() - 1);
        }

        public int startPosition() {
            return startPosition;
        }

        public int lastPosition() {
            return lastPosition;
        }

        public boolean isBlank() {
            if (tokens.isEmpty()) {
                return true;
            }
            return (allTokens.size() == 1) && AldorTokenTypes.WHITESPACE_TOKENS.contains(firstToken());
        }

        public IElementType firstToken() {
            return tokens.get(0);
        }

        @Override
        public String toString() {
            if (isBlank()) {
                return "{SL: " + startPosition + " indent: " + indent + "}";
            } else {
                //noinspection StringConcatenationMissingWhitespace
                return "{SL: " + startPosition + " indent: " + indent + " " + tokens.get(0) + (tokens.size() == 1 ? "" : " .. " + lastToken()) + "}";
            }
        }

        public void indent(int lastIndent) {
            this.indent = lastIndent;
        }

        public void join(SrcLine line) {
            if (!isPreDocument()) {
                assert !line.isPreDocument();
            }
            this.allTokens.addAll(line.allTokens());
            this.lastPosition = line.lastPosition();
        }

        public void joinTokens(SrcLine line) {
            if (!isPreDocument()) {
                assert !line.isPreDocument();
            }
            this.tokens.addAll(line.tokens());
            this.allTokens.addAll(line.allTokens());
            this.lastPosition = line.lastPosition();
        }

        public void lastPosition(int lastPosition) {
            this.lastPosition = lastPosition;
        }

        public int bracketDelta(final int oldCount) {
            int count = oldCount;
            for (IElementType type : tokens()) {
                if (OpenBrackets.contains(type)) {
                    count++;
                }
                if (CloseBrackets.contains(type) && (count > 0)) {
                    count--;
                }
            }
            return count;
        }

        public boolean isPreDocument() {
            return isPreDocument;
        }

        public void setType() {
            if (indent == 0 && allTokens.size() == 1 && Objects.equals(tokens.get(0), TK_PostDoc)) {
                System.out.println("Found docco line");
                this.isPreDocument = true;
            }
        }
    }

}
