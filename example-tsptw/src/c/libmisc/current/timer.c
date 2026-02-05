/*************************************************************************

 Simple timer functions.

 $Revision: 1338 $
 $Date: 2015-05-28 12:22:03 +0100 (Thu, 28 May 2015) $

 ---------------------------------------------------------------------

 Copyright (c) 2005, 2006, 2007 Manuel Lopez-Ibanez
 TeX: \copyright 2005, 2006, 2007 Manuel L{\'o}pez-Ib{\'a}{\~n}ez

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
 General Public License at:
                 http://www.gnu.org/copyleft/gpl.html
 or by writing to:
           Free Software Foundation, Inc., 59 Temple Place,
                 Suite 330, Boston, MA 02111-1307 USA

 -----------------------------------------------------------------------

 Based on previous source code from Thomas Stuetzle.

 -----------------------------------------------------------------------

 References:

 [1] man 7 time
 [2] man 2 getrusage
 [3] man 3 timercmp
 [4] man 3 clock
 [5] http://www.gnu.org/s/libc/manual/html_node/Elapsed-Time.html
 [6] http://rabbit.eng.miami.edu/info/functions/time.html
 [7] http://gcc.gnu.org/svn/gcc/trunk/gcc/timevar.c
 [8] man 2 times

 -----------------------------------------------------------------------
 NOTES:

 * The current implementation does not allow several concurrent
   timers. That would require defining a Timer object that
   encapsulates the internal state. Not a lot of work.

 * This implementation uses 'double' to internally store time. It
   would be slightly faster to store time as an integer or as a
   timeval, and converting to double when required by the user.

 * clock() works on DOS and Windows but it has many issues, like
   inaccuracy and wrapping after some time (e.g. 72 minutes) [4,6]. An
   implementation using clock is welcome, but it should try to
   warn/handle these as much as possible.

 * There are functions specific for handling struct timeval [3] but
   they are not POSIX standard, so it is better to not assume they are
   portable.

 * The best way to portably substract two struct timeval is the
   following function [5]:

     / Subtract the `struct timeval' values X and Y,
        storing the result in RESULT.
        Return 1 if the difference is negative, otherwise 0.  /
     
     int
     timeval_subtract (result, x, y)
          struct timeval *result, *x, *y;
     {
       / Perform the carry for the later subtraction by updating y. /
       if (x->tv_usec < y->tv_usec) {
         int nsec = (y->tv_usec - x->tv_usec) / 1000000 + 1;
         y->tv_usec -= 1000000 * nsec;
         y->tv_sec += nsec;
       }
       if (x->tv_usec - y->tv_usec > 1000000) {
         int nsec = (x->tv_usec - y->tv_usec) / 1000000;
         y->tv_usec += 1000000 * nsec;
         y->tv_sec -= nsec;
       }
     
       / Compute the time remaining to wait.
          tv_usec is certainly positive. /
       result->tv_sec = x->tv_sec - y->tv_sec;
       result->tv_usec = x->tv_usec - y->tv_usec;
     
       / Return 1 if result is negative. /
       return x->tv_sec < y->tv_sec;
     }

  * The implementation of timers in GCC could be simplified to be used
    here [7]. It is interesting that it favors the use of times(2)[8]
    but it can work with getrusage [2] and clock [4].

  * gettimeofday can go back in time and it is slow:
    http://blog.habets.pp.se/2010/09/gettimeofday-should-never-be-used-to-measure-time

  * clock_gettime seems a better wall-clock.

*************************************************************************/

#include <stdio.h>
#include <sys/time.h> /* for struct timeval */
#ifndef WIN32
#include <sys/resource.h> /* for getrusage */
#else
#include "resource.h"
#endif
#include "timer.h"

#define TIMER_CPUTIME(X) ( (double)X.ru_utime.tv_sec  +         \
                           (double)X.ru_stime.tv_sec  +         \
                          ((double)X.ru_utime.tv_usec +         \
                           (double)X.ru_stime.tv_usec ) * 1.0E-6)

