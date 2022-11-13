package xyz.zcraft.ACGPicDownload;

import xyz.zcraft.ACGPicDownload.Util.Config;
import xyz.zcraft.ACGPicDownload.Util.DownloadUtil;
import xyz.zcraft.ACGPicDownload.Util.SourceUtil.Source;
import xyz.zcraft.ACGPicDownload.Util.SourceUtil.SourceManager;
import xyz.zcraft.ACGPicDownload.Util.Result;
import xyz.zcraft.ACGPicDownload.Util.SourceUtil.SourceFetcher;
import com.alibaba.fastjson.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Config cfg = new Config();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s", "--source" -> {
                    if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                        cfg.setSourceName(args[i + 1]);
                    } else {
                        System.err.println("Please provide a source name.");
                    }
                }
                case "-o", "--output" -> {
                    if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                        cfg.setOutDir(args[i + 1]);
                    } else {
                        System.err.println("Please provide a output path.");
                    }
                }
                case "--arg" -> {
                    if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
                        String[] t = args[i + 1].split(",");
                        for (String s : t) {
                            String key = s.substring(0, s.indexOf("="));
                            String value = s.substring(s.indexOf("=") + 1);
                            cfg.getArg().put(key, value);
                        }
                    } else {
                        System.err.println("Please provide arguments.");
                    }
                }
                case "--list-sources" -> listSources();
                case "-h","--help" -> usage();
            }
        }

        if (cfg.getSourceName() == null) {
            List<Source> sources = getSourcesConfig();
           if(sources == null || sources.size() == 0) {
               System.err.println("No available sources");
               return;
           }else{
               cfg.setSourceName(sources.get(0).getName());
           }
        }
        if (cfg.getOutDir() == null) {
            cfg.setOutDir("");
        }
        execute(cfg);
    }

    private static List<Source> getSourcesConfig(){
        try {
            if(SourceManager.getSources() == null){
                SourceManager.readConfig();
            }
            return SourceManager.getSources();
        } catch (IOException e) {
            System.err.println("ERROR:Could not read source config. Please check your source config file. Error detail:" + e);
        } catch (JSONException e) {
            System.err.println("ERROR:Could not parse source config as JSON file. Please check if your sources.json is correctly configured. Error detail:" + e);
        }

        return null;
    }

    private static void listSources(){
        List<Source> sources = getSourcesConfig();
        if(sources == null){
            System.out.println("No sources found");
        }else{
            System.out.println("Name | Description | URL");
            sources.forEach(t -> System.out.println(t.getName() + " | " + t.getDescription() + " | " + t.getUrl()));
        }
    }

    private static void execute(Config cfg) {
        try {
            List<Source> sources = getSourcesConfig();
            if(sources == null){
                System.err.println("can't find source to use");
                return;
            }
            Source s = SourceManager.getSourceByName(sources, cfg.getSourceName());

            if (s != null) {
                s.getDefaultArgs().forEach((t, o) -> {
                    String value;

                    if (cfg.getArg().containsKey(t)) {
                        value = cfg.getArg().get(t);
                    } else {
                        value = String.valueOf(s.getDefaultArgs().get(t));
                    }

                    s.setUrl(s.getUrl().replaceAll("\\$\\{" + t + "}", value));
                });
                Result[] r = SourceFetcher.fetch(s);
                File outDir = new File(cfg.getOutDir());
                for (Result result : r) {
                    DownloadUtil.download(result, outDir);
                }
            } else {
                System.err.println("Could not find source named " + cfg.getSourceName() + ". Please check your sources.json file. To get all sources, use \"--list-sources\"");
            }
        } catch (IOException e) {
            System.err.println("ERROR:Could not read source config. Please check your source config file. Error detail:" + e);
        } catch (JSONException e) {
            System.err.println("ERROR:Could not parse source config as JSON file. Please check if your sources.json is correctly configured. Error detail:" + e);
        }
    }

    private static void usage() {
        System.out.println(
                """
                        Available arguments:\s
                           --list-sources : List all the sources
                         Fetching:
                           -s, --source <source name> : Set the source to use. Required.
                           -o, --output <output dictionary> : Set the output dictionary. Required.
                           --arg key1=value1,key2=value2,... : custom the argument in the url.
                                   Example:If the url is "https://www.someurl.com/pic?num=${num}", then with
                                            "--arg num=1", the exact url will be "https://www.someurl.com/pic?num=1\""""
        );
    }
}