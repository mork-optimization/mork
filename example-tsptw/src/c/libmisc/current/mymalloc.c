#include "mymalloc.h"
#include "eprintf.h"
#include <assert.h>

// From:     Prof. Peter Ross,
//           School of Computing, Napier University,
//           10 Colinton Road, Edinburgh EH10 5DT, Scotland
//
// Available at:
//     http://www.dcs.napier.ac.uk/~peter/linux/error-returns.html
//
// mmalloc(bytes,line,file)
// behaves like malloc(bytes) but writes a message and
// exits if there is not enough memory available.
//   This is mainly used via a macro such as
// #define MALLOC(n) mmalloc((n),__LINE__,__FILE__)

void *mmalloc(size_t n, int line, char *file)
{
   void *p = malloc(n);
   if (p == NULL) {
      eprintf("malloc could not get %d bytes: see line %d of file %s\n",
              n, line, file);
   }
   return p;
}
// End of copyright Prof. Peter Ross

#define MYMALLOC_create_shared_(TYPEOFVECTOR)           \
                                                        \
TYPEOFVECTOR *                                          \
m_create_shared_##TYPEOFVECTOR##_vector(                \
    int n, int line, const char*file)                   \
{                                                       \
    TYPEOFVECTOR *p;                                    \
    p = malloc_shared (sizeof(TYPEOFVECTOR) * n);       \
    if (p == NULL)                                      \
        eprintf("cannot create shared " #TYPEOFVECTOR   \
                " vector of size %d: "                  \
                "see line %d of file %s\n",             \
                n, line, file);                         \
    return p;                                           \
}                                                       \
                                                        \
TYPEOFVECTOR **                                         \
m_create_shared_##TYPEOFVECTOR##_matrix (               \
int dim1, int dim2, int line, const char*file)          \
{                                                       \
    TYPEOFVECTOR **p;                                   \
    int i;                                              \
                                                        \
    p = malloc_shared (sizeof(TYPEOFVECTOR) * dim1 * dim2      \
                + sizeof(TYPEOFVECTOR *) * dim1 );      \
    if (p == NULL)                                      \
        eprintf("cannot create shared " #TYPEOFVECTOR   \
                " matrix of size (%d x %d): "           \
                "see line %d of file %s\n",             \
                dim1, dim2, line, file);                \
    for (i = 0; i < dim1; i++)                          \
        p[i] = (TYPEOFVECTOR *) (p + dim1) + i * dim2;  \
    return p;                                           \
}                                                       

#define MYMALLOC_create_(TYPEOFVECTOR)                  \
                                                        \
TYPEOFVECTOR * m_create_##TYPEOFVECTOR##_vector(        \
    int n, int line, const char*file)                   \
{                                                       \
    TYPEOFVECTOR *p;                                    \
    p = malloc (sizeof(TYPEOFVECTOR) * n);              \
    if (p == NULL)                                      \
        eprintf("cannot create " #TYPEOFVECTOR          \
                " vector of size %d: "                  \
                "see line %d of file %s\n",             \
                n, line, file);                         \
    return p;                                           \
}                                                       \
                                                        \
TYPEOFVECTOR ** m_create_##TYPEOFVECTOR##_matrix (      \
int dim1, int dim2, int line, const char*file)          \
{                                                       \
    TYPEOFVECTOR **p;                                   \
    int i;                                              \
                                                        \
    p = malloc (sizeof(TYPEOFVECTOR) * dim1 * dim2      \
                + sizeof(TYPEOFVECTOR *) * dim1);       \
    if (p == NULL)                                      \
        eprintf("cannot create " #TYPEOFVECTOR          \
                " matrix of size (%d x %d): "           \
                "see line %d of file %s\n",             \
                dim1, dim2, line, file);                \
    for (i = 0; i < dim1; i++)                          \
        p[i] = (TYPEOFVECTOR *) (p + dim1) + i * dim2;  \
    return p;                                           \
}                                                       \
MYMALLOC_create_shared_(TYPEOFVECTOR)                   \
double                                                              \
matrix_##TYPEOFVECTOR##_avg (TYPEOFVECTOR **matrix, int dim1, int dim2) \
{                                                                       \
    int i,j;                                                            \
    double sum, total;                                                  \
                                                                        \
    total = 0;                                                          \
    for (i = 0; i < dim1; i++) {                                        \
        sum = 0;                                                        \
        for (j = 0; j < dim2; j++)                                      \
            sum += matrix[i][j];                                        \
        total += sum;                                                   \
    }                                                                   \
    return ((double)total / (double)(dim1 * dim2));                     \
}                                                                       \
void                                                                    \
matrix_##TYPEOFVECTOR##_init (TYPEOFVECTOR **matrix, int dim1, int dim2, \
                              TYPEOFVECTOR init_value)                  \
{                                                                       \
    int  i, j;                                                          \
    for (i = 0; i < dim1; i++) {                                        \
        for (j = 0; j < dim2; j++) {                                    \
            matrix[i][j] = init_value;                                  \
        }                                                               \
    }                                                                   \
}                                                                       \
void                                                                    \
vector_##TYPEOFVECTOR##_fprint_fmt (FILE *stream, const TYPEOFVECTOR *vector, \
                                    int size, const char *fmt)          \
{                                                                       \
    int i;                                                              \
                                                                        \
    if (size < 1) return;                                               \
                                                                        \
    fprintf (stream, fmt, vector[0]);                                   \
                                                                        \
    for (i = 1; i < size; i++) {                                        \
        fprintf (stream, " ");                                          \
        fprintf (stream, fmt, vector[i]);                               \
    }                                                                   \
}                                                                       \
                                                                        \
