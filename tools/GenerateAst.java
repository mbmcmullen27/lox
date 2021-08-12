package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Ternary  : Expr condition, Expr thenBranch, Expr elseBranch",
            "Function : List<Token> parameters, List<Stmt> body",
            "Assign   : Token name, Expr value",
            "Binary   : Expr left, Token operator, Expr right",
            "Call     : Expr callee, Token paren, List<Expr> arguments",
            "Get      : Expr Object, Token name",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Logical  : Expr left, Token operator, Expr right",
            "Set      : Expr object, Token name, Expr value",
            "This     : Token keyword",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block      : List<Stmt> statements",
            "Class      : Token name, List<Stmt.Function> methods, List<Stmt.Function> classMethods",
            "Expression : Expr expression",
            "Function   : Token name, Expr.Function function",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Print      : Expr expression",
            "Return     : Token keyword, Expr value",
            "Var        : Token name, Expr initializer",
            "While      : Expr condition, Stmt body",
            "Break      : Token keyword"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) 
    throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println("package com.craftinginterpreters.lox;");
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit" + typeName + baseName + "(" + 
            typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("\t}");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("\tstatic class " + className + " extends " + baseName + " {");
        // Constructor
        String list = fieldList.equals("NONE") ? "" : fieldList;
        writer.println("\t\t" + className + "(" + list + ") {");

        // Store parameters in fields.
        String [] fields = fieldList.split(", ");
        for (String field : fields) {
            if(!field.equals("NONE")) {
                String name = field.split(" ")[1];
                writer.println("\t\t\tthis." + name + " = " + name + ";");
            }
        }
        
        writer.println("\t\t}");

        // Visitor pattern.
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println("\t\treturn visitor.visit" + className+ baseName + "(this);");
        writer.println("\t\t}");

        // Fields.
        writer.println();
        for (String field : fields) {
            if(!field.equals("NONE"))
                writer.println("\t\tfinal " + field + ";");
        }

        writer.println("\t}");
    }
}