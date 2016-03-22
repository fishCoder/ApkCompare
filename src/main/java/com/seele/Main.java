package com.seele;

import java.io.File;

import org.apache.commons.cli.*;

public class Main {
    private static final String VERSION = "1.0.0";
    private static final Options allOptions = new Options();
    private static final Options patchOptions = new Options();
    private static final Options mergeOptions = new Options();

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = null;
        option();
        try {
            commandLine = parser.parse(allOptions, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            usage(commandLine);
            return;
        }

        if ((!commandLine.hasOption('k')) && (!commandLine.hasOption("keystore"))) {
            usage(commandLine);
            return;
        }
        if ((!commandLine.hasOption('p')) && (!commandLine.hasOption("kpassword"))) {
            usage(commandLine);
            return;
        }
        if ((!commandLine.hasOption('a')) && (!commandLine.hasOption("alias"))) {
            usage(commandLine);
            return;
        }
        if ((!commandLine.hasOption('e')) && (!commandLine.hasOption("epassword"))) {
            usage(commandLine);
            return;
        }

        File out = null;
        if ((!commandLine.hasOption('o')) && (!commandLine.hasOption("out")))
            out = new File("");
        else {
            out = new File(commandLine.getOptionValue('o'));
        }
        String keystore = commandLine.getOptionValue('k');
        String password = commandLine.getOptionValue('p');
        String alias = commandLine.getOptionValue('a');
        String entry = commandLine.getOptionValue('e');
        String name = "main";
        if ((commandLine.hasOption('n')) || (commandLine.hasOption("name")))
        {
            name = commandLine.getOptionValue('n');
        }

        if ((commandLine.hasOption('m')) || (commandLine.hasOption("merge"))) {
            String[] merges = commandLine.getOptionValues('m');
            File[] files = new File[merges.length];
            for (int i = 0; i < merges.length; i++) {
                files[i] = new File(merges[i]);
            }
            MergePatch mergePatch = new MergePatch(files, name, out, keystore,
                    password, alias, entry);
            mergePatch.doMerge();
        } else {
            if ((!commandLine.hasOption('f')) && (!commandLine.hasOption("from"))) {
                usage(commandLine);
                return;
            }
            if ((!commandLine.hasOption('t')) && (!commandLine.hasOption("to"))) {
                usage(commandLine);
                return;
            }

            File from = new File(commandLine.getOptionValue("f"));
            File to = new File(commandLine.getOptionValue('t'));
            if ((!commandLine.hasOption('n')) && (!commandLine.hasOption("name"))) {
                name = from.getName().split("\\.")[0];
            }

            ApkPatch apkPatch = new ApkPatch(from, to, name, out, keystore,
                    password, alias, entry);
            apkPatch.doPatch();
        }

    }

    private static void option() {
        OptionBuilder.withLongOpt("from");
        OptionBuilder.withDescription("new Apk file path.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("loc");

        Option fromOption =
                OptionBuilder.create("f");
        patchOptions.addOption(fromOption);
        allOptions.addOption(fromOption);

        OptionBuilder.withLongOpt("to");
        OptionBuilder.withDescription("old Apk file path.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("loc");

        Option toOption =
                OptionBuilder.create("t");
        patchOptions.addOption(toOption);
        allOptions.addOption(toOption);

        OptionBuilder.withLongOpt("keystore");
        OptionBuilder.withDescription("keystore path.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("loc");

        Option keystoreOption =
                OptionBuilder.create("k");
        patchOptions.addOption(keystoreOption);
        mergeOptions.addOption(keystoreOption);
        allOptions.addOption(keystoreOption);

        OptionBuilder.withLongOpt("kpassword");
        OptionBuilder.withDescription("keystore password.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("***");

        Option kPasswordOption =
                OptionBuilder.create("p");
        patchOptions.addOption(kPasswordOption);
        mergeOptions.addOption(kPasswordOption);
        allOptions.addOption(kPasswordOption);

        OptionBuilder.withLongOpt("alias");
        OptionBuilder.withDescription("alias.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("alias");

        Option aliasOption =
                OptionBuilder.create("a");
        patchOptions.addOption(aliasOption);
        mergeOptions.addOption(aliasOption);
        allOptions.addOption(aliasOption);

        OptionBuilder.withLongOpt("epassword");
        OptionBuilder.withDescription("entry password.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("***");

        Option ePasswordOption =
                OptionBuilder.create("e");
        patchOptions.addOption(ePasswordOption);
        mergeOptions.addOption(ePasswordOption);
        allOptions.addOption(ePasswordOption);

        OptionBuilder.withLongOpt("out");
        OptionBuilder.withDescription("output dir.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("dir");

        Option outOption =
                OptionBuilder.create("o");
        patchOptions.addOption(outOption);
        mergeOptions.addOption(outOption);
        allOptions.addOption(outOption);

        OptionBuilder.withLongOpt("name");
        OptionBuilder.withDescription("patch name.");
        OptionBuilder.hasArg(true);
        OptionBuilder.withArgName("name");

        Option nameOption =
                OptionBuilder.create("n");
        patchOptions.addOption(nameOption);
        mergeOptions.addOption(nameOption);
        allOptions.addOption(nameOption);

        OptionBuilder.withLongOpt("merge");
        OptionBuilder.withDescription("path of .apatch files.");
//     OptionBuilder.hasArgs(-2); OptionBuilder.withArgName("loc...");

        Option mergeOption =
                OptionBuilder.create("m");
        mergeOptions.addOption(mergeOption);
        allOptions.addOption(mergeOption);
    }

    private static void usage(CommandLine commandLine) {
        option();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);

        formatter
                .printHelp(
                        "apkmerge -f <new> -t <old> -o <output> -k <keystore> -p <***> -a <alias> -e <***>",
                        patchOptions);

        System.out.println("");

//        formatter
//                .printHelp(
//                        "apkpatch -m <apatch_path...> -k <keystore> -p <***> -a <alias> -e <***>",
//                        mergeOptions);
    }
}

