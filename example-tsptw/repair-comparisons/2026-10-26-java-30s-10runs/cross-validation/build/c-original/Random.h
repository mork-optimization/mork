#ifndef RANDOM_H
#define RANDOM_H

#include "misc-math.h"

#include <numeric>
#include <algorithm>
#include <vector>
#include <cassert>
#include <cstdlib>
using namespace std;

class Random {
private:
  /* constants for a pseudo-random number generator, 
     for details see numerical recipes in C 
  */
  static const int IA = 16807;
  static const int IM = 2147483647;
  static constexpr double AM = (1.0/IM);
  static const int IQ = 127773;
  static const int IR = 2836;
  
  double ran01(long *idum);

public:
  Random(long int arg) : seed(arg) {}

  long int seed;
  double next() { return ran01(&seed);}

  // Generate a random integer in the range [0, MAXIMUM - 1]
  int rand_int (int maximum);

  ptrdiff_t operator() (ptrdiff_t maximum) {
    assert (maximum > 0);
    return ptrdiff_t(ran01 (&seed) * maximum);
  }

  vector<int> generate_vector(int size);
  int * generate_array(int size);
};
#endif
// Local Variables:
// mode: c++
// End:
