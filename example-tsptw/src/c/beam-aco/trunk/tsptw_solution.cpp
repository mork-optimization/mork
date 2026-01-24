/*************************************************************************

 Travelling Salesman Problem with Time Windows

 ---------------------------------------------------------------------

                       Copyright (c) 2008-2014
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

/* FIXMEs: 

   - Too much use of temporary Solution variables. Undoing moves may
     avoid unnecessary copies. Alternatively, use lighter data
     structure for moves.

   - When optimizing feasibility, computing tour cost delta is a small waste.
*/

#include "tsptw_solution.h"

string Solution::instance;

// number of customers
int Solution::n = 0;

unsigned int Solution::evaluations = 0;

// time-window start
vector<number_t> Solution::window_start;
number_t Solution::window_start_min = NUMBER_T_MAX; 
number_t Solution::window_start_max = NUMBER_T_MIN; 

// time-window end
vector<number_t> Solution::window_end;
number_t Solution::window_end_min =  NUMBER_T_MAX;
number_t Solution::window_end_max =  NUMBER_T_MIN;

// travel time/distance
vector<vector<number_t> > Solution::distance;
number_t Solution::distance_min =  NUMBER_T_MAX; 
number_t Solution::distance_max =  NUMBER_T_MIN; 

vector<vector<double> > Solution::heuristic_info;

bool Solution::is_symmetric;

#if DEBUG>=1
bool Solution::heuristic_info_ready = false;
#endif

vector<vector<bool> > Solution::tw_infeasible;
int Solution::num_tw_infeasible = 0;

heuristic_type_t Solution::heuristic_type = EARLIEST_WINDOW_END;
localsearch_type_t Solution::localsearch_type = LOCALSEARCH_NONE;

double Solution::dist_heuristic_weight = 0.0;
double Solution::winstart_heuristic_weight = 0.0;
double Solution::winend_heuristic_weight = 0.0;

bool
Solution::set_heuristic_weights (char *arg)
{
  return strcmp (arg, "random") == 0;

  char *endp;

  double dist_w = strtod (arg, &endp);
  if (arg == endp) {
    fprintf(stderr, "error processing weights '%s'\n", arg);
    return false;
  }
  arg = endp;
  while (*arg != '\0' and *arg != '.' and !isdigit(*arg)) arg++;

  double winstart_w = strtod (arg, &endp);
  if (arg == endp) {
    fprintf(stderr, "error processing weights '%s'\n", arg);
    return false;
  }
  arg = endp;
  while (*arg != '\0' and *arg != '.' and !isdigit(*arg)) arg++;

  double winend_w = strtod (arg, &endp);
  if (arg == endp) {
    fprintf(stderr, "error processing weights '%s'\n", arg);
    return false;
  }

  double total = dist_w + winstart_w + winend_w;

  if (total < 1e-6) return true;

  dist_heuristic_weight = dist_w / total;
  winstart_heuristic_weight = winstart_w / total;
  winend_heuristic_weight = winend_w / total;

  return true;
}

string
Solution::get_heuristic_type(void)
{
  return "random";

  static char buf[100];
  sprintf (buf, "%.5f %.5f %.5f",
           dist_heuristic_weight,
           winstart_heuristic_weight,
           winend_heuristic_weight);
  return buf;
}

string
Solution::get_localsearch_type(void)
{
  switch (localsearch_type) {
    case LOCALSEARCH_NONE:  return "none";
    case LOCALSEARCH_FIRST: return "first";
    case LOCALSEARCH_BEST:  return "best";
    default: abort();
  }
}

void
Solution::calculate_static_hinfo(void)
{
  // Calculate normalisation bounds.
  for (int i = 0; i < n; i++) {
    if (window_start[i] > window_start_max)
      window_start_max = window_start[i];
    if (window_start[i] < window_start_min)
      window_start_min = window_start[i];
  }

  for (int i = 0; i < n; i++) {
    if (window_end[i] > window_end_max)
      window_end_max = window_end[i];
    if (window_end[i] < window_end_min)
      window_end_min = window_end[i];
  }

  for (int i = 0 ; i < n; i++) {
    for (int j = 0; j < n; j++) {
      if (i == j) continue;
      if (distance[i][j] > distance_max)
        distance_max = distance[i][j];
      if (distance[i][j] < distance_min)
        distance_min = distance[i][j];
    }
  }

  // Pre-compute heuristic_info.
  /*
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      heuristic_info[i][j] = weighted_hinfo (i, j,
                                             dist_heuristic_weight,
                                             winstart_heuristic_weight,
                                             winend_heuristic_weight);
    }
  }
  DEBUG1(Solution::heuristic_info_ready = true);
  */
 
}

double
Solution::weighted_hinfo (int prev, int next,
                          double dist_w, 
                          double winstart_w, 
                          double winend_w)
{
  if (prev == next) return 0.0;

  if (dist_w < 1e-6
      and winstart_w < 1e-6
      and winend_w < 1e-6)
    return 1.;

  /* ??? We cannot return zero here because this might be the only
     remaining option for an ant. It would be better to return zero
     here and if the ant does no have any option with higher than zero
     desirability, then choose between the options with zero
     desirability.  */
  //if (tw_infeasible[prev][next]) return 1e-6;

  /* Normalisation:

     [orig_min, orig_max] -> [nor_min, nor_max] :

     X' = nor_min + (nor_max - nor_min) * (X - orig_min)
                                        / (orig_max - orig_min)

     [orig_max, orig_min] -> [nor_min, nor_max] :

     X' = nor_min + (nor_max - nor_min) * (orig_max - X)
                                        / (orig_max - orig_min)

  */
#define nor_min  0.
#define nor_max  1.
#define NORMALISE_INV(ORIG,ORIG_MIN,ORIG_MAX)                      \
  (nor_min + (nor_max - nor_min)                                   \
   * (((ORIG_MAX) - (ORIG)) / (double)(ORIG_MAX - ORIG_MIN)))

  double h_dist =
    NORMALISE_INV (distance[prev][next], distance_min, distance_max);
  double h_win_start =
    NORMALISE_INV (window_start[next], window_start_min, window_start_max);
  double h_win_end =
    NORMALISE_INV (window_end[next], window_end_min, window_end_max);

#undef nor_min
#undef nor_max
#undef NORMALISE_INV

  double h_final = dist_w * h_dist 
    + winstart_w * h_win_start 
    + winend_w * h_win_end;

#if DEBUG > 2
    fprintf (stderr, "D[%3d][%3d] = %.2f,  Ws[%3d] = %.2f, We[%3d], = %.2f"
    "  H(D) = %.5f, H(Ws) = %.5f, H(We) = %.5f  H = %.5f\n",
             prev, next, (double)distance[prev][next],
    next, (double)window_start[next],
    next, (double)window_end[next],
    h_dist, h_win_start, h_win_end, h_final);
#endif

  return h_final;
}

void
Solution::randomize_hinfo (Random *rng)
{
  double dist_w = rng->next();
  double winstart_w = rng->next();
  double winend_w = rng->next();
  double total = dist_w + winstart_w + winend_w;

  if (total < 1e-6) {
    dist_heuristic_weight = 0.0;
    winstart_heuristic_weight = 0.0;
    winend_heuristic_weight = 0.0;
  }
  else  {
    dist_heuristic_weight = dist_w / total;
    winstart_heuristic_weight = winstart_w / total;
    winend_heuristic_weight = winend_w / total;
  }

  // Pre-compute heuristic_info.
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
      heuristic_info[i][j] = weighted_hinfo (i, j,
                                             dist_heuristic_weight,
                                             winstart_heuristic_weight,
                                             winend_heuristic_weight);
    }
  }

  DEBUG1 (Solution::heuristic_info_ready = true);

#if DEBUG >= 2
  fprintf (stderr, "weights: %.5f %.5f %.5f\n", 
           dist_heuristic_weight,
           winstart_heuristic_weight,
           winend_heuristic_weight);
#endif
}

