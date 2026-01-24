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

*************************************************************************/

#include <iostream>
#include <fstream>
#include <stdio.h>
#include "ant.h"
#include "Timer.h"

vector<vector<double> > Ant::pheromone;
vector<vector<double> > Ant::total;
Random *Ant::rng = 0;
double Ant::time_sampling;

void matrix_fill(vector<vector<double> > &m, int n, double value)
{
    for (int i = 0; i < n; i++)
        for (int j = 0; j < n; j++)
            m[i][j] = value;
}
/* The method initUniformPheromoneValues initializes all the pheromone
   values to 0.5 */
void Ant::initUniformPheromoneValues() 
{
    matrix_fill(Ant::pheromone, Solution::n, 0.5);
}

/* The method resetUniformPheromoneValues resets all the pheromone
   values to 0.5 */

void Ant::resetUniformPheromoneValues() 
{
    matrix_fill(Ant::pheromone, Solution::n, 0.5);
}

void Ant::update_probs (int added) 
{
  basesum = 0.0;
  // ??? We currently have to enforce both _tau == 0.0 and
  // node_assigned[i] == true for assigned nodes. It would be better
  // to choose one approach and use only that consistently.
  probs[added] = 0.0;

  for (int k = 0, inode = 0; k < nodes_available; k++) {
    while (node_assigned[++inode]);
    probs[inode] = total[added][inode];
    basesum += probs[inode];
  }
  assert (probs[added] == 0.0);
  assert (isfinite (basesum));
}

int Ant::maximum_prob (void) const
{
  int max_node = -1;
  double max_prob = 0.0;

  for (int i = 1; i < n; i++) {
    if (max_prob < probs[i]) {
      max_prob = probs[i];
      max_node = i;
    }
  }
  return max_node;
}

int
Ant::construction_step (int last, double det_rate)
{
  int node;

  update_probs (last);
  if (det_rate >= 1.0 or (det_rate > 0.0  and rng->next() < det_rate)) 
    node = maximum_prob ();
  else
    node = random_wheel ();

  DEBUG2 (fprintf (stderr, " %d", node));

  assert (node >= 0); // Negative means not found.

  add (last, node);
  return node;
}

void
Ant::precompute_total (void)
{
  for (int k = 0; k < n; k++) {
    for (int j = 0; j < n; j++) {
      total[k][j] = pheromone[k][j] * heuristic_information (k, j);
    }
  }
}

Solution* 
Ant::construct(double det_rate) 
{
  randomize_hinfo (rng);
  precompute_total ();
  int last = 0;

  do {
    last = construction_step (last, det_rate);
  } while (nodes_available);

  Solution *solution = this;
  return solution->clone();  
}

int Ant::random_wheel ()
{
  double rand = rng->next() * basesum;
  int i = 1;
  double wheel = probs[i];
  
  //  fprintf (stderr, "%d: %.6f\t(%.6f < %.6f) _basesum = %.6f\n", 
  //          i, _tau[i], wheel, rand, _basesum);

  while (wheel <= rand) {
    i++;
    wheel += probs[i];
    //      fprintf (stderr, "%d: %.6f\t(%.6f < %.6f)\n", 
    //               i, _tau[i], wheel, rand);
  }
  
  assert (i > 0);
  assert (i < n);
  assert (!node_assigned[i]);
  assert (probs[i] > 0.0);
  return i;
}

/* Take the current solution and generate complete solutions sampling
   from current pheromone values. Calculate lower bounds for
   constraint violations and cost of the solution.  */
void Ant::complete (double det_rate)
{
  int last = permutation.back();

  DEBUG3 (fprintf (stderr, "Stochastic sampling from (%d): ", last);
          //          fprintf (stderr, "(basesum = %.6f) ", _basesum);
          for (size_t k = 1; k < permutation.size(); k++) {
            fprintf (stderr, " %d", permutation[k]); }
          fprintf (stderr, ":")
          );

  while (nodes_available) {
    last = construction_step (last, det_rate);
  }

  DEBUG3 (fprintf (stderr, " %d\n", permutation.back()));
  assert (check_solution());
}

Ant *
Ant::stochastic_sampling (int n_samples, double det_rate)
{
  Timer timer_sampling;

  _lower_bound = -1;
  _lower_bound_constraint_violations = -1;
  int &_cviols = _lower_bound_constraint_violations;

  Ant best;

  if (nodes_available <= 3) {
    int k = 0;
    int inode = 0;
    int p[nodes_available];
    for (k = 0; k < nodes_available; k++) {
      while (node_assigned[++inode]);
      p[k] = inode;
    }

    do {

      Ant sol = *this;

      sol.add (p);

      DEBUG3 (fprintf (stderr, "Permutation: ");
              for (size_t a = 1; a < permutation.size(); a++) {
                fprintf (stderr, " %d", permutation[a]); }
              fprintf (stderr, ":");
              for (int a = 0; a < nodes_available; a++) {
                fprintf (stderr, " %d", p[a]); }
              fprintf (stderr, "\t%g\t%d\n", 
                       double(sol.cost()), sol.constraint_violations());
              );

      if (_lower_bound == -1) {
        _lower_bound = sol.cost();
        _cviols = sol.constraint_violations();
        best = sol;
      }
      else if (sol.constraint_violations() < _cviols
               or (sol.constraint_violations() == _cviols 
                   and sol.cost() < _lower_bound)) {
        _lower_bound = sol.cost();
        _cviols = sol.constraint_violations();
        best = sol;
      }
    } while (next_permutation (p, p + nodes_available));

    goto finish;
  }

  for (int i = 0; i < n_samples; i++) {

    Ant sol = *this;
    sol.complete (det_rate);

    if (_lower_bound == -1) {
      _lower_bound = sol.cost();
      _cviols = sol.constraint_violations();
      best = sol;
    }
    else if (sol.constraint_violations() < _cviols
             || (sol.constraint_violations() == _cviols 
                 && sol.cost() < _lower_bound)) {
      _lower_bound = sol.cost();
      _cviols = sol.constraint_violations();
      best = sol;
    }

    DEBUG3 (fprintf (stderr, "_lower_bound = %g, _cviols = %d\n", 
                     double(_lower_bound), _cviols));
  }

 finish:
  time_sampling += timer_sampling.elapsed_time_virtual();
  return best.clone();
}

#include "beam_element.h"

bool lower_bound_compare(const Beam_Element* c1, const Beam_Element* c2)
{
  assert (c1->_lower_bound_constraint_violations >= 0 
          && c2->_lower_bound_constraint_violations >= 0
          && c1->_lower_bound >= 0
          && c2->_lower_bound >= 0);

  return (c1->_lower_bound_constraint_violations
          < c2->_lower_bound_constraint_violations 
          || (c1->_lower_bound_constraint_violations 
              == c2->_lower_bound_constraint_violations  
              && c1->_lower_bound < c2->_lower_bound));
}

// FIXME: This should not be a method of Ant but of a class Beam-ACO that
// depends on both Ant and Beam_Element.
// FIXME: Avoid playing with pointers, make more use of direct
// assignments. Use this pointer to return the solution.
Solution * 
Ant::beam_construct(double det_rate,
                    int beam_width, int max_children, 
                    int to_choose,
                    int n_samples, int sample_rate) 
{
  Beam beam;
  Ant *best = NULL;
  int beam_depth = 0;

  randomize_hinfo (rng);
  precompute_total ();
  // Initialize the root of the beam with an empty solution.
  Beam_Element * beam_root = new Beam_Element ();
  beam.push_back (beam_root);

  DEBUG2 (fprintf (stderr, "Beam construct:\n"));

  while (true) {
    Beam children;
    
    for (Beam::iterator beam_it = beam.begin(); 
         beam_it != beam.end(); beam_it++) {
      Beam_Element * beam_node = *beam_it;
      /* Create children solutions by adding one of the unassigned
         nodes to the current solution. Calculate _greedy_weight with
         the heuristic_information corresponding to the
         assignment. Calculate _greedy_rank_sum.  The value of
         child->value is the pheromone information corresponding to
         the new assignment.  */
      Beam tmp_children = beam_node->produce_children (max_children);
      children.splice (children.end(), tmp_children);
    }

    // FIXME: For complete solutions we could skip most of what follows.

    double greedy_rank_basesum = 0.0;
    for (Beam::iterator child = children.begin();
         child != children.end(); child++) {
      greedy_rank_basesum += 1.0 / (*child)->greedy_rank_sum;
    }
    
    double children_basesum = 0.0;
    for (Beam::iterator child = children.begin();
         child != children.end(); child++) {
      // normalise all values
      double val = (*child)->value
        * ((1.0 / (*child)->greedy_rank_sum) / greedy_rank_basesum);

      children_basesum += val;
      (*child)->value = val;
    }

    // Create a new beam from the current children.
    Beam new_beam;
    new_beam.choose_from (children, children_basesum, to_choose, det_rate, rng);


    beam_depth++;

    if (new_beam.size() > 0) {
      // Delete old beam.
      for (Beam::iterator beam_node = beam.begin(); 
           beam_node != beam.end(); beam_node++) {
        delete(*beam_node);
      }
      beam.clear();

      if (new_beam.size () > (size_t) beam_width) {

        int count_skipped = 0;

        DEBUG2 (int k = 1;
                fprintf (stderr, "new beam (done = %3d, sample_rate = %3d)   :\n",
                         Solution::n - beam_depth, sample_rate);
                for (Beam::iterator beam_node = new_beam.begin();
                     beam_node != new_beam.end(); beam_node++) {
                  fprintf (stderr, " beam %3d   : ", k);
                  (*beam_node)->print_one_line(stderr);
                  k++;
                });

        if (Solution::n - beam_depth <= sample_rate) {
        
          for (Beam::iterator beam_node = new_beam.begin(); 
               beam_node != new_beam.end();)     {
          
            if (best == NULL or (*beam_node)->better_than(best)) {
                // This solution may improve the beam so sample it.
                Ant *best_of_sampling = 
                    (*beam_node)->stochastic_sampling (n_samples, det_rate);

              if (best == NULL) {
                best = best_of_sampling;
                DEBUG2 (fprintf (stderr, "BEST OF SAMPLING: ");
                        best->print_one_line(stderr));
              } 
              else if (best_of_sampling->better_than(best)) {
                delete best;
                best = best_of_sampling;
                DEBUG2 (fprintf (stderr, "BEST OF SAMPLING: ");
                        best->print_one_line(stderr));
              } else {
                delete best_of_sampling;
              }
              beam_node++;
              
            } else {
              // Do not bother to sample this solution.
              delete (*beam_node);
              beam_node = new_beam.erase(beam_node);
              count_skipped++;
            }
          }
          
          DEBUG2 (if (count_skipped > 0) 
                    fprintf (stderr, "skipped: %d\n", count_skipped));
          
          // Sort in ascending order of _lower_bound_constraints and
          // _lower_bound (calculated by stochastic_sampling above).
          new_beam.sort (lower_bound_compare);

        } else { // beam_depth > sample_rate
          // Shuffle randomly
          random_shuffle_bidit (new_beam.begin(), new_beam.end(), *rng);
          DEBUG2 (int k = 1;
                  fprintf (stderr, "shuffle beam (depth = %3d)   :\n", beam_depth);
                  for (Beam::iterator beam_node = new_beam.begin();
                       beam_node != new_beam.end(); beam_node++) {
                    fprintf (stderr, " beam %3d   : ", k);
                    (*beam_node)->print_one_line(stderr);
                    k++;
                  });
        }
      }
      
      // Keep at most beam_width solutions from the new_beam.
      int count = 0;
      Beam::iterator beam_node = new_beam.begin();
      while (count < beam_width and beam_node != new_beam.end()) {
        beam.push_back(*beam_node);
        DEBUG3 (fprintf (stderr, "Add to beam (%2d): ", count + 1);
                (*beam_node)->print_one_line(stderr));
        beam_node++;
        count++;
      }

      // Delete remaining.
      while (beam_node != new_beam.end()) {
        delete(*beam_node);
        beam_node++;
      }

      // No solution in the beam can become better than best of sampling.
      if (new_beam.size() == 0) {
        // Best solution found during the beam search.
        assert (best != NULL);
        DEBUG2 (fprintf (stderr, "Best of beam    : NULL\n");
                fprintf (stderr, "Best of sampling: ");
                best->print_one_line(stderr));

        Solution *s = best;
        s = s->clone();
        delete best;
        return s;
      }

    } else { // new_beam.size() == 0

#if 1   
      // FIXME: No need to sort O(n * log(n)), just search for the
      // best O(n).
      beam.sort (solution_cost_compare);

      // Best solution found during the beam search.
      Ant *best_of_beam = beam.front();
      beam.pop_front();
#else
      // FIXME: This should be faster O(n) but it is not!
      Ant * best_of_beam = beam.best();
#endif
      DEBUG2 (fprintf (stderr, "Best of beam    : ");
              best_of_beam->print_one_line(stderr);
              if (best != NULL) {
                fprintf (stderr, "Best of sampling: ");
                best->print_one_line(stderr);
              });
      
      DEBUG2 (int k = 1;
              for (Beam::iterator beam_node = beam.begin();
                   beam_node != beam.end(); beam_node++) {
                fprintf (stderr, "beam %3d   : ", k);
                (*beam_node)->print_one_line(stderr);
                k++;
              });

      // Destroy the beam.
      for (Beam::iterator beam_node = beam.begin(); 
           beam_node != beam.end(); beam_node++) {
          //if (*beam_node != best_of_beam)
              delete (*beam_node);
      }
      beam.clear();

      if (best == NULL) {
        best = best_of_beam;
      }
      else if (best_of_beam->better_than(best)) {
        delete best;
        best = best_of_beam;
      } 
      else {
        delete best_of_beam; 
      }

      Solution *s = best;
      s = s->clone();
      delete best;
      return s;
    }
  }
}
