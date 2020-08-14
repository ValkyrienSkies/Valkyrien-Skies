package org.valkyrienskies.mod.common.util.multithreaded;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

/**
 * <p>An extension of CompletableFuture with some 'Sync' suffixed methods like
 * {@link TickSyncCompletableFuture#supplyClientSync(Supplier)}.</p>
 *
 * <p>Any method written like this will have its results executed on the <strong>provided game
 * thread</strong>. This allows for easily multithreading things that produce results that are
 * required on the tick thread.<p>
 *
 * <p>For example, if you want to calculate digits of PI in another thread and then send them to
 * the player in chat, you might write:</p>
 *
 * <pre>{@code
 * TickSyncCompletableFuture
 *      .supplyAsync(() -> generatePi())
 *      .thenAcceptServerSync(pi -> player.sendMessage(pi));
 * }</pre>
 *
 * @author Rubydesic
 */
@EventBusSubscriber(modid = ValkyrienSkiesMod.MOD_ID)
public class TickSyncCompletableFuture<T> {

    private CompletableFuture<T> base;

    private TickSyncCompletableFuture(CompletableFuture<T> future) {
        base = future;
    }

    public TickSyncCompletableFuture() {
        this(new CompletableFuture<>());
    }

    public static <U> TickSyncCompletableFuture<U> from(CompletableFuture<U> future) {
        return new TickSyncCompletableFuture<>(future);
    }

    // region API methods

    public static <K> TickSyncCompletableFuture<K> supplyClientSync(Supplier<K> supplier) {
        return TickSyncCompletableFuture.supplyAsync(supplier, VSExecutors.CLIENT);
    }

    public static TickSyncCompletableFuture<Void> runClientSync(Runnable runnable) {
        return TickSyncCompletableFuture.runAsync(runnable, VSExecutors.CLIENT);
    }

    public TickSyncCompletableFuture<Void> thenAcceptClientSync(Consumer<? super T> action) {
        return this.thenAcceptAsync(action, VSExecutors.CLIENT);
    }

    public TickSyncCompletableFuture<Void> thenRunClientSync(Runnable action) {
        return this.thenRunAsync(action, VSExecutors.CLIENT);
    }

    public static <K> TickSyncCompletableFuture<K> supplyServerSync(Supplier<K> supplier) {
        return TickSyncCompletableFuture.supplyAsync(supplier, VSExecutors.SERVER);
    }

    public static TickSyncCompletableFuture<Void> runServerSync(Runnable runnable) {
        return TickSyncCompletableFuture.runAsync(runnable, VSExecutors.SERVER);
    }

    public TickSyncCompletableFuture<Void> thenAcceptServerSync(Consumer<? super T> action) {
        return this.thenAcceptAsync(action, VSExecutors.SERVER);
    }

    public TickSyncCompletableFuture<Void> thenRunServerSync(Runnable action) {
        return this.thenRunAsync(action, VSExecutors.SERVER);
    }

    // endregion

    // region Delegation
    public <U> TickSyncCompletableFuture<U> thenApply(
        Function<? super T, ? extends U> fn) {
        return from(base.thenApply(fn));
    }

    public <U> TickSyncCompletableFuture<U> thenApplyAsync(
        Function<? super T, ? extends U> fn) {
        return from(base.thenApplyAsync(fn));
    }

    public <U> TickSyncCompletableFuture<U> thenApplyAsync(
        Function<? super T, ? extends U> fn,
        Executor executor) {
        return from(base.thenApplyAsync(fn, executor));
    }

    public TickSyncCompletableFuture<Void> thenAccept(
        Consumer<? super T> action) {
        return from(base.thenAccept(action));
    }

    public TickSyncCompletableFuture<Void> thenAcceptAsync(
        Consumer<? super T> action) {
        return from(base.thenAcceptAsync(action));
    }

