#ifndef _WORK_H_
#define _WORK_H_

#include "common.h"
#include <stdio.h>
/* These macros evaluate their arguments more than once and, hence,
 * they are easily misused.
 */
#define MAX(x,y)  ((x)>=(y)?(x):(y))
#define MIN(x,y)  ((x)<=(y)?(x):(y))
/*
#define XOR(x,y)  (( (x) && !(y)) || (!(x) && (y)))
#define SQR(x)    ((x)*(x))
*/

/* To avoid the ugly strcmp(a,b) == 0, or the confusing !strcmp(a,b).  */
#ifndef HAVE_STREQUAL
#define HAVE_STREQUAL
#include <string.h>
static inline bool strequal(const char *a, const char *b)
{
    return (strcmp (a, b) == 0);
}
#endif

/* To be used by qsort() and similar.  */
#define TYPE_cmp(TYPE)\
static inline int \
TYPE##_cmp(const void * a, const void * b) \
{                                          \
    if (*((TYPE *)a) < *((TYPE*)b))        \
        return -1;                         \
    else if (*((TYPE *)a) > *((TYPE *)b))  \
        return 1;                          \
    else                                   \
        return 0;                          \
}

TYPE_cmp (int)
TYPE_cmp (double)
TYPE_cmp (longlong)
#undef TYPE_cmp

double mean(const double data[], const size_t size);
double sd(const double data[], const size_t n, const double mean);
double variance(const double data[], const size_t n, const double mean);

int *sort_double_order_index (double *vector, int vector_size, bool increasing);

#if 0
double varianz ( int *vector, int vector_size, int mean_value );
void copy_vector_from_to ( int *v, int *w, int vector_size );
void swap_vector_items( int pos_1, int pos_2, int *q );
void print_double_vector( double *p, int vector_size );
void print_ld_matrix (int **matrix, int matrix_dimension,
                      char *titel, char *row_titel);
void print_double_matrix (double **matrix, int matrix_dimension,
                          char *titel, char *row_titel);
int *sort_order_index (int *vector, int vector_size, int increasing);
int best_of_int_vector(int *vector,int vector_size, int *index_of_best);
int worst_of_int_vector(int *vector,int vector_size, int *index_of_worst);
int calc_hash_value(int *vector, int vector_size);
double mean_double_value(double *vector, int vector_size);
double mean_int_value(int *vector, int vector_size);
#endif

#endif
