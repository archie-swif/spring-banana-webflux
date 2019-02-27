/*
 * PEARSON PROPRIETARY AND CONFIDENTIAL INFORMATION SUBJECT TO NDA
 * Copyright (c) 2017 Pearson Education, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Pearson Education, Inc. The intellectual and technical concepts contained
 * herein are proprietary to Pearson Education, Inc. and may be covered by U.S.
 * and Foreign Patents, patent applications, and are protected by trade secret
 * or copyright law. Dissemination of this information, reproduction of this
 * material, and copying or distribution of this software is strictly forbidden
 * unless prior written permission is obtained from Pearson Education, Inc.
 */
package com.banana;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.cluster.ClusterInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.couchbase.config.AbstractReactiveCouchbaseConfiguration;
import org.springframework.data.couchbase.config.BeanNames;
import org.springframework.data.couchbase.core.RxJavaCouchbaseTemplate;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.ReactiveRepositoryOperationsMapping;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableReactiveCouchbaseRepositories(basePackages = "com.banana")
class WorkingCouchbaseConfiguration extends AbstractReactiveCouchbaseConfiguration {

    @Value("#{'${glp.core.data.couchbase.hosts:${couchbaseserver.hostNames:}}'.split(',')}")
    private List<String> bootstrapHosts;

    @Value("${glp.core.data.couchbase.bucket:${couchbaseserver.bucketName:}}")
    private String bucketName;

    /* Not supported. */
    private String bucketPassword = null;

    @Value("${glp.core.data.couchbase.username:${couchbaseserver.cluster.username:}}")
    private String clusterUsername;

    @Value("${glp.core.data.couchbase.password:${couchbaseserver.cluster.password:}}")
    private String clusterPassword;


    @Override
    protected String getBucketName() {
        return bucketName;
    }

    @Override
    protected String getBucketPassword() {
        return bucketPassword;
    }

    @Override
    protected List<String> getBootstrapHosts() {
        return Collections.unmodifiableList(bootstrapHosts);
    }

    @Override
    @Bean(name = BeanNames.COUCHBASE_CLUSTER_INFO)
    public ClusterInfo couchbaseClusterInfo() throws Exception {
        return couchbaseCluster().authenticate(clusterUsername, clusterPassword).clusterManager().info();
    }

    @Override
    @Bean(destroyMethod = "close", name = BeanNames.COUCHBASE_BUCKET)
    public Bucket couchbaseClient() throws Exception {
        return couchbaseCluster().authenticate(clusterUsername, clusterPassword).openBucket(getBucketName());
    }

    @Bean(destroyMethod = "close")
    public Bucket secondBucket() throws Exception {
        return couchbaseCluster().authenticate(clusterUsername, clusterPassword).openBucket("cache2");
    }


    @Bean
    @Primary
    public RxJavaCouchbaseTemplate primaryTemplate() throws Exception {
        RxJavaCouchbaseTemplate template = new RxJavaCouchbaseTemplate(this.couchbaseConfigurer().couchbaseClusterInfo(), this.couchbaseConfigurer().couchbaseClient(), this.mappingCouchbaseConverter(), this.translationService());
        template.setDefaultConsistency(this.getDefaultConsistency());
        return template;
    }

    @Bean
    public RxJavaCouchbaseTemplate secondTemplate() throws Exception {
        RxJavaCouchbaseTemplate template = new RxJavaCouchbaseTemplate(
                this.couchbaseConfigurer().couchbaseClusterInfo(),
                this.secondBucket(),
                this.mappingCouchbaseConverter(),
                this.translationService());
        template.setDefaultConsistency(this.getDefaultConsistency());
        return template;
    }


    @Override
    protected void configureReactiveRepositoryOperationsMapping(ReactiveRepositoryOperationsMapping mapping) {
        try {
            mapping
                    .map(SecondRepository.class, secondTemplate());
        } catch (Exception e) {
            e.printStackTrace();
        }


        super.configureReactiveRepositoryOperationsMapping(mapping);
    }
}