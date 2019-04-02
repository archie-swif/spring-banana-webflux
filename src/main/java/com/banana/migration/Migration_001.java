package com.banana.migration;

import com.banana.data.UserRepository;
import com.couchbase.client.java.Bucket;
import com.github.liquicouch.changeset.ChangeLog;
import com.github.liquicouch.changeset.ChangeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@ChangeLog(order = "001")
public class Migration_001 {

    @Autowired
    UserRepository userRepository;

    Random random = new Random();

    @ChangeSet(order = "001", id = "create index", author = "ar")
    public void createSecondaryUserIndex(Bucket bucket) {
        bucket.bucketManager().createN1qlIndex("user-time", true, false, "activeMinutes");
    }

    @ChangeSet(order = "002", id = "update users", author = "ar")
    public void updateAllUsers() {
        userRepository.findAll()
                .doOnNext(u -> u.setActiveMinutes(random.nextInt()))
                .collectList()
                .flatMapMany(l -> userRepository.saveAll(l))
                .subscribe();
    }

}