static bool
matrix_is_symmetric(vector<vector<number_t> > &x, int n)
{
    for (int i = 0; i < n; i++) {
        for (int j = i+1; j < n; j++) {
            if (x[i][j] != x[j][i]) return false;
        }
    }
    return true;
}
#if 1
void
Solution::LoadInstance (string filename)
{
  ifstream indata;
  number_t rtime;
  number_t ddate;

  instance = filename;
  indata.open (instance.c_str());
  if (!indata) { // file couldn't be opened
    cerr << "error:LoadInstance(): file " << instance.c_str() << " could not be opened"
         << endl;
    exit (EXIT_FAILURE);
  }

  // Customer 0 is the depot.
  indata >> n;
  if (!indata || n <= 0) {
    cerr << "error:LoadInstance(): invalid number of customers" << endl;
    exit (EXIT_FAILURE);
  }

  window_start.reserve(n);
  window_end.reserve(n);
  distance.reserve(n);

  for (int i = 0 ; i < n; i++) {
    distance.push_back(vector<number_t>(n,0));
    for (int j = 0; j < n; j++) {
        indata >> distance[i][j];
    }
  }

  is_symmetric = matrix_is_symmetric(distance, n);
  
  if (!indata) {
      cerr << "error:LoadInstance(): invalid time windows (number_t ==" << STRING_NUMBER_IS << ")" << endl;
    exit (EXIT_FAILURE);
  }

  for (int i = 0; i < n; i++) {
    indata >> rtime >> ddate;
    window_start.push_back (rtime);
    window_end.push_back(ddate);
  }

  if (!indata) {
    cerr << "error:LoadInstance(): invalid distance matrix" << endl;
    exit (EXIT_FAILURE);
  }

  indata.close();

  strong_time_window_infeasibility();

  heuristic_info.reserve(n);
  for (int i = 0; i < n; i++)
    heuristic_info.push_back (vector<double>(n));

  calculate_static_hinfo ();
}
#else
void
Solution::LoadInstance (string filename)
{
  int k, s;
  char buffer[1024];
  ifstream indata;
  number_t rtime;
  number_t ddate;
  vector<number_t> service;

  instance = filename;
  indata.open (instance.c_str());
  if (!indata) { // file couldn't be opened
    cerr << "error: file " << instance.c_str() << " could not be opened"
         << endl;
    exit (EXIT_FAILURE);
  }

  // Customer 0 is the depot.
  do {
    indata.getline(buffer, 1024);
    //fprintf (stderr, "%s", buffer);
  } while (sscanf(buffer, "DIMENSION: %d", &n) != 1 && !indata.eof());

  assert (!indata.eof());


  n--;
  //  fprintf (stderr, "n = %d", n);
  window_start.reserve(n);
  window_end.reserve(n);
  distance.reserve(n);
  distance_s.reserve(n);
  service.reserve(n);

  indata.getline (buffer, 1024);
  indata.getline (buffer, 1024);
  indata.getline (buffer, 1024);

  for (int i = 0; i < n; i++) {
    indata.getline (buffer, 1024);
    sscanf (buffer, "%d : %d [%d, %d]", &k, &s, &rtime, &ddate);
    //    fprintf (stderr, "%d : %d [%d, %d]\n", k, s, rtime, ddate);
    service.push_back(s);
    window_start.push_back (rtime);
    window_end.push_back(ddate);
    distance.push_back(vector<number_t>(n,0));
    distance_s.push_back(vector<number_t>(n,0));
  }

  indata.getline (buffer, 1024);
  sscanf (buffer, "%d : %d [%d, %d]", &k, &s, &rtime, &ddate);
  //  fprintf (stderr, "%d : %d [%d, %d]\n", k, s, rtime, ddate);
  window_end[0] = ddate;

  indata.getline (buffer, 1024);

  for (int i = 0 ; i < n; i++) {
    int tmp;
    for (int j = 0; j < n; j++) {
      indata >> tmp;
      distance[i][j] = tmp;
      distance_s[i][j] = tmp;
      //      fprintf (stderr, " %2d+%-3d", distance[i][j], service[i]);
      if (i != j)
        distance[i][j] += service[i];
      //fprintf (stderr, " %3d", distance[i][j]);
    }
    indata >> tmp;
    //    fprintf (stderr, "\n");
  }
  indata.close();

  strong_time_window_infeasibility();

  heuristic_info.reserve(n);
  for (int i = 0; i < n; i++)
    heuristic_info.push_back (vector<double>(n));

  calculate_static_hinfo ();
}
#endif

void
Solution::strong_time_window_infeasibility()
{
  tw_infeasible.reserve(n);
  num_tw_infeasible = 0;
  
  for (int i = 0 ; i < n; ++i) {
    tw_infeasible.push_back (vector<bool>(n));
    for (int j = 0; j < n; ++j) {
      tw_infeasible[i][j] =  (window_start[i] + distance[i][j] > window_end[j])
          ? (++num_tw_infeasible, true) : false;
    }
  }
}

void
Solution::print_compile_parameters (FILE *stream)
{
#if defined(MINIMISE_TOURCOST) && defined(MINIMISE_MAKESPAN)
# error Both MINIMISE_TOURCOST and MINIMISE_MAKESPAN cannot be defined.
#elif defined(MINIMISE_TOURCOST)
  fprintf (stream, "TSPTW (minimise tourcost)");
#elif defined(MINIMISE_MAKESPAN)
  fprintf (stream, "TSPTW (minimise makespan)");
#else 
# error Either MINIMISE_TOURCOST or MINIMISE_MAKESPAN must be defined.
#endif

  fprintf (stream, " [%s values]", STRING_NUMBER_IS);
}

void
Solution::print_parameters (string prefix, FILE *stream)
{
  fprintf (stream, "%s Problem: ", prefix.c_str());
  print_compile_parameters (stream);
  fprintf (stream, "\n");

  fprintf (stream, "%s instance : %s\n", prefix.c_str(), instance.c_str());
  fprintf (stream, "%s n. customers + depot: %d\n", prefix.c_str(), n);
  fprintf (stream, "%s distances   : [%g, %g]\n", prefix.c_str(), (double) distance_min, (double) distance_max);
  fprintf (stream, "%s window_start: [%g, %g]\n", prefix.c_str(), (double) window_start_min, (double) window_start_max);
  fprintf (stream, "%s window_end  : [%g, %g]\n", prefix.c_str(), (double) window_end_min, (double) window_end_max);
  fprintf (stream, "%s n tw infeasible : %d (%g%%)\n", prefix.c_str(), num_tw_infeasible, 
           double(100.0*num_tw_infeasible/(n*n)));
  fprintf (stream, "%s symmetric : %s\n", prefix.c_str(), is_symmetric ? "true" : "false");
}

void Solution::print_one_line() const
{
  print_one_line (stdout);
}

void Solution::print_one_line (FILE *stream) const
{
  fprintf (stream, "%.2f\t%.2f\t%d\t%16g\t",
           double(makespan()), double(_tourcost), _constraint_violations, double(_infeasibility));

  // Customers 0 and n+1 are always the depot (permutation[0] == 0,
  // permutation[n] == 0), so do not print them.
#if 1
  for (int i = 1; i < int(permutation.size()) - 1; i++) {
    fprintf (stream, " %d", permutation[i]);
  }
#endif
  fprintf (stream, "\n");
}

static inline double min(int x, double y) {
  return min (double(x), y);
}

