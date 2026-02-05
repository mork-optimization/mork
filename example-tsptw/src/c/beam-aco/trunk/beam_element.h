#pragma once
#include "ant.h"

#include <list>
#include <vector>
#include <algorithm>
#include <functional>

class Beam; // forward declaration to avoid circular dependency.

// FIXME: The correct dependency would be a class Beam-ACO that makes
// use of both Beam_element and Ant, so Ant should not depend on
// beam_element. This will avoid circular dependencies.
class Beam_Element : public Ant {
public:
  int node;
  double value;
  double greedy_weight;
  double greedy_rank_sum;

  Beam_Element() 
    : Ant(), node (-1), value (0.0), greedy_weight (0.0), greedy_rank_sum (0.0)
  { };

  Beam_Element * clone (void) { return new Beam_Element(*this); };

  Beam produce_children (int max_children);

  void commit(void);
};

bool solution_cost_compare (const Beam_Element* c1, const Beam_Element* c2);

class Beam : public list<Beam_Element*> 
{
public:
  Beam::iterator random_wheel (Random *r, double basesum);
  void choose_from (Beam &children, double children_basesum, int to_choose, double det_rate,
                    Random *rng);
  Beam_Element *
  best(void) {
      auto it = this->begin();
      Beam_Element * best = *it;
      for (it++; it != this->end(); it++) {
          if (solution_cost_compare(best, *it))
              best = *it;
      }
      return best; 
  }
};

template<typename _BidirectionalIterator, typename _RandomNumberGenerator>
    void
    random_shuffle_bidit ( _BidirectionalIterator begin,
                           _BidirectionalIterator end,
                           _RandomNumberGenerator& __rand)
{
    typedef typename
        std::iterator_traits<_BidirectionalIterator>::value_type
        value_type;
    std::vector<value_type> v( begin, end );
    std::random_shuffle( v.begin(), v.end(), __rand);
    std::copy( v.begin(), v.end(), begin );
}

// Local Variables:
// mode: c++
// End:
