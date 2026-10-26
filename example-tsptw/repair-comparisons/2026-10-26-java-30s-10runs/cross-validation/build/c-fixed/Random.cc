/***************************************************************************
                          Random.cc  -  description
                             -------------------
    begin                : Fri Nov 10 2000
    copyright            : (C) 2000 by Christian Blum
    email                : cblum@ulb.ac.be
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
#include "Random.h"
#include <cassert> // assert

/* pseudo-random number generator as proposed in numerical recipes in C 
   Input:   a long value; has to be the seed variable
   Output:  a pseudo-random number uniformly distributed in [0,1]
   Side effects: changes the value of the input variable, must be this way
*/
double Random::ran01( long *idum )
{
  long k;
  double ans;

  k =(*idum)/IQ;
  *idum = IA * (*idum - k * IQ) - IR * k;
  if (*idum < 0 ) *idum += IM;
  ans = AM * (*idum);
  return ans;
}

// Generate a random integer in the range [0, MAXIMUM - 1]
int Random::rand_int (int maximum) 
{
  assert(maximum > 0);
  return int(ran01 (&seed) * maximum);
}

int * Random::generate_array(int size)
{
  int  i, j, help;
  
  int * v = new int[size];
  
  for ( i = 0 ; i < size; i++ ) 
    v[i] = i;
  
  for ( i = 0 ; i < size-1 ; i++) {
    j = int(ran01 (&seed) * (size - i)); 
    //assert( i + j < size );
    help = v[i];
    v[i] = v[i+j];
    v[i+j] = help;
  }
  return v;
}

vector<int> Random::generate_vector(int size)
{
  long int  i, j, help;
  vector<int>  v(size);

  for ( i = 0 ; i < size; i++ ) 
    v[i] = i;
  
  for ( i = 0 ; i < size-1 ; i++) {
    j = int(ran01(&seed) * (size - i)); 
    //assert( i + j < size );
    help = v[i];
    v[i] = v[i+j];
    v[i+j] = help;
  }
  return v;
}
