/*************************************************************************

 Travelling Salesman Problem with Time Windows

 ---------------------------------------------------------------------

                       Copyright (c) 2008
                  Christian Blum <christian.blum@ehu.es>
             Manuel Lopez-Ibanez <manuel.lopez-ibanez@manchester.ac.uk>

 This program is free software (software libre); you can redistribute
 it and/or modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, you can obtain a copy of the GNU
 General Public License at: http://www.gnu.org/licenses/gpl.html

*************************************************************************/

#ifndef SOLUTION_H
#define SOLUTION_H

#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <cfloat>
#include <cmath>
#include <cassert>
#include <string>
#include <cstring>
#include <climits>

#include "Random.h"
#include "misc-math.h"
#include "xvector.hpp" // For vector::reinsert

using namespace std;

#define NUMBER_TYPE_DOUBLE 0
#define NUMBER_TYPE_INT 1
#if !defined(NUMBER_TYPE_IS)
#define NUMBER_TYPE_IS NUMBER_TYPE_INT
#endif

#if NUMBER_TYPE_IS == NUMBER_TYPE_INT
typedef int number_t;
#define STRING_NUMBER_IS "integer"
#define NUMBER_T_MAX INT_MAX
#define NUMBER_T_MIN 0
#elif NUMBER_TYPE_IS == NUMBER_TYPE_DOUBLE
typedef double number_t;
#define STRING_NUMBER_IS "double"
#define NUMBER_T_MAX DBL_MAX
#define NUMBER_T_MIN DBL_MIN
#else
#error NUMBER_TYPE_IS is not a recognized value!
#endif

enum heuristic_type_t {
    NONE = 0,
    SHORTEST_TRAVEL_TIME,
    EARLIEST_WINDOW_END,
    EARLIEST_WINDOW_START,
    MIXED,
    WEIGHTED,
    HEURISTIC_TYPE_UNKNOWN
};

enum localsearch_type_t {
    LOCALSEARCH_NONE = 0,
    LOCALSEARCH_FIRST,
    LOCALSEARCH_BEST
};

class Solution {

public:

  static string instance;  
  static int n;   // number of customers
  static unsigned int evaluations;

  static heuristic_type_t heuristic_type;
  static localsearch_type_t localsearch_type;

  static bool is_symmetric;
  static void LoadInstance (string filename);
  static void print_parameters (string prefix="", FILE *stream=stdout);
  static void print_compile_parameters (FILE *stream=stdout);

  static bool set_heuristic_weights (char *arg);
  static string get_heuristic_type(void);
  static string get_localsearch_type(void);
  static double heuristic_information (int prev, int next);
  static void randomize_hinfo (Random *rng);

  vector<int> permutation;
  vector<bool> node_assigned;
  int nodes_available;

  int _constraint_violations;
  number_t _infeasibility;
  number_t _lower_bound;
  int _lower_bound_constraint_violations;

  Solution (void) 
    : permutation (1,0), // Start at the depot.
      node_assigned(n, false),
      nodes_available (n),
      _constraint_violations (0),
      _infeasibility(0),
      _lower_bound (-1),
      _lower_bound_constraint_violations (-1),
      _makespan (n+1),
      _tourcost (0)
  {
    permutation.reserve (n+1);
    node_assigned[0] = true;
    nodes_available--;
  };

  // Named-Constructor
  static Solution * RandomSolution (Random *rnd);

  Solution * clone(void) { return new Solution(*this); };

  void print_verbose (FILE *) const;
  void print_one_line (void) const;
  void print_one_line (FILE *stream) const;

  // This can either return _makespan or _tourcost depending on the
  // macros MINIMISE_MAKESPAN and MINIMISE_TOURCOST.
  number_t cost() const;

