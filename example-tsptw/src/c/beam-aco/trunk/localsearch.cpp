/*************************************************************************

 Beam-ACO

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

 ----------------------------------------------------------------------

 Relevant literature:

*************************************************************************/

#ifdef HAVE_CONFIG_H
#include <misc-math.h>
#endif

#include <cerrno>
// Requires #define _GNU_SOURCE
extern char * program_invocation_short_name;

#include "Random.h"
#include "Timer.h"
#include "tsptw_solution.h"
#include <string>
#include <cstring>
#include <list>
#include <vector>
#include <climits>

unsigned int random_seed;

// the following five variables are involved in termination criteria issues
int n_of_iter = INT_MAX;
double time_limit = DBL_MAX;

// n_of_trials: the number of trials that is to be executed for the
// given problem instance
int n_of_trials = 1;

// rnd: a random generator
Random* rnd;

// variable that holds the name of the input file
string input_filename;

FILE *trace_stream;
string trace_filename;

Solution* iteration_best = NULL;
Solution* best_so_far = NULL;

#include "common.h"

void usage(void)
{
  printf("\n"
         "Usage: %s -i FILE [OPTIONS]\n\n", program_invocation_short_name);

    printf(
"Description.\n"
"\n\n"

"Options:\n"
" -h, --help          print this summary and exit.                          \n"
" -v, --version       print version number and exit.                        \n"
" -i, --input   FILE  instance file.                                        \n"
" -s, --seed          random seed.                                          \n"
" -t, --time    REAL  time limit of each trial (seconds).                   \n"
" -n, --iterations INT number of iterations per trial.                      \n"
" -T, --trace   FILE  trace file.                                           \n"
" -r, --trials  INT   number of trials to be run on one instance.           \n"
"\n");
}

void version(void)
{
  printf ("%s", program_invocation_short_name);
#ifdef VERSION
  printf (" version %s", VERSION);
#endif
#ifdef MARCH
  printf (" (optimised for %s)", MARCH);
#endif

  printf("\n\n"
"Copyright (C) 2008\n"
"Christian Blum (christian.blum@ehu.es) and\n"
"Manuel Lopez-Ibanez (manuel.lopez-ibanez@manchester.ac.uk)\n"
"\n"
"This is free software, and you are welcome to redistribute it under certain\n"
"conditions.  See the GNU General Public License for details. There is NO   \n"
"warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n"
"\n"        );
}

void read_parameters(int argc, char **argv)
{
  for (int iarg = 1; iarg < argc; iarg++)   {

    if (strequal (argv[iarg],"-i")
        || strequal (argv[iarg],"--input")) {
      input_filename = argv[++iarg];
    }
    else if (strequal (argv[iarg],"-T")
        || strequal (argv[iarg],"--trace")) {
      trace_filename = argv[++iarg];
    }
    else if (strequal (argv[iarg],"-v")
             || strequal (argv[iarg],"--version")) {
      version ();
      exit (0);
    }
    else if (strequal (argv[iarg],"-h")
             || strequal (argv[iarg],"--help")) {
      usage ();
      exit (0);
    }
    else if (strequal (argv[iarg],"-t")
             || strequal (argv[iarg],"--time")) {
      time_limit = atof (argv[++iarg]);
    }
    else if (strequal(argv[iarg],"--iterations")
             || strequal(argv[iarg],"-n")) {
      n_of_iter = atoi (argv[++iarg]);
    }
    else if (strequal (argv[iarg],"--trials")
             || strequal (argv[iarg],"-r")) {
      n_of_trials=atoi(argv[++iarg]);
    }
    else if (strequal (argv[iarg], "-s")
             || strequal (argv[iarg], "--seed")) {
      random_seed = atoi (argv[++iarg]);
    }
    else {
      printf ("error: unknown parameter: %s\n", argv[iarg]);
      printf ("use --help for usage.\n");
      exit (1);
    }
  }

  if (input_filename.empty()) {
    printf ("error: no input file given (use parameter %s|%s).\n",
            "-i", "--input");
    exit(1);
  }

  if (trace_filename.empty()) 
    trace_stream = stderr;
  else if (NULL == (trace_stream = fopen (trace_filename.c_str(), "w"))) {
    printf ("error: trace file %s cannot be opened", trace_filename.c_str());
    exit (1);
  }

  if (time_limit == DBL_MAX && n_of_iter == INT_MAX) {
    printf ("error: no time limit or number of interations given."
            " Please specify:\n\n"
            " * a time limit in seconds (e.g., --time 20), or\n"
            " * an iteration limit (e.g., -maxiter 1000), or\n"
            " * both.\n");
    exit(1);
  }
}

