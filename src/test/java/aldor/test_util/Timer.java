package aldor.test_util;

public class Timer {

    private final String name;
    private final long startTime;
    private long endTime = 0;

    public Timer(String name) {
        this.name = name;
        startTime = System.currentTimeMillis();
    }

    public long duration() {
        return current() - startTime;
    }

    public long current() {
        if (endTime == 0) {
            return System.currentTimeMillis();
        }
        else {
            return endTime;
        }
    }

    @Override
    public String toString() {
        return "{Timer: " + name + " - " + duration() + (endTime == 0 ? " ms (Still running)": "") + "}";
    }

    private void stop() {
        this.endTime = System.currentTimeMillis();
    }

    public TimerRun run() {
        //noinspection ReturnOfInnerClass
        return new TimerRun();
    }

    public class TimerRun implements AutoCloseable {

        @Override
        public void close() throws Exception {
            stop();
        }

    }

}
