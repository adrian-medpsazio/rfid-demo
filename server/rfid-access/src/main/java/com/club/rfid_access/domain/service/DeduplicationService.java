package com.club.rfid_access.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeduplicationService {
    private final long dedupWindowMs;
    private final Map<String, Instant> lastReads = new ConcurrentHashMap<>();
    public DeduplicationService(@Value("${rfid.ingestion.dedup-window-ms:500}") long dedupWindowMs) {
        this.dedupWindowMs = dedupWindowMs;
    }
    /**
     * Returns true if this read is a duplicate (same reader + EPC within the window).
     */
    public boolean isDuplicate(String readerId, String epc) {
        String key = readerId + ":" + epc;
        Instant now = Instant.now();
        Instant last = lastReads.get(key);
        if (last != null && Duration.between(last, now).toMillis() < dedupWindowMs) {
            return true;
        }
        lastReads.put(key, now);
        return false;
    }
    /** Cleans up stale entries older than the window (optional, periodic). */
    public void cleanUp() {
        Instant cutoff = Instant.now().minusMillis(dedupWindowMs);
        lastReads.entrySet().removeIf(e -> e.getValue().isBefore(cutoff));
    }
}
