import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SymbolTable symbolTable;
    private String stKeyword;
    private String stType;


    CompilationEngine(File inputFile, File outputFile) throws IOException {
        minusOpFlag = false;
        this.symbolTable = new SymbolTable();
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

//        Set<String> keys = this.symbolTable.classTable.keySet();
//        for(String key: keys){
//            System.out.println(key + " " + Arrays.toString(this.symbolTable.classTable.get(key).toArray()));
//        }
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
        tokenizer.advance();
        compileSubroutineCall();
        writeToken("symbol", tokenizer.currentToken); // write: ;
        tokenizer.advance();
        writeLine("</doStatement>");
    }

    /**
     * compile compile statement
     */
    private void compileLet() throws IOException {
        writeLine("<letStatement>");
        writeToken("keyword", tokenizer.currentToken); // write "let"
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // the var name
        tokenizer.advance();
        if (!tokenizer.currentToken.equals("=")){
            writeToken("symbol", tokenizer.currentToken); // write: "["
            tokenizer.advance();
            CompileExpression();
            writeToken("symbol", tokenizer.currentToken); // write: "]"
            tokenizer.advance();
        }
        writeToken("symbol", tokenizer.currentToken); // write: =
        tokenizer.advance();
        CompileExpression();
        writeToken("symbol", tokenizer.currentToken); // write: ;
        tokenizer.advance();
        writeLine("</letStatement>");

    }

    /**
     * compile while statement
     */
    private void compileWhile() throws IOException {
        writeLine("<whileStatement>");
        compileWhileIf();
        writeLine("</whileStatement>");

    }

    /**
     * compile return statement
     */
    private void compileReturn() throws IOException {
        writeLine("<returnStatement>");
        writeToken("keyword", tokenizer.currentToken); // write return
        tokenizer.advance();
        if (!tokenizer.currentToken.equals(";")) { // there is an expression before ";"
            CompileExpression();
        }
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
        writeToken("symbol", tokenizer.currentToken); // write )
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write {
        tokenizer.advance();
        compileStatements();
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
            tokenizer.advance();

            JackTokenizer.TOKEN_TYPE nextTokenType = tokenizer.currentTokenType;
            String nextToken = tokenizer.currentToken;
            tokenizer.advance();
            CompileTerm(nextToken, nextTokenType);
        }
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
                writeToken("integerConstant", firstToken);
                break;
            case STRING_CONST:
                writeToken("stringConstant", firstToken);
                break;
            case KEYWORD:
                writeToken("keyword", firstToken);
                break;
            case IDENTIFIER:
                writeToken("identifier", firstToken); // varName
                if(tokenizer.currentToken.equals("[")){
                    writeToken("symbol", tokenizer.currentToken); // write "["
                    tokenizer.advance();
                    CompileExpression();
                    writeToken("symbol", tokenizer.currentToken);  // write "]"
                    tokenizer.advance(); //
                } else if (tokenizer.currentToken.equals("(") || tokenizer.currentToken.equals(".") ) {
                    compileSubroutineCall();
                }
                else if(tokenizer.currentToken.equals(")")){
                    break;

                } else if(tokenizer.currentToken.equals(",")){
                    break;
                }
                break;

            case SYMBOL:
                if(firstToken.equals("(")) { // check if "("
                    writeToken("symbol", firstToken); // write "("
                    CompileExpression(); // write expression
                    writeToken("symbol", tokenizer.currentToken);  // write ")"
                    tokenizer.advance();

                } else if(firstToken.equals("~")) {// check if "~"
                    writeToken("symbol", firstToken); // write "~"
                    JackTokenizer.TOKEN_TYPE tildaTokenType = tokenizer.currentTokenType;
                    String tildaNextToken = tokenizer.currentToken;
                    tokenizer.advance();
                    CompileTerm(tildaNextToken, tildaTokenType); // recursive call

                } else if(firstToken.equals("-") && !this.minusOpFlag) {// check if "-"
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

    /**
     * write list of expressions
     */
    private void CompileExpressionList() throws IOException {
        writeLine("<expressionList>");
        if(!tokenizer.currentToken.equals(")")){ // not empty
            CompileExpression();
            while (true){
                if(tokenizer.currentToken.equals(",")){
                    writeToken("symbol", tokenizer.currentToken); // write ","
                    tokenizer.advance(); //advance to the expression after the comma
                    CompileExpression();
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
        compileStatements();
        writeToken("symbol", tokenizer.currentToken); // write "}"
        tokenizer.advance();
        writeLine("</subroutineBody>");

    }

    /**
     * write subroutine call
     */
    private void compileSubroutineCall() throws IOException {

        if(tokenizer.currentToken.equals("(")){

        } else {
            writeToken("symbol", tokenizer.currentToken); // write "."
            tokenizer.advance();
            writeToken("identifier", tokenizer.currentToken); // write subroutineName
            tokenizer.advance();
        }
        writeToken("symbol", tokenizer.currentToken); // write "("
        tokenizer.advance();
        CompileExpressionList(); // compile expression
        writeToken("symbol", tokenizer.currentToken); // write ")"
        tokenizer.advance();
    }

    /**
     * compile the subroutine decleration
     */
    private void compileSubroutineDec() throws IOException {
        symbolTable.startSubroutine();
        writeLine("<subroutineDec>");
        writeToken("keyword", tokenizer.currentToken); // write constructor/function/method
        tokenizer.advance();
        if(tokenizer.currentToken.equals("void")){
            writeToken("keyword", tokenizer.currentToken); // write void
        } else {
            checkType();
        }
        tokenizer.advance();
        writeToken("identifier", tokenizer.currentToken); // write subroutineName
        tokenizer.advance();
        writeToken("symbol", tokenizer.currentToken); // write "("
        tokenizer.advance();
        compileParameterList();
        writeToken("symbol", tokenizer.currentToken); // write ")"
        tokenizer.advance();
        compileSubroutineBody();

        writeLine("</subroutineDec>");
        Set<String> keys = this.symbolTable.subroutineTable.keySet();
        for(String key: keys){
            System.out.println(key + " " + Arrays.toString(this.symbolTable.subroutineTable.get(key).toArray()));
        }
    }


}
