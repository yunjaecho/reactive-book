package com.yunjae.rxjava.ch2;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;
import twitter4j.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Ignore
public class TwitterSample {

    private static final Logger log = LoggerFactory.getLogger(TwitterSample.class);

    @Test
    public void sample_51() {
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                log.info("Status {}", status);
            }

            @Override
            public void onException(Exception ex) {
                log.error("Error callback", ex);
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
        });


        try {
            twitterStream.sample();
            TimeUnit.SECONDS.sleep(10);
            twitterStream.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Observable<Status> observe() {
        System.out.println("@@@@@@@@2");
        return Observable.create(subscriber -> {
           System.out.println("create");
           TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
           twitterStream.addListener(new StatusListener() {
               @Override
               public void onStatus(Status status) {
                   System.out.println("onStatus");
                   if (subscriber.isUnsubscribed()) {
                       twitterStream.shutdown();
                   } else {
                       subscriber.onNext(status);
                   }
               }

               @Override
               public void onException(Exception e) {
                   System.out.println("onException");
                   if (subscriber.isUnsubscribed()) {
                       twitterStream.shutdown();
                   } else {
                       subscriber.onError(e);
                   }

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
           });
            subscriber.add(Subscriptions.create(twitterStream::shutdown));
        });
    }

    @Test
    public void sample_52() {
        observe().subscribe(
                status -> log.info("Status: {}", status),
                ex -> log.error("Error callback", ex)
                );
    }
}
