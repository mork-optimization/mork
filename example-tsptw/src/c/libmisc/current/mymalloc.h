#ifndef _MY_MALLOC_H_
#define _MY_MALLOC_H_

#include "common.h"

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

__BEGIN_DECLS

void *mmalloc(size_t n, int line, char *file) __malloc;

#define mymalloc(n) (mmalloc((n),__LINE__,__FILE__))

#include <sys/mman.h>
#define malloc_shared(SIZE) mmap(0, SIZE, PROT_READ | PROT_WRITE, MAP_SHARED | MAP_ANON, -1,0);


//------------ vectors -------------------------------------
#define create_bool_vector(n) \
(m_create_bool_vector((n),__LINE__,__FILE__))

#define create_int_vector(n) \
(m_create_int_vector((n),__LINE__,__FILE__))

#define create_ulong_vector(n) \
(m_create_ulong_vector((n),__LINE__,__FILE__))

#define create_float_vector(n) \
(m_create_float_vector((n),__LINE__,__FILE__))

#define create_double_vector(n) \
(m_create_double_vector((n),__LINE__,__FILE__))

#define create_char_vector(n) \
(m_create_char_vector((n),__LINE__,__FILE__))

#define create_str_vector(vector_size, string_length) \
(m_create_char_matrix((vector_size),(string_length),__LINE__,__FILE__))

#define create_longlong_vector(n) \
(m_create_longlong_vector((n),__LINE__,__FILE__))

#define create_int_fast64_t_vector(n) \
(m_create_int_fast64_t_vector((n),__LINE__,__FILE__))
#define create_int_fast64_t_matrix(dim1, dim2) \
(m_create_int_fast64_t_matrix((dim1), (dim2),__LINE__,__FILE__))
#define create_shared_int_fast64_t_vector(n) \
(m_create_shared_int_fast64_t_vector((n),__LINE__,__FILE__))
#define create_shared_int_fast64_t_matrix(dim1, dim2) \
(m_create_shared_int_fast64_t_matrix((dim1), (dim2),__LINE__,__FILE__))

//--------- matrices ---------------------------------------

#define create_bool_matrix(dim1, dim2) \
(m_create_bool_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_int_matrix(dim1, dim2) \
(m_create_int_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_char_matrix(dim1, dim2) \
(m_create_char_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_ulong_matrix(dim1, dim2) \
(m_create_ulong_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_double_matrix(dim1, dim2) \
(m_create_double_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_float_matrix(dim1, dim2) \
(m_create_float_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_longlong_matrix(dim1, dim2) \
(m_create_longlong_matrix((dim1), (dim2),__LINE__,__FILE__))


#define mymalloc_create_shared_(TYPEOFVECTOR)    \
TYPEOFVECTOR *                                         \
m_create_shared_##TYPEOFVECTOR##_vector (int n, int line,               \
                                         const char*file) __malloc;     \
TYPEOFVECTOR **                                                         \
m_create_shared_##TYPEOFVECTOR##_matrix (int dim1, int dim2,            \
                                         int line, const char*file) __malloc; \


#define mymalloc_create_(TYPEOFVECTOR)                              \
void                                                                \
sort2_##TYPEOFVECTOR##_inc (TYPEOFVECTOR v[], int v2[], int left, int right); \
double                                                                  \
matrix_##TYPEOFVECTOR##_avg (TYPEOFVECTOR **matrix, int dim1, int dim2); \
void                                                                    \
matrix_##TYPEOFVECTOR##_init (TYPEOFVECTOR **matrix, int dim1, int dim2, \
                              TYPEOFVECTOR init_value);                 \
void vector_##TYPEOFVECTOR##_fprint_fmt                                 \
(FILE *stream, const TYPEOFVECTOR *vector, int size, const char*fmt);   \
void vector_##TYPEOFVECTOR##_fprint                                     \
(FILE *stream, const TYPEOFVECTOR *vector, int size);                   \
void vector_##TYPEOFVECTOR##_print                                      \
(const TYPEOFVECTOR *vector, int size);                                 \
void matrix_##TYPEOFVECTOR##_fprint_fmt                                 \
(FILE *stream, TYPEOFVECTOR **matrix, int dim1, int dim2, const char*fmt); \
void matrix_##TYPEOFVECTOR##_fprint                                     \
(FILE *stream, TYPEOFVECTOR **matrix, int dim1, int dim2);              \
void                                                                    \
matrix_##TYPEOFVECTOR##_print(TYPEOFVECTOR **matrix, int dim1, int dim2); \
mymalloc_create_shared_ (TYPEOFVECTOR)                                  \
TYPEOFVECTOR *                                                          \
m_create_##TYPEOFVECTOR##_vector (int n,                                \
                                  int line, const char*file) __malloc;  \
TYPEOFVECTOR **                                                         \
m_create_##TYPEOFVECTOR##_matrix (int dim1, int dim2,                   \
                                  int line, const char*file) __malloc;  \
static inline void                                                      \
vector_##TYPEOFVECTOR##_init (TYPEOFVECTOR * vector, int size,          \
                              const TYPEOFVECTOR value)                 \
{                                                                       \
    int i;                                                              \
    for (i = 0; i < size; i++)                                          \
        vector[i] = value;                                              \
}                                                                       \
                                                                        \
