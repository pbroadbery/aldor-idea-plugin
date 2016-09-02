package aldor;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import aldor.AldorTokenTypes;
import com.intellij.psi.TokenType;

%%

%class AldorLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return null;
%eof}

%state LINE_START

CRLF=\n|\r|\r\n
WHITE_SPACE=[\ \t\f]
INDENT=[\ \t]+
ID =([A-Za-z%?]|_.)([A-Za-z0-9%_?!]|_.)*
INT=[0-9]+

STRING=\"[^\"]*\"

COMMENT="-""-"[^\r\n]*
PREDOC=\+\+\+[^\r\n]*
POSTDOC=\+\+[^+][^\r\n]*

SYSCMD=#[^\n\r]*

%%
/*
"TK_Id" { return AldorTokenTypes.TK_Id; }
"TK_Blank" { return AldorTokenTypes.TK_Blank; }
"TK_Int" { return AldorTokenTypes.TK_Int; }
"TK_Float" { return AldorTokenTypes.TK_Float; }
"TK_String" { return AldorTokenTypes.TK_String; }
"TK_PreDoc" { return AldorTokenTypes.TK_PreDoc; }
"TK_PostDoc" { return AldorTokenTypes.TK_PostDoc; }
"TK_Comment" { return AldorTokenTypes.TK_Comment; }
"TK_SysCmd" { return AldorTokenTypes.TK_SysCmd; }
"TK_Error" { return AldorTokenTypes.TK_Error; }
*/
<YYINITIAL, LINE_START> {
{ COMMENT } {return AldorTokenTypes.TK_Comment;}
{ PREDOC }  { return AldorTokenTypes.TK_PreDoc;}
{ POSTDOC }  { return AldorTokenTypes.TK_PostDoc;}
"add" { return AldorTokenTypes.KW_Add; }
"and" { return AldorTokenTypes.KW_And; }
"always" { return AldorTokenTypes.KW_Always; }
"assert" { return AldorTokenTypes.KW_Assert; }
"break" { return AldorTokenTypes.KW_Break; }
"but" { return AldorTokenTypes.KW_But; }
"by" { return AldorTokenTypes.KW_By; }
"case" { return AldorTokenTypes.KW_Case; }
"catch" { return AldorTokenTypes.KW_Catch; }
"default" { return AldorTokenTypes.KW_Default; }
"define" { return AldorTokenTypes.KW_Define; }
"delay" { return AldorTokenTypes.KW_Delay; }
"do" { return AldorTokenTypes.KW_Do; }
"else" { return AldorTokenTypes.KW_Else; }
"except" { return AldorTokenTypes.KW_Except; }
"export" { return AldorTokenTypes.KW_Export; }
"exquo" { return AldorTokenTypes.KW_Exquo; }
"extend" { return AldorTokenTypes.KW_Extend; }
"finally" { return AldorTokenTypes.KW_Finally; }
"fix" { return AldorTokenTypes.KW_Fix; }
"for" { return AldorTokenTypes.KW_For; }
"fluid" { return AldorTokenTypes.KW_Fluid; }
"free" { return AldorTokenTypes.KW_Free; }
"from" { return AldorTokenTypes.KW_From; }
"generate" { return AldorTokenTypes.KW_Generate; }
"goto" { return AldorTokenTypes.KW_Goto; }
"has" { return AldorTokenTypes.KW_Has; }
"if" { return AldorTokenTypes.KW_If; }
"import" { return AldorTokenTypes.KW_Import; }
"in" { return AldorTokenTypes.KW_In; }
"inline" { return AldorTokenTypes.KW_Inline; }
"is" { return AldorTokenTypes.KW_Is; }
"isnt" { return AldorTokenTypes.KW_Isnt; }
"iterate" { return AldorTokenTypes.KW_Iterate; }
"let" { return AldorTokenTypes.KW_Let; }
"local" { return AldorTokenTypes.KW_Local; }
"macro" { return AldorTokenTypes.KW_Macro; }
"mod" { return AldorTokenTypes.KW_Mod; }
"never" { return AldorTokenTypes.KW_Never; }
"not" { return AldorTokenTypes.KW_Not; }
"of" { return AldorTokenTypes.KW_Of; }
"or" { return AldorTokenTypes.KW_Or; }
"pretend" { return AldorTokenTypes.KW_Pretend; }
"quo" { return AldorTokenTypes.KW_Quo; }
"ref" { return AldorTokenTypes.KW_Reference; }
"rem" { return AldorTokenTypes.KW_Rem; }
"repeat" { return AldorTokenTypes.KW_Repeat; }
"return" { return AldorTokenTypes.KW_Return; }
"rule" { return AldorTokenTypes.KW_Rule; }
"select" { return AldorTokenTypes.KW_Select; }
"then" { return AldorTokenTypes.KW_Then; }
"throw" { return AldorTokenTypes.KW_Throw; }
"to" { return AldorTokenTypes.KW_To; }
"try" { return AldorTokenTypes.KW_Try; }
"where" { return AldorTokenTypes.KW_Where; }
"while" { return AldorTokenTypes.KW_While; }
"with" { return AldorTokenTypes.KW_With; }
"yield" { return AldorTokenTypes.KW_Yield; }
"'" { return AldorTokenTypes. KW_Quote; }
"`" { return AldorTokenTypes.KW_Grave; }
"&" { return AldorTokenTypes.KW_Ampersand; }
"," { return AldorTokenTypes.KW_Comma; }
";" { return AldorTokenTypes.KW_Semicolon; }
"\$" { return AldorTokenTypes.KW_Dollar; }
"@" { return AldorTokenTypes.KW_At; }
":=" { return AldorTokenTypes.KW_Assign; }
":\*" { return AldorTokenTypes.KW_ColonStar; }
"::" { return AldorTokenTypes.KW_2Colon; }
":" { return AldorTokenTypes.KW_Colon; }
"\*\*" { return AldorTokenTypes.KW_2Star; }
"\*" { return AldorTokenTypes.KW_Star; }
"\.\." { return AldorTokenTypes.KW_2Dot; }
"\." { return AldorTokenTypes.KW_Dot; }
"==>" { return AldorTokenTypes.KW_MArrow; }
"==" { return AldorTokenTypes.KW_2EQ; }
"=>" { return AldorTokenTypes.KW_Implies; }
"=" { return AldorTokenTypes.KW_EQ; }
">" { return AldorTokenTypes.KW_GT; }
">>" { return AldorTokenTypes.KW_2GT; }
">=" { return AldorTokenTypes.KW_GE; }
"<<" { return AldorTokenTypes.KW_2LT; }
"<=" { return AldorTokenTypes.KW_LE; }
"<-" { return AldorTokenTypes.KW_LArrow; }
"<" { return AldorTokenTypes.KW_LT; }
"\^=" { return AldorTokenTypes.KW_HatE; }
"\^" { return AldorTokenTypes.KW_Hat; }
"\~" { return AldorTokenTypes.KW_Tilde; }
"\~=" { return AldorTokenTypes.KW_TildeE; }
"\+->\*" { return AldorTokenTypes.KW_MapsToStar; }
"\+->" { return AldorTokenTypes.KW_MapsTo; }
"\+-" { return AldorTokenTypes.KW_PlusMinus; }
"\+" { return AldorTokenTypes.KW_Plus; }
"-" { return AldorTokenTypes.KW_Minus; }
"->\*" { return AldorTokenTypes.KW_MapStar; }
"->" { return AldorTokenTypes.KW_RArrow; }
"\/" { return AldorTokenTypes.KW_Slash; }
"/\\" { return AldorTokenTypes.KW_Wedge; }
"\\" { return AldorTokenTypes.KW_Backslash; }
"\\/" { return AldorTokenTypes.KW_Vee; }
"\[" { return AldorTokenTypes.KW_OBrack; }
"\[|" { return AldorTokenTypes.KW_OBBrack; }
"\{" { return AldorTokenTypes.KW_OCurly; }
"\{\|" { return AldorTokenTypes.KW_OBCurly; }
"\(" { return AldorTokenTypes.KW_OParen; }
"\(\|" { return AldorTokenTypes.KW_OBParen; }
"]" { return AldorTokenTypes.KW_CBrack; }
"}" { return AldorTokenTypes.KW_CCurly; }
"\)" { return AldorTokenTypes.KW_CParen; }
"\|" { return AldorTokenTypes.KW_Bar; }
"\|]" { return AldorTokenTypes.KW_CBBrack; }
"\|}" { return AldorTokenTypes.KW_CBCurly; }
"\|\)" { return AldorTokenTypes.KW_CBParen; }
"\|\|" { return AldorTokenTypes.KW_2Bar; }
}

