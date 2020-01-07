import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Jack Jack Analyzer that translate jack programs to xml file
 */
public class JackAnalyzer {
    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        // handle directory and translate each file in it
        if (inputFile.isDirectory()){
            for (File file : inputFile.listFiles()){
                if (file.getAbsolutePath().endsWith(".jack")){
                    String outputPath = file.getAbsolutePath().replaceAll(".jack", ".xml");
                    File outputFile = new File(outputPath);
                    CompilationEngine compilationEngine = new CompilationEngine(file, outputFile);
                    compilationEngine.CompileClass();
                }
            }
        } else {
            // translate single file
            String outputPath = inputFile.getAbsolutePath().replaceAll(".jack", ".xml");
            File outputFile = new File(outputPath);
            CompilationEngine compilationEngine = new CompilationEngine(inputFile, outputFile);
            compilationEngine.CompileClass();

        }
    }
}
