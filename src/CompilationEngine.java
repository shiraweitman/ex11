import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * compile the jack file according to the jack grammar
 */
class CompilationEngine {

    private static final String ARGUMENT = "argument";

    private BufferedWriter bufferedWriter;
    private JackTokenizer tokenizer;
    private ArrayList<String> operations = new ArrayList<>();
    private boolean minusOpFlag;
    private boolean expArr;
    private boolean letArr;
    private SymbolTable symbolTable;
    private String stKeyword;
    private String stType;
    private VMWriter vmWriter;
    private String currentSubroutine;
    private int currentNargs;
    private boolean isConst;
    private boolean isMethod;
    private boolean lastOpFlg;
    private ArrayList<String> opsArr = new ArrayList<>();
    private boolean whileFlag;

    CompilationEngine(File inputFile, File outputFile, File vmOutputFile) throws IOException {
        minusOpFlag = false;
        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter(vmOutputFile);
        this.tokenizer = new JackTokenizer(inputFile);
        FileWriter fileWriter = new FileWriter(outputFile);
        this.bufferedWriter = new BufferedWriter(fileWriter);
        initialSymbols();
    }

    /**
     * add all the symbols to the array
     */
    private void initialSymbols(){
        operations.add("-");
        operations.add("+");
        operations.add("*");
        operations.add("/");
        operations.add("&amp;");
        operations.add("|");
        operations.add("&lt;");
        operations.add("&gt;");
        operations.add("=");
    }

    /**
     * write the currant token to the XML file
     */
    private void writeToken(String tokenType, String token) throws IOException {
        bufferedWriter.write("<");
        bufferedWriter.write(tokenType);
        bufferedWriter.write("> ");
        bufferedWriter.write(token);
        bufferedWriter.write(" </");
        bufferedWriter.write(tokenType);
        bufferedWriter.write(">");
        bufferedWriter.newLine();
    }

    private void writeLine(String line) throws IOException {
        this.bufferedWriter.write(line);
        this.bufferedWriter.newLine();
    }

