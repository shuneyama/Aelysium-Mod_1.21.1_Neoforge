package net.aelysium.aelysiummod.sistemas.midia.cinematica.cliente;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class FilaQuadros {

    private final ArrayBlockingQueue<QuadroVideo> queue;

    public FilaQuadros(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }


    public boolean offer(QuadroVideo frame) throws InterruptedException {
        while (true) {
            if (queue.offer(frame, 50, TimeUnit.MILLISECONDS)) return true;
            if (Thread.currentThread().isInterrupted()) return false;
        }
    }

    public QuadroVideo peek() { return queue.peek(); }
    public QuadroVideo poll() { return queue.poll(); }
    public boolean isEmpty() { return queue.isEmpty(); }
    public void clear() { queue.clear(); }
}