void Solution::print_verbose (FILE *stream = stdout) const
{
  number_t cost = 0;
  number_t mkspan = 0;
  number_t d;
  number_t departure;
  number_t waiting;
  
#define TSPTW_VERBOSE_PRINT(NODE1,NODE2, INDEX)                                \
  do {                                                                         \
      d = distance[(NODE1)][(NODE2)];                                          \
      cost += d;                                                               \
      departure = max (mkspan + d, window_start[(NODE2)]);                     \
      waiting = window_start[(NODE2)] - (mkspan + d);                          \
      waiting = waiting >= 0 ? waiting                                         \
          : min (0, window_end[(NODE2)] - departure);                          \
                                                                               \
      fprintf (stream, " D[%2d][%2d] = %7g, tour cost = %8.2f, "               \
               " window = [%7g, %7g], waiting = %8.2f, makespan = %8.2f "      \
               " [%8.2f]\n",                                                   \
               (NODE1), (NODE2),                                               \
               double(d), double(cost),                                        \
               (double)window_start[(NODE2)], (double)window_end[(NODE2)],     \
               (double) waiting, (double) departure, (double) _makespan[INDEX]); \
      mkspan = departure;                                                      \
  } while (0)
  

  for (int i = 1; i < n; i++) {
      TSPTW_VERBOSE_PRINT(permutation[i - 1], permutation[i], i);
  }

  TSPTW_VERBOSE_PRINT(permutation[n - 1], 0, n);

#undef TSPTW_VERBOSE_PRINT

  fprintf (stream, "\ntour cost = %.2f"
           "\tmakespan = %.2f\tconstraint_violations = %d\n",
           double (_tourcost), double(makespan()), _constraint_violations);
}

bool
Solution::better_than (const Solution * other) const
{
  return (_constraint_violations < other->_constraint_violations
          || (_constraint_violations == other->_constraint_violations
              && fless (cost(), other->cost())));
}

number_t Solution::makespan() const
{
  return _makespan[permutation.size() - 1];
}

number_t Solution::cost() const
{
#if defined(MINIMISE_TOURCOST) && defined(MINIMISE_MAKESPAN)
# error Both MINIMISE_TOURCOST and MINIMISE_MAKESPAN cannot be defined.
#elif defined(MINIMISE_TOURCOST)
  return _tourcost;
#elif defined(MINIMISE_MAKESPAN)
  return makespan();
#else 
# error Either MINIMISE_TOURCOST or MINIMISE_MAKESPAN must be defined.
#endif
}

int Solution::constraint_violations() const
{
  return _constraint_violations;
}

void Solution::add(int current, int node)
{
  assert (node > 0);
  assert (node != current);
  assert (!node_assigned[node]);

  permutation.push_back(node);
  int j = permutation.size() - 1;

  _makespan[j] = max (_makespan[j - 1] + distance[current][node],
                      window_start[node]);

  if (_makespan[j] > window_end[node]) {
      _infeasibility += _makespan[j] - window_end[node];
      _constraint_violations++;
  }

  _tourcost += distance[current][node];

  node_assigned[node] = true;
  nodes_available--;

  evaluations++;

  if (nodes_available == 1) {
    current = node;
    node = 0;
    while (node_assigned[++node]);
    add (current, node);
    // This is the last node, so connect it to the depot.
    permutation.push_back (0);
    _makespan[n] = _makespan[n-1] + distance[node][0];
    _tourcost +=  distance[node][0];
    if (_makespan[n] > window_end[0]) {
      _constraint_violations++;
      _infeasibility += _makespan[n] - window_end[0];
    }
    evaluations++;
  }

  /*
  fprintf (stderr, " nodes available: ");
  for (int i=1; i < n; i++) 
    if (!node_assigned[i]) 
      fprintf (stderr," %2d", i);
  fprintf(stderr, "\n");
  */
}

void Solution::add (const int * p)
{
  int last = permutation.back();
  int k = 0;

  while (nodes_available) {
    int node = p[k];
    add (last, node);
    last = node;
    ++k;
  }
}

void Solution::assert_solution() const
{
    if (!check_solution()) {
        print_verbose(); 
        abort();
    }
}

bool Solution::check_partial_solution(int max_node) const
{
  number_t mkspan = 0;
  number_t cost = 0;
  int prev = 0; // starts at the depot

  for (int i = 1; i < max_node; i++) {
    int node = permutation[i];

    cost += distance[prev][node];
    mkspan = max (mkspan + distance[prev][node], window_start[node]);
    if (!fequals (mkspan, _makespan[i])) {
      fprintf (stderr, "invalid: makespan = %g !=  _makespan[%d] = %g!\n",
              double(mkspan), i, double(_makespan[i]));
      return false;
    }
    
    if (_makespan[i] > window_end[node]) {
      fprintf (stderr, "n = %d, i = %d, node = %d\n", n, i, node);
      fprintf (stderr, "invalid: makespan[%d] = %g > tw_end[%d] = %g!\n",
              i, double(_makespan[i]), node, double(window_end[node]));
      return false;
    }
    prev = node;
  }
  return true;
}

bool Solution::check_solution() const
{
  number_t mkspan = 0;
  number_t cost = 0;
  int prev = 0; // starts at the depot
  int cviols = 0;
  int cviols_unsure = 0;
  number_t infeas = 0;

  if (int(permutation.size() - 1) != n) {
      fprintf (stderr, "invalid: (permutation.size() == %d) != (n == %d)\n",
               int(permutation.size() - 1), n);
      return false;
  }
  if (!is_a_permutation(permutation, permutation.begin(), permutation.end()-1)) {
      fprintf (stderr, "invalid: not a permutation!\n");
      return false;
  }

  for (int i = 1; i < n; i++) {
    int node = permutation[i];

    cost += distance[prev][node];
    mkspan = max (mkspan + distance[prev][node], window_start[node]);
    if (!fequals (mkspan, _makespan[i])) {
      fprintf (stderr, "invalid: makespan = %g !=  _makespan[%d] = %g!\n",
              double(mkspan), i, double(_makespan[i]));
      return false;
    }
    
    if (_makespan[i] > window_end[node]) {
      cviols++;
      infeas += _makespan[i] - window_end[node];
      /*
      fprintf (stderr, "n = %d, i = %d, node = %d\n", n, i, node);
      fprintf (stderr, "invalid: makespan[%d] = %g > tw_end[%d] = %g!\n",
              i, double(_makespan[i]), node, double(window_end[node]));
      */
    }
    if (fequals (_makespan[i], window_end[node]))
      cviols_unsure++;

    prev = node;
  }

  // finish at the depot
  cost += distance[prev][0];
  if (!fequals (cost, _tourcost)) {
    fprintf (stderr, "invalid: real cost = %g !=  _tourcost = %g!\n",
            double(cost), double(_tourcost));
    return false;
  }

  mkspan = max (mkspan + distance[prev][0], window_start[0]);
  if (!fequals (mkspan, _makespan[n])) {
    fprintf (stderr, "invalid: makespan = %g !=  _makespan[n] = %g!\n",
            double(mkspan), double(_makespan[n]));
    return false;
  }

  if (_makespan[n] > window_end[0]) {
      cviols++;
      infeas += _makespan[n] - window_end[0];
    /*
    fprintf (stderr, "n = %d, i = %d, node = %d\n", n, n, 0);
    fprintf (stderr, "invalid: makespan[%d] = %g > tw_end[%d] = %g!\n",
            n, double(_makespan[n]), 0, double(window_end[0]));
    */
  }
  if (fequals (_makespan[n], window_end[0]))
    cviols_unsure++;

  if (abs (cviols - _constraint_violations) > cviols_unsure) {
    fprintf (stderr, "invalid: real cviols = %d !=  _constraint_violations = %d  (unsure = %d) !\n",
            cviols, _constraint_violations, cviols_unsure);
    return false;
  } else if (_constraint_violations < 0) {
    fprintf (stderr, "invalid: _constraint_violations = %d  < 0 !\n",
            _constraint_violations);
    return false;
  }

  if (_infeasibility != infeas) {
      fprintf (stderr, "invalid: _infeasibility (%g) != real infeasibility (%g) !\n",
              double(_infeasibility), double(infeas));
      return false;
  }

  return true;
}

