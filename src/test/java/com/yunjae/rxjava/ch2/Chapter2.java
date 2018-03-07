package com.yunjae.rxjava.ch2;

import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

public class Chapter2 {

    @Test
    public void sample_35() {
        Observable<Tweet> tweets = Observable.empty();

        Observer<Tweet> observer = new Observer<Tweet>() {
            @Override
            public void onNext(Tweet tweet) {
                System.out.println(tweet);
            }

            @Override
            public void onCompleted() {
                noMore();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };

        tweets.subscribe(observer);
    }

    @Test
    public void sample_36() {
        /*Observable<Tweet> tweets = Observable.create(new Observable.OnSubscribe<Tweet>() {
            @Override
            public void call(Subscriber<? super Tweet> subscriber) {
                subscriber.onNext(new Tweet("Hello"));
                subscriber.onNext(new Tweet("world"));
                subscriber.onNext(new Tweet("java"));
                subscriber.onNext(new Tweet("Java"));
                subscriber.onCompleted();
            }
        });*/
        Observable<Tweet> tweets = Observable.create(s -> {
            s.onNext(new Tweet("Hello"));
            s.onNext(new Tweet("world"));
            s.onNext(new Tweet("java"));
            s.onNext(new Tweet("Java"));
            s.onCompleted();
        });

        Subscriber<Tweet> subscriber = new Subscriber<Tweet>() {
            @Override
            public void onNext(Tweet tweet) {
                System.out.println(tweet.getText());
                if (tweet.getText().contains("Java")) {
                    System.out.println("unsubscribe");
                    unsubscribe();
                }
            }

            @Override
            public void onCompleted() {
                System.out.println("sample_36 onCompleted...");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }
        };

        tweets.subscribe(subscriber);
    }

    private void noMore() {
        System.out.println("onCompleted");
    }


    @Test
    public void sample_38() {
        log("Before");
        Observable.range(5,3).subscribe(i -> {
            log(i);
        });
        log("After");
    }

    @Test
    public void sample_39() {
        System.out.println("===================== (sample_39) =====================");
        Observable<Integer> inits = Observable.create(s-> {
            log("Create");
            s.onNext(5);
            s.onNext(6);
            s.onNext(7);
            s.onCompleted();
            log("Completed");
        });

        log("Starting...");
        inits.subscribe(i -> log("Element : "+i),
                System.out::println,
                () -> {System.out.println("onCompleted");});

    }


    private static void log(Object msg) {
        System.out.println(Thread.currentThread().getName() + " : " + msg);
    }

    @Test
    public void sample_40_1() {
        System.out.println("===================== (sample_40_1) =====================");
        Observable<Integer> inits = Observable.create(s -> {
            log("Create");
            s.onNext(42);
            s.onCompleted();
        });
        log("Starting");
        inits.subscribe(i -> log("Element A :" + i));
        inits.subscribe(i -> log("Element B :" + i));
        log("Exit");
    }

    @Test
    public void sample_41() {
        System.out.println("===================== (sample_41) =====================");
        Observable<Integer> inits = Observable.<Integer>create(s -> {
            log("Create");
            s.onNext(42);
            s.onCompleted();
        }).cache();

        log("Starting");
        inits.subscribe(i -> log("Element A :" + i));
        inits.subscribe(i -> log("Element B :" + i));
        log("Exit");
    }

    static <T> Observable<T> just(T x) {
        return Observable.create(subscriber -> {
                    subscriber.onNext(x);
                    subscriber.onCompleted();
                }
        );
    }

    private Observable<BigInteger> naturalNumbers() {
        Observable<BigInteger> naturalNumbers = Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        BigInteger i = ZERO;
                        while (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(i);
                            i = i.add(ONE);
                        }
                    };
                    new Thread(r).start();
                });
        return naturalNumbers;
    }

    @Test
    public void sample_42() {
        final Observable<BigInteger> naturalNumbers = naturalNumbers();
        Subscription subscription = naturalNumbers.subscribe(x -> log(x));
    }

    static <T> Observable<T> delayed(T x) {
        return Observable.create(
                subscriber -> {
                    Runnable r = () -> {
                        sleep(10, SECONDS);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(x);
                            subscriber.onCompleted();
                        }
                    };
                    new Thread(r).start();
                });
    }

    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException ignored) {
            //intentionally ignored
        }
    }

    @Test
    public void sample_44() {
        delayed(new Tweet("new Tweet")).subscribe(s -> System.out.println(s.getText()));
    }

    static Observable<Data> loadAll(Collection<Integer> ids) {
        return Observable.create(s -> {
            ExecutorService pools = Executors.newFixedThreadPool(10);
            AtomicInteger countDown = new AtomicInteger(ids.size());
            ids.forEach(id -> pools.submit(()-> {
                final Data data = load(id);
                s.onNext(data);
                if (countDown.decrementAndGet() == 0) {
                    pools.shutdownNow();
                    s.onCompleted();
                }
            }));
        });
    }

    private static Data load(Integer id) {
        return new Data();
    }

    @Test
    public void sample_46() {
        List<Integer> list = IntStream.range(1, 1000).boxed().collect(toList());
        loadAll(list).subscribe(System.out::println);

    }
}
