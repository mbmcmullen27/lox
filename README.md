# lox

Build:
```bash
javac @args
```

Start REPL:
```bash
java com/craftinginterpreters/lox/Lox
```

Execute file:
```bash
java com/craftinginterpreters/lox/Lox testfile 
```

## Notes

### Chapter 6 - Parsing

- comma operator: 
    - added the comma operator first, giving it the lowest precendence, before equality since it separates expressions

- ternary operator: [c operators wiki](https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B)

    A precedence table, while mostly adequate, cannot resolve a few details. In particular, note that the ternary operator allows any arbitrary expression as its middle operand, despite being listed as having higher precedence than the assignment and comma operators. Thus a ? b, c : d is interpreted as a ? (b, c) : d, and not as the meaningless (a ? b), (c : d). So, the expression in the middle of the conditional operator (between ? and :) is parsed as if parenthesized. Also, note that the immediate, unparenthesized result of a C cast expression cannot be the operand of sizeof. Therefore, sizeof (int) * x is interpreted as (sizeof(int)) * x and not sizeof ((int) * x).

### Chapter 7 - Evaluating Expressions

- Ternary operator '?:'
    - I implemented the ternary operator as a separate ternary expression. Is this normal? or do other languages maybe consider the ternary operator as 2 separte binary expressions? I'm not sure that makes more sense. 
    - I'm concerned this might complicate using the '?' as a different operator later, since We throw an error if an ':' character isn't found after '?', maybe we just skip the error if it's not useful later.
        - second thought, this is not a problem because ternary op has the lowest precedence

### Chapter 8 - Statements and State

- I just noticed this in the parser, does it make sense to error if comma is missing a left operand? it discards the result anyway doesn't it? 
```java
    if (match(COMMA)) error(peek(), "Comma operator missing left operand");
```