package aldor.lexer;

import com.google.common.collect.Maps;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Map;

public final class AldorTokenTypes {
    private static final Map<String, AldorTokenType> tokenTypeForString = Maps.newHashMap();
    private static final Map<String, AldorTokenType> tokenTypeForName = Maps.newHashMap();
    public static final IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    public static final IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
    /*
	 * [A] TokenTag tag
	 * [B] Symbol	sym
	 * [C] String	str
	 * [D] Byte	hasString    i.e. use val.str (vs val.sym)
	 * [E] Byte	isComment    i.e. ++ --
	 * [F] Byte	isOpener     i.e. ( [ { etc
	 * [G] Byte	isCloser     i.e. ) ] } etc
	 * [H] Byte	isFollower   i.e. then else always in etc
	 * [I] Byte	isLangword   i.e. if then etc
	 * [J] Byte	isLeftAssoc  i.e., associates left to right
	 * [K] Byte	isMaybeInfix i.e., add, with, +
	 * [L] Byte	precedence   i.e., 0 is lowest, or don't know
	 * [M] Byte	isDisabled   i.e., non-zero means disabled
	 *
	 */
	//                                                                 [A]		 [B]     [C]		   [D][E][F][G][H][I][J][K] [L] [M]
    public static final AldorTokenType TK_Id        = createTokenType("TK_Id",      0, "TK_Id",         0, 0, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_Blank     = createTokenType("TK_Blank",   0, "TK_Blank",      0, 0, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_Int       = createTokenType("TK_Int",     0, "TK_Int",        1, 0, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_Float     = createTokenType("TK_Float",   0, "TK_Float",      1, 0, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_String    = createTokenType("TK_String",  0, "TK_String",     1, 0, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_PreDoc    = createTokenType("TK_PreDoc",  0, "TK_PreDoc",     1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_PostDoc   = createTokenType("TK_PostDoc", 0, "TK_PostDoc",    1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_Comment   = createTokenType("TK_Comment", 0, "TK_Comment",    1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_SysCmd    = createTokenType("TK_SysCmd",  0, "TK_SysCmd",     1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_SysCmdIf  = createTokenType("TK_SysCmdIf",0,  "TK_SysCmdIf",   1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_SysCmdEndIf  = createTokenType("TK_SysCmdEndIf",0, "TK_SysCmdEndIf",1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_SysCmdIncude = createTokenType("TK_SysCmdInclude",0, "TK_SysCmdInclude",1, 1, 0, 0, 0, 0, 1, 0, 170, 0);
    public static final AldorTokenType TK_Error     = createTokenType("TK_Error",   0, "TK_Error",      1, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType TK_IfLine = createTokenType("TK_IfLine",   0, "TK_IfLine",      1, 0, 0, 0, 0, 0, 1, 0, 0, 0);

    public static final AldorTokenType KW_Add       = createTokenType("KW_Add",     0, "add",           0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_And       = createTokenType("KW_And",     0, "and",           0, 0, 0, 0, 1, 1, 1, 1, 40, 0);
    public static final AldorTokenType KW_Always    = createTokenType("KW_Always",  0, "always",        0, 0, 0, 0, 1, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_Assert    = createTokenType("KW_Assert",  0, "assert",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Break     = createTokenType("KW_Break",   0, "break",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_But       = createTokenType("KW_But",     0, "but",           0, 0, 0, 0, 1, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_By        = createTokenType("KW_By",      0, "by",            0, 0, 0, 0, 0, 0, 1, 1, 110, 0);
    public static final AldorTokenType KW_Case      = createTokenType("KW_Case",    0, "case",          0, 0, 0, 0, 0, 0, 1, 1, 0, 0);
    public static final AldorTokenType KW_Catch     = createTokenType("KW_Catch",   0, "catch",         0, 0, 0, 0, 1, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_Default   = createTokenType("KW_Default", 0, "default",       0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Define    = createTokenType("KW_Define",  0, "define",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Delay     = createTokenType("KW_Delay",   0, "delay",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Do        = createTokenType("KW_Do",      0, "do",            0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Else      = createTokenType("KW_Else",    0, "else",          0, 0, 0, 0, 1, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Except    = createTokenType("KW_Except",  0, "except",        0, 0, 0, 0, 0, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_Export    = createTokenType("KW_Export",  0, "export",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Exquo     = createTokenType("KW_Exquo",   0, "exquo",         0, 0, 0, 0, 0, 0, 1, 1, 125, 0);
    public static final AldorTokenType KW_Extend    = createTokenType("KW_Extend",  0, "extend",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Finally   = createTokenType("KW_Finally", 0, "finally",       0, 0, 0, 0, 1, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_Fix       = createTokenType("KW_Fix",     0, "fix",           0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_For       = createTokenType("KW_For",     0, "for",           0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Fluid     = createTokenType("KW_Fluid",   0, "fluid",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Free      = createTokenType("KW_Free",    0, "free",          0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_From      = createTokenType("KW_From",    0, "from",          0, 0, 0, 0, 1, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Generate  = createTokenType("KW_Generate", 0, "generate",     0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Goto      = createTokenType("KW_Goto",    0, "goto",          0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Has       = createTokenType("KW_Has",     0, "has",           0, 0, 0, 0, 0, 0, 1, 1, 40, 0);
    public static final AldorTokenType KW_If        = createTokenType("KW_If",      0, "if",            0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Import    = createTokenType("KW_Import",  0, "import",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_In        = createTokenType("KW_In",      0, "in",            0, 0, 0, 0, 1, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_Inline    = createTokenType("KW_Inline",  0, "inline",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Is        = createTokenType("KW_Is",      0, "is",            0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Isnt      = createTokenType("KW_Isnt",    0, "isnt",          0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Iterate   = createTokenType("KW_Iterate", 0, "iterate",       0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Let       = createTokenType("KW_Let",     0, "let",           0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Local     = createTokenType("KW_Local",   0, "local",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Macro     = createTokenType("KW_Macro",   0, "macro",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Mod       = createTokenType("KW_Mod",     0, "mod",           0, 0, 0, 0, 0, 0, 1, 1, 125, 0);
    public static final AldorTokenType KW_Never     = createTokenType("KW_Never",   0, "never",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Not       = createTokenType("KW_Not",     0, "not",           0, 0, 0, 0, 0, 1, 1, 1, 150, 0);
    public static final AldorTokenType KW_Of        = createTokenType("KW_Of",      0, "of",            0, 0, 0, 0, 1, 1, 1, 1, 40, 0);
    public static final AldorTokenType KW_Or        = createTokenType("KW_Or",      0, "or",            0, 0, 0, 0, 1, 1, 1, 1, 40, 0);
    public static final AldorTokenType KW_Pretend   = createTokenType("KW_Pretend", 0, "pretend",       0, 0, 0, 0, 1, 1, 1, 1, 150, 0);
    public static final AldorTokenType KW_Quo       = createTokenType("KW_Quo",     0, "quo",           0, 0, 0, 0, 0, 0, 1, 1, 125, 0);
    public static final AldorTokenType KW_Reference = createTokenType("KW_Reference", 0, "ref",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Rem       = createTokenType("KW_Rem",     0, "rem",           0, 0, 0, 0, 0, 0, 1, 1, 125, 0);
    public static final AldorTokenType KW_Repeat    = createTokenType("KW_Repeat",  0, "repeat",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Return    = createTokenType("KW_Return",  0, "return",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Rule      = createTokenType("KW_Rule",    0, "rule",          0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Select    = createTokenType("KW_Select",  0, "select",        0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Then      = createTokenType("KW_Then",    0, "then",          0, 0, 0, 0, 1, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Throw     = createTokenType("KW_Throw",   0, "throw",         0, 0, 0, 0, 0, 1, 1, 1, 0, 0);
    public static final AldorTokenType KW_To        = createTokenType("KW_To",      0, "to",            0, 0, 0, 0, 1, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Try       = createTokenType("KW_Try",     0, "try",           0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Where     = createTokenType("KW_Where",   0, "where",         0, 0, 0, 0, 1, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_While     = createTokenType("KW_While",   0, "while",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_With      = createTokenType("KW_With",    0, "with",          0, 0, 0, 0, 0, 1, 1, 0, 0, 0);
    public static final AldorTokenType KW_Yield     = createTokenType("KW_Yield",   0, "yield",         0, 0, 0, 0, 0, 1, 1, 0, 0, 0);

    public static final AldorTokenType KW_Quote     = createTokenType("KW_Quote",   0, "'", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_Grave     = createTokenType("KW_Grave",   0, "`", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_Ampersand = createTokenType("KW_Ampersand",0, "&", 0, 0, 0, 0, 0, 0, 1, 0, 160, 0);
    public static final AldorTokenType KW_Comma     = createTokenType("KW_Comma",   0, ",", 0, 0, 0, 0, 1, 0, 1, 1, 11, 0);
    public static final AldorTokenType KW_Semicolon = createTokenType("KW_Semicolon",0, ";", 0, 0, 0, 0, 0, 0, 1, 0, 10, 0);
    public static final AldorTokenType KW_Dollar    = createTokenType("KW_Dollar",  0, "$", 0, 0, 0, 0, 1, 0, 0, 1, 70, 0);
    public static final AldorTokenType KW_Sharp     = createTokenType("KW_Sharp",   0, "#", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_At        = createTokenType("KW_At",      0, "@", 0, 0, 0, 0, 0, 0, 1, 1, 150, 0);

    public static final AldorTokenType KW_Assign    = createTokenType("KW_Assign",  0, ":=", 0, 0, 0, 0, 1, 0, 0, 1, 20, 0);
    public static final AldorTokenType KW_Colon     = createTokenType("KW_Colon",   0, ":", 0, 0, 0, 0, 1, 0, 1, 1, 36, 0);
    public static final AldorTokenType KW_ColonStar = createTokenType("KW_ColonStar", 0, ":*", 0, 0, 0, 0, 1, 0, 1, 1, 36, 0);
    public static final AldorTokenType KW_2Colon    = createTokenType("KW_2Colon",  0, "::", 0, 0, 0, 0, 1, 0, 1, 1, 150, 0);

    public static final AldorTokenType KW_Star      = createTokenType("KW_Star",    0, "*", 0, 0, 0, 0, 0, 0, 1, 1, 130, 0);
    public static final AldorTokenType KW_2Star     = createTokenType("KW_2Star",   0, "**", 0, 0, 0, 0, 0, 0, 1, 1, 140, 0);

    public static final AldorTokenType KW_Dot       = createTokenType("KW_Dot",     0, ".", 0, 0, 0, 0, 1, 0, 1, 1, 170, 0);
    public static final AldorTokenType KW_2Dot      = createTokenType("KW_2Dot",    0, "..", 0, 0, 0, 0, 0, 0, 1, 1, 110, 0);

    public static final AldorTokenType KW_EQ = createTokenType("KW_EQ", 0, "=", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_2EQ = createTokenType("KW_2EQ", 0, "==", 0, 0, 0, 0, 1, 0, 0, 1, 25, 0);
    public static final AldorTokenType KW_MArrow = createTokenType("KW_MArrow", 0, "==>", 0, 0, 0, 0, 1, 0, 0, 1, 25, 0);
    public static final AldorTokenType KW_Implies = createTokenType("KW_Implies", 0, "=>", 0, 0, 0, 0, 0, 0, 1, 1, 35, 0);

    public static final AldorTokenType KW_GT = createTokenType("KW_GT", 0, ">", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_2GT = createTokenType("KW_2GT", 0, ">>", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_GE = createTokenType("KW_GE", 0, ">=", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);

    public static final AldorTokenType KW_LT = createTokenType("KW_LT", 0, "<", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_2LT = createTokenType("KW_2LT", 0, "<<", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_LE = createTokenType("KW_LE", 0, "<=", 0, 0, 0, 0, 0, 0, 1, 1, 100, 0);
    public static final AldorTokenType KW_LArrow = createTokenType("KW_LArrow", 0, "<-", 0, 0, 0, 0, 0, 0, 1, 1, 0, 0);

    public static final AldorTokenType KW_Hat = createTokenType("KW_Hat", 0, "^", 0, 0, 0, 0, 0, 0, 1, 1, 140, 0);
    public static final AldorTokenType KW_HatE = createTokenType("KW_HatE", 0, "^=", 0, 0, 0, 0, 0, 0, 1, 1, 90, 0);

    public static final AldorTokenType KW_Tilde = createTokenType("KW_Tilde", 0, "~", 0, 0, 0, 0, 0, 0, 0, 1, 150, 0);
    public static final AldorTokenType KW_TildeE = createTokenType("KW_TildeE", 0, "~=", 0, 0, 0, 0, 0, 0, 1, 1, 90, 0);

    public static final AldorTokenType KW_Plus = createTokenType("KW_Plus", 0, "+", 0, 0, 0, 0, 0, 0, 1, 1, 120, 0);
    public static final AldorTokenType KW_PlusMinus = createTokenType("KW_PlusMinus", 0, "+-", 0, 0, 0, 0, 0, 0, 1, 1, 120, 0);
    public static final AldorTokenType KW_MapsTo = createTokenType("KW_MapsTo", 0, "+->", 0, 0, 0, 0, 1, 0, 1, 1, 30, 0);
    public static final AldorTokenType KW_MapsToStar = createTokenType("KW_MapsToStar", 0, "+->*", 0, 0, 0, 0, 1, 0, 1, 1, 30, 0);

    public static final AldorTokenType KW_Minus = createTokenType("KW_Minus", 0, "-", 0, 0, 0, 0, 0, 0, 1, 1, 120, 0);
    public static final AldorTokenType KW_RArrow = createTokenType("KW_RArrow", 0, "->", 0, 0, 0, 0, 0, 0, 0, 1, 80, 0);
    public static final AldorTokenType KW_MapStar = createTokenType("KW_MapStar", 0, "->*", 0, 0, 0, 0, 0, 0, 0, 1, 80, 0);

    public static final AldorTokenType KW_Slash = createTokenType("KW_Slash", 0, "/", 0, 0, 0, 0, 0, 0, 1, 1, 130, 0);
    public static final AldorTokenType KW_Wedge = createTokenType("KW_Wedge", 0, "/\\", 0, 0, 0, 0, 0, 0, 1, 1, 40, 0);

    public static final AldorTokenType KW_Backslash = createTokenType("KW_Backslash", 0, "\\", 0, 0, 0, 0, 0, 0, 1, 1, 130, 0);
    public static final AldorTokenType KW_Vee = createTokenType("KW_Vee", 0, "\\/", 0, 0, 0, 0, 0, 0, 1, 1, 40, 0);

    public static final AldorTokenType KW_OBrack = createTokenType("KW_OBrack", 0, "[", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_OBBrack = createTokenType("KW_OBBrack", 0, "[|", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_OCurly = createTokenType("KW_OCurly", 0, "{", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_OBCurly = createTokenType("KW_OBCurly", 0, "{|", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_OParen = createTokenType("KW_OParen", 0, "(", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_OBParen = createTokenType("KW_OBParen", 0, "(|", 0, 0, 1, 0, 0, 0, 1, 0, 0, 0);

    public static final AldorTokenType KW_CBrack = createTokenType("KW_CBrack", 0, "]", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_CCurly = createTokenType("KW_CCurly", 0, "}", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_CParen = createTokenType("KW_CParen", 0, ")", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);

    public static final AldorTokenType KW_Bar = createTokenType("KW_Bar", 0, "|", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_CBBrack = createTokenType("KW_CBBrack", 0, "|]", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_CBCurly = createTokenType("KW_CBCurly", 0, "|}", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_CBParen = createTokenType("KW_CBParen", 0, "|)", 0, 0, 0, 1, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_2Bar = createTokenType("KW_2Bar", 0, "||", 0, 0, 0, 0, 0, 1, 1, 0, 0, 0);

    public static final AldorTokenType KW_NewLine = createTokenType("KW_NewLine", 0, "\n", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_StartPile = createTokenType("KW_StartPile", 0, "#pile", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_EndPile = createTokenType("KW_EndPile", 0, "#endpile", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_BlkStart = createTokenType("KW_BlkStart", 0, "KW_BlkStart", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_BlkNext = createTokenType("KW_BlkNext", 0, "KW_BlkNext", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_BlkEnd = createTokenType("KW_BlkEnd", 0, "KW_BlkEnd", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
    public static final AldorTokenType KW_Juxtapose = createTokenType("KW_Juxtapose", 0, "KW_Juxtapose", 0, 0, 0, 0, 0, 0, 0, 0, 170, 0);
    public static final AldorTokenType KW_Indent = createTokenType("KW_Indent", 0, "KW_Indent", 0, 0, 0, 0, 0, 0, 0, 0, 170, 0);

    public static final AldorTokenType error = createTokenType("error", 0, "error", 0, 0, 0, 0, 0, 0, 0, 0, 170, 0);

    public static final AldorTokenType TK_LIMIT = createTokenType("TK_LIMIT", 0, "TK_LIMIT", 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);

    public static final TokenSet WHITESPACE_TOKENS = TokenSet.create(TK_PreDoc, TK_PostDoc, TK_Comment, TK_SysCmdEndIf, TK_SysCmdIf, TK_IfLine, TK_SysCmd);

    private static AldorTokenType createTokenType(String name, int i, String text, int hasString, int isComment, int isOpener, int isCloser, int isFollower, int isLangword, int isLeftAssoc, int isMaybeInfix, int precedence, int isDisabled) {
        AldorTokenType tokenType = new AldorTokenType(name, i, text, hasString, isComment, isOpener, isCloser, isFollower, isLangword, isLeftAssoc, isMaybeInfix, precedence, isDisabled);
        tokenTypeForString.put(name, tokenType);
        tokenTypeForName.put(text, tokenType);
        return tokenType;
    }


    public static AldorTokenType createToken(String token) {
        AldorTokenType tokenType = tokenTypeForString.get(token);
        if (tokenType == null) {
            throw new IllegalArgumentException("Unknown token: " + token);
        }

        return tokenType;
    }

    public static Iterable<AldorTokenType> all() {
        return tokenTypeForString.values();
    }

    public static boolean isOpener(IElementType eltType) {
        return ((eltType instanceof AldorTokenType) && ((AldorTokenType) eltType).isOpener());
    }

    public static boolean isCloser(IElementType eltType) {
        return ((eltType instanceof AldorTokenType) && ((AldorTokenType) eltType).isCloser());
    }

    public static boolean isFollower(IElementType eltType) {
        return ((eltType instanceof AldorTokenType) && ((AldorTokenType) eltType).isFollower());
    }

    public static AldorTokenType forText(String text) {
        return tokenTypeForName.get(text);
    }
}