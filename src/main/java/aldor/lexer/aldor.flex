package aldor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import aldor.lexer.AldorTokenTypes;
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
%state NORMAL

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
"TK_Id" { yybegin(NORMAL); return AldorTokenTypes.TK_Id; }
"TK_Blank" { yybegin(NORMAL); return AldorTokenTypes.TK_Blank; }
"TK_Int" { yybegin(NORMAL); return AldorTokenTypes.TK_Int; }
"TK_Float" { yybegin(NORMAL); return AldorTokenTypes.TK_Float; }
"TK_String" { yybegin(NORMAL); return AldorTokenTypes.TK_String; }
"TK_PreDoc" { yybegin(NORMAL); return AldorTokenTypes.TK_PreDoc; }
"TK_PostDoc" { yybegin(NORMAL); return AldorTokenTypes.TK_PostDoc; }
"TK_Comment" { yybegin(NORMAL); return AldorTokenTypes.TK_Comment; }
"TK_SysCmd" { yybegin(NORMAL); return AldorTokenTypes.TK_SysCmd; }
"TK_Error" { yybegin(NORMAL); return AldorTokenTypes.TK_Error; }
*/
<YYINITIAL, LINE_START, NORMAL> {
{ COMMENT } {yybegin(NORMAL); return AldorTokenTypes.TK_Comment;}
{ PREDOC }  { yybegin(NORMAL); return AldorTokenTypes.TK_PreDoc;}
{ POSTDOC }  { yybegin(NORMAL); return AldorTokenTypes.TK_PostDoc;}
"add" { yybegin(NORMAL); return AldorTokenTypes.KW_Add; }
"and" { yybegin(NORMAL); return AldorTokenTypes.KW_And; }
"always" { yybegin(NORMAL); return AldorTokenTypes.KW_Always; }
"assert" { yybegin(NORMAL); return AldorTokenTypes.KW_Assert; }
"break" { yybegin(NORMAL); return AldorTokenTypes.KW_Break; }
"but" { yybegin(NORMAL); return AldorTokenTypes.KW_But; }
"by" { yybegin(NORMAL); return AldorTokenTypes.KW_By; }
"case" { yybegin(NORMAL); return AldorTokenTypes.KW_Case; }
"catch" { yybegin(NORMAL); return AldorTokenTypes.KW_Catch; }
"default" { yybegin(NORMAL); return AldorTokenTypes.KW_Default; }
"define" { yybegin(NORMAL); return AldorTokenTypes.KW_Define; }
"delay" { yybegin(NORMAL); return AldorTokenTypes.KW_Delay; }
"do" { yybegin(NORMAL); return AldorTokenTypes.KW_Do; }
"else" { yybegin(NORMAL); return AldorTokenTypes.KW_Else; }
"except" { yybegin(NORMAL); return AldorTokenTypes.KW_Except; }
"export" { yybegin(NORMAL); return AldorTokenTypes.KW_Export; }
"exquo" { yybegin(NORMAL); return AldorTokenTypes.KW_Exquo; }
"extend" { yybegin(NORMAL); return AldorTokenTypes.KW_Extend; }
"finally" { yybegin(NORMAL); return AldorTokenTypes.KW_Finally; }
"fix" { yybegin(NORMAL); return AldorTokenTypes.KW_Fix; }
"for" { yybegin(NORMAL); return AldorTokenTypes.KW_For; }
"fluid" { yybegin(NORMAL); return AldorTokenTypes.KW_Fluid; }
"free" { yybegin(NORMAL); return AldorTokenTypes.KW_Free; }
"from" { yybegin(NORMAL); return AldorTokenTypes.KW_From; }
"generate" { yybegin(NORMAL); return AldorTokenTypes.KW_Generate; }
"goto" { yybegin(NORMAL); return AldorTokenTypes.KW_Goto; }
"has" { yybegin(NORMAL); return AldorTokenTypes.KW_Has; }
"if" { yybegin(NORMAL); return AldorTokenTypes.KW_If; }
"import" { yybegin(NORMAL); return AldorTokenTypes.KW_Import; }
"in" { yybegin(NORMAL); return AldorTokenTypes.KW_In; }
"inline" { yybegin(NORMAL); return AldorTokenTypes.KW_Inline; }
"is" { yybegin(NORMAL); return AldorTokenTypes.KW_Is; }
"isnt" { yybegin(NORMAL); return AldorTokenTypes.KW_Isnt; }
"iterate" { yybegin(NORMAL); return AldorTokenTypes.KW_Iterate; }
"let" { yybegin(NORMAL); return AldorTokenTypes.KW_Let; }
"local" { yybegin(NORMAL); return AldorTokenTypes.KW_Local; }
"macro" { yybegin(NORMAL); return AldorTokenTypes.KW_Macro; }
"mod" { yybegin(NORMAL); return AldorTokenTypes.KW_Mod; }
"never" { yybegin(NORMAL); return AldorTokenTypes.KW_Never; }
"not" { yybegin(NORMAL); return AldorTokenTypes.KW_Not; }
"of" { yybegin(NORMAL); return AldorTokenTypes.KW_Of; }
"or" { yybegin(NORMAL); return AldorTokenTypes.KW_Or; }
"pretend" { yybegin(NORMAL); return AldorTokenTypes.KW_Pretend; }
"quo" { yybegin(NORMAL); return AldorTokenTypes.KW_Quo; }
"ref" { yybegin(NORMAL); return AldorTokenTypes.KW_Reference; }
"rem" { yybegin(NORMAL); return AldorTokenTypes.KW_Rem; }
"repeat" { yybegin(NORMAL); return AldorTokenTypes.KW_Repeat; }
"return" { yybegin(NORMAL); return AldorTokenTypes.KW_Return; }
"rule" { yybegin(NORMAL); return AldorTokenTypes.KW_Rule; }
"select" { yybegin(NORMAL); return AldorTokenTypes.KW_Select; }
"then" { yybegin(NORMAL); return AldorTokenTypes.KW_Then; }
"throw" { yybegin(NORMAL); return AldorTokenTypes.KW_Throw; }
"to" { yybegin(NORMAL); return AldorTokenTypes.KW_To; }
"try" { yybegin(NORMAL); return AldorTokenTypes.KW_Try; }
"where" { yybegin(NORMAL); return AldorTokenTypes.KW_Where; }
"while" { yybegin(NORMAL); return AldorTokenTypes.KW_While; }
"with" { yybegin(NORMAL); return AldorTokenTypes.KW_With; }
"yield" { yybegin(NORMAL); return AldorTokenTypes.KW_Yield; }
"'" { yybegin(NORMAL); return AldorTokenTypes. KW_Quote; }
"`" { yybegin(NORMAL); return AldorTokenTypes.KW_Grave; }
"&" { yybegin(NORMAL); return AldorTokenTypes.KW_Ampersand; }
"," { yybegin(NORMAL); return AldorTokenTypes.KW_Comma; }
";" { yybegin(NORMAL); return AldorTokenTypes.KW_Semicolon; }
"\$" { yybegin(NORMAL); return AldorTokenTypes.KW_Dollar; }
"@" { yybegin(NORMAL); return AldorTokenTypes.KW_At; }
":=" { yybegin(NORMAL); return AldorTokenTypes.KW_Assign; }
":\*" { yybegin(NORMAL); return AldorTokenTypes.KW_ColonStar; }
"::" { yybegin(NORMAL); return AldorTokenTypes.KW_2Colon; }
":" { yybegin(NORMAL); return AldorTokenTypes.KW_Colon; }
"\*\*" { yybegin(NORMAL); return AldorTokenTypes.KW_2Star; }
"\*" { yybegin(NORMAL); return AldorTokenTypes.KW_Star; }
"\.\." { yybegin(NORMAL); return AldorTokenTypes.KW_2Dot; }
"\." { yybegin(NORMAL); return AldorTokenTypes.KW_Dot; }
"==>" { yybegin(NORMAL); return AldorTokenTypes.KW_MArrow; }
"==" { yybegin(NORMAL); return AldorTokenTypes.KW_2EQ; }
"=>" { yybegin(NORMAL); return AldorTokenTypes.KW_Implies; }
"=" { yybegin(NORMAL); return AldorTokenTypes.KW_EQ; }
">" { yybegin(NORMAL); return AldorTokenTypes.KW_GT; }
">>" { yybegin(NORMAL); return AldorTokenTypes.KW_2GT; }
">=" { yybegin(NORMAL); return AldorTokenTypes.KW_GE; }
"<<" { yybegin(NORMAL); return AldorTokenTypes.KW_2LT; }
"<=" { yybegin(NORMAL); return AldorTokenTypes.KW_LE; }
"<-" { yybegin(NORMAL); return AldorTokenTypes.KW_LArrow; }
"<" { yybegin(NORMAL); return AldorTokenTypes.KW_LT; }
"\^=" { yybegin(NORMAL); return AldorTokenTypes.KW_HatE; }
"\^" { yybegin(NORMAL); return AldorTokenTypes.KW_Hat; }
"\~" { yybegin(NORMAL); return AldorTokenTypes.KW_Tilde; }
"\~=" { yybegin(NORMAL); return AldorTokenTypes.KW_TildeE; }
"\+->\*" { yybegin(NORMAL); return AldorTokenTypes.KW_MapsToStar; }
"\+->" { yybegin(NORMAL); return AldorTokenTypes.KW_MapsTo; }
"\+-" { yybegin(NORMAL); return AldorTokenTypes.KW_PlusMinus; }
"\+" { yybegin(NORMAL); return AldorTokenTypes.KW_Plus; }
"-" { yybegin(NORMAL); return AldorTokenTypes.KW_Minus; }
"->\*" { yybegin(NORMAL); return AldorTokenTypes.KW_MapStar; }
"->" { yybegin(NORMAL); return AldorTokenTypes.KW_RArrow; }
"\/" { yybegin(NORMAL); return AldorTokenTypes.KW_Slash; }
"/\\" { yybegin(NORMAL); return AldorTokenTypes.KW_Wedge; }
"\\" { yybegin(NORMAL); return AldorTokenTypes.KW_Backslash; }
"\\/" { yybegin(NORMAL); return AldorTokenTypes.KW_Vee; }
"\[" { yybegin(NORMAL); return AldorTokenTypes.KW_OBrack; }
"\[|" { yybegin(NORMAL); return AldorTokenTypes.KW_OBBrack; }
"\{" { yybegin(NORMAL); return AldorTokenTypes.KW_OCurly; }
"\{\|" { yybegin(NORMAL); return AldorTokenTypes.KW_OBCurly; }
"\(" { yybegin(NORMAL); return AldorTokenTypes.KW_OParen; }
"\(\|" { yybegin(NORMAL); return AldorTokenTypes.KW_OBParen; }
"]" { yybegin(NORMAL); return AldorTokenTypes.KW_CBrack; }
"}" { yybegin(NORMAL); return AldorTokenTypes.KW_CCurly; }
"\)" { yybegin(NORMAL); return AldorTokenTypes.KW_CParen; }
"\|" { yybegin(NORMAL); return AldorTokenTypes.KW_Bar; }
"\|]" { yybegin(NORMAL); return AldorTokenTypes.KW_CBBrack; }
"\|}" { yybegin(NORMAL); return AldorTokenTypes.KW_CBCurly; }
"\|\)" { yybegin(NORMAL); return AldorTokenTypes.KW_CBParen; }
"\|\|" { yybegin(NORMAL); return AldorTokenTypes.KW_2Bar; }
}

<LINE_START, YYINITIAL> {
    { SYSCMD} { yybegin(NORMAL); return AldorTokenTypes.TK_SysCmd;}
    { INDENT } { yybegin(NORMAL); return AldorTokenTypes.KW_Indent; }
    [^] { yypushback(1); yybegin(NORMAL); }
}

<NORMAL> {
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
<YYINITIAL,LINE_START, NORMAL> {

{ ID } { yybegin(NORMAL); return AldorTokenTypes.TK_Id; }

{ INT } { yybegin(NORMAL); return AldorTokenTypes.TK_Int; }

{ CRLF } { yybegin(LINE_START); return AldorTokenTypes.KW_NewLine; }

{ STRING } { yybegin(NORMAL); return AldorTokenTypes.TK_String; }

. { System.out.println("Bad " + yytext()); return TokenType.BAD_CHARACTER; }
}