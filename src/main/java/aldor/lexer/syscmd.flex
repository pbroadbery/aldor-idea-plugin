package aldor.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import aldor.lexer.AldorTokenTypes;
import com.intellij.psi.TokenType;

%%

%class SysCmdLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%{
%}


%eof{  return null;
%eof}

%state ARGS
%state CMD

WHITE_SPACE=[\ \t\f]
ID = ([A-Za-z%?]|_.)([A-Za-z0-9%_?!]|_.)*

%%

<YYINITIAL> {
"\)" {yybegin(CMD); return AldorTokenTypes.SysCmd_Spec;}
"#" {yybegin(CMD); return AldorTokenTypes.SysCmd_Spec;}
}

<CMD> {
    "if" {yybegin(ARGS); return AldorTokenTypes.SysCmd_If;}
    "else" {yybegin(ARGS); return AldorTokenTypes.SysCmd_Else;}
    "endif" {yybegin(ARGS); return AldorTokenTypes.SysCmd_Endif;}
    "abbrev" {yybegin(ARGS); return AldorTokenTypes.SysCmd_Abbrev;}
    {ID} {yybegin(ARGS); return AldorTokenTypes.SysCmd_Other;}
}

<ARGS> {
    {ID} {return AldorTokenTypes.TK_Id; }
}

<YYINITIAL, CMD, ARGS> {
    WHITE_SPACE {return TokenType.WHITE_SPACE; }
    . { System.out.println("Bad token `" + yytext() + "'"); return TokenType.BAD_CHARACTER; }

}
