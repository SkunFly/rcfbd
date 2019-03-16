import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Filter {

    private static final int REFRESH_RATE = 10000; // milliseconds

    public static void main(String[] args) {
        List<JsonToken> ignoreTks = Arrays.asList(JsonToken.START_OBJECT, JsonToken.END_OBJECT,
                JsonToken.START_ARRAY, JsonToken.END_ARRAY);
        if(args.length < 4){
            System.out.println("Usage: [input file] [output file] [initial timestamp] [final timestamp]");
            System.exit(-1);
        }

        final long[] t = {System.currentTimeMillis()};
        String input = args[0], output =  args[1];
        int initialts = Integer.parseInt(args[2]), finalts = Integer.parseInt(args[3]);
        JsonFactory jfactory = new JsonFactory();
        try (Stream<String> stream = Files.lines(Paths.get(input))) {
            PrintWriter pw = new PrintWriter(new FileOutputStream(output));
            stream.forEach(comment -> {
                try {
                    if(System.currentTimeMillis() - t[0] >= REFRESH_RATE){
                        t[0] = System.currentTimeMillis();
                        System.out.println(comment);
                    }
                    JsonParser jparser = jfactory.createParser(comment);
                    while(true){
                        jparser.nextToken();
                        if(!jparser.hasCurrentToken()) break;
                        if(ignoreTks.contains(jparser.currentToken()))
                            continue;
                        if(jparser.getCurrentName().equals("created_utc")){
                            if(jparser.getValueAsInt() >= initialts && jparser.getValueAsInt() <= finalts)
                                pw.println(comment);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