/* Exchange the customers at positions k and k+1.  */
void Solution::swap (int k)
{
  assert (k < n - 1);
  assert (k > 0);
  int a = permutation[k-1];
  int b = permutation[k];
  int c = permutation[k+1];

  do_swap(k);

#if DEBUG >= 3
  fprintf (stderr, "makespan:");
  for (int i = 0; i < n+1; i++)
    fprintf (stderr, " %g", (double)_makespan[i]);
  fprintf (stderr, "\n");
#endif

  // FIXME: If we are minimising the tour cost and if
  // constraint_violations == 0 and delta_cost > 0 then do not bother
  // to update the makespan. However, we would need to mark this
  // solution as "dirty" so the next iteration would know that the
  // makespan is up-to-date.

  if (_makespan[k] > window_end[b]) {
    _constraint_violations--;
    _infeasibility -= (_makespan[k] - window_end[b]);
  }
  if (_makespan[k+1] > window_end[c]) {
    _constraint_violations--;
    _infeasibility -= (_makespan[k+1] - window_end[c]);
  }

  _makespan[k] = max (_makespan[k-1] + distance[a][c], window_start[c]);
  _makespan[k+1] = max (_makespan[k] + distance[c][b], window_start[b]);

  if (_makespan[k] > window_end[c]) {
    _constraint_violations++;
    _infeasibility += (_makespan[k] - window_end[c]);
  }
  if (_makespan[k+1] > window_end[b]) {
    _constraint_violations++;
    _infeasibility += (_makespan[k+1] - window_end[b]);
  }

  auto mkspan = _makespan[k+1];
  int i, current, prev = b; // permutation[k+1]
  for (i = k + 2; i < n + 1; i++, prev = current) {
    current = permutation[i];
    
    /* There are some problems with rounding and check_solution()
       complains. Use 'volatile' to avoid optimizations here. This
       unfortunately may produce slower results.  */
    mkspan += distance[prev][current];

    if (_makespan[i] > window_start[current]) {
        // We did not have to wait ...
        if (_makespan[i] > window_end[current]) {
            // ... we broke a constraint before.
            _constraint_violations--;
            _infeasibility -= (_makespan[i] - window_end[current]);
        }
        if (mkspan <= window_start[current]) {
            // ... we now have to wait so everything changes.
            _makespan[i] = window_start[current];
            mkspan = window_start[current];
            continue;
        }
    } else {//(_makespan[i] <= window_start[current])
        // We had to wait before ...
        if (mkspan <= window_start[current]) {
            // ... we still have to wait and everything else stays the same.
            _makespan[i] = window_start[current];
            break;
        }
    }
    if (mkspan > window_end[current]) {
        // ... we do not wait but we break a constraint.
        _constraint_violations++;
        _infeasibility += (mkspan - window_end[current]);
    }
    _makespan[i] = mkspan;
  }
  evaluations += i - (k + 2);

#if DEBUG > 2
  fprintf (stderr, "makespan[%d]: ", _makespan.size());
  for (int i = 0; i < n+1; i++)
    fprintf (stderr, " %g", (double)_makespan[i]);
  fprintf (stderr, "\n\n");
#endif
}

Solution *
Solution::RandomSolution (Random *rng)
{
  Solution s;
  int * r = rng->generate_array (n-1);
  for (int k = 0; k < n - 1; k++)
    r[k]++;
  s.add (r);
  DEBUG1 (s.assert_solution());
  delete [] r;
  return s.clone();
}


/* "An insertion move corresponds to an Or-opt move of one customer who
   is removed form its current position and placed d positions later
   (d > 0) or earlier (d < 0) in the tour."

   William B. Carlton and J. Wesley Barnes, "Solving the
   traveling-salesman problem with time windows using tabu search",
   IIE Transactions, vol. 28, pp. 617--629, 1996.
*/
void
Solution::insertion_move (int k, int i __unused, int d __unused)
{
    DEBUG3 (fprintf (stderr, "# insertion: %2d:%2d:%2d: ", k, i, d));
//          print_one_line (stderr));
  swap (k);
  DEBUG1(if (!check_solution()) {
          fprintf (stderr, "swap(%2d, %2d, %2d)\n", k, i, d);
          print_verbose();
          abort();
      });
}

/* Is moving the customer at initial to final position going to change
   the feasibility fo this solution?  */
bool inline
Solution::infeasible_move(int initial, int end) const
{
  /* It depends whether it is strongly infeasible. Accepting strongly
     infeasible moves for infeasible solutions
     (!_constraint_violations && ...) provides a more thorough
     search, however, it is far slower.  */
    return tw_infeasible[permutation[end]][permutation[initial]];
}


/* FIXME: Add a first-improvement local search that does not return
   immediately but continues from best.  */
Solution *
Solution::localsearch_insertion (const bool first_improvement_p)
{
  Solution best = *this;
  Solution sol;
  /* for i=1 to n-1, we incrementally search all of the transitions
     that examine I(i,d) insertions of customer i, d positions later
     in the tour.  */
  for (int i = 1; i < n - 1; i++) {
    DEBUG2 (fprintf (stderr, "%2d:%2d: ", 0, 0); sol.print_one_line (stderr));
    bool move_p = this->infeasible_move (i, i+1);
    if (move_p) {
#if DEBUG >= 2
        sol = *this;
      /* See what would happen if we did the move.  */
      int cviols = sol._constraint_violations; 
      sol.print_one_line (stderr);
      /* I(i,1) == I(i+1, -1) == Swap(i) */
      sol.insertion_move (i, i, 1);
      if (sol.better_than (best)) {
        best = sol;
      }
      fprintf(stderr, "infeasible move: %d, %d, before = %d, after = %d!\n", 
              i, i+1, cviols, sol._constraint_violations);
      sol.print_one_line (stderr);
#endif
      continue;//goto next;
    }
    sol = *this;
    /* I(i,1) == I(i+1, -1) == Swap(i) */
    sol.insertion_move (i, i, i+1);
    if (sol.better_than (best)) {
      if (first_improvement_p)
        return sol.clone();
      best = sol;
    }
    
    Solution orb1 = sol;
    for (int d = i + 1; d < n - 1; d++) {
      move_p = sol.infeasible_move (d, d + 1);
      if (move_p) {
#if DEBUG >= 2
        /* See what would happen if we did the move.  */
        int cviols = sol._constraint_violations;
        sol.print_one_line (stderr);
        sol.insertion_move (d, i, (d+1) - i);
        if (sol.better_than (best)) {
          best = sol;
        }
        fprintf(stderr, "infeasible move2: %d, %d, before = %d, after = %d!\n", 
                d, d+1, cviols, sol._constraint_violations);
        sol.print_one_line (stderr);
#endif
        break;
      }

      /* I(i,d) == (Swap(i), Swap(i+1), ..., Swap(d - 1)) */
      sol.insertion_move (d, i, d+1); 
      if (sol.better_than (best)) {
        if (first_improvement_p)
          return sol.clone();
        best = sol;
      }
    }

    /* then for i = 2 to n, we incrementally examine all I(i,-d)
     insertions of customer i, d positions earlier in the tour for
     d >= 2.  */
    // FIXME: For i == 1, the below is never run because d = i - 1 == 0, so skip it */
    sol = orb1; /* I(i+1, -1) == I(i,1) == Swap(i) */

    // This is in fact swapping backwards i + 1
    for (int d = i - 1; d > 0; d--) {
      move_p = sol.infeasible_move (d, d + 1);
      if (move_p) {
#if DEBUG >= 2
        /* See what would happen if we did the move.  */
        int cviols = sol._constraint_violations;
        sol.print_one_line (stderr);
        sol.insertion_move (d, i+1, d - (i+1));
        if (sol.better_than (best)) {
          best = sol;
        }
        fprintf(stderr, "infeasible move3: %d, %d, before = %d, after = %d!\n", 
                d, d+1, cviols, sol._constraint_violations);
        sol.print_one_line (stderr);
#endif
        break;
      }

      sol.insertion_move (d, i + 1, d);
      if (sol.better_than (best)) {
        if (first_improvement_p)
          return sol.clone();
        best = sol;
      }
    }

//  next:
//   delete sol;
  }
#if DEBUG > 2
  this->print_one_line (stderr);
  best.print_one_line (stderr);
#endif
  return best.clone();
}


