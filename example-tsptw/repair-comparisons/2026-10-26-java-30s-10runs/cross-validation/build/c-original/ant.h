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

#ifndef ANT_H
#define ANT_H

#include "tsptw_solution.h"
#include "Random.h"
#include <vector>

class Ant : public Solution
{
public:

  static vector<vector<double> > pheromone;
  static Random *rng;
  static double time_sampling;
  
  static void Init (string instance, Random * rnd) {
    Ant::rng = rnd;
    
    // reading the problem instance
    Solution::LoadInstance (instance);
    
    // initialization of the structure that holds the pheromone values
    Ant::pheromone = vector<vector<double> > 
      (Solution::n, vector<double> (Solution::n));
    
    // pheromone * heuristic info
    Ant::total = vector<vector<double> > 
      (Solution::n, vector<double> (Solution::n));

    Ant::time_sampling = 0;
  };
  
  static void resetUniformPheromoneValues();
  static void initUniformPheromoneValues();
  

  Ant() : Solution(), probs(n) {};
  
  Ant * clone (void) { return new Ant(*this); };
  
  void complete (double det_rate);
  Ant * stochastic_sampling(int n_samples, double det_rate);
  
  Solution* construct(double det_rate);
  Solution* beam_construct(double det_rate,
                           int beam_width, int max_children, 
                           int to_choose, int n_samples, int sample_rate);
  
private:
  
  static vector<vector<double> > total;
  vector<double> probs;
  double basesum;
  
  void precompute_total (void);
  void update_probs(int);
  int maximum_prob () const;
  int random_wheel ();
  int construction_step (int last, double det_rate);
};

#endif
// Local Variables: 
// mode: c++; 
// End:
