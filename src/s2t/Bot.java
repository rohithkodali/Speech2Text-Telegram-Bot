package s2t;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Bot {
    final private String TOKEN = PARAM.botToken;
    private String url = "https://api.telegram.org/bot"+TOKEN+"/";
    static JSONParser parser = new JSONParser();
    int lastOffset = 857551151;

    public void start() throws IOException {
        ExecutorService ex = Executors.newFixedThreadPool(6);
        File dir = new File("audio");
        if(!dir.exists()){
            try{dir.mkdir();}
            catch (SecurityException e){
                System.err.println("Non hai i permessi per creare la cartella audio");
                return;
            }
        }

        while(true){
            int offset = lastOffset+1;
            URL update = new URL(url+"getUpdates?offset="+offset);
            JSONObject response = callJSON(update);
            System.out.println(response);
            //System.out.println("Update at "+System.currentTimeMillis()/1000L);
            JSONArray results = (JSONArray) response.get("result");
            lastOffset = getLastID(response);

            for(Object res : results){
                ex.submit(new TranscriptAudio(res, url));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private int getLastID(JSONObject updateResult) {

        JSONParser parser = new JSONParser();
        Vector<Integer> returnarray = new Vector<>();

        JSONObject response = updateResult;

        JSONArray results = (JSONArray) response.get("result");
        if (results.isEmpty())
            return this.lastOffset;
        return Integer.parseInt( (((JSONObject)results.get(results.size()-1)).get("update_id")).toString());
    }


    static String callString(URL url) throws IOException{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String res = "";
        BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
        String nline;
        while(  (nline = bf.readLine()) != null ){
            res += nline;
        }
        return res;
    }

    static JSONObject callJSON(URL url) throws IOException{
        try {
            return (JSONObject) parser.parse(callString(url));
        } catch (ParseException e) {
            return null;
        }
    }

    public static void main(String[] args){
        try {
            new Bot().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
