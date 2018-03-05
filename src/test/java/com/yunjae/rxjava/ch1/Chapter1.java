package com.yunjae.rxjava.ch1;

import com.yunjae.rxjava.util.Sleeper;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Scheduler;
import rx.Single;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;


@Ignore
public class Chapter1 {

    private static final String SOME_KEY = "FOO";

    @Test
    public void sample_7() {
        Observable.create(s -> {
            s.onNext("Hello World!");
            s.onCompleted();
        }).subscribe(System.out::println);
    }

    @Test
    public void sample_8() {
        Map<String, String> cache = new ConcurrentHashMap<>();
        cache.put(SOME_KEY, "123");

        Observable.create(s -> {
            s.onNext(cache.get(SOME_KEY));
            s.onCompleted();
        }).subscribe(System.out::println);
    }

    @Test
    public void sample_82() {
        Observable.create(s -> {
            String fromCache = getFromCache(SOME_KEY);
            if (fromCache != null) {
                s.onNext(fromCache);
                s.onCompleted();
            } else {
                getDataAsynchronously(SOME_KEY)
                        .onResponse(v -> {
                            s.onNext(v);
                            s.onCompleted();
                        })
                        .onFailure(excepton -> {
                            s.onError(excepton);
                        });
            }
        }).subscribe(System.out::println);
    }

    @Test
    public void sample_9() {
        Observable<Integer> o = Observable.create(s -> {
            s.onNext(1);
            s.onNext(2);
            s.onNext(3);
            s.onCompleted();
        });

        o.map(i -> "Number" + i)
                .subscribe(System.out::println);
    }

    @Test
    public void sample_92() {
        System.out.println("Thread.currentThread() : " + Thread.currentThread());
        Observable.<Integer>create(s -> {
            new Thread(() -> s.onNext(42), "MyThread").start();
        }).doOnNext(i -> System.out.println(Thread.currentThread()))
          .filter(i -> i % 2 == 0)
          .map(i -> "Value " + i + " processed on " + Thread.currentThread())
          .subscribe(s -> System.out.println("SOME_VALUE => " + s));

        System.out.println("Will print BEFORE values are emitted because Observable is async");
        Sleeper.sleep(Duration.ofSeconds(1));
    }

    @Test
    public void sample_10() {
        Observable.create(s -> {
            new Thread(() -> {
                s.onNext("one");
                s.onNext("two");
                s.onNext("three");
                s.onNext("four");
                s.onCompleted();
            }).start();
        }).subscribe(System.out::println);
    }

    @Test
    public void sample_11() {
        Observable<String> a = Observable.create(s -> {
            new Thread(() -> {
                s.onNext("one");
                s.onNext("two");
                s.onCompleted();
            }).start();
        });

        Observable<String> b = Observable.create(s -> {
            new Thread(() -> {
                s.onNext("three");
                s.onNext("four");
                s.onCompleted();
            }).start();
        });

        Observable<String> c = Observable.merge(a, b);
        c.subscribe(System.out::println);
    }

    /**
     * lazy evaluation
     */
    @Test
    public void sample_14() {
        String args = SOME_KEY;

        Observable<String> someData = Observable.create(s -> {
            getDataFromServerWithCallback(args, data -> {
               s.onNext(data);
               s.onCompleted();
            });
        });
        someData.subscribe(System.out::println);

        someData.subscribe(s -> System.out.println("Subscriber1 :" + s));
        someData.subscribe(s -> System.out.println("Subscriber2 :" + s));

        Observable<String> lazyFallback = Observable.just("Fallback");
        someData.onErrorResumeNext(lazyFallback)
                .subscribe(System.out::println);
    }

    /**
     * duality
     */
    @Test
    public void sample_16() {
        // Iterable
        getDataFromLocalMemorySynchronously()
                .skip(10)
                .limit(5)
                .map(s -> s + "_transformed")
                .forEach(System.out::println);

        System.out.println("########################");

        // Observable
        getDataFromNetworkAsynchronously()
                .skip(10)
                .limit(5)
                .map(s -> s + "_transformed")
                .subscribe(System.out::println);
    }

    private Stream<String> getDataFromLocalMemorySynchronously() {
        return IntStream
                .range(0, 100)
                .mapToObj(Integer::toString);
    }

    private Observable<String> getDataFromNetworkAsynchronously() {
        return Observable
                .range(0, 100)
                .map(Object::toString);
    }

    /**
     * Composition Future(CompletableFuture)
     */
    @Test
    public void sample_19() {
        CompletableFuture<String> f1 = getDataAsFuture(1);
        CompletableFuture<String> f2 = getDataAsFuture(2);

        CompletableFuture<String> f3 = f1.thenCombine(f2, (x, y) -> {
            System.out.println("x + y = " + (x+y));
            return x + y;
        });
    }

    /**
     * Composition Observable
     */
    @Test
    public void sample_19_2() {
        Observable<String> o1 = getDataAsObservable(1);
        Observable<String> o2 = getDataAsObservable(2);

        Observable<String> o3 = Observable.zip(o1, o2, (x, y) -> {
            return x + y;
        });
        o3.subscribe(System.out::println);

        System.out.println("=============================");

        Observable<String> o4 = Observable.merge(o1, o2);
        o4.subscribe(System.out::println);
    }

    @Test
    public void sample_20() {
        Observable<String> a_merage_b = getDataA().mergeWith(getDataB());
        a_merage_b.subscribe(System.out::println);
    }

    public Single<String> getDataA() {
        return Single.<String>create(o -> {
            o.onSuccess("DataA");
        }).subscribeOn(Schedulers.io());
    }

    private Single<String> getDataB() {
        return Single.just("DataB").subscribeOn(Schedulers.io());
    }

    @Test
    public void sample_21() {
        Single<String> s1 = getDataAsSingle(1);
        Single<String> s2 = getDataAsSingle(2);

        Observable<String> s3 = Single.merge(s1, s2);
        s3.subscribe(System.out::println);
    }

    private Single<String> getDataAsSingle(int i) {
        return Single.just("Done: " + i);
    }



    private CompletableFuture<String> getDataAsFuture(int num) {
        return CompletableFuture.completedFuture("Done " + num + "\n");
    }

    private Observable<String> getDataAsObservable(int num) {
        return Observable.just("Done " + num + "\n");
    }


    private Callback getDataAsynchronously(String someKey) {
        final Callback callback = new Callback();
        new Thread(() -> {
            Sleeper.sleep(Duration.ofSeconds(1));
            callback.getOnResponse().accept(someKey + ":123");
        }).start();
        return callback;
    }

    private String getFromCache(String key) {
        return null;
        //return key + ":123";
    }

    private void getDataFromServerWithCallback(String args, Consumer<String> consumer) {
        consumer.accept("Random: " + Math.random());
    }
}
