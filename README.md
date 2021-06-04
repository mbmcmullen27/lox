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

### Chapter 5 - Parsing

- comma operator: 
    - added the comma operator first, giving it the lowest precendence, before equality since it separates expressions

- ternary operator: [c operators wiki](https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B)

    A precedence table, while mostly adequate, cannot resolve a few details. In particular, note that the ternary operator allows any arbitrary expression as its middle operand, despite being listed as having higher precedence than the assignment and comma operators. Thus a ? b, c : d is interpreted as a ? (b, c) : d, and not as the meaningless (a ? b), (c : d). So, the expression in the middle of the conditional operator (between ? and :) is parsed as if parenthesized. Also, note that the immediate, unparenthesized result of a C cast expression cannot be the operand of sizeof. Therefore, sizeof (int) * x is interpreted as (sizeof(int)) * x and not sizeof ((int) * x).