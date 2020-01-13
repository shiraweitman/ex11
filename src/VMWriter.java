import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter bufferedWriter;
    int labelCounter;


    VMWriter(File outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        this.bufferedWriter = new BufferedWriter(fileWriter);
    }

    public void writeCommand(Command command) throws IOException {
        this.bufferedWriter.write(getStrCmd(command));
        this.bufferedWriter.newLine();
    }

    public void writePush(Segment segment, int index) throws IOException {
        String strSegment = getStringVal(segment);
        this.bufferedWriter.write("push "+strSegment+" "+index);
        this.bufferedWriter.newLine();
    }

    public void writePop(Segment segment, int index) throws IOException {
        String strSegment = getStringVal(segment);
        this.bufferedWriter.write("pop "+strSegment+" "+index);
        this.bufferedWriter.newLine();
    }

    public void WriteLabel(String label) throws IOException {
        this.bufferedWriter.write("label "+ label + this.labelCounter);
        this.bufferedWriter.newLine();
    }

    public void writeGoto(String label) throws IOException {
        this.bufferedWriter.write("goto "+label+this.labelCounter);
        this.bufferedWriter.newLine();
    }

    public void writeIf(String label) throws IOException {
        this.bufferedWriter.write("if-goto "+label+labelCounter);
        this.bufferedWriter.newLine();
    }

    public void writeCall(String name, int argsNum) throws IOException {
        this.bufferedWriter.write("call "+name+ " "+argsNum);
        this.bufferedWriter.newLine();
    }

    public void writeFunction(String name, int localsNum) throws IOException {
        this.bufferedWriter.write("function "+name+ " "+localsNum);
        this.bufferedWriter.newLine();
    }

    public void writeReturn() throws IOException {
        this.bufferedWriter.write("return");
        this.bufferedWriter.newLine();
    }

    public void close() throws IOException {this.bufferedWriter.close();}

    private String getStringVal(Segment segment){
        switch (segment){
            case POINTER:
                return "pointer";
            case ARG:
                return "argument";
            case TEMP:
                return "temp";
            case THAT:
                return "that";
            case CONST:
                return "constant";
            case THIS:
                return "this";
            case LOCAL:
                return "local";
            default:
                return "static";
        }
    }

    private String getStrCmd(Command command) {
        switch (command){
            case ADD:
                return "add";
            case EQ:
                return "eq";
            case LT:
                return "lt";
            case GT:
                return "gt";
            case OR:
                return "or";
            case AND:
                return "and";
            case NEG:
                return "neg";
            case NOT:
                return "not";
            default:
                return "sub";
        }
    }

}
