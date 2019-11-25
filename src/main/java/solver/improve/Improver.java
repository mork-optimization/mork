package solver.improve;

import solution.Solution;

import java.util.concurrent.TimeUnit;

public interface Improver {
    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current solution any further
     * @param s model.Solution to improve
     * @return Improved s
     */
    default Solution improve(Solution s, long quantity, TimeUnit time) {
        long stopTime = System.nanoTime() + time.toNanos(quantity);
        //int rounds = 0;
        while (System.nanoTime() < stopTime && iteration(s)){
            //rounds++;
        }
        return s;
    }

    /**
     * Improves a Solution "indefinitely", timeouts after 1 Day (timeout may change)
     * @param s
     * @return
     */
    default Solution improve(Solution s){
        return improve(s, 1, TimeUnit.DAYS);
    }

    /**
     * Tries to improve the recieved solution
     * @param s Solution to improve
     * @return True if the solution has been improved, false otherwise
     */
    boolean iteration(Solution s);


}
