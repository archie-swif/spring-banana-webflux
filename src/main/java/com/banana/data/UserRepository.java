package com.banana.data;

import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.core.query.ViewIndexed;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
@ViewIndexed(designDoc = "user")
@N1qlPrimaryIndexed
public interface UserRepository extends ReactiveCouchbaseRepository<User, String> {
}
