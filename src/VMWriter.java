import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter bufferedWriter;

    VMWriter(File outputFile) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFile);
        this.bufferedWriter = new BufferedWriter(fileWriter);
    }

    public void writeCommand(Command command) throws IOException {
        this.bufferedWriter.write(String.valueOf(command));
        this.bufferedWriter.newLine();
    }

    public void writePush(Segment segment, int index) throws IOException {
        this.bufferedWriter.write("push "+segment+" "+index);
        this.bufferedWriter.newLine();
    }

    public void writePop(Segment segment, int index) throws IOException {
        this.bufferedWriter.write("pop "+segment+" "+index);
        this.bufferedWriter.newLine();
    }

    public void WriteLabel(String label) throws IOException {
        this.bufferedWriter.write("label "+label);
        this.bufferedWriter.newLine();
    }

    public void WriteGoto(String label) throws IOException {
        this.bufferedWriter.write("goto "+label);
        this.bufferedWriter.newLine();
    }

    public void WriteIf(String label) throws IOException {
        this.bufferedWriter.write("if-goto "+label);
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

}
