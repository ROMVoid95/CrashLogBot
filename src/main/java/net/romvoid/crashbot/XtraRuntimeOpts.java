package net.romvoid.crashbot;

import java.util.OptionalInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XtraRuntimeOpts {
    public static final boolean DEBUG = getValue("crashbot.debug") != null;
    public static final boolean DEBUG_LOGS = getValue("crashbot.debug_logs") != null;
    public static final boolean LOG_DB_ACCESS = getValue("crashbot.log_db_access") != null;
    public static final boolean TRACE_LOGS = getValue("crashbot.trace_logs") != null;
    public static final boolean VERBOSE = getValue("crashbot.verbose") != null;
    public static final boolean PRINT_VARIABLES = getValue("crashbot.print_variables") != null;
    public static final boolean VERBOSE_SHARD_LOGS = getValue("crashbot.verbose_shard_logs") != null;

    public static final OptionalInt FROM_SHARD = maybeInt("crashbot.from-shard");
    public static final OptionalInt TO_SHARD = maybeInt("crashbot.to-shard");
    public static final OptionalInt SHARD_COUNT = maybeInt("crashbot.shard-count");
    public static final boolean SHARD_SUBSET = FROM_SHARD.isPresent() && TO_SHARD.isPresent() && SHARD_COUNT.isPresent();
    public static final boolean SHARD_SUBSET_MISSING = !SHARD_SUBSET && (
            FROM_SHARD.isPresent() || TO_SHARD.isPresent()
    );
    
    public static final OptionalInt NODE_NUMBER = maybeInt("crashbot.node-number");
    
    private static OptionalInt maybeInt(String name) {
        var value = getValue(name);
        if (value == null) return OptionalInt.empty();
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
    
    @Nullable
    private static String getValue(@Nonnull String name) {
        return System.getProperty(name, System.getenv(name.replace("-", "_").replace(".", "_").toUpperCase()));
    }
}
