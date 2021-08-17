package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Interpreter 
implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    
    private static class BreakException extends RuntimeException {}

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public static Boolean isRepl = true;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    void interpret(List<Stmt> statements){
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass)environment.getAt(distance, "super");
        
        LoxInstance object = (LoxInstance)environment.getAt(distance -1, "this");

        LoxFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }

        environment.define(stmt.name.lexeme, null);

        if (stmt.superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        Map<String, LoxFunction> classMethods = new HashMap<>();
        for (Stmt.Function method : stmt.classMethods) {
            LoxFunction function = new LoxFunction(method.name.lexeme, method.function, environment, false);
            classMethods.put(method.name.lexeme, function);
        }

        LoxClass metaclass = new LoxClass(null, null, stmt.name.lexeme + " metaclass", classMethods);

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method.name.lexeme, method.function, environment,
                method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(metaclass, (LoxClass)superclass, stmt.name.lexeme, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if(isRepl) System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        String fnName = stmt.name.lexeme;
        LoxFunction function = new LoxFunction(fnName, stmt.function, environment, false);
        environment.define(stmt.name.lexeme, function);
        environment.assign(stmt.name, function);
        return null;
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new LoxFunction(null, expr, environment, false);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null; 
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            environment.define(stmt.name.lexeme, value);
            environment.assign(stmt.name, value);
            
        }else{
            environment.define(stmt.name.lexeme, value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTruthy(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (BreakException ex) {
            // do nothing
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator,left,right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator,left,right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator,left,right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                if (left instanceof String && right instanceof Double) {
                    return (String)left + stringify(right);
                }

                if (left instanceof Double && right instanceof String) {
                    return stringify(left) + (String)right;
                }

                throw new RuntimeError(expr.operator, "+ operands must be numbers or strings.");
            case SLASH:
                checkNumberOperands(expr.operator,left,right);
                if((double)right==0.0) throw new RuntimeError(expr.operator, "/ by Zero is ILLEGAL.");
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator,left,right);
                return (double)left * (double)right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Only functions and classes are callable objects.");
        }

        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                function.arity() + " arguments but got " +
                arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.Object);
        if (object instanceof LoxInstance) {
            Object result = ((LoxInstance) object).get(expr.name);
            if (result instanceof LoxFunction && ((LoxFunction) result).isGetter()) {
                result = ((LoxFunction) result).call(this, null);
            }

            return result;
        }

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);
        if(isTruthy(condition)) return evaluate(expr.thenBranch);
        else return evaluate(expr.elseBranch);
    }

}