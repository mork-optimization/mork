#include <assert.h>
#include <float.h> /* DBL_MAX */
#include <math.h> /* sd(): sqrt */
#include "work.h"
#include "mymalloc.h"
#include "eprintf.h"

double mean(const double data[], const size_t size)
{
    /* Compute the arithmetic mean of a dataset using the recurrence
       relation mean_(n) = mean(n-1) + (data[n] - mean(n-1))/(n+1)  */

    double mean = 0;
    size_t i;

    for (i = 0; i < size; i++) {
        mean += (data[i] - mean) / (i + 1);
    }

    return mean;
}

double variance(const double data[], const size_t n, const double mean)
{
    double variance = 0 ;
    size_t i;

    /* find the sum of the squares */
    for (i = 0; i < n; i++)  {
        const double delta = (data[i] - mean);
        variance += (delta * delta - variance) / (i + 1);
    }
    return variance ;
}

double sd(const double data[], const size_t n, const double mean)
{
    return sqrt(variance(data, n, mean));
}




int *
sort_double_order_index (double *vector, int vector_size, bool increasing)
/* giving the indizes of the ordered values of a vector */
{
    int        i, j, k;
    double     *sorted_vector; 
    int        *result_vector;
    double     help;

    /* copy vector */
    result_vector = create_int_vector(vector_size);
    sorted_vector = create_double_vector(vector_size);

    if (increasing) {
        for (i = 0; i < vector_size; i++)
            sorted_vector[i] = vector[i];
    } else {
        for (i = 0; i < vector_size; i++)
            sorted_vector[i] = -vector[i];
    }

    /* determine the sort order of the vector */
    for (i = 0; i < vector_size; i++) {
        help = DBL_MAX;
        k = 0;
        for (j = 0; j < vector_size; j++) {
            if (help > sorted_vector[j]) {
                help = sorted_vector[j];
                k = j;
            }
        }
        assert (k < vector_size);
        result_vector[i] = k;
        sorted_vector[k] = DBL_MAX;
    }
    free (sorted_vector);
    return result_vector;
}


#if 0
char **create_str_vector(int vector_size, int string_length);

char **create_str_vector(int vector_size, int string_length)
{
  char      **matrix;
  int        i;

  matrix =
      (char **) MALLOC (sizeof (char) * vector_size *
                        string_length + sizeof (char *) * vector_size);

  for (i = 0; i < vector_size; i++) {
    matrix[i] = (char *) (matrix + vector_size) + i * string_length;
  }
  return (matrix);
}

void print_double_vector( double *p, int vector_size ) {
/*
      FUNCTION:      prints solution p
      INPUT:         pointer to the solution, vector_size
      OUTPUT:        none
      (SIDE)EFFECTS: none
*/
  int i;

  for ( i = 0 ; i < vector_size ; i++ ) {
    printf(" %f",p[i]);
  }
}

void print_ld_matrix (int **matrix, int matrix_dimension, char *titel, char *row_titel)
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

void print_double_matrix (double **matrix, int matrix_dimension, char *titel, char *row_titel)
/*DONE*/
{
  int        i, j;

  printf ("\n\n%s \n", titel);
  for (i = 0; i < matrix_dimension; i++) {
    printf ("%s (%d):  ", row_titel, i);
    for (j = 0; j < matrix_dimension; j++) {
      printf (" %.2f ", matrix[i][j]);
    }
    printf ("\n");
  }
}

int *sort_order_index (int *vector, int vector_size, int increasing)
  /* giving the indizes of the ordered values of a vector */
{
  int        i, j, k;
  int        *sorted_vector, *result_vector;
  int        help;

  /* copy vector */
  result_vector = (int *) create_int_vector(vector_size);
  sorted_vector = (int *) create_int_vector(vector_size);

  if (increasing == FALSE)
  {
    for (i = 0; i < vector_size; i++)
      sorted_vector[i] = -vector[i];
  } else {
    for (i = 0; i < vector_size; i++)
      sorted_vector[i] = vector[i];
  }

  /* determine the sort order of the vector */
  for (i = 0; i < vector_size; i++) {
    help = LONG_MAX;
    k = 0;
    for (j = 0; j < vector_size; j++) {
      if (help > sorted_vector[j]) {
	      help = sorted_vector[j];
	      k = j;
      }
    }
    assert (k < vector_size);
    result_vector[i] = k;
    sorted_vector[k] = LONG_MAX;
  }

  free(sorted_vector);
  return(result_vector);
}

int best_of_int_vector(int *vector,int vector_size, int *index_of_best) {

  int min;
  int   k;

/*  DEBUG(
    printf("\nVor Sortierung: ");
    print_vector(vector, vector_size);
  )
  */

  *index_of_best = 0;
  min = vector[0];
  for( k = 1 ; k < vector_size ; k++ ) {
    if(vector[k] < min){
      min = vector[k];
      *index_of_best = k;
    }
  }

  return min;
}

int worst_of_int_vector(int *vector,int vector_size, int *index_of_worst) {

  int max;
  int   k;

/*  DEBUG(
    printf("\nVor Sortierung: ");
    print_vector(vector, vector_size);
  )
*/
  *index_of_worst = 0;
  max = vector[0];
  for( k = 1 ; k < vector_size ; k++ ) {
    if(vector[k] > max){
      max = vector[k];
      *index_of_worst = k;
    }
  }

/*  DEBUG(
    printf("\nmaximum: %li, Index of maximum: %li", max, *index_of_worst);
  )
*/
  return max;
}

int calc_hash_value(int *vector, int vector_size)
{
  int i, hash_value=0;

  for (i=0; i<vector_size; i++)
  {
    hash_value = (hash_value + vector[i] * i) % INT_MAX;
  }

  return (hash_value);
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
  return(mean_solution_value);
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
  return(mean_solution_value);
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
  return((double) sum / vector_size);
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

//------------------------------------------------------------------
#endif
