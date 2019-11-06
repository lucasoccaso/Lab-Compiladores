/**

<done> Annot ::= “@” Id [ “(” { AnnotParam } “)” ]

<done>AnnotParam ::= IntValue | StringValue | Id

<done> AssertStat ::= “assert” Expression “,” StringValue

<done> AssignExpr ::= Expression [ “=” Expression ]

<done> BasicType ::= “Int” | “Boolean” | “String”

<done> BasicValue ::= IntValue | BooleanValue | StringValue

<done> BooleanValue ::= “true” | “false”

<done> ClassDec ::= [ “open” ] “class” Id [ “extends” Id ] MemberList “end”

<done> CompStatement ::= “{” { Statement } “}”

<done> Digit ::= “0” | ... | “9”

<done> Expression ::= SimpleExpression [ Relation SimpleExpression ]

<done> ExpressionList ::= Expression { “,” Expression }

<incomplete>Factor ::= BasicValue |
“(” Expression “)” |
“!” Factor |
“nil” |
ObjectCreation |
PrimaryExpr

<done> FieldDec ::= “var” Type IdList [ “;” ]

<done> FormalParamDec ::= ParamDec { “,” ParamDec }

<done> HighOperator ::= “∗” | “/” | “&&”

<done> IdList ::= Id { “,” Id }

<done> IfStat ::= “if” Expression “{” Statement “}”
[ “else” “{” Statement “}” ]

IntValue ::= Digit { Digit }

<done> LocalDec ::= “var” Type IdList [ “=” Expression ]

LowOperator ::= “+” | “−” | “||”

<done> MemberList ::= { [ Qualifier ] Member }

Member ::= FieldDec | MethodDec

<done> MethodDec ::= “func” IdColon FormalParamDec [ “->” Type ]
“{” StatementList “}” |
“func” Id [ “->” Type ] “{” StatementList “}”

ObjectCreation ::= Id “.” “new”

ParamDec ::= Type Id

Program ::= { Annot } ClassDec { { Annot } ClassDec }

<done> Qualifier ::= “private”
“public”
“override”
“override” “public”
“final”
“final” “public”
“final” “override”
“final” “override” “public”
“shared” “private”
“shared” “public”

ReadExpr ::= “In” “.” ( “readInt” | “readString” )

<done> RepeatStat ::= “repeat” StatementList “until” Expression

<done>PrimaryExpr ::= “super” “.” IdColon ExpressionList |
“super” “.” Id |
Id |
Id “.” Id |
Id “.” IdColon ExpressionList |
“self” |
“self” “.” Id |
“self” ”.” IdColon ExpressionList |
“self” ”.” Id “.” IdColon ExpressionList |
“self” ”.” Id “.” Id |
ReadExpr

Relation ::= “==” | “<” | “>” | “<=” | “>=” | “! =”

<done> ReturnStat ::= “return” Expression

Signal ::= “+” | “−”

SignalFactor ::= [ Signal ] Factor

SimpleExpression ::= SumSubExpression { “++” SumSubExpression }

SumSubExpression ::= Term { LowOperator Term }

<done> Statement ::= AssignExpr “;” | IfStat | WhileStat | ReturnStat “;” |
WriteStat “;” | “break” “;” | “;” |
RepeatStat “;” | LocalDec “;” |
AssertStat “;”

<done> StatementList ::= { Statement }

Term ::= SignalFactor { HighOperator SignalFactor }

<done> Type ::= BasicType | Id

<done> WriteStat ::= “Out” “.” [ “print:” | “println:” ] Expression

<done> WhileStat ::= “while” Expression “{” StatementList “}”

*/