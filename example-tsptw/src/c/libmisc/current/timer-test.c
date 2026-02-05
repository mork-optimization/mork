/*************************************************************************

 Test for virtual timer.

 ---------------------------------------------------------------------

 Copyright (c) 2009 Manuel Lopez-Ibanez  <manuel.lopez-ibanez@ulb.ac.be>
 TeX: \copyright 2009 Manuel L{\'o}pez-Ib{\'a}{\~n}ez

 This program is free software (software libre); you can redistribute
 it and/or modify it under the terms of version 2 of the GNU General
 Public License as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, you can obtain a copy of the GNU
 General Public License at:
                 http://www.gnu.org/copyleft/gpl.html
 or by writing to:
           Free Software Foundation, Inc., 59 Temple Place,
                 Suite 330, Boston, MA 02111-1307 USA

 -----------------------------------------------------------------------

  Compile with: gcc -O3 -o timer-test timer-test.c timer.c -lm

  Run with: ./timer-test <TIME_LIMIT_IN_SECONDS>

*************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "timer.h"

double alpha, beta;
int size = 1000;

#define WORK_create_matrix(TYPEOFMATRIX)                \
                                                        \
TYPEOFMATRIX ** m_create_##TYPEOFMATRIX##_matrix (      \
int dim1, int dim2, int line, const char*file)          \
{                                                       \
    TYPEOFMATRIX **p;                                   \
    int i;                                              \
                                                        \
    p = malloc (sizeof(TYPEOFMATRIX) * dim1 * dim2      \
                + sizeof(TYPEOFMATRIX *) * dim1 );      \
    if (p == NULL) {                                    \
        fprintf(stderr, "cannot create " #TYPEOFMATRIX  \
                " matrix of size (%d x %d): "           \
                "see line %d of file %s\n",             \
                dim1, dim2, line, file);                \
        exit(1);                                        \
    }                                                   \
    for (i = 0; i < dim1; i++)                          \
        p[i] = (TYPEOFMATRIX *) (p + dim1) + i * dim2;  \
    return p;                                           \
}                                                       

WORK_create_matrix(int)
WORK_create_matrix(double)
#undef WORK_create_matrix
#define create_double_matrix(dim1,dim2)\
    m_create_double_matrix(dim1,dim2,__LINE__,__FILE__)


int main(int argc, char *argv[])
{
    double **matrix1 = create_double_matrix(size, size);
    double **matrix2 = create_double_matrix(size, size);
    int iterations = 0;
    int i,j;
    if (argc < 2) {
        fprintf (stderr, "%s: a time limit is needed\n", argv[0]);
        exit (1);
    }
    
    double time_limit = atof(argv[1]);

    Timer_start();
    while (Timer_elapsed_virtual() < time_limit) {
        for (i = 0; i < size; i++) {
            for (j = 0; j < size; j++) {
                if (i == j) continue;
                matrix2[i][j] = pow(matrix1[i][j], alpha)
                    * pow(matrix2[j][i], beta);
                matrix1[j][i] = matrix2[i][j];
                iterations++;
            }
        }
    }

    printf("Iterations = %d\n", iterations);
    return 0;
}
