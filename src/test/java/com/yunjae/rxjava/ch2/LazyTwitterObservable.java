package com.yunjae.rxjava.ch2;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import twitter4j.*;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class LazyTwitterObservable {
    private final Set<Subscriber<? super Status>> subscribers = new CopyOnWriteArraySet<>();
    private final TwitterStream twitterStream;

    public LazyTwitterObservable() {
        this.twitterStream = new TwitterStreamFactory().getInstance();
        this.twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                subscribers.forEach(s -> s.onNext(status));
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int i) {

            }

            @Override
            public void onScrubGeo(long l, long l1) {

            }

            @Override
            public void onStallWarning(StallWarning stallWarning) {

            }

            @Override
            public void onException(Exception ex) {
                subscribers.forEach(s -> s.onError(ex));
            }
        });
    }

    private final Observable<Status> observable = Observable.create(
            subscriber -> {
                register(subscriber);
                subscriber.add(Subscriptions.create(() -> this.deregister(subscriber)));
            }
    );

    private synchronized void register(Subscriber<? super Status> subscriber) {
        if (subscribers.isEmpty()) {
            subscribers.add(subscriber);
            twitterStream.sample();
        } else {
            subscribers.add(subscriber);
        }
    }

    private synchronized void deregister(Subscriber<? super Status> subscriber) {
        subscribers.remove(subscriber);
        if (subscribers.isEmpty()) {
            twitterStream.shutdown();
        }
    }
}
