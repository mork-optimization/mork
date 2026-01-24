#include "parameter.h"
#include "eprintf.h"

#include <assert.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>
#include <errno.h>
extern int errno;

#ifndef HAVE_STREQUAL
#define HAVE_STREQUAL
static inline int strequal(const char *a, const char *b)
{
    return (!strcmp (a, b));
}
#endif

#define PARAM_MATCH(ARGSTR, PARAM_INDEX)                 \
((PARAMETERS[(PARAM_INDEX)][0] != NULL                   \
 && strequal (ARGSTR, PARAMETERS[PARAM_INDEX][0]))       \
|| (PARAMETERS[PARAM_INDEX][1] != NULL                   \
    && strequal (ARGSTR, PARAMETERS[PARAM_INDEX][1])))

extern const char * const PARAMETERS[][3];
//-------- EXAMPLE OF PARAMETERS ----------------------------
/*-----------------------------------------------------------
enum {   PARAM_HELP
       , PARAM_VERSION
       , PARAM_INPUT
       , PARAM_SEED
       , PARAM_OUTPUT
       , PARAM_COUNT
};

const char *(PARAMETERS[PARAM_COUNT][3]) = {
[PARAM_HELP] =
{"-h", "--help", "prints this information and exits"},
[PARAM_VERSION] =
{"-v", "--version",   "prints version and exits"},
[PARAM_INPUT] =
{"-i", "--input",     "FILE     data file"},
[PARAM_SEED] =
{"-s", "--seed",      "INT:>0  Seed for random number generator"},
[PARAM_OUTPUT] =
{"-o", "--output",    "FILE     report file"},
};
-----------------------------------------------------------*/
//------------------------------------------------

void print_function_call(int argc, char **argv);
void checkOutOfRange(int value, int MIN, int MAX, char *optionName);


const char *param_getlong(int param_index)
{
    return(PARAMETERS[param_index][1]);
}
const char *param_getshort(int param_index)
{
    return(PARAMETERS[param_index][0]);
}

void param_print(FILE *stream, int param_index)
{
    if (PARAMETERS[param_index][0] == NULL)
        fprintf(stream, " %-3s %-14s %s\n",
                "",
                PARAMETERS[param_index][1],
                PARAMETERS[param_index][2]);
    else if (PARAMETERS[param_index][1] == NULL)
        fprintf(stream, " %-3s %-14s %s\n",
                PARAMETERS[param_index][0],
                "",
                PARAMETERS[param_index][2]);
    else
        fprintf(stream, " %-2s, %-14s %s\n",
                PARAMETERS[param_index][0],
                PARAMETERS[param_index][1],
                PARAMETERS[param_index][2]);
}

long int
param_int(int argc, char **argv, int param_index, long int defaultval)
{
    int i;
    long int value;
    char *endptr;

    for(i=1; i < argc; i++) {
        if (PARAM_MATCH (argv[i], param_index)) {
            i++;
            if (i < argc) {
                errno = 0;
                value = strtol(argv[i], &endptr,10);
                if (errno == 0 && argv[i] != endptr) {
                    argv[i-1] = "";
                    argv[i] = "";
                    return value;
                }
                fprintf(stderr,"Error in parameter (%s): %s\n",
                        argv[i-1], argv[i]);
            } else {
                fprintf(stderr,"Missing value for parameter (%s)\n",
                        argv[i-1]);
            }
            exit(1);
        }
    }
    return(defaultval);
}

unsigned long int
param_uint(int argc, char **argv, int param_index, unsigned long defaultval)
{
    int i;
    unsigned long int value;
    char *endptr;

    for(i=1; i < argc; i++) {
        if (PARAM_MATCH (argv[i], param_index)) {
            i++;
            if(i < argc) {
                errno=0;
                value = strtoul(argv[i],&endptr,10);
                if ( errno == 0 && argv[i] != endptr ) {
                   argv[i-1] = "";
                   argv[i] = "";
                   return(value);
                }
                fprintf(stderr,"Error in parameter (%s): %s\n",
                        argv[i-1], argv[i]);
            } else {
                fprintf(stderr,"Missing value for parameter (%s)\n",
                        argv[i-1]);
            }
            exit(1);
        }
    }
    return(defaultval);
}

double
param_double(int argc, char **argv, int param_index, double defaultval)
{
    int i;
    double value;
    char *endptr;

    for (i = 1; i < argc; i++) {
        if (PARAM_MATCH (argv[i], param_index)) {
            i++;
            if (i < argc) {
                errno = 0;
                value = strtod(argv[i], &endptr);
                if (errno == 0 && argv[i] != endptr && endptr[0] == '\0') {
                    argv[i-1] = "";
                    argv[i] = "";
                    return value;
                }
                fprintf(stderr,
                        "Parameter '%s' is not a floating-point number: %s\n",
                        argv[i-1], argv[i]);
            } else {
                fprintf(stderr, "Missing value for parameter '%s'\n",
                        argv[i-1]);
            }
            exit(1);
        }
    }
    return defaultval;
}

const char *
param_char(int argc, char **argv, int param_index, const char *defaultval)
{
    int i;
    char *value;

    for (i = 1; i < argc; i++) {
        if (PARAM_MATCH (argv[i], param_index)) {
            i++;
            if (i < argc) {
                value = argv[i];
                argv[i-1] = "";
                argv[i] = "";
                return value;
            } 
            eprintf ("missing value for parameter `%s'!\n",
                     param_getlong (param_index));
        }
    }
    return defaultval;
}

int
param_char_select (int argc, char **argv, int param_index,
                   const char *defaultval,
                   const param_select_type *alternatives)
{
    int i;
    const char *val = param_char (argc, argv, param_index, defaultval);

    if (defaultval == val && defaultval == NULL) {
        for (i = 0; alternatives[i].label; i++) ;
        return alternatives[i].value;
    }
    assert (val);

    for (i = 0; alternatives[i].label; i++) {
        if (strequal (val, alternatives[i].label))
            return alternatives[i].value;
    }
    eprintf ("unknown option `%s' for parameter %s!\n",
             val, param_getlong (param_index));
}


bool
param_set(int argc, char **argv, int param_index)
{
    int i;

    for(i=1; i < argc; i++) {
        if (PARAM_MATCH (argv[i], param_index)) {
            argv[i]="";
            return true;
        }
    }
    return false;
}


void print_function_call(int argc, char **argv)
{
    long int i;

    for (i=0; i< argc; i++)
        printf("\nArgument(%li): %s",i,argv[i]);
}

void checkOutOfRange(int value, int min, int max, char *optionName)
{
    if ((value < min)||(value > max)){
        fprintf(stderr,"Error: Option `%s' out of range\n",optionName);
        exit(1);
    }

}
