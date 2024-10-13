package core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Random;
import java.util.zip.GZIPInputStream;

public class TextFile {
    public Path path;
    public String text;

    public TextFile(String text) {
        this.path = null;
        this.text = text;        
    }
    
    public TextFile(Path path) throws IOException {
        this.path = path;
        this.text = TextFile.readPossiblyCompressed(path);
    }
    
    public static TextFile random(int size, String alphabet) {
        Random random = new Random();
        char[] text = new char[size];
        for (int i = 0; i < size; i++)
            text[i] = alphabet.charAt(random.nextInt(alphabet.length()));

        return new TextFile(new String(text));
    }

    public int size() {
        return this.text.length();
    }

    public char getChar(int position) {
        if (!(position < text.length()))
            return '\0';
        
        return text.charAt(position);
    };

    public int compareSuffixes(int positionA, int positionB) {
        if (positionA == positionB)
            return 0;

        int end = text.length();
        while (true) {
            if (positionA == end)
                return -1;
            if (positionB == end)
                return 1;

            char char1 = text.charAt(positionA);
            char char2 = text.charAt(positionB);
            if (char1 != char2)
                return char1 < char2 ? -1 : 1;

            positionA++;
            positionB++;
        }
    }
    
    // Helper functions.
    // You can stop reading here.
    
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final int GZIP_BUFFERSIZE = 16 * 1024;
    
    public static String readPossiblyCompressed(Path path) throws IOException {
        if (path.getFileName().toString().endsWith(".gz"))
            return readCompressed(path);
        
        return readUncompressed(path);
    }
    
    public static String readCompressed(Path path) throws IOException {
        try (
            InputStream compressed = Files.newInputStream(path);
            InputStream uncompressed = new GZIPInputStream(compressed);
            Reader reader = new InputStreamReader(uncompressed, ENCODING);
            Writer writer = new StringWriter();
        ) {
            char[] buffer = new char[GZIP_BUFFERSIZE];
            while (true) {
                int length = reader.read(buffer);
                if (length == -1)
                    break;
                
                writer.write(buffer, 0, length);
            }
            return writer.toString();
        }
    }

    public static String readUncompressed(Path path) throws IOException {
        return Files.readString(path, ENCODING);
    }    
}
