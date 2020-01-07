import java.util.ArrayList;
import java.util.Hashtable;

public class SymbolTable {
    Hashtable<String, ArrayList<String>> classTable;
    Hashtable<String, ArrayList<String>> subroutineTable;
    private int staticCounter;
    private int fieldCounter;
    private int argsCounter;
    private int varCounter;
    String className;

    /**
     * Creates a new empty symbol table
     */
    SymbolTable(){
        this.classTable = new Hashtable<>();
        this.subroutineTable = new Hashtable<>();
    }

    /**
     * Starts a new subroutine scope
     */
    void startSubroutine(){
        this.varCounter = 0;
        this.argsCounter = 0;
        this.subroutineTable = new Hashtable<>();
        ArrayList<String> varDetails = new ArrayList<>();
        varDetails.add(this.className);
        varDetails.add("argument");
        varDetails.add("0");
        this.subroutineTable.put("this", varDetails);
    }

    /**
     * Defines a new identifier of a given name,
     * type, and kind and assigns it a running
     * index. STATIC and FIELD identifiers
     * have a class scope, while ARG and VAR
     * identifiers have a subroutine scope
     * @param name given var name
     * @param type given var type
     * @param kind given var kind
     */
    void define(String name, String type, String kind){
        if(!isInTable(name)) {
            ArrayList<String> varDetails = new ArrayList<>();
            varDetails.add(type);
            varDetails.add(kind);
            switch (kind) {
                case "static":
                    varDetails.add(String.valueOf(this.staticCounter));
                    this.staticCounter++;
                    this.classTable.put(name, varDetails);
                    break;
                case "field":
                    varDetails.add(String.valueOf(this.fieldCounter));
                    this.fieldCounter++;
                    this.classTable.put(name, varDetails);
                case "argument":
                    varDetails.add(String.valueOf(this.argsCounter));
                    this.argsCounter++;
                    this.subroutineTable.put(name, varDetails);
                case "var":
                    varDetails.add(String.valueOf(this.varCounter));
                    this.varCounter++;
                    this.subroutineTable.put(name, varDetails);
            }
        }
    }



    /**
     * check if the given var is already in the class or subroutine table
     * @param name the giver var name
     * @return true if in table, false otherwise
     */
    private boolean isInTable(String name){
        if(!this.subroutineTable.contains(name)){
            // return true if the class table contains the var, false otherwise
            return this.classTable.contains(name);
        }
        return true; // if the subroutine table contains the var
    }

    /**
     * Returns the number of variables of the
     * given kind already defined in the current
     * scope
     */
    int varCount(String kind){
        switch (kind){
            case "static":
                return this.staticCounter;
            case "field":
                return this.fieldCounter;
            case "argument":
                return this.argsCounter;
            case "var":
                return this.varCounter;
        }
        // illegal kind
        return -1;
    }

    /**
     * Returns the kind of the named identifier in
     * the current scope. Returns NONE if the
     * identifier is unknown in the current scope.
     */
    String kindOf(String name){
        if(this.subroutineTable.contains(name)){
            return this.subroutineTable.get(name).get(1); // return the var kind
        }
        if(this.classTable.contains(name)) {
            return this.classTable.get(name).get(1);
        }
        return null; // if the name isn't in the tables
    }

    /**
     * Returns the type of the named identifier in
     * the current scope.

     */
    String typeOf(String name){
        if(this.subroutineTable.contains(name)){
            return this.subroutineTable.get(name).get(0); // return the var kind
        }
        if(this.classTable.contains(name)) {
            return this.classTable.get(name).get(0);
        }
        return null; // if the name isn't in the tables
    }

    /**
     * Returns the index assigned to named
     * identifier.
     */
    int indexOf(String name){
        if(this.subroutineTable.contains(name)){
            return Integer.parseInt(this.subroutineTable.get(name).get(2)); // return the var kind
        }
        else{
            return Integer.parseInt(this.classTable.get(name).get(2));
        }
    }
}
