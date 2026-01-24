/*********************************************************************

  Rand() genera un numero real pseudoaleatorio entre 0 y 1, excluyendo
   el 1.

  Rand_int() genera un numero entero entre low y high, ambos incluidos.

  Rand_double() genera un numero real entre low y high, incluido low y
  no incluido high

*********************************************************************/

#include "myrandom.h"
#include "gsl_rng.h"

#include <assert.h>

static gsl_rng * rng=NULL;


void RandInitialize(unsigned long int seed)
{
    rng = gsl_rng_alloc (gsl_rng_taus2);
    gsl_rng_set(rng, seed);
}

double Rand()
{
#if DEBUG > 0
    assert(rng != NULL);
#endif

    return(gsl_rng_uniform(rng));
}

double Rand_double(double low, double high)
{
#if DEBUG > 0
    assert(rng != NULL);
#endif
    return(low + (gsl_rng_uniform(rng)*high));
}

int Rand_int(int low, int high)
{
#if DEBUG > 0
    assert(rng != NULL);
#endif

    int tmp = high - low;

#if DEBUG > 0
    assert(tmp >= 0);
#endif

    if (tmp <= 0) {
        return high;
    } else if (tmp == 1) {
        if(Rand() >= 0.5) return high;
        else return low;
    } else {
#if DEBUG > 0
        assert( ((unsigned long)tmp + 1UL) < (gsl_rng_max (rng) - gsl_rng_min(rng)));
        assert( ((unsigned long)tmp + 1UL) > 0);
#endif
        return(low + (int) gsl_rng_uniform_int(rng, (unsigned long)tmp + 1UL));
    }
}

/* 
   The correct unbiased random shuffling based on
   http://en.wikipedia.org/wiki/Fisher-Yates_shuffle
*/
void Rand_int_permutation(int *vector, int vector_size)
{
    int  i;

    for (i = 0; i < vector_size; i++)
        vector[i] = i;

    for (i = vector_size - 1; i > 0 ; i--) {
        /* 0 <= j <= i is the correct range for unbiased shuffling.  */
        int j = Rand_int (0, i);
        /* Testing if (i == j) decreases performance for large vectors.  */
        int tmp = vector[i];
        vector[i] = vector[j];
        vector[j] = tmp;
    }
}

double Rand_normal(double mean, double sd)
/** REFERENCES (http://www.cast.uark.edu/~kkvamme/ACN37.htm)

 Hays, W.L.
 1988 Statistics (4th ed.). Holt, Rinehart and Winston, New York.

 Hodder, Ian (ed.)
 1978 Simulation Studies in Archaeology. Cambridge University Press, Cambridge.

 Olkin, I., L.J. Gleser, and C. Derman
 1980 Probability Models and Applications. Macmillan Publishing, New York.

 Ross, Sheldon M.
 1989 Introduction to Probability Models (4th ed.). Academic Press, Boston.
**/

{
    int i;
    double x;

    for (x = 0.0, i = 0; i < 12; i++) {
        x += Rand();
    }
    return (mean + sd * (x - 6.0));

}

// REMOVE ME!
#if 0

/**

// "Minimal" random number generator of Park and Miller.
// Returns a uniform random deviate between 0.0 and 1.0.
#define IA 16807
#define IM 2147483647
#define AM (1.0/IM)
#define IQ 127773
#define IR 2836
#define MASK 123459876

float rand0(void);

float rand0(void)
{
     long k;
     float ans;

     Seed ^= MASK;

     k = (Seed)/IQ;

     Seed=IA*(Seed - k*IQ) - IR*k;

     if (Seed < 0) Seed += IM;

     ans=AM*(Seed);
     Seed ^= MASK;

     return ans;
}

// From manpage rand(3) 2003-11-15
//
//   POSIX 1003.1-2003 gives the following example of an
//   implementation of rand() and srand(), possibly useful when one
//   needs the same sequence on two different machines.
static unsigned long next = 1;

// RAND_MAX assumed to be 32767
int myrand(void) {
    next = next * 1103515245 + 12345;
    return((unsigned)(next/65536) % 32768);
}

void mysrand(unsigned seed) {
    next = seed;
}


float Randfloat(float low, float high)
{
//     Initrand(Seed);

    return(low + (high*rand0()));
}

int Rand_int(int low, int high)
{
//     static int first_time = TRUE;
//     if(first_time == TRUE) {
//         Initrand(Seed);
//         first_time = FALSE;
//     }
    if(high == low) {
        return high;
    } else if (high - low == 1) {
        if(rand0() > 0.5) return high;
        else return low;

    } else {
        return(low + (int) ( (high - low+1)*rand0()));
    }
}
**/
#endif