  number_t makespan() const;
  int constraint_violations() const;
  number_t infeasibility() const { return _infeasibility; }
  bool check_solution() const;
    bool check_partial_solution(int max_node) const;
    void assert_solution() const;
  bool better_than (const Solution * other) const;
  bool better_than (const Solution & other) const { return better_than (&other); };
  void add(int current, int node);
  void add (const int p[]);
    // FIXME: Change this to a function pointer.
  Solution * localsearch (void) {
    switch (localsearch_type) {
    case LOCALSEARCH_FIRST: return localsearch_first();
    case LOCALSEARCH_BEST:  return localsearch_best();
    default: abort();
    }
  }
  Solution * localsearch_2opt_first (void);
  bool feasibility_1shift_first(void);
  bool feasibility_1shift_first(Random &rng);
    bool feasible_1shift_first();
    bool do_feasible_swap(int k, number_t &delta_cost, int &first_m);
    void shuffle_1shift_feasible_nodes(vector<int> &v, Random &rng);
    bool feasible_1shift_first(Random &rng);
    void compute_feas_set(vector<int> &feas, Random &rng);
    void compute_infeas_set(vector<int> &infeas, Random &rng);
    bool backward_violated(bool &improved, Random &rng);
    bool forward_nonviolated(bool &improved, Random &rng);
    bool forward_violated(bool &improved, Random &rng);
    bool backward_nonviolated(bool &improved, Random &rng);
    bool feasibility_1shift_first_code(Random &rng);
    bool feasibility_1shift_first_paper(Random &rng);
    bool insertion_is_feasible(int from, int to);
    void full_eval(void);
    bool ls_feasibility_1shift_first(void);
    bool ls_feasibility_1shift_first(Random &rng);
    void perturb_1shift(int level, Random &rng);
    void perturb_1shift_feasible(int level, Random &rng);
    int two_opt_is_infeasible(int h1, int h3);
    void two_opt_move(int h1, int h3);
    bool two_opt_first(void);
    bool two_opt_first(Random &rng);
    bool three_opt_first(Random &rng);

private:
  vector<number_t> _makespan;
  number_t _tourcost; // Sum of the traversal cost along the tour.

  static double dist_heuristic_weight;
  static double winstart_heuristic_weight;
  static double winend_heuristic_weight;
  static vector<vector<double> > heuristic_info;
#if DEBUG>=1
  static bool heuristic_info_ready;
#endif

  static void calculate_static_hinfo (void);
  static double weighted_hinfo (int prev, int next,
                                double dist_w,
                                double winstart_w,
                                double winend_w);

  // time-window start
  static vector<number_t> window_start;
  static number_t window_start_min, window_start_max;
  
  // time-window end
  static vector<number_t> window_end;
  static number_t window_end_min, window_end_max;

  // travel time/distance
  static vector<vector<number_t> > distance;
  static number_t distance_min, distance_max;

  static vector<vector<bool> > tw_infeasible;
  static int num_tw_infeasible;
  static void strong_time_window_infeasibility(void);

  bool inline infeasible_move (int initial, int final) const;
  void swap (int k);
  void insertion_move (int k, int i, int d);
  Solution * localsearch_insertion (bool first_improvement_p);
  Solution * localsearch_first (void) { return localsearch_insertion(true); };
  Solution * localsearch_best (void) { return localsearch_insertion(false); };
    number_t delta_swap(int k);
    bool is_feasible_swap(int k, int &first_m);
    number_t do_swap(int k);
};


inline double 
Solution::heuristic_information (int prev, int next)
{
  assert (Solution::heuristic_info_ready);

  return heuristic_info[prev][next];
  /*  return weighted_hinfo (prev, next, 
                         dist_heuristic_weight,
                         winstart_heuristic_weight,
                         winend_heuristic_weight);*/
}


/*
template<typename T_type>
typename T_type::iterator
random_wheel (Random * rnd, T_type &T, double basesum)
{
  double rand = rnd->next() * basesum;
  typename T_type::iterator it = T.begin();
  double wheel = it->second;

  while (wheel < rand) {
      it++;
      wheel += it->second;
      cerr << it->first << "  " << it->second << endl;
  }

  assert (it->first > 0);
  assert (it != T.end());
  
  return it;
}
*/

#endif
// Local Variables: 
// mode: c++; 
// End:
