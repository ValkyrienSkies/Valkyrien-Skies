package org.valkyrienskies.mod.common.multithreaded;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

/**
 * An extension of CompletableFuture with thenAcceptTickSync to execute thenAccept on the tick
 * thread
 */
@EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
public class TickSyncCompletableFuture<T> extends CompletableFuture<T> {

    private Map<CompletableFuture<Void>, Consumer<? super T>> acceptHandlers =
        new ConcurrentHashMap<>();

    @Delegate
    private CompletableFuture<T> base;
    private boolean isCompleted = false;

    private TickSyncCompletableFuture(CompletableFuture<T> future) {
        this.base = future;
    }

    public static <U> TickSyncCompletableFuture<U> from(CompletableFuture<U> future) {
        return new TickSyncCompletableFuture<>(future);
    }

    @SubscribeEvent
    @SneakyThrows
    public void onTick(ServerTickEvent e) {
        if (super.isDone() && !this.isCompleted) {
            acceptHandlers.forEach((f, c) -> {
                try {
                    c.accept(super.get());
                    f.complete(null);
                } catch (Throwable ex) {
                    f.completeExceptionally(ex);
                }
            });
            this.isCompleted = true;
            acceptHandlers.clear();
        }
    }

    public static <U> TickSyncCompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return from(CompletableFuture.supplyAsync(supplier));
    }

    public CompletableFuture<Void> thenAcceptTickSync(Consumer<? super T> action) {
        CompletableFuture<Void> voidCompletableFuture = new CompletableFuture<>();
        acceptHandlers.put(voidCompletableFuture, action);
        return voidCompletableFuture;
    }

}