    /**
     * compile full jack class
     */
    void CompileClass() throws IOException {
        writeLine("<class>");
        tokenizer.advance();
        writeToken("keyword", tokenizer.currentToken); // write "class"
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // write the class name
        this.symbolTable.className = tokenizer.currentToken;
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write {
        tokenizer.advance();
        while (tokenizer.currentToken.equals("static") || tokenizer.currentToken.equals("field")){
            CompileClassVarDec();
        }
        while (!tokenizer.currentToken.equals("}")){
            compileSubroutineDec();
        }
        writeToken("symbol", tokenizer.currentToken); // write }
        writeLine("</class>");
        tokenizer.bufferedReader.close();
        this.bufferedWriter.close();
        this.vmWriter.close();

    }

    /**
     * compile the class var declerations
     */
    private void CompileClassVarDec() throws IOException {
        writeLine("<classVarDec>");
        writeToken("keyword", tokenizer.currentToken); // write static/field
        this.stKeyword = tokenizer.currentToken;
        tokenizer.advance();
        checkType(); // write the var type
        this.stType = tokenizer.currentToken;
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // the var name
        this.symbolTable.define(tokenizer.currentToken, this.stType, this.stKeyword);
        tokenizer.advance();
        checkMoreVars();
        writeToken("symbol", tokenizer.currentToken); // write ;
        tokenizer.advance();
        writeLine("</classVarDec>");
    }

    /**
     * compile parameter list
     */
    private void compileParameterList() throws IOException {
        writeLine("<parameterList>");
        if(!tokenizer.currentToken.equals(")")){ // not empty
            checkType(); // write the var type
            this.stType = tokenizer.currentToken;
            tokenizer.advance();
            writeToken("identifier", tokenizer.currentToken); // write the var name
            this.symbolTable.define(tokenizer.currentToken, this.stType, ARGUMENT);
            tokenizer.advance();
            while (true){
                if(tokenizer.currentToken.equals(",")){
                    writeToken("symbol", tokenizer.currentToken); // write ","
                    tokenizer.advance(); //advance to the expression after the comma
                    checkType(); // write the var type
                    this.stType = tokenizer.currentToken;
                    tokenizer.advance();
                    writeToken("identifier", tokenizer.currentToken); // write the var name
                    this.symbolTable.define(tokenizer.currentToken, this.stType, ARGUMENT);
                    tokenizer.advance();
                } else {
                    break;
                }
            }
        }

        writeLine("</parameterList>");
    }

    /**
     * compile var declerations
     */
    private void compileVarDec() throws IOException {
        writeLine("<varDec>");
        writeToken("keyword", tokenizer.currentToken); // write var
        this.stKeyword = tokenizer.currentToken;
        tokenizer.advance();
        checkType(); // write the var type
        this.stType = tokenizer.currentToken;
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // the var name
        this.symbolTable.define(tokenizer.currentToken, this.stType, this.stKeyword);
        tokenizer.advance();
        checkMoreVars();
        writeToken("symbol", tokenizer.currentToken); // write ;
        tokenizer.advance();
        writeLine("</varDec>");
    }


    /**
     * check id the given type is primitive or nor
     */
    private void checkType() throws IOException {
        if (tokenizer.currentToken.equals("int") || tokenizer.currentToken.equals("char") ||
                tokenizer.currentToken.equals("boolean")){
            this.writeToken("keyword", tokenizer.currentToken);
        } else {
            this.writeToken("identifier", tokenizer.currentToken);
        }
    }

    /**
     * check if there are more vars is the dec
     */
    private void checkMoreVars() throws IOException {
        while (!tokenizer.currentToken.equals(";")){
            if(tokenizer.currentToken.equals(",")){
                writeToken("symbol", tokenizer.currentToken); // write ","
            } else {
                writeToken("identifier", tokenizer.currentToken); // write varName
                this.symbolTable.define(tokenizer.currentToken, this.stType, this.stKeyword);
            }
            tokenizer.advance();
        }
    }

    /**
     * compile all possible statements
     */
    private void compileStatements() throws IOException {
        writeLine("<statements>");
        mainLoop: while (!tokenizer.currentToken.equals("}")) {
            switch (tokenizer.currentToken) {
                case "do":
                    compileDo();
                    break;
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    break mainLoop;
            }
        }
        writeLine("</statements>");

    }

    /**
     * compile do statement
     */
    private void compileDo() throws IOException {
        writeLine("<doStatement>");
        writeToken("keyword", tokenizer.currentToken); // write "do"
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // write the first identifier of subroutineCall
        this.currentSubroutine = tokenizer.currentToken;
        tokenizer.advance();
        compileSubroutineCall();
        this.currentSubroutine = "";
        writeToken("symbol", tokenizer.currentToken); // write: ;
        tokenizer.advance();
        vmWriter.writePop(Segment.TEMP, 0);
        writeLine("</doStatement>");
    }

    /**
     * compile let statement
     */
    private void compileLet() throws IOException {
        writeLine("<letStatement>");
        this.letArr = false;
        writeToken("keyword", tokenizer.currentToken); // write "let"
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // the var name
        String var = tokenizer.currentToken;

        tokenizer.advance();
        if (!tokenizer.currentToken.equals("=")){
            this.letArr = true;
            writePushVar(var);

            writeToken("symbol", tokenizer.currentToken); // write: "["

            tokenizer.advance();
            CompileExpression();

            vmWriter.writeCommand(Command.ADD);

            writeToken("symbol", tokenizer.currentToken); // write: "]"
            tokenizer.advance();
        }
        writeToken("symbol", tokenizer.currentToken); // write: =

        tokenizer.advance();
//        CompileExpression();
        if(!this.letArr && this.expArr){
            CompileExpression();
            writePopVar(var);
        }

        if(this.letArr && this.expArr){
            CompileExpression();
            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePush(Segment.THAT, 0);
            vmWriter.writePop(Segment.TEMP, 0);
            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.THAT, 0);
        }

        if(this.letArr && !this.expArr){
            vmWriter.writePop(Segment.POINTER, 1);
            CompileExpression();
            vmWriter.writePop(Segment.THAT, 0);
        } else {
                System.out.println("error in compile arr case");
        }

        writeToken("symbol", tokenizer.currentToken); // write: ;
        tokenizer.advance();
        writeLine("</letStatement>");

    }

