# lox
as defined in [Crafting Interpreters](https://craftinginterpreters.com/) by Bob Nystrom

Build:
```bash
# compile
javac @args

# make a jar
jar -cfm lox.jar manifest.txt com/

```

Start REPL:
```bash
# java style
java com/craftinginterpreters/lox/Lox

# as a jar
java -jar lox.jar

#bash shortcut
./lox
```

Execute file:
```bash
# java style
java com/craftinginterpreters/lox/Lox test/testfile 

# as a jar
java -jar lox.jar test/testfile

# bash shortcut
./lox test/testfile
```

## Notes
___
### Chapter 6 - Parsing

- comma operator: 
    - added the comma operator first, giving it the lowest precendence, before equality since it separates expressions

- ternary operator: [c operators wiki](https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B)

    > A precedence table, while mostly adequate, cannot resolve a few details. In particular, note that the ternary operator allows any arbitrary expression as its middle operand, despite being listed as having higher precedence than the assignment and comma operators. Thus a ? b, c : d is interpreted as a ? (b, c) : d, and not as the meaningless (a ? b), (c : d). So, the expression in the middle of the conditional operator (between ? and :) is parsed as if parenthesized. Also, note that the immediate, unparenthesized result of a C cast expression cannot be the operand of sizeof. Therefore, sizeof (int) * x is interpreted as (sizeof(int)) * x and not sizeof ((int) * x).

___
### Chapter 7 - Evaluating Expressions

- Ternary operator '?:'
    - I implemented the ternary operator as a separate ternary expression. Is this normal? or do other languages maybe consider the ternary operator as 2 separte binary expressions? I'm not sure that makes more sense. 
    - I'm concerned this might complicate using the '?' as a different operator later, since We throw an error if an ':' character isn't found after '?', maybe we just skip the error if it's not useful later.
        - second thought, this is not a problem because ternary op has the lowest precedence

___
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

- Challenge:
    1. implemented these prints in the repl by making visitExpressionStatement include a print
    2. implemented the unassigned var excception, by setting each name to true in a new 'assigned' map when they are assigned a value
        - this lets us use nil has a legit value, and forces us to provide _something_ as a var initializer before the var can be used.
        - gives us explicit nils but doesn't work when you intialize on the definition.
            
            the first throws 'unassigned variable' 
            ```lox
            var b = nil;
            print b;

            var b;
            b = nil;
            print b;
            ```

        - so we force an assign when nil is given to a var statement, this feels kind of messy
        ```java
        public Void visitVarStmt(Stmt.Var stmt) {
            Object value = null; 
            if (stmt.initializer != null) {
                value = evaluate(stmt.initializer);
                if(value == null && stringify(value) != "nil") throw new RuntimeError(stmt.name, "Unassigned variable'" + stmt.name.lexeme + "'.");
                environment.define(stmt.name.lexeme, value);
                environment.assign(stmt.name, value);
                
            }else{
                environment.define(stmt.name.lexeme, value);
            }

            return null;
        }
        ```

___
### Chapter 9 - Control Flow

    "... But the opening parenthesis after if doesn’t do anything useful. Dennis Ritchie put it there so he could use ) as the ending delimiter without having unbalanced parentheses."

- not sure who Dennis Ritchie is but I think I'm on his side on this one. I hate the weird grouping chars that Bash uses sometimes like if->fi and case->esac, just because I think its ugly.

___
### Chapter 10 - Functions

- Maximum argument counts
    > Other languages have various approaches. The C standard says a conforming implementation has to support at least 127 arguments to a function, but doesn’t say there’s any upper limit. The Java specification says a method can accept no more than 255 arguments. 
    
    - never occured to me there'd be a limit here, but I suppose there has to be in compiled languages? 
    
    > Our Java interpreter for Lox doesn’t really need a limit, but having a maximum number of arguments will simplify our bytecode interpreter in Part III. We want our two interpreters to be compatible with each other, even in weird corner cases like this, so we’ll add the same limit to jlox.

- Interpreting function calls
    > This is another one of those subtle semantic choices. Since argument expressions may have side effects, the order they are evaluated could be user visible. Even so, some languages like Scheme and C don’t specify an order. This gives compilers freedom to reorder them for efficiency, but means users may be unpleasantly surprised if arguments aren’t evaluated in the order they expect.

    - I think having a call or an assignment as an argument to another call is okay in practice, but having multiple that need to be executed in a specific order in the same call, would be bad practice anyway.

- Native Functions 
    > Many languages also allow users to provide their own native functions. The mechanism for doing so is called a foreign function interface (FFI), native extension, native interface, or something along those lines.

    - I've not heard of this before but wonder what this looks like for something like javascript, or python

- Function Declaration
    ```java
    private Stmt declaration() {
        try {
            if (match(FUN)) return function("function"); // this is kind of strange
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    ```
    - Weird that he got lazy with new enums and classes here, I suppose the overhead of another class method that inherits function is too much boilerplate for just this 1 check
    - is there a reason why we might implement another class?
        >Just like we reuse the grammar rule, we’ll reuse the function() method later to parse methods inside classes. When we do that, we’ll pass in “method” for kind so that the error messages are specific to the kind of declaration being parsed.

    - [Return](src/Retrun.java)
        > This class wraps the return value with the accoutrements Java requires for a runtime exception class. The weird super constructor call with those null and false arguments disables some JVM machinery that we don’t need. Since we’re using our exception class for control flow and not actual error handling, we don’t need overhead like stack traces.
        
        - neat use of Exception for something other than error handling


- Function Assignment?
    - He didn't assign his functions to the calling environment when they were declared, or anywhere else, causing runtime errors for "unassigned var"
    - I suspect this is going to cause more trouble later, and only caused a problem right now because unassigned vars is defined as an Error.
    - This gets us almost there, tests/functions and tests/count now work, but tests/fibonacci throws errors because of functions as operands for the + operator
    - fixed in issue #8

### Chapter 11 - Resolving and Binding

- Second pass - static analysis
   > "There are no side effects. When the static analysis visits a print statement, it doesn’t actually print anything. Calls to native functions or other operations that reach out to the outside world are stubbed out and have no effect."

    > "There is no control flow. Loops are visited only once. Both branches are visited in if statements. Logic operators are not short-circuited."

- The author tends to lean towards more restrictive syntax, assuming no one would ever want to do a thing, make it an error or make it impossible.
- I tend to disagree, language shouldn't tell the speaker what to say or how to say it.
- I think  there's space for both, and heavily Object Oriented code, makes programatic analysis easier I suppose, and stops the programmer from making common mistakes, but makes for a poor programming experience in my opinion.
- "Unlikely to be deliberate" isn't enough to convince me no one would/should ever try.
    > "Do either of those first two options look like something a user actually wants? Shadowing is rare and often an error, so initializing a shadowing variable based on the value of the shadowed one seems unlikely to be deliberate."

- functions are expressions and statements to make anonymous functions happen. This calls for changes in the resolver. 
    - resolveFunction
        - change definition to take Expr.Function instead of Stmt.Function
    - visitFunctionStmt
        - pass the function expression field from the function statement to resolve
            ```Java
            @Override
            public Void visitFunctionStmt(Stmt.Function stmt) {
                declare(stmt.name);
                define(stmt.name);

                resolveFunction(stmt.function);
                return null;
            }
            ```
    - will need to test this thoroughly with anonymous functions
    - implemented visitFunctionExpr to resolve variables in anonymous functions

- Java void vs Void?

- we also need to implement visitTernaryExpr in Resolver class
- and break statement
    - As part of the challenge to add breaks, we added the check for loops in the parser
    - Is it better to catch this in the parser or the resolver?
        - right now its in both but the parser runs first and is more restrictive (only break in loops) I kind of want to be able to break out of any scope
            - functions, blocks, loops
            - Is there legitimate use for breaking out of functions and blocks??
            
### Restarting Chapter 11 Resolution and Binding
- we stepped away from this book when our code was broken to finish Art of WebAssembly and got a bit turned around.
- DONT FORGET TO MERGE IN MAIN'S README BEFORE ROLLING IT BACK SO WE DON'T LOSE NOTES (addressed as merge conflict resolution)

> "Those are the only changes to Interpreter. This is why I chose a representation for our resolved data that was minimally invasive. All of the rest of the nodes continue working as they did before. Even the  code for modifying environments is unchanged" (188)

- OOP is bad for this, boilerplate complexity seems like grows exponentially with the number of classes and interfaces involved 

because a function statement holds a function expression...
```java
        for (Token param : function.function.parameters) {
            declare(param);
            define(param);
        }
        resolve(function.function.body);
```


### Chapter 12 Classes
- page 206, in visitClassStmt() in Interpreter.java, the loop to methods for the class are defined
```java
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, environment);
            methods.put(method.name.lexeme, function);
        }
```
- but lox function constructor takes 3 arguments (I'm not sure if this was a change because of a challenge question or not we will have to back track...) and we need a function expr not a function stmt (this I'm pretty sure was a challenge question)
```java
    LoxFunction(String name, Expr.Function declaration, Environment closure) {
        this.name = name;
        this.closure = closure;
        this.declaration = declaration;
    }
```

- fixed "package structure" - not a huge fan of how this is enforced
- AstPrinter.java I moved to tools, but in order for it to see Expr everything in Expr needs to be made public
- 12.6 My LoxFunction implementation also has 'name' in the constructor, the book just has declaration, and environment
- 12.7.2 
    > "We've been assuming that a user-written initializer doesn't explicitly return a value because most constructors don't. What should happen if a user tries:
    ```lox
    class Foo {
        init() {
            return "something else";
        }
    }
    ```
    > It's definitely not going to do what they want, so we may as well make it as static error"

    ^^ This I vehemently disagree with... I'll submit it's not a common case but I have done [this](https://github.com/mbmcmullen/Capstone/blob/master/Iteration_2/ExpressKPI/rxSource.js) with intention and I was happy javascript allowed me to. 
    I think it's better is to return 'this' by default, unless provided something else.
    