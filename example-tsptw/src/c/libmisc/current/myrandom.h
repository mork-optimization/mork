#ifndef _MYRANDOM_H_
#define _MYRANDOM_H_
/*******************************************************************

  Rand() genera un numero real pseudoaleatorio entre 0 y 1, excluyendo
   el 1.

  Rand_int() genera un numero entero entre low y high, ambos incluidos.

  Rand_double() genera un numero real entre low y high, incluido low y
  no incluido high.

********************************************************************/


#include "common.h"
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#ifdef __cplusplus
extern "C" {
#endif

void RandInitialize(unsigned long int seed);
double Rand(void);
double Rand_double(double low, double high);
int  Rand_int(int low, int high);

void Rand_int_permutation(int *vector, int vector_size );
double Rand_normal(double mean, double sd);

static inline int
random_wheel (const double * probabilities, int size __unused, double basesum)
{
    int i = 0;
    double wheel = probabilities[0];
    double rnd = Rand() * basesum;

    DEBUG3 (vector_double_fprint_fmt (stderr, probabilities, size, "%.2f");
            fprintf (stderr, "\nbasesum = %g, rnd = %g, i = %d, wheel = %g\n",
                     basesum, rnd, i, wheel)
        );

    assert (basesum > 0.0);
    assert (rnd < basesum);

    /* FIXME: This should be a binary search.  */
    while (wheel <= rnd) {
        i++;
        wheel += probabilities[i];
    }

    assert (0 <= i);
    assert (i < size);
    assert (probabilities[i] > 0.0);
    return i;
}

#ifdef __cplusplus
}
#endif

#endif //_MYRANDOM_H_
