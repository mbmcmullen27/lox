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
java com/craftinginterpreters/lox/Lox test/testfile 
```

## Notes

### Chapter 6 - Parsing

- comma operator: 
    - added the comma operator first, giving it the lowest precendence, before equality since it separates expressions

- ternary operator: [c operators wiki](https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B)

    > A precedence table, while mostly adequate, cannot resolve a few details. In particular, note that the ternary operator allows any arbitrary expression as its middle operand, despite being listed as having higher precedence than the assignment and comma operators. Thus a ? b, c : d is interpreted as a ? (b, c) : d, and not as the meaningless (a ? b), (c : d). So, the expression in the middle of the conditional operator (between ? and :) is parsed as if parenthesized. Also, note that the immediate, unparenthesized result of a C cast expression cannot be the operand of sizeof. Therefore, sizeof (int) * x is interpreted as (sizeof(int)) * x and not sizeof ((int) * x).

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
- sure feels like I'm seeing a lot of references to lisp lately, might be time to learn.

    > The bindings that associate variables to values need to be stored somewhere. Ever since the Lisp folks invented parentheses, this data structure has been called an environment.

- this guy talks about Scheme a lot and I've never heard if it before

    > My rule about variables and scoping is, “When in doubt, do what Scheme does”. 

- the AstPrinter needs to implement all the visitor functions in order to compile, but we no longer use it to print. We should remove it from the args file (stop building it) or extend it to read from a file for test purposes
    - cool aside might be to add an option to the interpreter to print to AST instead of interpreting
    - Start witht the visit variable methods
    - for now just taking it out of the args file so it stops trying to compile

- (Assignment 8.4)
    >I find it delightful that the same group of people who pride themselves on dispassionate logic are also the ones who can’t resist emotionally loaded terms for their work: “pure”, “side effect”, “lazy”, “persistent”, “first-class”, “higher-order”.

    - I find statements like this^^ ('funny how those other people do this thing while...'- type commentary) to be unproductive. Not even sure what he's taking a jab at here, guess he doesn't like functional programmers.

- (Scope 8.5)
    >Lexical scope came onto the scene with ALGOL. Earlier languages were often dynamically scoped. Computer scientists back then believed dynamic scope was faster to execute. Today, thanks to early Scheme hackers, we know that isn’t true. If anything, it’s the opposite.

    - ^^ interesting bit of CS history (and another Scheme reference, this guy _loves_ Scheme apparently)

    > Dynamic scope for variables lives on in some corners. Emacs Lisp defaults to dynamic scope for variables. The [binding macro in Clojure](https://clojuredocs.org/clojure.core/binding) provides it. The widely disliked [with statement in JavaScript](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/with) turns properties on an object into dynamically scoped variables.