#define TIMER_WALLTIME(X)  ( (double)X.tv_sec +         \
                             (double)X.tv_usec * 1.0E-6 )

static struct rusage res;
static struct timeval tp;
static double virtual_time, real_time;
static double stop_virtual_time, stop_real_time;

/*
 *  The virtual time of day and the real time of day are calculated and
 *  stored for future use.  The future use consists of subtracting these
 *  values from similar values obtained at a later time to allow the user
 *  to get the amount of time used by the backtracking routine.
 */

void Timer_start()
{
    gettimeofday (&tp, NULL );
    real_time =   TIMER_WALLTIME(tp);

    getrusage (RUSAGE_SELF, &res );
    virtual_time = TIMER_CPUTIME(res);
}

/*
 *  Return the time used in seconds (either
 *  REAL or VIRTUAL time, depending on ``type'').
 */
double Timer_elapsed_virtual (void)
{
    double timer_tmp_time;
    getrusage (RUSAGE_SELF, &res);
    timer_tmp_time = TIMER_CPUTIME(res) - virtual_time;

#if DEBUG >= 4
    if (timer_tmp_time  < 0.0) {
        fprintf(stderr, "%s: Timer_elapsed(): warning: "
                "negative increase in time ", __FILE__);
        fprintf(stderr, "(%.6g - %.6g = ",
                TIMER_CPUTIME(res) , virtual_time);
        fprintf(stderr, "%.6g)\n", timer_tmp_time);
    }
#endif

    return (timer_tmp_time < 0.0) ? 0 : timer_tmp_time;
}

double Timer_elapsed_real (void)
{
    double timer_tmp_time;
    
    gettimeofday (&tp, NULL);
    timer_tmp_time = TIMER_WALLTIME(tp) - real_time;

#if DEBUG >= 4
    if (timer_tmp_time  < 0.0) {
        fprintf(stderr, "%s: Timer_elapsed(): warning: "
                "negative increase in time ", __FILE__);
        fprintf(stderr, "(%.6g - %.6g = ",
                TIMER_WALLTIME(tp) , real_time);
        fprintf(stderr, "%.6g)\n", timer_tmp_time);
    }
#endif

    return (timer_tmp_time < 0.0) ? 0 : timer_tmp_time;
}

double Timer_elapsed( TIMER_TYPE type )
{
    return (type == REAL_TIME) 
        ? Timer_elapsed_real () 
        : Timer_elapsed_virtual ();
}

void Timer_stop(void)
{
    gettimeofday (&tp, NULL);
    stop_real_time =  TIMER_WALLTIME(tp);

    getrusage (RUSAGE_SELF, &res);
    stop_virtual_time = TIMER_CPUTIME(res);
}

void Timer_continue(void)
{
    double timer_tmp_time;

    gettimeofday( &tp, NULL );
    timer_tmp_time = TIMER_WALLTIME(tp) - stop_real_time;

#if DEBUG >= 4
    if (timer_tmp_time  < 0.0) {
        fprintf(stderr, "%s: Timer_continue(): warning: "
                "negative increase in time (%.6g - %.6g = %.6g)\n",
                __FILE__, TIMER_WALLTIME(tp), stop_real_time, timer_tmp_time);
    }
#endif

    if (timer_tmp_time > 0.0) real_time += timer_tmp_time;

    getrusage( RUSAGE_SELF, &res );
    timer_tmp_time =  TIMER_CPUTIME(res) - stop_virtual_time;

#if DEBUG >= 4
    if (timer_tmp_time  < 0.0) {
        fprintf(stderr, "%s: Timer_continue(): warning: "
                "negative increase in time (%.6g - %.6g = %.6g)\n",
                __FILE__, TIMER_CPUTIME(res),stop_virtual_time,timer_tmp_time);
    }
#endif

    if (timer_tmp_time > 0.0) virtual_time += timer_tmp_time;
}
