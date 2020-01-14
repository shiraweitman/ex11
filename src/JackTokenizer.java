import java.io.*;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * create jack tokens
 */
class JackTokenizer {
    private static String commentPattern = "(^//.*)|(^/\\*.*)|^\\*.*";
    String currentToken; // hold the current token
    private final ArrayList<String> symbols = new ArrayList<>();
    BufferedReader bufferedReader;
    private File cleaned;
    private char pointer; // hold the current char from the jack file
    private boolean tempPointer;
    private boolean intFlag;
    TOKEN_TYPE currentTokenType;


    JackTokenizer (File inputFile) throws FileNotFoundException {
        this.tempPointer = false;
        try {
            // clean the file from comments and empty lines
            cleaned = new File(inputFile.getAbsolutePath().
                    replaceAll(".jack", ".txt"));
            FileWriter fileWriter = new FileWriter(cleaned);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            this.cleanFile(bufferedWriter, new BufferedReader(new FileReader(inputFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileReader fileReader = new FileReader(cleaned);
        this.bufferedReader = new BufferedReader(fileReader);
        initialSymbols();
    }

    /** the possible token types*/
    enum TOKEN_TYPE {KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST}


    /** the saved words in the jack language*/
    enum KEYWORD {CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR,
        VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN,
        TRUE, FALSE, NULL, THIS}

    /**
     * clean the input file from comments and empty lines
     */
    private void cleanFile(BufferedWriter bufferedWriter, BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null){
            if(!line.startsWith("//") && !isComment(line) && !line.contains("/*") && !line.startsWith(" *") && !line.endsWith("/")
                    &&  !line.equals("") && !line.matches("^\\s+\\*") && !line.equals("\t")){
                if(line.contains("else")){
                    System.out.println("");
                }
                line = line.replaceAll("\t", "");
                line = line.replaceAll("  ", "");
                String[] splitedLine = line.split("//");
                bufferedWriter.write(splitedLine[0]);
            }
        }

        bufferedWriter.close();
        bufferedReader.close();
     }

    private boolean isComment(String line) {
        return line.matches(commentPattern);
    }

    private void initialSymbols(){
        symbols.add("{");
        symbols.add("}");
        symbols.add("(");
        symbols.add(")");
        symbols.add("[");
        symbols.add("]");
        symbols.add(".");
        symbols.add(",");
        symbols.add(";");
        symbols.add("+");
        symbols.add("-");
        symbols.add("*");
        symbols.add("/");
        symbols.add("&");
        symbols.add("|");
        symbols.add("<");
        symbols.add(">");
        symbols.add("=");
        symbols.add("~");
    }

    /**
     *  check if the file have tokens
     */
    private boolean hasMoreTokens() throws IOException {
        int currentChar;
        currentChar =  this.bufferedReader.read();
        if(currentChar == -1){
            return false;
        } else {
            this.pointer = (char) currentChar;
            return true;
        }
    }

    /**
     * advance to the next legal token
     */
    void advance() throws IOException {
        this.currentToken = "";
        if(this.tempPointer){
            this.currentTokenType = tokenType();
            this.tempPointer = false;
            return;
        }
        if(this.intFlag){
            this.currentTokenType = tokenType();
            this.intFlag = false;
            return;
        }
        if (hasMoreTokens()){
            while (this.pointer==' '){
                this.pointer = (char) this.bufferedReader.read();
            }
            this.currentTokenType = tokenType();
        }
    }

    /**
     * get the next token and find it's type
     */
    private TOKEN_TYPE tokenType() throws IOException {
        // handle symbol token
        if(symbols.contains(String.valueOf(this.pointer))){
            return symbol();

        // handle int case
        } else if (String.valueOf(this.pointer).matches("-?\\d+(\\.\\d+)?")){
            return intVal();

        // handle string case
        } else if(String.valueOf(this.pointer).equals("\"")){
            return stringVal();

        // decide if it is a identifier or keyword
        } else {
            return identifier();
        }

    }

    /**
     * handle special symbols
     */
    private String specialSymbol(char pointer) {
        switch (pointer){
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '\"':
                return "&quot;";
            case '&':
                return "&amp;";
            default: return "0";
        }
    }


    /**
     * return the right keyword
     */
    private KEYWORD keyWord(){
        switch (this.currentToken){
            case "class": return KEYWORD.CLASS;
            case "method": return KEYWORD.METHOD;
            case "function": return KEYWORD.FUNCTION;
            case "constructor": return KEYWORD.CONSTRUCTOR;
            case "int": return KEYWORD.INT;
            case "boolean": return KEYWORD.BOOLEAN;
            case "char": return KEYWORD.CHAR;
            case "void": return KEYWORD.VOID;
            case "var": return KEYWORD.VAR;
            case "static": return KEYWORD.STATIC;
            case "field": return KEYWORD.FIELD;
            case "let": return KEYWORD.LET;
            case "do": return KEYWORD.DO;
            case "if": return KEYWORD.IF;
            case "else": return KEYWORD.ELSE;
            case "while": return KEYWORD.WHILE;
            case "return": return KEYWORD.RETURN;
            case "true": return KEYWORD.TRUE;
            case "false": return KEYWORD.FALSE;
            case "null": return KEYWORD.NULL;
            case "this": return KEYWORD.THIS;
        }
        return null;
    }

    /**
     * handle symbol token
     */
    private TOKEN_TYPE symbol(){
        String specialSymb = specialSymbol(this.pointer);
        this.currentToken = (!specialSymb.equals("0")) ? specialSymb : String.valueOf(this.pointer);
        this.pointer = 0;
        return TOKEN_TYPE.SYMBOL;
    }

    /**
     * handle identifier token
     */
    private TOKEN_TYPE identifier() throws IOException {
        this.currentToken = String.valueOf(this.pointer);
        char nextChar = (char) this.bufferedReader.read();
        // while isn't a a symbol or space, continue
        while ((!symbols.contains(String.valueOf(nextChar))) && (!String.valueOf(nextChar).equals(" "))){
            this.currentToken = this.currentToken + nextChar;
            nextChar = (char) this.bufferedReader.read();
        }
        // save the final char to a var, maybe it's a symbol
        this.pointer = nextChar;
        if(this.symbols.contains(String.valueOf(this.pointer))){
            this.tempPointer = true;
        }
        // clean the line
        this.currentToken = this.currentToken.replaceAll("[\r\n]+", "");

        // get the keyword val (if it's identifier return null)
        KEYWORD keyword = keyWord();
        if(keyword != null){
            return TOKEN_TYPE.KEYWORD;
        }
        // it is an identifier :-)
        if(!this.currentToken.equals(" ") && !this.currentToken.equals(".*(?:[ \r\n\t].*)+")) {
            this.currentToken = this.currentToken.replaceAll(" ", "");

            return TOKEN_TYPE.IDENTIFIER;
        }
        return null;
    }

    /**
     * handle int token
     */
    private TOKEN_TYPE intVal() throws IOException {
        this.currentToken = String.valueOf(this.pointer);
        char nextChar = (char) this.bufferedReader.read();
        // while we have more integers, concat to one integer
        while (String.valueOf(nextChar).matches("-?\\d+(\\.\\d+)?")){
            this.currentToken = this.currentToken + nextChar;
            nextChar = (char) this.bufferedReader.read();
        }
        this.pointer = (nextChar == ' ') ? (char) bufferedReader.read() :nextChar;
        this.intFlag = true;
        return TOKEN_TYPE.INT_CONST;
    }

    /**
     * handle string token
     */
    private TOKEN_TYPE stringVal() throws IOException {
        // while we have more string's chars, concat to one string
        char nextChar = (char) this.bufferedReader.read();
        while (!String.valueOf(nextChar).equals("\"")){
            this.currentToken = this.currentToken+nextChar;
            nextChar = (char) this.bufferedReader.read();
        }
        return TOKEN_TYPE.STRING_CONST;
    }

}
