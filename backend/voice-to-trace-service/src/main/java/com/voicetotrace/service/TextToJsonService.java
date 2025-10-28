package com.voicetotrace.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

/**
 * TextToJsonService is responsible for transforming a transcribed text
 * into a structured JSON-compatible map. This is a simple NLP simulation,
 * but can later be extended to integrate with LLMs or Google NLP APIs.
 */
@Service
public class TextToJsonService {

    public Map<String, Object> convertToJson(String text) {
        Map<String, Object> result = new HashMap<>();
        result.put("rawText", text);

        text = text.toLowerCase();

        // Determine intent heuristically
        if (text.contains("create account") || text.contains("open account")) {
            result.put("intent", "create_account");
        } else if (text.contains("balance") || text.contains("check balance")) {
            result.put("intent", "check_balance");
        } else if (text.contains("transfer") || text.contains("send money")) {
            result.put("intent", "money_transfer");
        } else {
            result.put("intent", "general_query");
        }

        // Extract possible name
        Pattern namePattern = Pattern.compile("my name is ([a-z ]+)");
        Matcher nameMatcher = namePattern.matcher(text);
        if (nameMatcher.find()) {
            result.put("name", nameMatcher.group(1).trim());
        }

        // Extract possible email
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            result.put("email", emailMatcher.group());
        }

        // Extract possible amount
        Pattern amountPattern = Pattern.compile("(\\d+(?:\\.\\d{1,2})?)\\s?(?:rupees|rs|usd|dollars)?");
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            result.put("amount", amountMatcher.group(1));
        }

        result.put("timestamp", new Date().toString());
        return result;
    }
}