number_t Solution::delta_swap(int k)
{
    const std::vector<int> &p = this->permutation;

    int a = p[k-1];
    int b = p[k];
    int c = p[k+1];
    int d = p[k+2];

    number_t delta = (distance[a][c] + distance[c][b] + distance[b][d])
        - (distance[a][b] + distance[b][c] + distance[c][d]);

    evaluations += 6;
    DEBUG3(fprintf (stderr, "a = %2d, b = %2d, c = %2d, d = %2d  ", a, b, c, d);
           fprintf (stderr, "cost + delta = %g + %g = %g \t",
                    (double)_tourcost, (double)delta, (double)_tourcost + delta));
    return delta;
}
#define DEBUG_PRINT_MAKESPAN()                                                 \
    DEBUG3(fprintf (stderr, "makespan:");                                      \
           for (int i = 0; i < first_m; i++)                                   \
               fprintf (stderr, " %g", (double)_makespan[i]);                  \
           fprintf (stderr, "\n");                                             \
        )                                                                      
bool Solution::is_feasible_swap(int k, int &first_m)
{
    const std::vector<int> &p = this->permutation;
    // first_m == n + 1 means makespan is up-to-date, so we only have to update k, k + 1.
    // Otherwise, we have to update all.
    DEBUG1 (assert(check_partial_solution(min(first_m, k))));

    int last_m = (first_m == n + 1) ? k + 2 : n + 1;
    int j, prev, current;
    for (j = min (first_m, k); j < last_m; j++) {
        _makespan[j] = max (_makespan[j-1] + distance[p[j-1]][p[j]], window_start[p[j]]);
        if (_makespan[j] > window_end[p[j]]) {
            first_m = j;
            DEBUG1 (assert(check_partial_solution(first_m)));
            DEBUG_PRINT_MAKESPAN();
            return false;
        }
    }

    prev = p[last_m - 1];
    number_t mkspan = _makespan[last_m - 1];
    for (j = last_m; j < n + 1; j++, prev = current) {
        current = p[j];
        mkspan += distance[prev][current];
        // We had to wait before ...
        if (_makespan[j] <= window_start[current]) {
            if (mkspan <= window_start[current]) {
                // ... we still have to wait and everything else stays the same.
                _makespan[j] = window_start[current];
                first_m = n + 1;
                DEBUG_PRINT_MAKESPAN();
                return true;
            }
        } else {
            // We did not have to wait ...
            assert (_makespan[j] <= window_end[current]);
            // ... we did NOT break a constraint before.
            if (mkspan <= window_start[current]) {
                // ... we now have to wait so everything changes.
                _makespan[j] = window_start[current];
                mkspan = window_start[current];
                continue;
            }
        }
        if (mkspan > window_end[current]) {
            // ... we do not wait but we break a constraint.
            _makespan[j] = mkspan;
            first_m = j;
            DEBUG1 (assert(check_partial_solution(first_m)));
            DEBUG_PRINT_MAKESPAN();
            return false;
        }
        _makespan[j] = mkspan;
    }
    first_m = n + 1;
    DEBUG_PRINT_MAKESPAN();
    return true;
}

number_t Solution::do_swap(int k)
{
    std::vector<int> &p = this->permutation;
    number_t gain = delta_swap(k);
    _tourcost += gain;

    // Swap customers.
    int b = p[k];
    p[k] = p[k+1];
    p[k+1] = b;

    return gain;
}

bool
Solution::do_feasible_swap(int k, number_t &delta_cost, int &first_m)
{
    number_t gain = do_swap(k);
    delta_cost += gain;

    if (delta_cost >= 0) {
        // Since we don't check, all makespan between [k, n+1] will
        // be invalid from now on
        first_m = min(k, first_m);
        DEBUG_PRINT_MAKESPAN();
        return false;
    }
    return is_feasible_swap(k, first_m);
}

void
Solution::shuffle_1shift_feasible_nodes(vector<int> &v, Random &rng)
{
    v.clear();

    for (int i = 1; i < n - 1; i++) {
        int ci = permutation[i];
        int cj = permutation[i+1];
        if (tw_infeasible[cj][ci]) continue;
        v.push_back(i);
    }    
    std::random_shuffle(v.begin(), v.end(), rng);
}

bool
Solution::insertion_is_feasible(int from, int to)
{
    /* FIXME: Only copy the relevant makespan */
    vector<number_t> makespan = this->_makespan;
    int low = min(from, to);
    int high = max(from, to);
    number_t mkspan = this->_makespan[low - 1];
    int pred_ci = permutation[low - 1];
    int i, ci;
    if (from < to) {
        ci = permutation[low + 1];
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]){
            return false;
        }
        makespan[low] = mkspan;
        for (i = low + 2, pred_ci = ci;
             i <= high; 
             i++, pred_ci = ci) {
            ci = permutation[i];
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[i - 1] = mkspan;
        }
        pred_ci = permutation[high];
        ci = permutation[from];
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]) {
            return false;
        }
        makespan[high] = mkspan;
        pred_ci = permutation[low];

    } else /* from > to */{
        ci = permutation[from];
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]) {
            /* ??? In theory, if it was feasible before, so by the triangle inequality, it
               has to be feasible now, but this doesn't work for some reason.  */
            /* assert(mkspan <= window_end[ci]); */
            return false;
        }
        makespan[low] = mkspan;
        for (i = low, pred_ci = ci;
             i < high; 
             i++, pred_ci = ci) {
            ci = permutation[i];
            mkspan += distance[pred_ci][ci];
            if (mkspan < window_start[ci]) {
                mkspan = window_start[ci];
            } else if (mkspan > window_end[ci]) {
                return false;
            }
            makespan[i + 1] = mkspan;
        }
        pred_ci = permutation[high - 1];
    }
    for (i = high + 1;
         i < n + 1;
         i++, pred_ci = ci) {
        ci = permutation[i];
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]) {
            return false;
        }
        makespan[i] = mkspan;
    }
    // copy range affected from temp makespan to current solution.
    std::copy(makespan.begin() + low, makespan.end(), 
              this->_makespan.begin() + low);
    return true;
}

bool
Solution::feasible_1shift_first(Random &rng)
{
    assert (_constraint_violations == 0);
    Solution ngh;
    Solution saved_ngh;

    DEBUG2(fprintf(stderr, "# 1shift_feasible: START: ");
           this->print_one_line (stderr));

    vector<int> rand_nodes;
    shuffle_1shift_feasible_nodes(rand_nodes, rng);

    for (int k = 0; k < int(rand_nodes.size()); k++) {
        int i = rand_nodes[k];
        int ci = permutation[i];
        number_t delta1 = distance[permutation[i-1]][ci] 
            + distance[ci][permutation[i+1]]
            - distance[permutation[i-1]][permutation[i+1]];
        // This is in fact swapping backwards i + 1
        for (int d = i - 1; d > 0; d--) {
            int cj = permutation[d];
            if (tw_infeasible[ci][cj]) break;
            number_t delta2 = distance[permutation[d-1]][ci]
                + distance[ci][cj] - distance[permutation[d-1]][cj];
            
            if (delta2 >= delta1) continue;
            if (!insertion_is_feasible(i, d)) continue;
            reinsert(permutation, i, d);
            DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", i, i+1, 
                             double(this->_tourcost),
                             double(this->_tourcost - delta1 + delta2));
            this->_tourcost +=  delta2 - delta1;
            DEBUG1 (assert_solution());
            return true;
        }
        // This is in fact swapping forward i
        for (int d = i + 2; d < n; d++) {
            int cj = permutation[d];
            if (tw_infeasible[cj][ci]) break;
            number_t delta2 = distance[ci][permutation[d+1]]
                + distance[cj][ci] - distance[cj][permutation[d+1]];
            
            if (delta2 >= delta1) continue;
            if (!insertion_is_feasible(i, d)) continue;
            reinsert(permutation, i, d);
            DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", i, i+1, 
                             double(this->_tourcost),
                             double(this->_tourcost - delta1 + delta2));
            this->_tourcost += delta2 - delta1;
            DEBUG1 (assert_solution());
            return true;
        }
    }
    DEBUG2(fprintf(stderr, "# 1shift_feasible: END: ");
           this->print_one_line (stderr));
    return false;
}

