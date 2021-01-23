package net.romvoid.crashbot.core.shard;

import net.dv8tion.jda.api.utils.SessionController;
import net.dv8tion.jda.api.utils.SessionControllerAdapter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


public class SessionControllerWrapper extends SessionControllerAdapter {
    private final SessionController[] shardControllers;

    public SessionControllerWrapper(@Nonnegative int bucketFactor, long homeGuildId) {
        if (bucketFactor < 1) {
            throw new IllegalArgumentException("Bucket factor must be at least 1");
        }
        this.shardControllers = new SessionController[bucketFactor];
        for (int i = 0; i < bucketFactor; i++) {
            this.shardControllers[i] = new PrioritizingSessionController(homeGuildId);
        }
    }

    public SessionControllerWrapper(long homeGuildId) {
        this(16, homeGuildId);
    }

    @Override
    public void appendSession(@Nonnull SessionConnectNode node) {
        controllerFor(node).appendSession(node);
    }

    @Override
    public void removeSession(@Nonnull SessionConnectNode node) {
        controllerFor(node).removeSession(node);
    }

    @Nonnull
    @CheckReturnValue
    private SessionController controllerFor(@Nonnull SessionConnectNode node) {
        return shardControllers[node.getShardInfo().getShardId() % shardControllers.length];
    }
}