static void 
print_trace_header (void)
{
  fprintf (trace_stream, "# Trial Iteration     Cost  Cviols     Time\n");
}

static void 
print_trace (Solution *s, int trial_counter, int iter, double time_taken)
{
  fprintf (trace_stream, "%7d %9d %8.2f  %6d  %8g\n", 
           trial_counter, iter,
           double(s->cost()), s->constraint_violations(), time_taken);
  //s->print_one_line(trace_stream);
}

static void
trial_begin (int trial_counter)
{
  printf ("# begin try %d\n", trial_counter);
  print_trace_header();
}

static void
trial_end (int trial_counter, int iter, double time_elapsed)
{
  printf("%.2f\t%.1f\t", double(best_so_far->cost()), time_elapsed);
  best_so_far->print_one_line();
  printf("#end try %d (Evaluations = %u, Iterations = %d, Time = %.1f)\n",
         trial_counter, Solution::evaluations, iter,
         time_elapsed);
}


void check_valid (Solution *s, string ok __unused, string fail)
{
  if (s->check_solution()) {
#if DEBUG > 2
    cerr << ok << endl;
    s->print_verbose (stderr);
#endif
  } else {
    cerr << fail << endl;
    s->print_verbose (stderr);
  }
}

static void print_parameters (void)
{
  printf ("# Localsearch ");
  printf ("%s", program_invocation_short_name);
#ifdef VERSION
  printf (" version %s", VERSION);
#endif
#ifdef MARCH
  printf (" (optimised for %s)", MARCH);
#endif
  printf ("\n#\n");


  printf ("# instance : %s\n", input_filename.c_str());
  printf ("# number trials : %d\n", n_of_trials);
  printf ("# number iterations : %d\n", n_of_iter);
  printf ("# time limit : %g\n", time_limit);
  printf ("# seed : %u\n", random_seed);

  printf ("#\n");

  printf ("\n");
}

#define update_best_so_far()                                    \
do {                                                            \
  delete best_so_far;                                           \
  best_so_far = iteration_best->clone();                        \
  time_taken = timer.elapsed_time(Timer::VIRTUAL);              \
                                                                \
  print_trace (best_so_far, trial_counter, iter, time_taken);   \
                                                                \
  DEBUG1 (check_valid (best_so_far, "best_so_far is valid",     \
                       "best_so_far is NOT valid"));            \
                                                                \
  results[trial_counter-1] = best_so_far->cost();               \
  viols[trial_counter-1] = best_so_far->constraint_violations();\
  times_best_found[trial_counter-1] = time_taken;               \
  iter_best_found[trial_counter-1] = iter;                      \
} while(0)

/* 'main' is the main body of the program */

