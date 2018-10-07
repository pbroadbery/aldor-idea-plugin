package aldor.lexer;

import aldor.lexer.SysCmd.SysCommandType;
import aldor.util.IntegerRange;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import static aldor.lexer.AldorTokenTypes.KW_Repeat;
import static aldor.lexer.AldorTokenTypes.KW_Slash;
import static aldor.lexer.AldorTokenTypes.KW_Then;
import static aldor.lexer.AldorTokenTypes.KW_Try;
import static aldor.lexer.AldorTokenTypes.KW_Where;
import static aldor.lexer.AldorTokenTypes.KW_With;
import static aldor.lexer.AldorTokenTypes.TK_PostDoc;
import static aldor.lexer.AldorTokenTypes.TK_PreDoc;
import static aldor.lexer.AldorTokenTypes.TK_SysCmd;
import static aldor.lexer.AldorTokenTypes.TK_SysCmdAbbrev;
import static aldor.lexer.AldorTokenTypes.WHITE_SPACE;
import static aldor.lexer.LexMode.Spad;

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
            PiledSection whole = parsePiledSection(lexer);
            sections = whole.lines().isEmpty() ? Collections.emptyList() : Collections.singletonList(whole);
        } else {
            sections = scanForPiledSections(lexer);
        }
        for (PiledSection section : sections) {
            IndentNode indentNode = scan(section, 0);
            (new BlockMarker(section)).markBlocks(indentNode);
/*
            System.out.println("Scanned for newlines: " + indentNode);
            System.out.println("Scanned for newlines: " + section);
            int index = 0;
            for (SrcLine line: section.lines()) {
                System.out.println("LINE: " + index + " " + line);
                index++;
            }
            */
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
        if (Objects.equals(eltType, TK_SysCmd) || Objects.equals(eltType, TK_SysCmdAbbrev)) {
            SysCmd sysCmd = SysCmd.parse(lexer.getTokenText());
            if (sysCmd.type() == command) {
                return true;
            }
        }
        return false;
    }


    private IndentNode scan(PiledSection section, final int startIndex) {
        List<IndentNode> children = Lists.newArrayList();
        int prevIndex = startIndex;
        assert !section.isBlank(prevIndex);
        int startIndent = section.indentForLine(startIndex);
        int index = startIndex + 1;
        while (true) {
            if (index >= section.lines().size()) {
                break;
            }
            SrcLine prevLine = section.lines().get(prevIndex);
            SrcLine thisLine = section.lines().get(index);
            if (prevLine.bracketCount() != 0) {
                prevIndex = index;
                index++;
            }
            else if (startIndent < thisLine.indent()) {
                IndentNode node = scan(section, index);
                children.add(node);
                prevIndex = node.endLine();
                index = node.endLine()+1;
            }
            else if (startIndent > thisLine.indent()) {
                break;
            }
            else {
                prevIndex = index;
                index++;
            }
        }
        return new IndentNode(startIndex, index-1, children);
    }

    /*
     * "isPileRequired" decides whether a SetTab/BackTab empiling is needed
     *  for the line which is to be joined added
     *
     *  A single line pile is formed whenever the previous word is an alphabetic
     *  language keyword, e.g.  "return", "then", "else", etc.
     *  (Note, this does not include user-definable operators such as "quo".)
     */
    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private static boolean isPileRequired(SrcLine lastLine, SrcLine thisLine) {
        if (lastLine.isBlank()) {
            return false;
        }
        IElementType tok = lastLine.lastToken();

        // TODO: Use a set!
        return Objects.equals(tok, KW_Then) || Objects.equals(tok, KW_Else) ||
                Objects.equals(tok, KW_With) || Objects.equals(tok, KW_Add) ||
                Objects.equals(tok, KW_Try) || Objects.equals(tok, KW_But) ||
                Objects.equals(tok, KW_Catch) || Objects.equals(tok, KW_Finally) ||
                Objects.equals(tok, KW_Always) || Objects.equals(tok, KW_Repeat) ||
                Objects.equals(tok, KW_Where);
    }

    private static final class BlockMarker {
        private final PiledSection section;

        BlockMarker(PiledSection section) {
            this.section = section;
        }

        void markBlocks(IndentNode node) {
            for (IndentNode child: node.children()) {
                markBlocks(child);
            }

            boolean hadBackSet = false;
            for (int lineIndex : node.localLineEnds()) {
                if (isBackSetRequired(lineIndex)) {
                    section.setBackSetLine(lineIndex);
                    hadBackSet = true;
                }
            }

            if ((node.startLine() > 0) && (hadBackSet || isBlockStart(node))) {
                section.setBlock(node.startLine(), node.endLine());
            }
        }

        boolean isBlockStart(IndentNode node) {
            if (node.startLine() == 0) {
                return false;
            }
            SrcLine prevLine = section.line(node.startLine()-1);
            SrcLine thisLine = section.line(node.startLine());

            return isPileRequired(prevLine, thisLine);
        }

        private boolean isBackSetRequired(int index) {
            if (index >= section.size()) {
                return false;
            }
            SrcLine thisLine = section.lines().get(index);
            if (thisLine.bracketCount() > 0) {
                return false;
            }
            int nextLineIdx = index + 1;
            if (nextLineIdx >= section.size()) {
                return false;
            }
            SrcLine nextLine = section.line(nextLineIdx);
            if (thisLine.indent() < nextLine.indent()) {
                return false;
            }
            return isBackSetRequired(thisLine, nextLine);
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
            return isBackSetRequired1(thisLine, nextLine);
        }

        private boolean isBackSetRequired1(SrcLine thisLine, SrcLine nextLine) {
            if (thisLine.isPreDocument() || thisLine.firstToken().equals(TK_SysCmdAbbrev)) {
                return false;
            }
        /* 1. *//*
            if (thisLine.isBlank() || nextLine.isBlank()) {
                return false;
            }*/

        /* 2. */
            if (AldorTokenTypes.isOpener(thisLine.lastToken())
                    || AldorTokenTypes.isFollower(thisLine.lastToken())
                    || AldorTokenTypes.KW_Comma.equals(thisLine.lastToken())
                    || AldorTokenTypes.KW_Colon.equals(thisLine.lastToken())) {
                return false;
            }

        /* 3. */
            if (AldorTokenTypes.isFollower(nextLine.firstToken())) {
                // Ouch.. "or/" is legal. And used at line start.
                if (!Objects.equals(nextLine.secondToken(), KW_Slash)) {
                    return false;
                }
            }
            if (Objects.equals(KW_Add, nextLine.firstToken())) {
                return false;
            }

            if (AldorTokenTypes.isCloser(nextLine.firstToken())) {
                return false;
            }

            if (AldorTokenTypes.isMaybeInfix(thisLine.lastToken())) {
                return false;
            }

            return true;
        }


    }

    private static class IndentNode {
        private final List<IndentNode> children;
        private final int startLine;
        private final int endLine;

        IndentNode(int startLine, int endLine, List<IndentNode> children) {
            this.children = children;
            this.startLine = startLine;
            this.endLine = endLine;
        }

        public int endLine() {
            return endLine;
        }

        public Iterable<? extends IndentNode> children() {
            return Collections.unmodifiableList(children);
        }

        Iterable<Integer> localLineEnds() {
            Collection<Integer> lines = new TreeSet<>(new IntegerRange(startLine, endLine));

            for (IndentNode child : children) {
                lines.removeAll(new IntegerRange(child.startLine(), child.endLine()));
            }
            return lines;
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public int startLine() {
            return startLine;
        }

        @Override
        public String toString() {
            return "{" + startLine + " -> " + endLine + " " + children + "}";
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

        private final Set<Integer> backSets = Sets.newTreeSet();
        private final List<Integer> startLines = Lists.newArrayList();
        private final Set<Integer> endLines = Sets.newTreeSet();
        private final NavigableMap<Integer, Integer> lineIndexForOffset;
        private final NavigableMap<Integer, Integer> lineEndForLineStart;
        private final List<SrcLine> lines;
        private final int endOffset;

        public PiledSection(int offset, List<SrcLine> lines) {
            this.lineIndexForOffset = new TreeMap<>();
            this.lineEndForLineStart = new TreeMap<>();
            this.lines = joinBlankLines(lines);
            int i = 0;
            for (SrcLine line : this.lines) {
                lineIndexForOffset.put(line.startPosition(), i);
                i++;
            }
            endOffset = offset;
        }

        private SrcLine line(int nextLineIdx) {
            return lines.get(nextLineIdx);
        }


        private List<SrcLine> joinBlankLines(@SuppressWarnings("TypeMayBeWeakened") List<SrcLine> lines) {
            List<SrcLine> joinedLines = new ArrayList<>(lines.size());
            SrcLine lastLine = null;
            int bracketCount = 0;

            for (SrcLine line : lines) {
                line.setType();
                bracketCount = line.bracketDelta(bracketCount);
                if ((lastLine == null) && line.isPreDocument()) {
                    lastLine = line;
                }
                else if (line.isPreDocument() && (lastLine != null) && lastLine.isPreDocument()) {
                    lastLine.joinWhitespaceTokens(line);
                }
                else //noinspection StatementWithEmptyBody
                    if (line.isBlank() && (lastLine == null)) {
                    //System.out.println("Skipping " + line);
                }
                else if (lastLine == null) {
                    line.bracketCount(bracketCount);
                    joinedLines.add(line);
                    lastLine = line;
                }
                else if (!line.isPreDocument() && line.isPostDoc()) {
                    lastLine.join(line);
                }
                else if (line.isPreDocument()) {
                    joinedLines.add(line);
                    lastLine = line;
                }
                else if (!line.isPreDocument() && line.isBlank()) {
                    lastLine.joinWhitespaceTokens(line);
                } else {
                    line.bracketCount(bracketCount);
                    joinedLines.add(line);
                    lastLine = line;
                }
            }
            return joinedLines;
        }

        @Override
        public String toString() {
            return "{PS: Start " + this.startLines + " backsets: " + this.backSets + " ends: " + this.endLines + "}";
        }

        public int endOffset() {
            return endOffset;
        }

        public List<SrcLine> lines() {
            return lines;
        }

        public void addBlock(int startIndex, int endIndex) {
            lineEndForLineStart.put(startIndex, endIndex);
        }

        public NavigableMap<Integer, Integer> blockMarkers() {
            return lineEndForLineStart;
        }

        public int start() {
            return lineIndexForOffset.firstKey();
        }

        public boolean isBlockStart(int tokenStart) {
            Map.Entry<Integer, Integer> ent = lineIndexForOffset.floorEntry(tokenStart);
            if (ent == null) {
                return false;
            }
            Integer prevLine = (ent.getKey() == tokenStart) ? ent.getValue() : (ent.getValue() + 1);
            if (!this.startLines.contains(prevLine)) {
                return false;
            }
            SrcLine line = line(prevLine-1);

            if (line.lastPosition() == tokenStart) {
                return true;
            }
            return false;
        }

        public boolean isBlockEnd(int tokenEnd) {
            if (tokenEnd >= endOffset) {
                return false;
            }
            int idx = lineIndexForOffset.floorEntry(tokenEnd-1).getValue();
            SrcLine line = lines.get(idx);
            if (line.lastPosition() != tokenEnd) {
                return false;
            }
            return endLines.contains(idx);
        }

        public boolean isBlockNewline(int tokenEnd) {
            if (tokenEnd >= endOffset) {
                return false;
            }
            int idx = lineIndexForOffset.floorEntry(tokenEnd - 1).getValue();
            SrcLine line = lines.get(idx);

            if (line.lastPosition() == tokenEnd) {
                return backSets.contains(idx);
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

        public void setBackSetLine(int i) {
            backSets.add(i);
        }

        public void setBlock(int startLine, int endLine) {
            startLines.add(startLine);
            endLines.add(endLine);
        }

        public int size() {
            return lines.size();
        }

        public boolean isBlank(int prevIndex) {
            return line(prevIndex).isBlank();
        }
    }


    private static class SrcLine {
        private final List<IElementType> tokens = new ArrayList<>();
        private final Collection<IElementType> allTokens = new ArrayList<>(); // Probably don't need this.
        private int indent;
        private int bracketCount = -1;
        private final int startPosition;
        private int lastPosition;
        private boolean isPreDocument = false;
        private boolean isPostDocument = false;

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

        public Iterable<IElementType> tokens() {
            return tokens;
        }

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
            String bc = (bracketCount > 0) ? ("bc: " + bracketCount) : "";
            if (isBlank()) {
                return "{SL: " + startPosition + " indent: " + indent + "}";
            } else {
                return "{SL: " + startPosition + " indent: " + indent + " " + tokens.get(0) + (tokens.size() == 1 ? "" : " .. " + lastToken()) + " " + bc + "}";
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

        public void joinWhitespaceTokens(SrcLine line) {
            if (!isPreDocument()) {
                assert !line.isPreDocument();
            }
            //this.tokens.addAll(line.tokens());
            this.allTokens.addAll(line.allTokens());
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
            if ((indent == 0) && (allTokens.size() == 1) && Objects.equals(tokens.get(0), TK_PostDoc)) {
                this.isPreDocument = true;
            }
            else if ((allTokens.size() == 1) && Objects.equals(tokens.get(0), TK_PostDoc)) {
                this.isPostDocument = true;
            }
            else if ((allTokens.size() == 1) && Objects.equals(tokens.get(0), TK_PreDoc)) {
                this.isPreDocument = true;
            }

        }

        public int bracketCount() {
            return bracketCount;
        }
        public void bracketCount(int bracketCount) {
            this.bracketCount = bracketCount;
        }

        public boolean isPostDoc() {
            return this.isPostDocument;
        }

        @Nullable
        public IElementType secondToken() {
            if (tokens.size() < 2) {
                return null;
            }
            return tokens.get(1);
        }
    }

}
