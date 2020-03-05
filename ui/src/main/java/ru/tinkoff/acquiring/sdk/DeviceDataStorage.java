package ru.tinkoff.acquiring.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mariya Chernyadieva
 */
public class DeviceDataStorage {

    private final ReentrantLock locker = new ReentrantLock();
    private final Condition condition = locker.newCondition();

    private Map<String, String> deviceData = new HashMap<>();

    public Map<String, String> getData() throws InterruptedException {
        locker.lock();
        try {
            if (deviceData.isEmpty()) {
                condition.await();
            }
        } finally {
            locker.unlock();
        }

        return deviceData;
    }

    public void putData(Map<String, String> data) {
        locker.lock();
        deviceData.putAll(data);
        condition.signalAll();
        locker.unlock();
    }

    public void clearData() {
        deviceData.clear();
    }
}
