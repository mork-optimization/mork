/***************************************************************************
                          Timer.h  -  description
                             -------------------
    begin                : Wed Dec 6 2000
    copyright            : (C) 2000 by Christian Blum
    email                : chr_blum@hotmail.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
#ifndef TIMER_H
#define TIMER_H

#include "misc-math.h"

#include <sys/time.h>
#include <sys/resource.h>
#include <stdio.h>

class Timer {
private:
  struct rusage res;
  struct timeval tp;
  double virtual_time, real_time;

public:
  enum TYPE {REAL, VIRTUAL};
  Timer(void);
  double elapsed_time(const TYPE& type);
  double elapsed_time_virtual(void);
  void reset(void);
};
#endif