<LINE_START> {
    { SYSCMD} { yybegin(YYINITIAL); return AldorTokenTypes.KW_SysCmd;}
    { INDENT } { return AldorTokenTypes.KW_Indent; }
    [^] { yypushback(1); yybegin(YYINITIAL); }
}

<YYINITIAL> {
"#" { return AldorTokenTypes.KW_Sharp; }
{ WHITE_SPACE } { return TokenType.WHITE_SPACE; }
}
//"#pile" { return AldorTokenTypes.KW_StartPile; }
//"#endpile" { return AldorTokenTypes.KW_EndPile; }
//"KW_SetTab" { return AldorTokenTypes.KW_SetTab; }
//"KW_BackSet" { return AldorTokenTypes.KW_BackSet; }
//"KW_BackTab" { return AldorTokenTypes.KW_BackTab; }
//"KW_Juxtapose" { return AldorTokenTypes.KW_Juxtapose; }

//"TK_LIMIT" { return AldorTokenTypes.TK_LIMIT; }
<YYINITIAL,LINE_START> {

{ ID } { return AldorTokenTypes.TK_Id; }

{ INT } { return AldorTokenTypes.TK_Int; }

{ CRLF } { System.out.println("Line start!"); yybegin(LINE_START); return TokenType.WHITE_SPACE; }

{ STRING } { return AldorTokenTypes.TK_String; }

. { System.out.println("Bad " + yytext()); return TokenType.BAD_CHARACTER; }
}