static inline TYPEOFVECTOR *                                            \
vector_##TYPEOFVECTOR##_copy (TYPEOFVECTOR *dest, const TYPEOFVECTOR *scr, \
                              int vector_size)                          \
{                                                                       \
    int i;                                                              \
    for (i = 0; i < vector_size; i++)                                   \
        dest[i] = scr[i];                                               \
    return dest;                                                        \
}                                                                       \

#define VECTOR_INT_FUNCTIONS_(TYPEOFVECTOR)                             \
int vector_##TYPEOFVECTOR##_find_duplicate                              \
(const TYPEOFVECTOR *vector, int size);                                 \

mymalloc_create_ (bool)
mymalloc_create_ (char)
mymalloc_create_ (int)
VECTOR_INT_FUNCTIONS_ (int)
mymalloc_create_ (uint)
mymalloc_create_ (ulong)
mymalloc_create_ (double)
mymalloc_create_ (float)
mymalloc_create_ (longlong)
mymalloc_create_ (int_fast64_t)
#undef mymalloc_create_
#undef mymalloc_create_shared_
#undef VECTOR_INT_FUNCTIONS_

#define matrix_read_(TYPEOFVECTOR,TYPE_FMT)               \
                                                                           \
TYPEOFVECTOR **                                                            \
matrix_##TYPEOFVECTOR##_read (FILE *input, int dim1, int dim2); 

matrix_read_(longlong, "%lld")
matrix_read_(int, "%d")
matrix_read_(int_fast64_t, SCNdFAST64)
#undef matrix_read_

#define matrix_is_symmetric_(TYPEOFVECTOR)               \
                                                         \
bool                                                                   \
matrix_##TYPEOFVECTOR##_is_symmetric (TYPEOFVECTOR **matrix, int dim); 

matrix_is_symmetric_(longlong)
matrix_is_symmetric_(int)
matrix_is_symmetric_(int_fast64_t)
#undef matrix_is_symmetric_


//------------ vectors -------------------------------------
#define create_shared_int_vector(n) \
(m_create_shared_int_vector((n),__LINE__,__FILE__))

#define create_shared_ulong_vector(n) \
(m_create_shared_ulong_vector((n),__LINE__,__FILE__))

#define create_shared_float_vector(n) \
(m_create_shared_float_vector((n),__LINE__,__FILE__))

#define create_shared_double_vector(n) \
(m_create_shared_double_vector((n),__LINE__,__FILE__))

#define create_shared_char_vector(n) \
(m_create_shared_char_vector((n),__LINE__,__FILE__))

#define create_shared_str_vector(vector_size, string_length) \
(m_create_shared_char_matrix((vector_size),(string_length),__LINE__,__FILE__))

#define create_shared_longlong_vector(n) \
(m_create_shared_longlong_vector((n),__LINE__,__FILE__))


//--------- matrices ---------------------------------------
#define create_shared_int_matrix(dim1, dim2) \
(m_create_shared_int_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_shared_char_matrix(dim1, dim2) \
(m_create_shared_char_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_shared_ulong_matrix(dim1, dim2) \
(m_create_shared_ulong_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_shared_double_matrix(dim1, dim2) \
(m_create_shared_double_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_shared_float_matrix(dim1, dim2) \
(m_create_shared_float_matrix((dim1), (dim2),__LINE__,__FILE__))

#define create_shared_longlong_matrix(dim1, dim2) \
(m_create_shared_longlong_matrix((dim1), (dim2),__LINE__,__FILE__))

#define init_int_vector vector_int_init
#define init_double_vector vector_double_init

void init_int_vector(int *p, int n, const int init_value);
void init_int_matrix(int **p, int dim1, int dim2,
                     const int init_value);
void init_double_vector(double *p, int n, const double init_value);
void init_double_matrix(double **p, int dim1, int dim2,
                        const double init_value);
__END_DECLS
#endif