// FIXME: This is almost the same as
// Solution::localsearch_insertion (const bool first_improvement_p)
// Merge them!
bool
Solution::feasible_1shift_first()
{
    assert (_constraint_violations == 0);
    Solution ngh;
    Solution back_ngh;
    bool improved = false;

    DEBUG2(fprintf(stderr, "# 1shift_feasible: START: ");
           this->print_one_line (stderr));

    for (int i = 1; i < n - 1; i++) {
        if (this->infeasible_move(i, i+1)) continue;
        ngh = *this;
        int first_m = n + 1;
        number_t delta_cost = 0;
        DEBUG2 (fprintf (stderr, "# insertion: %2d:%2d:%2d: ", i, i, i+1));
        if (ngh.do_feasible_swap(i, delta_cost, first_m)) {
            assert(delta_cost < 0);
            assert(this->_tourcost + delta_cost == ngh._tourcost);
            assert(first_m == n + 1);
            DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", i, i+1, 
                             double(this->_tourcost), double(ngh._tourcost));
            *this = ngh;
            DEBUG1 (assert_solution());
            return true;
            // delta_cost = 0;
            // improved = true;
        }
        // Save this move for the backwards insertion.
        back_ngh = ngh;
        int back_first_m = first_m;
        number_t back_delta_cost = delta_cost;
        // std::copy(permutation.begin(), permutation.end(), p);
        // std::copy(_makespan.begin(), _makespan.end(), makespan);
        for (int d = i + 1; d < n - 1; d++) {
            if (ngh.infeasible_move(d, d+1)) break;
            DEBUG2 (fprintf (stderr, "# insertion: %2d:%2d:%2d: ", d, i, d+1));
            if (ngh.do_feasible_swap(d, delta_cost, first_m)) {
                assert(delta_cost < 0);
                assert(this->_tourcost + delta_cost == ngh._tourcost);
                assert(first_m == n + 1);
                DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", i, d, 
                                 double(this->_tourcost), double(ngh._tourcost));
                *this = ngh;
                DEBUG1 (assert_solution());
                return true;
                // delta_cost = 0;
                // improved = true;
            }
        }

        delta_cost = back_delta_cost; //back_ngh._tourcost - this->_tourcost;
        // This is in fact swapping backwards i + 1
        for (int d = i - 1; d > 0; d--) {
            if (back_ngh.infeasible_move(d,d+1)) break;
            DEBUG2 (fprintf (stderr, "# insertion: %2d:%2d:%2d: ", d, i+1, d));
            if (back_ngh.do_feasible_swap(d, delta_cost, back_first_m)) {
                assert(delta_cost < 0);
                assert(this->_tourcost + delta_cost == back_ngh._tourcost);
                assert(back_first_m == n + 1);
                DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", i+1, d, 
                                 double(this->_tourcost), double(back_ngh._tourcost));
                *this = back_ngh;
                DEBUG1 (assert_solution());
                return true;
                // delta_cost = 0;
                // improved = true;
            }
        }
    }
    DEBUG2(fprintf(stderr, "# 1shift_feasible: END: ");
           this->print_one_line (stderr));
    return improved;
}

bool
Solution::ls_feasibility_1shift_first(void)
{
    bool improved = false;
    while (feasibility_1shift_first()) { improved = true; }
    return improved;
}

bool
Solution::ls_feasibility_1shift_first(Random &rng)
{
    bool improved = false;
    while (feasibility_1shift_first_code(rng)) { improved = true; }
    return improved;
}

void
Solution::compute_feas_set(vector<int> &feas, Random &rng)
{
    feas.clear();

    for (int i = 1; i < n; i++) {
        if (_makespan[i] <= window_end[i]) feas.push_back(i);
    }
    std::random_shuffle(feas.begin(), feas.end(), rng);
}

void
Solution::compute_infeas_set(vector<int> &infeas, Random &rng)
{
    infeas.clear();

    for (int i = 1; i < n; i++) {
        if (_makespan[i] > window_end[i]) infeas.push_back(i);
    }
    std::random_shuffle(infeas.begin(), infeas.end(), rng);
}