void                                                                    \
matrix_##TYPEOFVECTOR##_fprint_fmt (FILE *stream, TYPEOFVECTOR **matrix, \
                                    int dim1, int dim2, const char *fmt) \
{                                                                       \
    int i, k;                                                           \
                                                                        \
    if (dim1 < 1 && dim2 < 1) return;                                   \
                                                                        \
    for (i = 0; i < dim1; i++) {                                        \
        fprintf (stream, fmt, matrix[i][0]);                            \
        for (k = 1; k < dim2; k++) {                                    \
            fprintf (stream, " ");                                      \
            fprintf (stream, fmt, matrix[i][k]);                        \
        }                                                               \
        fprintf (stream, "\n");                                         \
    }                                                                   \
}                                                                       \
/*    
      FUNCTION:       auxiliary routine for sorting an integer array  
      INPUT:          two arraya, two indices
      OUTPUT:         none
      (SIDE)EFFECTS:  elements at position i and j of the two arrays are swapped
*/                                                                      \
static void                                                             \
swap2_##TYPEOFVECTOR (TYPEOFVECTOR v[], int v2[], int i, int j)         \
{                                                                       \
    TYPEOFVECTOR tmp;                                                   \
    int tmp2;                                                           \
                                                                        \
    tmp = v[i];                                                         \
    v[i] = v[j];                                                        \
    v[j] = tmp;                                                         \
                                                                        \
    tmp2 = v2[i];                                                       \
    v2[i] = v2[j];                                                      \
    v2[j] = tmp2;                                                       \
}                                                                       \
/*    
      FUNCTION:       recursive routine (quicksort) for sorting one array; second 
                      arrays does the same sequence of swaps  
      INPUT:          two arrays, two indices
      OUTPUT:         none
      (SIDE)EFFECTS:  elements at position i and j of the two arrays are swapped
*/                                                                      \
void                                                                    \
sort2_##TYPEOFVECTOR##_inc(TYPEOFVECTOR v[], int v2[], int left, int right) \
{                                                                       \
    int k, last;                                                        \
                                                                        \
    if (left >= right)                                                  \
        return;                                                         \
    swap2_##TYPEOFVECTOR (v, v2, left, (left + right)/2);               \
    last = left;                                                        \
    for (k =left + 1; k <= right; k++)                                  \
        if (v[k] < v[left])                                             \
            swap2_##TYPEOFVECTOR (v, v2, ++last, k);                    \
    swap2_##TYPEOFVECTOR (v, v2, left, last);                           \
    sort2_##TYPEOFVECTOR##_inc (v, v2, left, last);                     \
    sort2_##TYPEOFVECTOR##_inc (v, v2, last + 1, right);                \
}                                                                       \


#define VECTOR_INT_FUNCTIONS_(TYPEOFVECTOR)                                \
/* Check if vector is a permutation between [0, size] */               \
int vector_##TYPEOFVECTOR##_find_duplicate                                 \
(const TYPEOFVECTOR *vector, int size)                                     \
{                                                                          \
    int i;                                                              \
    int pos = -1;                                                       \
    /* FIXME: This implementation will break if any value in the        
       vector is >= size. This should be implemented as sorting +       
       linear checking for duplicates, keeping a vector of indexes on   
       the side to be able to return the correct position.  */          \
    bool *used = create_bool_vector (size);                             \
    vector_bool_init (used, size, false);                               \
    for (i = 0; i < size; i++) {                                        \
        assert (vector[i] >= 0);                                        \
        assert (vector[i] < size);                                      \
        if (used[vector[i]]) { pos = i; goto finish; }                  \
        else used[vector[i]] = true;                                    \
    }                                                                   \
finish:                                                                 \
    free (used);                                                        \
    return pos;                                                         \
}                                                                       \

