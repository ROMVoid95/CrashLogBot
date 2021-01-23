package net.romvoid.crashbot.utilities.commands;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class RateLimiter {
    private static final ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    private final long max;
    private final long timeout;
    private final ConcurrentHashMap<String, Pair<AtomicInteger, Long>> usersRateLimited = new ConcurrentHashMap<>();

    /**
     * Default constructor normally used in Currency commands to ratelimit all people.
     *
     * @param timeout How much time until the ratelimit gets lifted
     */
    public RateLimiter(TimeUnit timeUnit, int timeout) {
        this.max = 1;
        this.timeout = timeUnit.toMillis(timeout);
    }

    /**
     * @param timeUnit The timeunit you'll input the RL time in. For example, TimeUnit#SECONDS.
     * @param max      How many times before you get ratelimited.
     * @param timeout  How much time until the ratelimit gets lifted.
     */
    public RateLimiter(TimeUnit timeUnit, int max, int timeout) {
        this.max = max;
        this.timeout = timeUnit.toMillis(timeout);
    }

    //Basically where you get b1nzy'd.
    public boolean process(String key) {
        Pair<AtomicInteger, Long> p = usersRateLimited.get(key);

        // Put the user on the RL map if they aren't here already, but we already let them pass.
        if (p == null) {
            usersRateLimited.put(key, p = new Pair<>());
            p.first = new AtomicInteger();
        }

        AtomicInteger a = p.first;
        long i = a.get();
        if (i >= max) return false;

        a.incrementAndGet();
        long now = System.currentTimeMillis();

        Long tryAgain = p.second;
        if (tryAgain == null || tryAgain < now) {
            p.second = now + timeout;
        }

        ses.schedule(a::decrementAndGet, timeout, TimeUnit.MILLISECONDS);
        return true;
    }

    //Method overload.
    public long tryAgainIn(String key) {
        Pair<AtomicInteger, Long> p = usersRateLimited.get(key);
        if (p == null || p.second == null)
            return 0;

        return Math.max(p.second - System.currentTimeMillis(), 0);
    }

    public long tryAgainIn(Member key) {
        return tryAgainIn(key.getUser().getId());
    }

    public long tryAgainIn(User key) {
        return tryAgainIn(key.getId());
    }

    public boolean process(User user) {
        return process(user.getId());
    }

    public boolean process(Member member) {
        return process(member.getUser());
    }

    public ConcurrentHashMap<String, Pair<AtomicInteger, Long>> getUsersRateLimited() {
        return this.usersRateLimited;
    }

    private static class Pair<F, S> {
        F first;
        S second;
    }
}
