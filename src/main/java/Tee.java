import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by ksenya on 18.02.2018.
 */
public class Tee {
    private static boolean appendFlag;
    private static boolean ignoreFlag;
    private static List<String> files = new ArrayList<String>();
    private static List<PrintStream> streams = new ArrayList<PrintStream>();

    public static void main(String[] args) {
        for (String str : args) {
            if (str.charAt(0) == '-') {
                if (!parseArgs(str)) {
                    return;
                }
            } else {
                files.add(str);
            }
        }

        if (ignoreFlag) {
            Signal.handle(new Signal("INT"), new SignalHandler() {
                public void handle(Signal sig) {
                }
            });
        }

        for (String file : files) {
            Path path = Paths.get(file);

            if (!appendFlag) {
                try {
                    if (Files.exists(Paths.get(file), LinkOption.NOFOLLOW_LINKS)) {
                        Files.delete(Paths.get(file));
                    }
                    createFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                createFile(path);
            }

        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            System.out.println(str);
            for(PrintStream stream : streams) {
                stream.println(str);
            }
        }
        for(PrintStream stream : streams) {
            stream.close();
        }
    }

    private static void createFile(Path path) {
        try {
            OutputStream stream = Files.newOutputStream(path, CREATE, APPEND);
            streams.add(new PrintStream(stream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printArgumentError(char c) {
        System.out.printf("tee: illegal option -- %s\n", c);
        System.out.println("usage: tee [-ai] [file ...]");
    }

    private static boolean parseArgs(String str) {
        int pointer = 1;
        while (pointer < str.length()) {
            switch (str.charAt(pointer)) {
                case 'a':
                    appendFlag = true;
                    break;
                case 'i':
                    ignoreFlag = true;
                    break;
                default:
                    printArgumentError(str.charAt(pointer));
                    return false;
            }
            pointer++;
        }
        return true;
    }
}