MYMALLOC_create_(bool)
MYMALLOC_create_(int)
VECTOR_INT_FUNCTIONS_ (int)
MYMALLOC_create_(char)
MYMALLOC_create_(ulong)
MYMALLOC_create_(float)
MYMALLOC_create_(double)
MYMALLOC_create_(longlong)
MYMALLOC_create_(int_fast64_t)
#undef MYMALLOC_create_
#undef MYMALLOC_create_shared_
#undef VECTOR_INT_FUNCTIONS_

#define matrix_fprint(PRINT_FUN_NAME,TYPEOFVECTOR,FMT)       \
void vector_##PRINT_FUN_NAME##_fprint                               \
(FILE *stream, const TYPEOFVECTOR *vector, int dim1)                    \
{                                                                       \
    vector_##TYPEOFVECTOR##_fprint_fmt (stream, vector, dim1, FMT);     \
}                                                                       \
void vector_##PRINT_FUN_NAME##_print                                    \
(const TYPEOFVECTOR *vector, int dim1)                                  \
{                                                                       \
    vector_##TYPEOFVECTOR##_fprint (stdout, vector, dim1);              \
}                                                                       \
void matrix_##PRINT_FUN_NAME##_fprint                                   \
(FILE *stream, TYPEOFVECTOR **matrix, int dim1, int dim2)               \
{                                                                       \
    matrix_##TYPEOFVECTOR##_fprint_fmt (stream, matrix, dim1, dim2, FMT); \
}                                                                       \
void matrix_##PRINT_FUN_NAME##_print                                    \
(TYPEOFVECTOR **matrix, int dim1, int dim2)                             \
{                                                                       \
    matrix_##TYPEOFVECTOR##_fprint (stdout, matrix, dim1, dim2);        \
}                                                                             

matrix_fprint (bool, bool, "%d")
matrix_fprint (int, int, "%d")
matrix_fprint (longlong, longlong, "%lld")
matrix_fprint (double, double, "%f")
matrix_fprint (int_fast64_t, int_fast64_t, "%"PRIdFAST64 )
#undef matrix_fprint


#define matrix_read_(MATRIX_READ_FUN_NAME,TYPEOFVECTOR,TYPE_FMT)           \
                                                                           \
TYPEOFVECTOR **                                                            \
matrix_##MATRIX_READ_FUN_NAME##_read (FILE *input, int dim1, int dim2)     \
{                                                                              \
    int i, j;                                                                  \
    TYPEOFVECTOR **matrix = malloc (sizeof(TYPEOFVECTOR) * dim1 * dim2         \
                                    + sizeof(TYPEOFVECTOR *) * dim1);          \
    if (matrix == NULL) {                                                      \
        eprintf ("cannot create matrix of size (%d x %d): "                \
                 "see line %d of file %s\n",                               \
                 dim1, dim2, __LINE__, __FILE__);                          \
    }                                                                      \
                                                                           \
    for (i = 0; i < dim1; i++) {                                           \
        matrix[i] = (TYPEOFVECTOR *)(matrix + dim1) + i * dim2;            \
        for (j = 0; j < dim2; j++) {                                       \
            if (fscanf (input, TYPE_FMT, &(matrix[i][j])) < 0) {           \
                eprintf ("%s:error reading (%d, %d) in data file\n",       \
                         __FUNCTION__, i, j);                              \
            }                                                              \
        }                                                                  \
    }                                                                      \
    return matrix;                                                         \
}

matrix_read_(longlong, long long, "%lld")
matrix_read_(int, int, "%d")
matrix_read_(int_fast64_t, int_fast64_t, "%"SCNdFAST64)
#undef matrix_read_

#define matrix_is_symmetric_(TYPEOFVECTOR)                                    \
                                                                              \
bool                                                                       \
matrix_##TYPEOFVECTOR##_is_symmetric (TYPEOFVECTOR **matrix, int dim)      \
{                                                                          \
    for (int i = 0; i < dim - 1; i++)                                      \
        for (int j = i + 1; j < dim; j++)                                  \
            if (matrix[i][j] != matrix[j][i])                              \
                return false;                                              \
    return true;                                                           \
}
matrix_is_symmetric_(longlong)
matrix_is_symmetric_(int)
matrix_is_symmetric_(int_fast64_t)
#undef matrix_is_symmetric_

/*
 * Quick Intializers
 *
 */

void init_int_matrix(int **p, int dim1, int dim2, const int init_value)
{
    int  i, j;
    for (i = 0; i < dim1; i++)
        for (j = 0; j < dim2; j++)
            p[i][j] = init_value;
}

