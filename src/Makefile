###############################################################################
#
# Makefile for project10 in Java
#
# Students:
# Maayan Magenheim, ID 203237953, maayan.magenheim@mail.huji.ac.il
# Shira Weitman, ID 314842519, shira.weitman@mail.huji.ac.il
#
###############################################################################

JAVAC=javac

JAVACFLAGS=-encoding ISO-8859-1

SRCS=*.java

EXEC=JackAnalyzer

TAR=tar
TARFLAGS=cvf
TARNAME=project10.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

all: compile 

compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)
	
tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)
	
clean:
	rm -f *.class *~

