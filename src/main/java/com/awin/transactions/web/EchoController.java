package com.awin.transactions.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/echo")
public class EchoController {

    @GetMapping
    public Map<String, Object> echo(@RequestParam(defaultValue = "hello") String message) {
        return Map.of("message", message, "receivedAt", Instant.now().toString());
    }

    @PostMapping
    public Map<String, Object> echo(@RequestBody Map<String, Object> body) {
        return Map.of("payload", body, "receivedAt", Instant.now().toString());
    }
}