void init_double_matrix(double **p, int dim1, int dim2,
                        const double init_value)
{
    int  i, j;
    for (i = 0; i < dim1; i++)
        for (j = 0; j < dim2; j++)
            p[i][j] = init_value;
}


//------------------------------------------------------------------
// FIXME: Cleanup unused. Templatize useful ones.
#if 0

void print_ld_matrix(int **matrix, int matrix_dimension, char *titel, char *row_titel)
/*DONE*/
{
  int        i, j;

  printf ("\n\n%s \n", titel);
  for (i = 0; i < matrix_dimension; i++) {
    printf ("%s (%d):  ", row_titel, i);
    for (j = 0; j < matrix_dimension; j++) {
      printf (" %d ", matrix[i][j]);
    }
    printf ("\n");
  }
}

int calc_hash_value(int *vector, int vector_size)
{
  int i, hash_value=0;

  for (i=0; i<vector_size; i++)
  {
    hash_value = (hash_value + vector[i] * i) % INT_MAX;
  }

  return hash_value;
}

double mean_double_value(double *vector, int vector_size)
{
  int last_mean_sum_iteration=0;
  double sum=0;
  int i;
  double mean_solution_value=0.;

//  DEBUG(printf("\n"); print_double_vector(vector, vector_size);)
  for (i=0; i<vector_size; i++)
  {
    if (sum + vector[i] < sum)
    {
      /* overload */
      if (last_mean_sum_iteration == 0)
      {
        mean_solution_value = sum / i;

        last_mean_sum_iteration = i;
        sum = vector[i];
      }
      else
      {
        mean_solution_value =
          (mean_solution_value / i) * last_mean_sum_iteration +
          ((double) sum / i);

        last_mean_sum_iteration = i;
        sum = vector[i];
      }
    }
    else
    {
      sum += vector[i];
    }
  }

  /* now last time to calculate mean value */
  if (last_mean_sum_iteration == 0)
  {
    mean_solution_value = (double) sum / vector_size;
  }
  else
  {
    mean_solution_value = (double)
      mean_solution_value / vector_size * last_mean_sum_iteration +
      (double) sum / vector_size;
  }

//  DEBUG(printf("\nmean: %f", mean_solution_value); WAIT;)
  return mean_solution_value;
}

double mean_int_value(int *vector, int vector_size)
{
  int last_mean_sum_iteration=0;
  int sum=0;
  int i;
  double mean_solution_value=0.;

//  DEBUG(printf("\n"); print_vector(vector, vector_size);)
  for (i=0; i<vector_size; i++)
  {
    if (sum + vector[i] < sum)
    {
      /* overload */
      if (last_mean_sum_iteration == 0)
      {
        mean_solution_value = (double) sum / i;

        last_mean_sum_iteration = i;
        sum = vector[i];
      }
      else
      {
        mean_solution_value =
          (( mean_solution_value / i) * last_mean_sum_iteration) +
          ( (double) sum / i );

        last_mean_sum_iteration = i;
        sum = vector[i];
      }
    }
    else
    {
      sum += vector[i];
    }
  }

  /* now last time to calculate mean value */
  if (last_mean_sum_iteration == 0)
  {
    mean_solution_value = (double) sum / vector_size;
  }
  else
  {
    mean_solution_value = (double)
      mean_solution_value / vector_size * last_mean_sum_iteration +
      (double) sum / vector_size;
  }

//  DEBUG(printf("\nmean: %f", mean_solution_value); WAIT;)
  return mean_solution_value;
}
double varianz ( int *vector, int vector_size, int mean_value )
{
  int i;
  int sum=0;

//  DEBUG(print_vector(vector, vector_size);)


  for ( i = 0 ; i < vector_size ; i++ ) {
    sum += sqr(vector[i] - mean_value);
  }

//  DEBUG(printf("\nVarianz: %lf (%li, %li)", (double) sum / vector_size, sum, mean_value);)
  return ((double) sum / vector_size);
}

void copy_vector_from_to ( int *v, int *w, int vector_size )
{
  int i;

  for ( i = 0 ; i < vector_size ; i++ ) {
    w[i] = v[i];
  }
}


void swap_vector_items( int pos_1, int pos_2, int *q ) {
/*
      FUNCTION:      swap items at positions pos_1 and pos_2
      INPUT:         positions 1 and 2, pointer to current assignment
      OUTPUT:        none
      (SIDE)EFFECTS: current assignment is modified
*/
  int  help;

  help     = q[pos_1];
  q[pos_1] = q[pos_2];
  q[pos_2] = help;
}
#endif