int main( int argc, char **argv )
{
  // a variable that is involved in initializing the random generator
  random_seed = (unsigned) time(NULL);

  read_parameters (argc,argv);

  cout.precision (10);

  // initialization of the random generator
  rnd = new Random (random_seed);
  rnd->next();

  Solution::LoadInstance (input_filename);

  print_parameters ();

  /* The following variables are for collecting statistics on several
     trials.  */
  Solution* best = NULL;
  vector<double> results;
  vector<double> viols;
  vector<double> times_best_found;
  vector<int> iter_best_found;

  /* The following for loop is for controlling the number of trials as
     specified by command line parameters.  */
  for (int trial_counter = 1; trial_counter <= n_of_trials; trial_counter++) {

    trial_begin (trial_counter);

    // upon declaration of a variable of type 'Timer' the time is running ...
    Timer timer;
    // 'iter' is the iteration counter
    int iter = 1;
    double time_taken = 0.0;
    Solution::evaluations = 0;

    if (best_so_far != NULL)
      delete best_so_far;
    best_so_far = NULL;

    Solution * newSol = Solution::RandomSolution (rnd);
    iteration_best = NULL;
    /* this is the main loop of the algorithm. At each iteration ants
       produce a solution each and the pheromone values are
       updated. */
    while (timer.elapsed_time (Timer::VIRTUAL) < time_limit
           && iter <= n_of_iter) {
      double avg_cost = 0.0;
      double avg_viols = 0.0;

      iteration_best = newSol->localsearch();
      avg_cost = avg_cost + iteration_best->cost();
      avg_viols = avg_viols + iteration_best->constraint_violations();

      if (iter == 1) {
        // if we are in the first iteration then we can initialize all
        // the variables
        if (best_so_far != NULL) {
          delete best_so_far;
        }
        best_so_far = iteration_best->clone();
        time_taken = timer.elapsed_time (Timer::VIRTUAL);
        results.push_back (best_so_far->cost());
        viols.push_back (best_so_far->constraint_violations());
        times_best_found.push_back (time_taken);
        iter_best_found.push_back (iter);
        
        print_trace (best_so_far, trial_counter, iter, time_taken);
        
        DEBUG1 (check_valid (best_so_far, "best_so_far is valid",
                             "best_so_far is NOT valid"));
      }

      if (! iteration_best->better_than(newSol)) {
        delete newSol;
        newSol = Solution::RandomSolution (rnd);
        delete iteration_best;
        //break;
      } else {
        delete newSol;
        newSol = iteration_best;
        if (iteration_best->better_than (best_so_far)) {
          update_best_so_far();
        }
      }
      
      if (best == NULL) {
        best = best_so_far->clone();
      }
      else if (best_so_far->better_than(best)) {
        delete(best);
        best = best_so_far->clone();
      }
      
      iter = iter + 1;
    }
    
    trial_end (trial_counter, iter_best_found[trial_counter-1],
               times_best_found[trial_counter-1]);
  }
  
  /* The following lines are for writing the statistics about the
     experiments onto the screen

     1) the best solution found in all the trials
     2) the average of the best solutions found in all the trials
     3) the standard deviation of the average in 2)
     4) the average CPU at which the best solutions of the trials were found
     5) the standard deviation of the average in 4)
  */

  double r_mean = 0.0;
  double v_mean = 0.0;
  double t_mean = 0.0;
  for (size_t i = 0; i < results.size(); i++) {
    r_mean = r_mean + results[i];
    v_mean = v_mean + viols[i];
    t_mean = t_mean + times_best_found[i];
  }
  r_mean = r_mean / ((double)results.size());
  v_mean = v_mean / ((double)viols.size());
  t_mean = t_mean / ((double)times_best_found.size());
  double rsd = 0.0;
  double vsd = 0.0;
  double tsd = 0.0;
  for (size_t i = 0; i < results.size(); i++) {
    rsd = rsd + pow(results[i] - r_mean, 2.0);
    vsd = vsd + pow(viols[i] - v_mean, 2.0);
    tsd = tsd + pow(times_best_found[i] - t_mean, 2.0);
  }
  rsd = rsd / ((double)(results.size() - 1.0));
  if (rsd > 0.0) {
    rsd = sqrt(rsd);
  }
  vsd = vsd / ((double)(viols.size()-1.0));
  if (vsd > 0.0) {
    vsd = sqrt(vsd);
  }
  tsd = tsd / ((double)(times_best_found.size()-1.0));
  if (tsd > 0.0) {
    tsd = sqrt(tsd);
  }
  printf("# statistics\t(%g,%d)\t(%f,%f)\t(%f,%f)\t%f\t%f\n",
         double(best->cost()), best->constraint_violations(),
         r_mean, v_mean, rsd, vsd, t_mean, tsd);
  
}
