package com.yunjae.rxjava.ch1;

import com.yunjae.rxjava.util.Sleeper;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


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
