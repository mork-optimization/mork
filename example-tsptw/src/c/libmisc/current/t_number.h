#ifndef T_NUMBER_H
#define T_NUMBER_H

#define T_NUMBER_IS_LONG_LONG 1
#define T_NUMBER_IS_INT 2
#define T_NUMBER_IS_INT_FAST64 3
#define T_NUMBER_IS_DOUBLE 4

#ifndef T_NUMBER_IS
#error You must define T_NUMBER_IS, and then include t_number.h
#endif

#include <limits.h>

#if T_NUMBER_IS == T_NUMBER_IS_LONG_LONG

typedef long long t_number;
#define PRINT_NUMBER "lld"
#define SCAN_NUMBER "lld"
// I do not know why using LONG_LONG_MAX or LLONG_MAX does not work...
#define T_NUMBER_MAX 9223372036854775807LL // LONG_LONG_MAX or LLONG_MAX
#define T_NUMBER_MIN (-T_NUMBER_MAX - 1LL) // LONG_LONG_MIN or LLONG_MIN
#define create_number_vector create_longlong_vector
#define create_number_matrix create_longlong_matrix
#define matrix_number_avg matrix_longlong_avg
#define matrix_number_read matrix_longlong_read
#define matrix_number_is_symmetric matrix_longlong_is_symmetric
#define sort2_number_inc sort2_longlong_inc
#define vector_number_fprint_fmt vector_longlong_fprint_fmt

#elif T_NUMBER_IS == T_NUMBER_IS_INT

typedef int t_number;
#define PRINT_NUMBER "d"
#define SCAN_NUMBER "d"
#define T_NUMBER_MAX INT_MAX
#define T_NUMBER_MIN INT_MIN
#define create_number_vector create_int_vector
#define create_number_matrix create_int_matrix
#define matrix_number_avg matrix_int_avg
#define matrix_number_read matrix_int_read
#define sort2_number_inc sort2_int_inc
#define vector_number_fprint_fmt vector_int_fprint_fmt

#elif T_NUMBER_IS == T_NUMBER_IS_INT_FAST64

#include <inttypes.h>
typedef int_fast64_t t_number;
#define PRINT_NUMBER PRIdFAST64
#define SCAN_NUMBER SCNdFAST64
#define T_NUMBER_MAX INT_FAST64_MAX
#define T_NUMBER_MIN INT_FAST64_MIN
#define create_number_vector create_int_fast64_t_vector
#define create_number_matrix create_int_fast64_t_matrix
#define matrix_number_avg matrix_int_fast64_t_avg
#define matrix_number_read matrix_int_fast64_t_read
#define matrix_number_is_symmetric matrix_int_fast64_t_is_symmetric
#define sort2_number_inc sort2_int_fast64_t_inc
#define vector_number_fprint_fmt vector_int_fast64_t_fprint_fmt

#elif T_NUMBER_IS == T_NUMBER_IS_DOUBLE

#include <float.h>
typedef double t_number;
#define PRINT_NUMBER "16.15g"
#define SCAN_NUMBER "16.15lg"
#define T_NUMBER_MAX DBL_MAX
#define T_NUMBER_MIN -DBL_MAX
#define create_number_vector create_double_vector
#define create_number_matrix create_double_matrix
#define matrix_number_avg matrix_double_avg
#define matrix_number_read matrix_double_read
#define sort2_number_inc sort2_double_inc
#define vector_number_fprint_fmt vector_double_fprint_fmt

#else
#error Unknown definition of T_NUMBER_IS
#endif

#include <stdbool.h>

static inline bool
matrix_number_has_null_diag (t_number **matrix, int size)
{
    for (int i = 0; i < size; i++)
        if (matrix[i][i] != 0)
            return false;
    return true;
}

static inline void
matrix_number_init (t_number **matrix, int dim1, int dim2, t_number init_value)
{
    int  i, j;
    for (i = 0; i < dim1; i++) {
        for (j = 0; j < dim2; j++) {
            matrix[i][j] = init_value;
        }
    }
}

#endif
