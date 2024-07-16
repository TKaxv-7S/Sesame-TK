package tkaxv7s.xposed.sesame.util;

public class AverageMath {

    private final CircularFifoQueue<Integer> queue;

    private double sum;

    private double average;

    public AverageMath(int size) {
        this.queue = new CircularFifoQueue<>(size);
        this.sum = 0.0;
        this.average = 0.0;
    }

    public double nextDouble(int value) {
        Integer last = queue.push(value);
        if (last != null) {
            sum -= last;
        }
        sum += value;
        return average = sum / queue.size();
    }

    public int nextInteger(int value) {
        return (int) nextDouble(value);
    }

    public double averageDouble() {
        return average;
    }

    public int getAverageInteger() {
        return (int) average;
    }

    public void clear() {
        queue.clear();
        sum = 0.0;
        average = 0.0;
    }

}