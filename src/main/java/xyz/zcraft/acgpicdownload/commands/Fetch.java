package xyz.zcraft.acgpicdownload.commands;

import com.alibaba.fastjson2.JSONException;
import xyz.zcraft.acgpicdownload.Main;
import xyz.zcraft.acgpicdownload.exceptions.SourceNotFoundException;
import xyz.zcraft.acgpicdownload.util.Logger;
import xyz.zcraft.acgpicdownload.util.ResourceBundleUtil;
import xyz.zcraft.acgpicdownload.util.fetchutil.FetchUtil;
import xyz.zcraft.acgpicdownload.util.fetchutil.Result;
import xyz.zcraft.acgpicdownload.util.sourceutil.Source;
import xyz.zcraft.acgpicdownload.util.sourceutil.argument.Argument;
import xyz.zcraft.acgpicdownload.util.sourceutil.argument.IntegerArgument;
import xyz.zcraft.acgpicdownload.util.sourceutil.argument.StringArgument;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Fetch {
    public static final String USAGE = """
                    Available arguments:
                       --list-sources : List all the sources.
                       -s, --source <source name> : Set the source to use.
                       -o, --output <output dictionary> : Set the output dictionary.
                       --arg key1=value1,key2=value2,... : custom the argument in the url.
                       --multi-thread : (Experimental) Enable multi thread download. May improve download speed.
                       -t, --t <times> : Set the number of times to fetch.
                       -f, --full : Download the json data together with the image.

                    See the documentation to get more information about usage
            """;
    private final LinkedList<Argument<?>> arguments = new LinkedList<>();
    private final HashMap<String, String> argumentsTmp = new HashMap<>();
    public boolean enableConsoleProgressBar = false;
    private String sourceName;
    private String outputDir = new File("").getAbsolutePath();
    private Logger logger;
    private int maxThread = 1;
    private String proxyHost;
    private int proxyPort;
    private int times = 1;
    private boolean saveFullResult = false;

    public static void usage(Logger logger) {
        logger.info(USAGE);
    }

    private boolean parseArguments(ArrayList<String> args) {
        for (int i = 0; i < args.size(); i++) {
            switch (args.get(i)) {
                case "-s", "--source" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        sourceName = args.get(i + 1);
                        i += 1;
                    } else {
                        logger.err(ResourceBundleUtil.getString("cli.fetch.err.sourceNameInvalid"));
                        return false;
                    }
                }
                case "-o", "--output" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        outputDir = args.get(i + 1);
                        i += 1;
                    } else {
                        logger.err(ResourceBundleUtil.getString("cli.fetch.err.pathInvalid"));
                        return false;
                    }
                }
                case "--arg", "-a", "--args" -> {
                    if (args.size() > i + 1 && !args.get(i + 1).startsWith("-")) {
                        String[] t = args.get(i + 1).split(",");
                        for (String s : t) {
                            String key = s.substring(0, s.indexOf("="));
                            String value = s.substring(s.indexOf("=") + 1);
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            argumentsTmp.put(key, value);
                        }
                        i += 1;
                    } else {
                        logger.err(ResourceBundleUtil.getString("cli.fetch.err.noArg"));
                        return false;
                    }
                }
                case "-f", "--full" -> saveFullResult = true;
                case "--debug" -> Main.debugOn();
                case "--list-sources" -> {
                    try {
                        FetchUtil.listSources(logger);
                    } catch (IOException e) {
                        logger.err(ResourceBundleUtil.getString("cli.fetch.err.cannotReadSource"));
                        Main.logError(e);
                    }
                    return false;
                }
                case "-h", "--help" -> {
                    usage(logger);
                    return false;
                }
                case "-t", "--times" -> {
                    if (args.size() > (i + 1)) {
                        try {
                            times = Integer.parseInt(args.get(i + 1));
                            i++;
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    logger.err(ResourceBundleUtil.getString("cli.fetch.err.numberInvalid"));
                    return false;
                }
                case "-p", "--proxy" -> {
                    if (args.size() > (i + 1)) {
                        String[] str = args.get(i + 1).split(":");
                        if (str.length == 2) {
                            proxyHost = str[0];
                            try {
                                proxyPort = Integer.parseInt(str[1]);
                                i++;
                                break;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    logger.err(ResourceBundleUtil.getString("cli.fetch.err.proxyInvalid"));
                    return false;
                }
                case "-m", "--max-thread" -> {
                    if (args.size() > (i + 1)) {
                        try {
                            maxThread = Integer.parseInt(args.get(i + 1));
                            i++;
                        } catch (NumberFormatException ignored) {
                            maxThread = -1;
                        }
                    } else {
                        maxThread = -1;
                    }
                }
                default -> {
                    logger.err(ResourceBundleUtil.getString("cli.fetch.err.cannotParseArgFull"));
                    return false;
                }
            }
        }

        if (sourceName == null || sourceName.trim().equals("")) {
            List<Source> sources;
            try {
                sources = FetchUtil.getSourcesConfig();
            } catch (IOException e) {
                logger.err(ResourceBundleUtil.getString("cli.fetch.err.cannotReadSource"));
                Main.logError(e);
                return false;
            }
            if (sources == null || sources.size() == 0) {
                logger.err(ResourceBundleUtil.getString("cli.fetch.err.noSourceFound"));
                return false;
            } else {
                sourceName = sources.get(0).getName();
            }
        }
        if (outputDir == null || outputDir.trim().equals("")) {
            outputDir = "";
        }

        return true;
    }

    private Source parseSource() {
        Source s;
        try {
            s = FetchUtil.getSourceByName(sourceName);
        } catch (IOException e) {
            logger.err(ResourceBundleUtil.getString("cli.fetch.err.cannotReadSource"));
            return null;
        } catch (JSONException e) {
            logger.err(ResourceBundleUtil.getString("cli.fetch.err.cannotParseSource"));
            return null;
        } catch (SourceNotFoundException e) {
            logger.err(String.format(Objects.requireNonNull(ResourceBundleUtil.getString("cli.fetch.err.sourceNotFound")), sourceName));
            return null;
        }
        if (s == null) {
            logger.err(String.format(Objects.requireNonNull(ResourceBundleUtil.getString("cli.fetch.err.sourceNotFoundFull")), sourceName));
            return null;
        }
        return s;
    }

    public void main(ArrayList<String> args, Logger logger) {
        this.logger = logger;

        if (!parseArguments(args)) {
            return;
        }

        if (proxyHost != null && proxyPort != 0) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", proxyHost);
            System.getProperties().put("proxyPort", String.valueOf(proxyPort));
        }

        Source s = parseSource();

        if (s == null) {
            return;
        }

        try {
            for (Argument<?> arg : s.getArguments()) {
                String str = argumentsTmp.get(arg.getName());
                if (str != null) {
                    if (arg instanceof StringArgument) {
                        ((StringArgument) arg).set(str);
                    } else if (arg instanceof IntegerArgument) {
                        ((IntegerArgument) arg).set(Integer.parseInt(argumentsTmp.get(arg.getName())));
                    }
                    arguments.add(arg);
                }
            }
        } catch (Exception e) {
            Main.logError(e);
            logger.err(ResourceBundleUtil.getString("fetch.err.cannotParseArg") + " : " + e);
            return;
        }

        FetchUtil.replaceArgument(s, arguments);

        ArrayList<Result> r = FetchUtil.fetch(
                s,
                times,
                logger,
                enableConsoleProgressBar,
                proxyHost,
                proxyPort
        );
        if (r.size() == 0) {
            logger.info(ResourceBundleUtil.getString("cli.fetch.info.noPicGot"));
            return;
        } else {
            logger.info(String.format(Objects.requireNonNull(ResourceBundleUtil.getString("cli.fetch.info.gotPic")), r.size()));
        }

        FetchUtil.startDownload(r, outputDir, logger, saveFullResult, enableConsoleProgressBar, maxThread);
    }
}
