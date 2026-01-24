#pragma once
#include "common.h"

#ifdef __cplusplus
extern "C++" {
#include <cstdarg>
#include <iostream>
#include <cmath>
using namespace std;
}
#else
#include <math.h>
#endif

static inline double powx(double x, double y)
{
    /* On 64-bits, pow() may be more than 10,000 times slower for some (rare)
       inputs than for other nearby inputs.  This affects only pow(), and not
       powf() nor powl().  We use powl to work-around this bug in GNU libc:
       https://sourceware.org/bugzilla/show_bug.cgi?id=13932 */
#if __x86_64__ || __ppc64__
    return powl(x, y);
#else
    return pow(x, y);
#endif
}

#if 0
static inline double
pow_ui (double base, unsigned int exponent)
{
    double result = 1;
    while (exponent > 0) {
        if (exponent & 1U) {
            result = result * base;
        }
        base = (base * base);
        exponent >>= 1;
    }
    return result;
}
#endif

static inline bool
#ifdef __cplusplus
fequals(double left, double right, double epsilon = 1e-6)
#else
fequals(double left, double right, double epsilon)
#endif
{
    return fabs(left - right) < epsilon;
}

#ifdef __cplusplus
static inline bool 
fless(double left, double right, double epsilon = 1e-6, bool orequal = false)
{
    if (fabs(left - right) < epsilon) {
        return (orequal);
    }
    return (left < right);
}
static inline bool 
fless_or_equal(double left, double right, double epsilon = 1e-6)
{
    return fless (left, right, epsilon, true);
}
#else
static inline bool 
fless(double left, double right, double epsilon)
{
    if (fabs(left - right) < epsilon) {
        return false;
    }
    return (left < right);
}
static inline bool 
fless_or_equal(double left, double right, double epsilon)
{
    if (fabs(left - right) < epsilon)
        return true;
    return left < right;
}
#endif

#ifdef __cplusplus
static inline bool 
fgreater(double left, double right, double epsilon = 1e-6, bool orequal = false)
{
    if (fabs(left - right) < epsilon) {
        return (orequal);
    }
    return (left > right);
}
static inline bool 
fgreater_or_equal(double left, double right, double epsilon = 1e-6)
{
    return fgreater (left, right, epsilon, true);
}
#else
static inline bool 
fgreater(double left, double right, double epsilon)
{
    if (fabs(left - right) < epsilon)
        return false;
    return left > right;
}
static inline bool 
fgreater_or_equal(double left, double right, double epsilon)
{
    if (fabs(left - right) < epsilon)
        return true;
    return left > right;
}
#endif