bool
Solution::backward_violated(bool &improved, Random &rng)
{
    vector<int> infeas;
    infeas.reserve(n - 1);
    compute_infeas_set(infeas, rng);
    
    Solution sol;
    // Backward movements of violated customers.
    do {
        int  i = infeas.back();
        infeas.pop_back();
        assert (_makespan[i] > window_end[i]);
        sol = *this;
        bool moved = false;
        for (int d = i - 1; d > 0; d--) {
            if (sol.infeasible_move(d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                moved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: BV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (sol.infeasibility() == 0) return true;
            }
        }
        if (moved) {
            compute_infeas_set(infeas, rng);
        }
    } while (infeas.size());

    return false;
}

bool
Solution::forward_nonviolated(bool &improved, Random &rng)
{
    vector<int> feas;
    compute_feas_set(feas, rng);
    
    Solution sol;
    // Forward movements of non-violated customers.
    do {
        int i = feas.back();
        feas.pop_back();
        assert (_makespan[i] <= window_end[i]);
        sol = *this;
        bool moved = false;
        for (int d = i; d < n - 1; d++) {
            if (sol.infeasible_move (d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                moved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: FNV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (this->infeasibility() == 0) return true;
            }
        }
        if (moved) {
            compute_feas_set(feas, rng);
        }
    } while (feas.size());
    return false;
}

bool
Solution::forward_violated(bool &improved, Random &rng)
{
    vector<int> infeas;
    compute_infeas_set(infeas, rng);
    
    Solution sol;
    // Forward movements of violated customers.
    do {
        int i = infeas.back();
        infeas.pop_back();
        assert (_makespan[i] > window_end[i]);
        sol = *this;
        bool moved = false;
        for (int d = i; d < n - 1; d++) {
            if (sol.infeasible_move (d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                moved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: FV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (this->infeasibility() == 0) return true;
            }
        }
        if (moved) {
            compute_infeas_set(infeas, rng);
        }

    } while(infeas.size());
    return false;
}

bool
Solution::backward_nonviolated(bool &improved, Random &rng)
{
    vector<int> feas;
    compute_feas_set(feas, rng);
    
    Solution sol;

    // Backward movements of non-violated customers.
    do {
        int i = feas.back();
        feas.pop_back();
        assert (_makespan[i] <= window_end[i]);
        sol = *this;
        bool moved = false;
        for (int d = i - 1; d > 0; d--) {
            if (sol.infeasible_move (d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                moved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: FNV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (sol.infeasibility() == 0) return true;
            }
        }
        if (moved) {
            compute_feas_set(feas, rng);
        }
    } while (feas.size());
    return false;
}

bool
Solution::feasibility_1shift_first_code(Random &rng)
{
    if (_constraint_violations == 0) return false;
    DEBUG2(fprintf(stderr, "# feas_1shift: START: ");
           this->print_one_line (stderr));

    bool improved = false;

    if (backward_violated(improved, rng)) return true;
    if (forward_nonviolated(improved, rng)) return true;
    if (forward_violated(improved, rng)) return true;
    if (backward_nonviolated(improved, rng)) return true;

    DEBUG2(fprintf(stderr, "# feas_1shift: END: ");
           this->print_one_line (stderr));

    return improved;
}

bool
Solution::feasibility_1shift_first_paper(Random &rng)
{
    if (_constraint_violations == 0) return false;
    DEBUG2(fprintf(stderr, "# feas_1shift: START: ");
           this->print_one_line (stderr));

    bool improved = false;

    if (backward_violated(improved, rng)) return true;
    if (forward_nonviolated(improved, rng)) return true;
    if (backward_nonviolated(improved, rng)) return true;
    if (forward_violated(improved, rng)) return true;

    DEBUG2(fprintf(stderr, "# feas_1shift: END: ");
           this->print_one_line (stderr));

    return improved;
}

bool
Solution::feasibility_1shift_first()
{
    if (_constraint_violations == 0) return false;
    DEBUG2(fprintf(stderr, "# feas_1shift: START: ");
           this->print_one_line (stderr));

    bool improved = false;

    Solution sol = *this;
    // Backward movements of violated customers.
    for (int i = 2; i < n; i++) {
        if (_makespan[i] <= window_end[i]) continue;
        sol = *this;
        for (int d = i - 1; d > 0; d--) {
            if (sol.infeasible_move(d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: BV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (sol.infeasibility() == 0) return true;
            }
        }
    }

    for (int i = 1; i < n - 1; i++) {
        if (_makespan[i] > window_end[i]) continue;
        if (infeasible_move (i, i+1)) continue;
        sol = *this;
        // Forward movements of non-violated customers.
        sol.swap(i);
        if (sol.infeasibility() < this->infeasibility()) {
            *this = sol;
            improved = true;
            DEBUG2(fprintf(stderr, "# feas_1shift: FNV: %d: %d: ", i, i+1);
                   this->print_one_line (stderr));
            DEBUG1(assert_solution());
            if (sol.infeasibility() == 0) return true;
        }
        Solution back_sol = sol;

        for (int d = i + 1; d < n - 1; d++) {
            if (sol.infeasible_move (d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: FNV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (sol.infeasibility() == 0) return true;
            }
        }

        // Backward movements of non-violated customers.
        for (int d = i - 1; d > 0; d--) {
            if (back_sol.infeasible_move(d, d + 1)) break;
            back_sol.swap(d);
            if (back_sol.infeasibility() < this->infeasibility()) {
                *this = back_sol;
                improved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: BNV: %d: %d: ", i + 1, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (this->infeasibility() == 0) return true;
            }
        }
    }

    // Forward movements of violated customers.
    for (int i = 1; i < n - 1; i++) {
        if (_makespan[i] <= window_end[i]) continue;
        sol = *this;
        for (int d = i; d < n - 1; d++) {
            if (sol.infeasible_move (d, d + 1)) break;
            sol.swap(d);
            if (sol.infeasibility() < this->infeasibility()) {
                *this = sol;
                improved = true;
                DEBUG2(fprintf(stderr, "# feas_1shift: FV: %d: %d: ", i, d);
                       this->print_one_line (stderr));
                DEBUG1(assert_solution());
                if (this->infeasibility() == 0) return true;
            }
        }
    }
    DEBUG2(fprintf(stderr, "# feas_1shift: END: ");
           this->print_one_line (stderr));

    return improved;
}

/* The tour to check is:

   [H1] -> [H3] -> B -> ... A -> [H1 + 1] -> [H3 + 1]
*/
int
Solution::two_opt_is_infeasible(int h1, int h3)
{
    /* FIXME: Only copy the relevant makespan */
    vector<number_t> makespan = this->_makespan;
    int mkspan = this->_makespan[h1];
    // Check feasibility of the new edge
    int pred_ci = permutation[h1];
    int ci = permutation[h3];
    mkspan += distance[pred_ci][ci];
    if (mkspan < window_start[ci]) {
        mkspan = window_start[ci];
    } else if (mkspan > window_end[ci]) {
        return 1;
    }
    makespan[h1 + 1] = mkspan;
    int i = h3 - 1;
    int j = h1 + 2;
    pred_ci = permutation[h3];
    // Check feasibility of the reversed part.
    // i moves back in the reversed part, j moves forward in makespan.
    while (i >= h1 + 1) {
        ci = permutation[i];
        mkspan += distance[pred_ci][ci];
        if (mkspan < window_start[ci]) {
            mkspan = window_start[ci];
        } else if (mkspan > window_end[ci]) {
            return 2; /* This is infeasible and all similar moves will be as well */
        }
        pred_ci = ci;
        makespan[j] = mkspan;
        i--, j++;
    }
    
    // Check feasibility of the rest
    for (i = h3 + 1, pred_ci = permutation[h1 + 1];
         i < n + 1;
         i++, pred_ci = ci) {
        ci = permutation[i];
        mkspan += distance[pred_ci][ci];
        // We had to wait before ...
        if (makespan[i] <= window_start[ci]) {
            if (mkspan <= window_start[ci]) {
                // ... we still have to wait and everything else stays the same.
                makespan[i] = window_start[ci];
                i++;
                break;
            }
        } else {// We did not have to wait ...
            if (mkspan <= window_start[ci]) {
                // ... we now have to wait so everything changes.
                mkspan = makespan[i] = window_start[ci];
                continue;
            }
        }
        if (mkspan > window_end[ci]) {
            // ... we do not wait but we break a constraint.
            return 1; // If we moved ci earlier, it could be feasible.
            break;
        }
        makespan[i] = mkspan;
    }
    // i is now one after the last affected.
    // copy range affected from temp makespan to current solution.
    std::copy(makespan.begin() + h1 + 1, makespan.begin() + i, 
              this->_makespan.begin() + h1 + 1);
    return 0;
}

void
Solution::two_opt_move(int h1, int h3)
{
    /* reverse inner part from h1+1 to h3 */
    std::reverse(permutation.begin() + h1 + 1,
                 permutation.begin() + h3 + 1);
    /* Equivalent to:
    for (int i = h1 + 1, j = h3; i < j; i++, j--) {
        int ci = permutation[i];
        int cj = permutation[j];
        permutation[i] = cj;
        permutation[j] = ci;
    }
    */
}

/*
  FIXME: Implement just this move, which works for asymmetric instances:
  
  c1 - s1 - A - c2 - s2 - B - c3 - s3 - C =>
  c1 - s2 - B - c3 - s1 - A - c2 - s3 - C

*/
bool Solution::three_opt_first(Random &rng)
{
    assert(false);
    assert(_constraint_violations == 0);
    bool improved = false;
/*    vector<int> rand_nodes = rng.generate_vector(n);
    int c1, c2, c3;
    int s1, s2, s3;
    int p1, p2, p3;

    while (!rand_nodes.empty()) {
    }
*/
    return improved;
}
 
/* Explore moves of the type:

    C1 - S1 - A - ... B - C2 - S2 => C1 - C2 - B - ... - A - S1 - S2

*/
bool
Solution::two_opt_first (Random &rng)
{
    assert (is_symmetric);
    assert(_constraint_violations == 0);

    bool improved = false;
    vector<int> rand_nodes = rng.generate_vector(n);
    
    while (!rand_nodes.empty()) {
        int pos_c1 = rand_nodes.back();
        rand_nodes.pop_back();
        int c1 = permutation[pos_c1];
        int s1 = permutation[pos_c1 + 1];
        number_t radius = distance[c1][s1];

        for (int h = pos_c1 + 2; h < n; h++) {
            int pos_c2 = h;
            int c2 = permutation[pos_c2];
            if (tw_infeasible[c2][s1]) break;
            int s2 = permutation[h + 1];
            number_t gain = distance[c1][c2] + distance[s1][s2]
                - radius - distance[c2][s2];
            if (gain >= 0) continue;

            int infeas = two_opt_is_infeasible (pos_c1, pos_c2);
            if (infeas == 2) {
                break;
            } else if (infeas == 1) {
                continue;
            } else {
                assert(infeas == 0);
                two_opt_move(pos_c1, pos_c2);
                DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", pos_h2, pos_h3,
                                 _tourcost, _tourcost + gain);
                _tourcost += gain;
                DEBUG1 (assert_solution());
                improved = true;
                rand_nodes = rng.generate_vector(n);
                break;
            }
        }
    }
    return improved;
}

// FIXME: two_opt_first(Random) seems a better implementation.
bool
Solution::two_opt_first (void)
{
    assert (is_symmetric);
    if (_constraint_violations > 0) return false;

    bool improved = false;

    int h1, h2, h3, h4;
    int c2, s2;

    std::vector<number_t> makespan = _makespan;

    for (int pos_c1 = 0; pos_c1 < n; pos_c1++) {
        int c1 = permutation[pos_c1];
        int s1 = permutation[pos_c1 + 1];
        number_t radius = distance[c1][s1];
        for (int h = pos_c1 + 2; h < n; h++) {
            c2 = permutation[h];
            s2 = permutation[h + 1];
            if (tw_infeasible[c2][s1]) break;
            if (radius <= distance[c1][c2]) continue;
            number_t gain = - radius + distance[c1][c2] 
                + distance[s1][s2] - distance[c2][s2];
            if (gain >= 0) continue;
            h1 = c1; h2 = s1; h3 = c2; h4 = s2;
            int pos_h2 = pos_c1 + 1;
            int pos_h3 = h;
            int pos_h4 = h + 1;
            int i = pos_h2;
            int j = pos_h3; 
            int ci, cj;
            int pred_cj = c1;
            makespan = _makespan;
            bool infeasible = false;
            int mkspan = _makespan[pos_c1];
            // Check feasibility of the reversed part.
            while (j >= pos_h2) {
                cj = permutation[j];
                /* We don't need to check this because the solution is assumed to be feasible
                   if (_neighbor.solution._makespan[j] > window_end[cj])
                   delta_constraint_violations--;
                */
                mkspan = max (mkspan + distance[pred_cj][cj], window_start[cj]);
                if (mkspan > window_end[cj]) {
                    infeasible = true;
                    break;
                }
                pred_cj = cj;
                makespan[i] = mkspan;
                j--; i++;
            }
            if (infeasible) break;
            // Check feasibility of the rest
            for (j = pos_h4, cj = h4, pred_cj = h2;
                 j < n + 1;
                 j++, pred_cj = cj) {
                cj = permutation[j];
                mkspan += distance[pred_cj][cj];
                // We had to wait before ...
                if (makespan[j] <= window_start[cj]) {
                    if (mkspan <= window_start[cj]) {
                        // ... we still have to wait and everything else stays the same.
                        makespan[j] = window_start[cj];
                        j++;
                        break;
                    }
                } else {// We did not have to wait ...
                    /*
                      if (makespan[j] > window_end[cj])
                      // ... we broke a constraint before.
                      _constraint_violations--;
                    */
                    
                    if (mkspan <= window_start[cj]) {
                        // ... we now have to wait so everything changes.
                        mkspan = makespan[j] = window_start[cj];
                        continue;
                    }
                }
                if (mkspan > window_end[cj]) {
                    // ... we do not wait but we break a constraint.
                    infeasible = true;
                    break;
                }
                makespan[j] = mkspan;
            }
            int last_pos = j;

            if (infeasible) break;

            /* reverse inner part from pos_h2 to pos_h3 */
            i = pos_h2; j = pos_h3;
            while (i < j) {
                ci = permutation[i];
                cj = permutation[j];
                permutation[i] = cj;
                permutation[j] = ci;
                i++; j--;
            }
            DEBUG2_FUNPRINT ("improved (%d, %d): %g -> %g\n", pos_h2, pos_h3,
                             _tourcost, _tourcost + gain);
            _tourcost += gain;
            // copy range affected from temp makespan to current solution.
            std::copy(makespan.begin() + pos_h2, makespan.begin() + last_pos, 
                      this->_makespan.begin() + pos_h2);
            DEBUG1 (assert_solution());
            s1 = permutation[pos_c1 + 1];
            radius = distance[c1][s1];
            improved = true;
        }
    }
    return improved;
}

Solution *
Solution::localsearch_2opt_first (void)
{
    Solution * s = this->clone();
    s->two_opt_first();
    return s;
}


// FIXME: This a bit of a hack. Do a partial evaluation.
void Solution::full_eval(void)
{
    Solution tmp;
    std::vector<int> sub(this->permutation.begin() + 1, this->permutation.end() - 2);
    tmp.add(&sub.front());
    *this = tmp;
}

void
Solution::perturb_1shift_feasible(int level, Random &rng)
{
    assert (_constraint_violations == 0);
    assert(level > 0);
    int num = min(n, level);
    DEBUG2_PRINT("# perturb_insert: %d (%d%%)\n", num, percent);

    std::vector<int> index;
    index.resize(n - 2);
    for (int k = 0; k < n - 2; k++) {
        index[k] = k + 1;
    }

    Solution ngh;

    for (int j = n - 3; j >= 0; j--) {
        // Knuth shuffle
        int k = rng.rand_int (j + 1);
        std::swap(index[k], index[j]);
        k = index[j];
        assert(k > 0 && k < n);
        int pos = 1 + rng.rand_int (n - 3);
        if (pos == k) continue;

        DEBUG3_PRINT("%d -> [ %d, %d] = %d\n", k, 1, permutation.size() - 2, pos);
        DEBUG3(fprintf(stderr,"Before:");
               for(int _j = 1; _j <= permutation.size() - 2; _j++) {
                   fprintf(stderr, " %2d", permutation[_j]);
               }
               fprintf(stderr, "\n"));
        
        ngh = *this;
        number_t delta_cost = 0;
        int first_m = n + 1;
        // FIXME: the bodies of the for-loops are the same, merge them.
        if (k < pos) { // Forward
            for (int d = k; d < pos; d++) {
                int ci = ngh.permutation[d];
                int cj = ngh.permutation[d+1];
                if (tw_infeasible[cj][ci]) break;
                delta_cost += ngh.do_swap(d);
                if (ngh.is_feasible_swap(d, first_m)) {
                    assert(ngh.constraint_violations() == 0);
                    assert(this->_tourcost + delta_cost == ngh._tourcost);
                    assert(first_m == n + 1);
                    *this = ngh;
                    DEBUG1 (assert_solution());
                    delta_cost = 0;
                }
            }
        } else { // Backward
            for (int d = k - 1; d >= pos; d--) {
                int ci = ngh.permutation[d];
                int cj = ngh.permutation[d+1];
                if (tw_infeasible[cj][ci]) break;
                delta_cost += ngh.do_swap(d);
                if (ngh.is_feasible_swap(d, first_m)) {
                    assert(ngh.constraint_violations() == 0);
                    assert(this->_tourcost + delta_cost == ngh._tourcost);
                    assert(first_m == n + 1);
                    *this = ngh;
                    DEBUG1 (assert_solution());
                    delta_cost = 0;
                }
            }
        }
        DEBUG3(fprintf(stderr, "After :");
               for(int _j = 1; _j <= permutation.size() - 2; _j++) {
                   fprintf(stderr, " %2d", permutation[_j]);
               }
               fprintf(stderr, "\n"));
        num--;
        if (num == 0) break;
    }
    
    DEBUG1(assert_solution());
}

void
Solution::perturb_1shift(int level, Random &rng)
{
    assert(level > 0);
    int num = min(n, level);
    DEBUG2_PRINT("# perturb_insert: %d (%d)\n", num, level);

    int earliest = n;
    do {
        int k = 1 + rng.rand_int (n - 1);
        int pos;
        do {
            pos = 1 + rng.rand_int (n - 1);
        } while (pos == k);

        earliest = min(earliest, min(pos, k));
        DEBUG3_PRINT("%d -> [ %d, %d] = %d\n", k, 1, permutation.size() - 2, pos);
        DEBUG3(fprintf(stderr,"Before:");
               for(int _j = 1; _j <= permutation.size() - 2; _j++) {
                   fprintf(stderr, " %2d", permutation[_j]);
               }
               fprintf(stderr, "\n"));
        
        int tmp = permutation[k];
        reinsert(permutation, tmp, k, pos);
        DEBUG3(fprintf(stderr, "After :");
               for(int _j = 1; _j <= permutation.size() - 2; _j++) {
                   fprintf(stderr, " %2d", permutation[_j]);
               }
               fprintf(stderr, "\n"));
        num--;
    } while (num > 0);
    
    if (earliest < n) {
        // FIXME: for large instances it would be better to do the
        // evaluation incrementally from earliest to n
        full_eval();
        DEBUG1(assert_solution());
    }
}