    public TickSyncCompletableFuture<Void> thenAcceptAsync(
        Consumer<? super T> action, Executor executor) {
        return from(base.thenAcceptAsync(action, executor));
    }

    public TickSyncCompletableFuture<Void> thenRun(Runnable action) {
        return from(base.thenRun(action));
    }

    public TickSyncCompletableFuture<Void> thenRunAsync(Runnable action) {
        return from(base.thenRunAsync(action));
    }

    public TickSyncCompletableFuture<Void> thenRunAsync(Runnable action,
        Executor executor) {
        return from(base.thenRunAsync(action, executor));
    }

    public <U, V> TickSyncCompletableFuture<V> thenCombine(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn) {
        return from(base.thenCombine(other, fn));
    }

    public <U, V> TickSyncCompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn) {
        return from(base.thenCombineAsync(other, fn));
    }

    public <U, V> TickSyncCompletableFuture<V> thenCombineAsync(
        CompletionStage<? extends U> other,
        BiFunction<? super T, ? super U, ? extends V> fn,
        Executor executor) {
        return from(base.thenCombineAsync(other, fn, executor));
    }

    public <U> TickSyncCompletableFuture<Void> thenAcceptBoth(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action) {
        return from(base.thenAcceptBoth(other, action));
    }

    public <U> TickSyncCompletableFuture<Void> thenAcceptBothAsync(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action) {
        return from(base.thenAcceptBothAsync(other, action));
    }

    public <U> TickSyncCompletableFuture<Void> thenAcceptBothAsync(
        CompletionStage<? extends U> other,
        BiConsumer<? super T, ? super U> action,
        Executor executor) {
        return from(base.thenAcceptBothAsync(other, action, executor));
    }

    public TickSyncCompletableFuture<Void> runAfterBoth(
        CompletionStage<?> other, Runnable action) {
        return from(base.runAfterBoth(other, action));
    }

    public TickSyncCompletableFuture<Void> runAfterBothAsync(
        CompletionStage<?> other, Runnable action) {
        return from(base.runAfterBothAsync(other, action));
    }

    public TickSyncCompletableFuture<Void> runAfterBothAsync(
        CompletionStage<?> other, Runnable action,
        Executor executor) {
        return from(base.runAfterBothAsync(other, action, executor));
    }

    public <U> TickSyncCompletableFuture<U> applyToEither(
        CompletionStage<? extends T> other,
        Function<? super T, U> fn) {
        return from(base.applyToEither(other, fn));
    }

    public <U> TickSyncCompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other,
        Function<? super T, U> fn) {
        return from(base.applyToEitherAsync(other, fn));
    }

    public <U> TickSyncCompletableFuture<U> applyToEitherAsync(
        CompletionStage<? extends T> other,
        Function<? super T, U> fn, Executor executor) {
        return from(base.applyToEitherAsync(other, fn, executor));
    }

    public TickSyncCompletableFuture<Void> acceptEither(
        CompletionStage<? extends T> other,
        Consumer<? super T> action) {
        return from(base.acceptEither(other, action));
    }

    public TickSyncCompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other,
        Consumer<? super T> action) {
        return from(base.acceptEitherAsync(other, action));
    }

    public TickSyncCompletableFuture<Void> acceptEitherAsync(
        CompletionStage<? extends T> other,
        Consumer<? super T> action, Executor executor) {
        return from(base.acceptEitherAsync(other, action, executor));
    }

    public TickSyncCompletableFuture<Void> runAfterEither(
        CompletionStage<?> other, Runnable action) {
        return from(base.runAfterEither(other, action));
    }

    public TickSyncCompletableFuture<Void> runAfterEitherAsync(
        CompletionStage<?> other, Runnable action) {
        return from(base.runAfterEitherAsync(other, action));
    }

    public TickSyncCompletableFuture<Void> runAfterEitherAsync(
        CompletionStage<?> other, Runnable action,
        Executor executor) {
        return from(base.runAfterEitherAsync(other, action, executor));
    }

    public <U> TickSyncCompletableFuture<U> thenCompose(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return from(base.thenCompose(fn));
    }

    public <U> TickSyncCompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn) {
        return from(base.thenComposeAsync(fn));
    }

    public <U> TickSyncCompletableFuture<U> thenComposeAsync(
        Function<? super T, ? extends CompletionStage<U>> fn,
        Executor executor) {
        return from(base.thenComposeAsync(fn, executor));
    }

    public TickSyncCompletableFuture<T> whenComplete(
        BiConsumer<? super T, ? super Throwable> action) {
        return from(base.whenComplete(action));
    }

    public TickSyncCompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action) {
        return from(base.whenCompleteAsync(action));
    }

    public TickSyncCompletableFuture<T> whenCompleteAsync(
        BiConsumer<? super T, ? super Throwable> action,
        Executor executor) {
        return from(base.whenCompleteAsync(action, executor));
    }

    public <U> TickSyncCompletableFuture<U> handle(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return from(base.handle(fn));
    }

    public <U> TickSyncCompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn) {
        return from(base.handleAsync(fn));
    }

    public <U> TickSyncCompletableFuture<U> handleAsync(
        BiFunction<? super T, Throwable, ? extends U> fn,
        Executor executor) {
        return from(base.handleAsync(fn, executor));
    }

    public static <U> TickSyncCompletableFuture<U> supplyAsync(Supplier<U> action) {
        return from(CompletableFuture.supplyAsync(action));
    }

    public static <U> TickSyncCompletableFuture<U> supplyAsync(Supplier<U> supplier,
        Executor executor) {
        return from(CompletableFuture.supplyAsync(supplier, executor));
    }

    public static TickSyncCompletableFuture<Void> runAsync(Runnable runnable) {
        return from(CompletableFuture.runAsync(runnable));
    }

    public static TickSyncCompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return from(CompletableFuture.runAsync(runnable, executor));
    }

    public static <U> TickSyncCompletableFuture<U> completedFuture(U value) {
        return from(CompletableFuture.completedFuture(value));
    }

    public boolean isDone() {
        return base.isDone();
    }

    public T get() throws InterruptedException, ExecutionException {
        return base.get();
    }

    public T get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return base.get(timeout, unit);
    }

    public T join() {
        return base.join();
    }

    public T getNow(T valueIfAbsent) {
        return base.getNow(valueIfAbsent);
    }

    public boolean completeExceptionally(Throwable ex) {
        return base.completeExceptionally(ex);
    }

    public CompletableFuture<T> toCompletableFuture() {
        return base.toCompletableFuture();
    }

    public TickSyncCompletableFuture<T> exceptionally(
        Function<Throwable, ? extends T> fn) {
        return from(base.exceptionally(fn));
    }

    public static TickSyncCompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        return from(CompletableFuture.allOf(cfs));
    }

    public static TickSyncCompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        return from(CompletableFuture.anyOf(cfs));
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return base.cancel(mayInterruptIfRunning);
    }

    public boolean complete(T value) {
        return base.complete(value);
    }

    public boolean isCancelled() {
        return base.isCancelled();
    }

    public boolean isCompletedExceptionally() {
        return base.isCompletedExceptionally();
    }

    public void obtrudeValue(T value) {
        base.obtrudeValue(value);
    }

    public void obtrudeException(Throwable ex) {
        base.obtrudeException(ex);
    }

    public int getNumberOfDependents() {
        return base.getNumberOfDependents();
    }
    // endregion

    // Literally just a supplier attached to a CompletableFuture
    private static class CompletableSupplier<K> {

        Supplier<K> supplier;
        CompletableFuture<K> completable;

        CompletableSupplier(Supplier<K> supplier, CompletableFuture<K> completable) {
            this.supplier = supplier;
            this.completable = completable;
        }

        void complete() {
            completable.complete(supplier.get());
        }

    }

}
