package com.example;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class QuoteService {

    static final String FALLBACK = "The quick brown fox jumps over the lazy dog and runs far away.";

    private static final String URL = "https://zenquotes.io/api/random";

    private final RestTemplate rest;

    public QuoteService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        rest = new RestTemplate(factory);
    }

    /** Returns a random quote, or the static text if anything goes wrong. */
    public String fetchQuote() {
        try {
            JsonNode body = rest.getForObject(URL, JsonNode.class);
            String quote = body.get(0).get("q").asText().trim();
            quote = normalize(quote);
            return quote.isEmpty() ? FALLBACK : quote;
        } catch (Exception e) {
            return FALLBACK;
        }
    }

    // Keep the text typeable on a standard keyboard
    private static String normalize(String s) {
        return s.replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201C', '"').replace('\u201D', '"')
                .replace("\u2013", "-").replace("\u2014", "-")
                .replace("\u2026", "...")
                .replaceAll("\\s+", " ");
    }
}
