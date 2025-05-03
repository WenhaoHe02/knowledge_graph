package org.example;


import java.util.Map;
import java.util.concurrent.*;

class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }

    @Override
    public String toString() {
        return "Pair{key=" + key + ", value=" + value + "}";
    }
}

/**
 * 总线管理器（适配原DataBaseBusManager）
 */
public class BusManager {
    private static final BusManager INSTANCE = new BusManager();
    private final BlockingQueue<Pair<String, DbRequest>> requestQueue = new LinkedBlockingQueue<>();
    private final ConcurrentMap<String, CompletableFuture<DbResponse>> pendingResponses = new ConcurrentHashMap<>();
    private final Bus bus = new Bus();

    public static BusManager getInstance() {
        return INSTANCE;
    }

    public void receive(DbRequest dbRequest, String correlationId) {
        CompletableFuture<DbResponse> future = new CompletableFuture<>();
        pendingResponses.put(dbRequest.getCorrelationId(), future);
        requestQueue.offer(new Pair<>(correlationId, dbRequest));
        processRequestsAsync();
    }

    private void processRequestsAsync() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (!requestQueue.isEmpty()) {
                Pair<String, DbRequest> pair = requestQueue.poll();
                DbRequest req = pair.getValue();
                bus.sendRequestAsync(req);
            }
        });
    }

    public CompletableFuture<DbResponse> getFuture(String correlationId) {
        return pendingResponses.get(correlationId);
    }

    public void completeResponse(DbResponse response) {
        String cid = response.getCorrelationId();
        CompletableFuture<DbResponse> future = pendingResponses.remove(cid);
        if (future != null) {
            future.complete(response);
        } else {
            System.err.println("No pending future for correlationId: " + cid);
        }
    }

    public DbResponse waitForResponse(String correlationId) {
        try {
            return getFuture(correlationId).get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new DbResponse(correlationId, false, null, "Timeout or error: " + e.getMessage());
        }
    }
}



