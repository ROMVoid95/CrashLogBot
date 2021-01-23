package net.romvoid.crashbot.data;

import static com.rethinkdb.RethinkDB.r;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rethinkdb.net.Connection;

import net.romvoid.crashbot.config.MainConfig;
import net.romvoid.crashbot.db.ManagedDatabase;
import net.romvoid.crashbot.utilities.data.JsonDataManager;
import redis.clients.jedis.JedisPool;

public class CrashBotData {
	
	private static final Logger log = LoggerFactory.getLogger(CrashBotData.class);
	
    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(
            1, new ThreadFactoryBuilder().setNameFormat("CrashBot-Executor Thread-%d").build()
    );
	
    private static JsonDataManager<MainConfig> config;
    private static Connection connection;
    private static ManagedDatabase db;
    private static final JedisPool defaultJedisPool = new JedisPool(config().get().jedisPoolAddress, config().get().jedisPoolPort);
    
    public static JsonDataManager<MainConfig> config() {
        if (config == null) {
            config = new JsonDataManager<>(MainConfig.class, "config.json", MainConfig::new);
        }

        return config;
    }
    
    public static Connection conn() {
        var config = config().get();
        if (connection == null) {
            synchronized (CrashBotData.class) {
                if (connection != null) {
                    return connection;
                }

                connection = r.connection()
                        .hostname(config.getDbHost())
                        .port(config.getDbPort())
                        .db(config.getDbDb())
                        .user(config.getDbUser(), config.getDbPassword())
                        .connect();

                log.info("Established first database connection to {}:{} ({})",
                        config.getDbHost(), config.getDbPort(), config.getDbUser()
                );
            }
        }

        return connection;
    }
    
    public static ManagedDatabase db() {
        if (db == null) {
            db = new ManagedDatabase(conn());
        }

        return db;
    }
    
    public static ScheduledExecutorService getExecutor() {
        return exec;
    }

    public static void queue(Callable<?> action) {
        getExecutor().submit(action);
    }

    public static void queue(Runnable runnable) {
        getExecutor().submit(runnable);
    }

    public static JedisPool getDefaultJedisPool() {
        return CrashBotData.defaultJedisPool;
    }
}
