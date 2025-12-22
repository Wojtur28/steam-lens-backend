package com.example.steamlensbackend.config;

import org.bson.Document;
import org.javamoney.moneta.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import javax.money.MonetaryAmount;
import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new MonetaryAmountWriter(),
                new MonetaryAmountReader()
        ));
    }

    @WritingConverter
    static class MonetaryAmountWriter implements Converter<MonetaryAmount, Document> {
        @Override
        public org.bson.Document convert(MonetaryAmount source) {
            org.bson.Document document = new org.bson.Document();
            document.put("amount", source.getNumber().toString());
            document.put("currency", source.getCurrency().getCurrencyCode());
            return document;
        }
    }

    @ReadingConverter
    static class MonetaryAmountReader implements Converter<org.bson.Document, MonetaryAmount> {
        @Override
        public MonetaryAmount convert(org.bson.Document source) {
            String amount = source.getString("amount");
            String currency = source.getString("currency");
            return Money.of(new java.math.BigDecimal(amount), currency);
        }
    }
}
