import java.util.ArrayList;
import java.util.Hashtable;

public class SymbolTable {
    Hashtable<String, ArrayList<String>> classTable;
    Hashtable<String, ArrayList<String>> subroutineTable;
    private int staticCounter;
    private int fieldCounter;
    private int argsCounter;
    private int varCounter;

    SymbolTable(){
        this.classTable = new Hashtable<>();
    }

    void startSubroutine(){
        this.subroutineTable = new Hashtable<>();
    }

    void define(String name, String type, String kind){
        ArrayList<String> varDetails = new ArrayList<>();
        varDetails.add(type);
        varDetails.add(kind);
        switch (kind){
            case "static":
                varDetails.add(String.valueOf(this.staticCounter));
                this.staticCounter++;
                this.classTable.put(name, varDetails);
                break;
            case "field":

        }

    }

    boolean isInTable(){return  false;}

    int varCount(String kind){return 0;}

    String kindOf(String name){return  null;}

    String typeOf(String name){return null;}

    int indexOf(String name){return 0;}
}
