/***************************************************************************
                          Timer.cc  -  description
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
#include "Timer.h"

#define TIMER_CPUTIME(X) ( (double)X.ru_utime.tv_sec  +         \
                           (double)X.ru_stime.tv_sec  +         \
                          ((double)X.ru_utime.tv_usec +         \
                           (double)X.ru_stime.tv_usec ) * 1.0E-6)

/*
 *  The virtual time of day and the real time of day are calculated and
 *  stored for future use.  The future use consists of subtracting these
 *  values from similar values obtained at a later time to allow the user
 *  to get the amount of time used by the algorithm.
 */
Timer::Timer(void)
{
  this->reset();
}

/*
 *  Return the time elapsed in seconds (either
 *  REAL or VIRTUAL time, depending on ``type'').
 */
double Timer::elapsed_time(const TYPE& type) {
  if (type == REAL) {
    gettimeofday( &tp, NULL );
    return( (double) tp.tv_sec + (double) tp.tv_usec * 1.0E-6 - real_time );
  }
  else
    return elapsed_time_virtual();
}

double Timer::elapsed_time_virtual (void)
{
    double timer_tmp_time;
    getrusage (RUSAGE_SELF, &res);
    timer_tmp_time = TIMER_CPUTIME(res) - virtual_time;
    return (timer_tmp_time < 0.0) ? 0 : timer_tmp_time;
}

void Timer::reset(void)
{
  getrusage( RUSAGE_SELF, &res );
  virtual_time = (double) res.ru_utime.tv_sec +
    (double) res.ru_stime.tv_sec +
    (double) res.ru_utime.tv_usec * 1.0E-6 +
    (double) res.ru_stime.tv_usec * 1.0E-6;
  
  gettimeofday( &tp, NULL );
  real_time =  (double) tp.tv_sec + (double) tp.tv_usec * 1.0E-6;
}
