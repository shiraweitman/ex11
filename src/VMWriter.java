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

    public void writePush(Segment segment, int index){}

    public void writePop(Segment segment, int index){}

    public void WriteLabel(String label){}

    public void WriteGoto(String gotoToWrite){}

    public void WriteIf(String ifToWrite){}

    public void writeCall(String name, int argsNum){}

    public void writeFunction(String name, int localsNum){}

    public void writeReturn(){}

    public void close() throws IOException {this.bufferedWriter.close();}


}
