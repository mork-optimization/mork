#ifndef GVNS_H
#define GVNS_H

#include "Random.h"
#include "Timer.h"
#include <vector>

template<class Solution>
class GVNS
{
public:
    Random &rng;
    Timer &timer;
    int level_max;
    double time_limit;

    GVNS(Random &_rng, Timer &_timer, int _level_max, double _time_limit)
        : rng(_rng), timer(_timer), level_max(_level_max),
          time_limit(_time_limit)
    {      
    }


void gvns(Solution &x)
{
    int level = 1;
    int iterlevel_max = 30;
    int iterlevel = 0;
    //vnd(x);
    Solution x2 = x;
    while (level < level_max
           && timer.elapsed_time_virtual() < time_limit) {
        x2.perturb_1shift_feasible(level, rng);
        //int pert_cost = x2.cost();
        vnd(x2);
        if (x2.cost() < x.cost()) {
            DEBUG2(
            cerr << "# (pert. " << level << ")\t" << pert_cost << endl;        
            cerr << "# (ls)\t" << x2.cost() << endl;
                );
            DEBUG2(fprintf (stderr, "%7s %9d %8.2f  %6d  %8.1f\n", 
                            "gvns", level, double(x2.cost()), x2.constraint_violations(), timer.elapsed_time_virtual()));
            x = x2;
            level = 1;
            iterlevel = 0;
        } else {
            x2 = x;
            iterlevel++;
            if (iterlevel > iterlevel_max) {
                level++;
                iterlevel = 0;
            }
        }
    }
}

void vnd(Solution &x)
{
    bool improved = false;
    assert(x.constraint_violations() == 0);

    do {
        while (x.feasible_1shift_first(rng)) {
            improved = true;
        }
        DEBUG2(if (improved) 
                   fprintf (stderr, "%7s %9d %8.2f  %6d  %8.1f\n", 
                            "insert", int(improved), double(x.cost()), x.constraint_violations(), timer.elapsed_time_virtual()));

        improved = false;
        while(x.two_opt_first(rng)) {
            improved = true; 
        }
        DEBUG2(if (improved) 
                   fprintf (stderr, "%7s %9d %8.2f  %6d  %8.1f\n", 
                            "2opt", int(improved), double(x.cost()), x.constraint_violations(), timer.elapsed_time_virtual()));
    } while (improved);
}

Solution
vns_feasible()
{
    Solution x;
    Solution x2;
    int level_max_vns_feasible = Solution::n / 2;

    do  {
        int level = 1;
        // FIXME: Fix this mess!
        x = *Solution::RandomSolution(&rng);
        x.ls_feasibility_1shift_first(rng);
        x2 = x;
        while (x.constraint_violations() && level < level_max_vns_feasible
               && timer.elapsed_time_virtual() < time_limit) {
            x2.perturb_1shift(level, rng);
            x2.ls_feasibility_1shift_first(rng);
            if (x2.infeasibility() < x.infeasibility()) {
                DEBUG2(fprintf (stderr, "%7s %9d %8.2f  %6d  %8.1f\n", 
                                "vnd_f", level, double(x2.cost()), x2.constraint_violations(), timer.elapsed_time_virtual()));

                x = x2;
                level = 1; // Improved
            } else {
                x2 = x;
                level++; // Not improved
            }
        }
    } while (x.constraint_violations()
             && timer.elapsed_time_virtual() < time_limit);

    DEBUG2(cerr << "# (vnd feasible)\t" << x.cost() << endl);        
    return x;
}

};


#endif
