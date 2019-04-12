package com.banana.manager;

import org.springframework.cloud.stream.binder.PartitionSelectorStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class PartitionConfig {

    Random random = new Random();

    //    @Bean
//    public CustomPartitionKeyExtractorClass customPartitionKeyExtractor() {
//        return new CustomPartitionKeyExtractorClass();
//    }

//    private class CustomPartitionKeyExtractorClass implements PartitionKeyExtractorStrategy {
//        @Override
//        public Object extractKey(Message<?> message) {
//            return message.getHeaders().getId();
//        }
//    }

    @Bean
    public CustomPartitionSelectorClass customPartitionSelector() {
        return new CustomPartitionSelectorClass();
    }


    private class CustomPartitionSelectorClass implements PartitionSelectorStrategy {
        @Override
        public int selectPartition(Object key, int partitionCount) {
            String keyString = String.valueOf(key);

            if (keyString.endsWith("0")) {
                return 0;
            }
            return random.nextInt(partitionCount-1) + 1;
        }
    }
}
