###############################################################################
#
# Makefile for project11 in Java
#
# Students:
# Tom Saltsberg, ID 204241400, tom.saltsberg@mail.huji.ac.il
#
###############################################################################

JAVAC=javac

JAVACFLAGS=-encoding ISO-8859-1

SRCS=*.java

EXEC=JackCompiler

TAR=tar
TARFLAGS=cvf
TARNAME=project11.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

all: compile 

compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)
	
tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)
	
clean:
	rm -f *.class *~

