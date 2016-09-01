package aldor.list;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import aldor.list.ListTokenTypes;
import com.intellij.psi.TokenType;

%%

%class ListLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return null;
%eof}

CRLF= \n|\r|\r\n
WHITE_SPACE=[\ \t\f]
OP=[(]
CP=[)]
OSQ=[\[]
CSQ =[\]]
V1=V1
V2=V2
V1a=V1a
Sep =,
E=[0-9]+

%%
{ OP } { return ListTokenTypes.OP;}
{ CP } { return ListTokenTypes.CP;}
{ OSQ } { return ListTokenTypes.OSQ;}
{ CSQ } { return ListTokenTypes.CSQ;}

{ V1 } { return ListTokenTypes.V1;}
{ V1a } { return ListTokenTypes.V1a;}
{ V2 } { return ListTokenTypes.V2;}
{ E } { return ListTokenTypes.E;}
{ Sep } { return ListTokenTypes.Sep; }
{ CRLF } | { WHITE_SPACE } { return TokenType.WHITE_SPACE; }
. { return TokenType.BAD_CHARACTER; }
