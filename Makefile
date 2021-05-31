JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSS = \
	Lox.java \
	Scanner.java \
	Token.java \
	TokenType.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) -r com