package solver.improve;

import solution.Solution;
import util.DoubleComparator;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class VND {

    public Solution doIt(Solution current,List<Improver> improvers) {
        int currentLS = 0;
        while(currentLS < improvers.size()){
            double prev = current.getOptimalValue();
            Improver ls = improvers.get(currentLS);
            current = ls.improve(current, 10, TimeUnit.MINUTES);
            if (DoubleComparator.isLessOrEquals(prev, current.getOptimalValue())) {
                currentLS++;
            } else {
                currentLS = 0;
            }
        }
        return current;
    }
}