    /**
     * compile while statement
     */
    private void compileWhile() throws IOException {
        writeLine("<whileStatement>");
        this.whileFlag = true;
        this.vmWriter.WriteLabel("WHILE");
        compileWhileIf();
        writeLine("</whileStatement>");
        this.vmWriter.WriteLabel("END-WHILE");
        this.vmWriter.labelCounter++;

    }

    /**
     * compile return statement
     */
    private void compileReturn() throws IOException {
        writeLine("<returnStatement>");
        writeToken("keyword", tokenizer.currentToken); // write return
        tokenizer.advance();
        if (tokenizer.currentToken.equals(";")) {
            vmWriter.writePush(Segment.CONST, 0);
        } else {
            CompileExpression();
        }
        this.vmWriter.writeReturn();
        writeToken("symbol", tokenizer.currentToken); // write: ;
        tokenizer.advance();
        writeLine("</returnStatement>");
    }

    /**
     * compile if statement
     */
    private void compileIf() throws IOException {
        writeLine("<ifStatement>");
        compileWhileIf();
        if(tokenizer.currentToken.equals("else")){
            compileElse();
        }
        writeLine("</ifStatement>");
    }

    /**
     * compile part of the while and the if statements
     */
    private void compileWhileIf() throws IOException {
        writeToken("keyword", tokenizer.currentToken); // write if/while
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write (
        tokenizer.advance();
        CompileExpression();
        this.vmWriter.writeCommand(Command.NOT);

        this.vmWriter.writeGoto("END-WHILE");

        writeToken("symbol", tokenizer.currentToken); // write )
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write {
        tokenizer.advance();
        compileStatements();

        if(this.whileFlag) this.vmWriter.writeGoto("WHILE");
        else this.vmWriter.writeGoto("IF");

        writeToken("symbol", tokenizer.currentToken); // write }
        tokenizer.advance();
    }

    /**
     * compile the else part
     */
    private void compileElse() throws IOException {
        writeToken("keyword", tokenizer.currentToken); // write else
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write {
        tokenizer.advance();
        compileStatements();
        writeToken("symbol", tokenizer.currentToken); // write }
        tokenizer.advance();
    }

    /**
     * compile single expression
     */
    private void CompileExpression() throws IOException {
        writeLine("<expression>");
        this.expArr = false;
        if(tokenizer.currentToken.equals(")")){
            return;
        }
        JackTokenizer.TOKEN_TYPE firstTokenType = tokenizer.currentTokenType;
        String firstToken = tokenizer.currentToken;
        tokenizer.advance();
        CompileTerm(firstToken, firstTokenType);
        while (operations.contains(tokenizer.currentToken)){
            if(tokenizer.currentToken.equals("-")){
                this.minusOpFlag = true;
            }

            writeToken("symbol", tokenizer.currentToken); // write operation
            String lastOp = tokenizer.currentToken;

            tokenizer.advance();
            if(!tokenizer.currentToken.equals("(")){
                this.lastOpFlg = true;
            } else {
                this.opsArr.add(lastOp);
            }

            JackTokenizer.TOKEN_TYPE nextTokenType = tokenizer.currentTokenType;
            String nextToken = tokenizer.currentToken;
            tokenizer.advance();
            CompileTerm(nextToken, nextTokenType);
            if(this.lastOpFlg){
                writeOperation(lastOp);
            }
            this.lastOpFlg = false;
        }
        Collections.reverse(this.opsArr);
        for (String op : this.opsArr){
            writeOperation(op);
            //System.out.println(op);
        }
        this.opsArr.clear();
        writeLine("</expression>");
    }


    /**
     * compile single term
     * @param firstToken the token before the current
     * @param firstTokenType the token type
     */
    private void CompileTerm(String firstToken, JackTokenizer.TOKEN_TYPE firstTokenType) throws IOException {
        writeLine("<term>");
        switch (firstTokenType){
            case INT_CONST:
                vmWriter.writePush(Segment.CONST, Integer.parseInt(firstToken));


                writeToken("integerConstant", firstToken);
                break;
            case STRING_CONST:
                // todo check this case
                writeToken("stringConstant", firstToken);
                break;
            case KEYWORD:
                writeToken("keyword", firstToken);
                break;
            case IDENTIFIER:

                writeToken("identifier", firstToken); // varName
                if(tokenizer.currentToken.equals("[")){
                    this.expArr = true;
                    writePushVar(firstToken);

                    writeToken("symbol", tokenizer.currentToken); // write "["
                    tokenizer.advance();
                    CompileExpression();

                    vmWriter.writeCommand(Command.ADD);


                    writeToken("symbol", tokenizer.currentToken);  // write "]"
                    tokenizer.advance(); //

                } else if (tokenizer.currentToken.equals("(") || tokenizer.currentToken.equals(".") ) {
                    this.currentSubroutine = firstToken;
                    compileSubroutineCall();
                    this.currentSubroutine = "";
                }
                else if(tokenizer.currentToken.equals(")")){
                    break;

                } else if(tokenizer.currentToken.equals(",")){
                    break;
                } else {
                    writePushVar(firstToken);

                }
                break;

            case SYMBOL:
                if(firstToken.equals("(")) { // check if "("
                    writeToken("symbol", firstToken); // write "("
                    CompileExpression(); // write expression
                    writeToken("symbol", tokenizer.currentToken);  // write ")"
                    tokenizer.advance();

                } else if(firstToken.equals("~")) {// check if "~"
                    vmWriter.writeCommand(Command.NOT);

                    writeToken("symbol", firstToken); // write "~"
                    JackTokenizer.TOKEN_TYPE tildaTokenType = tokenizer.currentTokenType;
                    String tildaNextToken = tokenizer.currentToken;
                    tokenizer.advance();
                    CompileTerm(tildaNextToken, tildaTokenType); // recursive call

                } else if(firstToken.equals("-") && !this.minusOpFlag) {// check if "-"

                    vmWriter.writeCommand(Command.NEG);

                    writeToken("symbol", firstToken); // write "-"
                    ///
                    JackTokenizer.TOKEN_TYPE tildaTokenType = tokenizer.currentTokenType;
                    String tildaNextToken = tokenizer.currentToken;
                    tokenizer.advance();
                    ///
                    CompileTerm(tildaNextToken, tildaTokenType); // recursive call
                }
                this.minusOpFlag = false;

        }
        writeLine("</term>");
    }


    private void writeOperation(String token) throws IOException {
        switch (token){
            case "*":
                vmWriter.writeCall("Math.multiply", 2);
                break;
            case "/":
                vmWriter.writeCall("Math.divide", 2);
                break;
            default:
                Command command = findOperation(token);
                vmWriter.writeCommand(command);
        }
    }

    private Command findOperation(String op){
        switch (op){
            case "+":
                return Command.ADD;
            case "<":
                return Command.LT;
            case ">":
                return Command.GT;
            case "-":
                return Command.SUB;
            case "&":
                return Command.AND;
            case "|":
                return Command.OR;
            default: // we assume correct input, write "="
                return Command.EQ;
        }
    }

    private void writePushVar(String token) throws IOException {
//        if(!this.symbolTable.isInTable(token)){
//            System.out.println(token);
//            //this.vmWriter.writePush(Segment.POINTER, 0);
        //} else {
            String kind = symbolTable.kindOf(token);
            int index = symbolTable.indexOf(token);
            switch (kind) {
                case "static":
                    vmWriter.writePush(Segment.STATIC, index);

                case "field":
                    vmWriter.writePush(Segment.THIS, index);

                case "argument":
                    vmWriter.writePush(Segment.ARG, index);

                case "var":
                    vmWriter.writePush(Segment.LOCAL, index);
            }
       // }
    }

    private void writePopVar(String token) throws IOException {
        String kind = symbolTable.kindOf(token);
        int index = symbolTable.indexOf(token);
        switch (kind){
            case "static":
                vmWriter.writePop(Segment.STATIC, index);

            case "field":
                vmWriter.writePop(Segment.THIS, index);

            case "argument":
                vmWriter.writePop(Segment.ARG, index);

            case "var":
                vmWriter.writePop(Segment.LOCAL, index);
        }
    }

    /**
     * write list of expressions
     */
    private void CompileExpressionList() throws IOException {
        this.currentNargs = 0;
        writeLine("<expressionList>");
        if(!tokenizer.currentToken.equals(")")){ // not empty
            CompileExpression();
            this.currentNargs++;
            while (true){
                if(tokenizer.currentToken.equals(",")){
                    writeToken("symbol", tokenizer.currentToken); // write ","
                    tokenizer.advance(); // advance to the expression after the comma
                    CompileExpression();
                    this.currentNargs++;
                } else {
                    break;
                }
            }
        }
        writeLine("</expressionList>");
    }

    /**
     * write the subroutine body
     */
    private void compileSubroutineBody() throws IOException {
        writeLine("<subroutineBody>");
        writeToken("symbol", tokenizer.currentToken); // write "{"
        tokenizer.advance();

        while (tokenizer.currentToken.equals("var")){
            compileVarDec();
        }

        this.vmWriter.writeFunction(this.symbolTable.className+"."+this.currentSubroutine, this.symbolTable.varCounter);
        if(this.isConst){
            int index = this.symbolTable.fieldCounter;
            this.vmWriter.writePush(Segment.CONST, index);
            this.vmWriter.writeCall("Memory.alloc", 1);
            this.vmWriter.writePop(Segment.POINTER, 0);
            this.isConst = false;
        }

        else if(this.isMethod){
            this.vmWriter.writePush(Segment.ARG, 0);
            this.vmWriter.writePop(Segment.POINTER, 0);
        }


        compileStatements();
        writeToken("symbol", tokenizer.currentToken); // write "}"
        tokenizer.advance();
        writeLine("</subroutineBody>");
    }

    boolean isClass(String identifier){
        String classRegx = "^[A-Z].*";
        return identifier.matches(classRegx);
    }

    /**
     * write subroutine call
     */
    private void compileSubroutineCall() throws IOException {
        // todo handle this case
        boolean funcFlag = false;
        String subName = "";
        if(!tokenizer.currentToken.equals("(")){
            writeToken("symbol", tokenizer.currentToken); // write "."
            tokenizer.advance();
            writeToken("identifier", tokenizer.currentToken); // write subroutineName
            subName = tokenizer.currentToken;
            funcFlag = true;
            // writePushVar("this");
            tokenizer.advance();
        }

        writeToken("symbol", tokenizer.currentToken); // write "("
        tokenizer.advance();
        CompileExpressionList(); // compile expression

        if(funcFlag){
            if (!isClass(this.currentSubroutine)) {
                writePushVar(this.currentSubroutine);
            }
            vmWriter.writeCall(this.currentSubroutine+"."+subName, this.currentNargs);
        } else {
            vmWriter.writePush(Segment.POINTER, 0);
            vmWriter.writeCall( this.currentSubroutine, this.currentNargs);
        }

        writeToken("symbol", tokenizer.currentToken); // write ")"
        tokenizer.advance();
    }

    /**
     * compile the subroutine declaration
     */
    private void compileSubroutineDec() throws IOException {
        symbolTable.startSubroutine();
        writeLine("<subroutineDec>");
        writeToken("keyword", tokenizer.currentToken); // write constructor/function/method
        if(tokenizer.currentToken.equals("constructor")){
            this.isConst = true;
        } else if(tokenizer.currentToken.equals("method")){
            this.isMethod = true;
        }

        tokenizer.advance();
        if(tokenizer.currentToken.equals("void")){
            writeToken("keyword", tokenizer.currentToken); // write void
        } else {
            checkType();
        }
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // write subroutineName
        this.currentSubroutine = tokenizer.currentToken;
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write "("
        tokenizer.advance();
        compileParameterList();
        writeToken("symbol", tokenizer.currentToken); // write ")"
        tokenizer.advance();
        compileSubroutineBody();

        this.isMethod = false;

        writeLine("</subroutineDec>");
        //Set<String> keys = this.symbolTable.subroutineTable.keySet();
//        for(String key: keys){
//            System.out.println(key + " " + Arrays.toString(this.symbolTable.subroutineTable.get(key).toArray()));
//        }
    }

}
