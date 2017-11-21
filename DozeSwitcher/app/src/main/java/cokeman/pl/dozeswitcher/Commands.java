package cokeman.pl.dozeswitcher;


class Commands {
    final static String COMMAND_DUMPSYS_ENABLE = "dumpsys deviceidle enable\n";
    final static String COMMAND_DUMPSYS_DISABLE = "dumpsys deviceidle enable\n";

    final static String COMMAND_DUMPSYS_GREP_M = "dumpsys deviceidle | grep mEnabled\n";
    final static String COMMAND_DUMPSYS_GREP_ABOVE_M = "dumpsys deviceidle | grep mDeepEnabled\n";

    final static String COMMAND_SU = "su";
}
