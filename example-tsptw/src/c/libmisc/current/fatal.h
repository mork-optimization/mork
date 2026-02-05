#ifndef FATAL_H
#define FATAL_H
#include "eprintf.h"
/* FIXME: this would be better as a static inline function.  */
#define fatal(...) eprintf (__VA_ARGS__)
#endif
