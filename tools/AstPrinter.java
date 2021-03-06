package tools;

import com.craftinginterpreters.lox.Expr;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) { 
        return expr.accept(this);
    }
    
    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize(expr.operator.lexeme+expr.operator2.lexeme, expr.condition, expr.thenBranch, expr.elseBranch);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return parenthesize(expr.value.toString());
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        if (expr.name == null) return "nil";
        return parenthesize(expr.name.lexeme);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
            new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(123)),
            new Token(TokenType.STAR, "*", null, 1),
            new Expr.Grouping(
                new Expr.Literal(45.67)));
        
        System.out.println(new AstPrinter().print(expression));
    }
}