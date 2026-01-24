/* Usage:
  (in your main file) 
#define PARAMETER_FILENAME "foo.h"
#include "parameter.h"

  (in foo.h) DEFINE_PARAMETER(label, short, long, description)
  for example:
    DEFINE_PARAMETER(PARAM_VERSION,"-v","--version","print version")
*/

#include <stdlib.h> /* DEFINE_PARAMETER may contain NULL */

#ifdef PARAMETER_FILENAME
#define DEFINE_PARAMETER(a, b, c, d)   a,
enum {
#include PARAMETER_FILENAME
    PARAM_COUNT
};
#undef DEFINE_PARAMETER

#define DEFINE_PARAMETER(a,b,c,d) {b,c,d},
const char * const PARAMETERS[PARAM_COUNT][3] = {
#include PARAMETER_FILENAME
};
#undef DEFINE_PARAMETER
#undef PARAMETER_FILENAME
#define PARAMETERS_DEFINED
#endif // ! PARAMETER_FILENAME

#ifndef _PARAMETER_H_
#define _PARAMETER_H_

#include "common.h"
#include <stdio.h>
#include <stdbool.h>

const char *param_getlong(int param_index);
const char *param_getshort(int param_index);

void param_print(FILE *stream, int param_index);

long 
param_int (int argc, char **argv, int param_index, long defaultval);

unsigned long
param_uint (int argc, char **argv, int param_index, unsigned long defaultval);

double 
param_double (int argc, char **argv, int param_index, double defaultval);

const char *
param_char (int argc, char **argv, int param_index, const char *defaultval);

typedef struct {
    const char * label;
    int value;
} param_select_type;

int
param_char_select (int argc, char **argv, int param_index, const char *defaultval,
                   const param_select_type *alternatives);

bool param_set(int argc, char **argv, int param_index);


#endif
