#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <mpi.h>

//main matrix's height and width
#define MATRIX_HEIGHT(size_) size_ * 10
#define MATRIX_WIDTH(size_) size_ * 10
#define EPS 0.00001
#define TETTA 0.01

//returns height of one matrix in one process
#define PART_HEIGHT(size) MATRIX_HEIGHT(size) / (size)

int size = 0;

double *multiplyMatrix(double *m1, double *vector) {
	double *result = (double *)malloc(PART_HEIGHT(size)*sizeof(double));
	for (int i = 0; i < PART_HEIGHT(size); i++) {
		for (int j = 0; j < MATRIX_WIDTH(size); j++) {
			result[i] += m1[i * MATRIX_WIDTH(size) + j] * vector[j];
		}
	}
	return result;
}

void printfMatrix(double *m1) {
	for (int i = 0; i < MATRIX_HEIGHT(size); i++) {
		printf("%lf\n",m1[i]);
	}
}

double *MPI_MatrixMultiplication(double *m1, double *vector,int rank) {
	double *result = multiplyMatrix(m1,vector);

	return result;
}

double countNorm(const double *vector) {
	double result = 0;
	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}

double *substract(double *v1, double *v2) {
	for(int i = 0; i < MATRIX_WIDTH(size); i++) {
		v1[i] = v1[i] - v2[i];
	}
	return v1;
}

double *multDouble(double *v1, double k) {
	for(int i =0; i < MATRIX_WIDTH(size); i++) {
		v1[i] *= k;
	}
	return v1;
}

int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int rank;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	
//init matrix
//общий случай
	double *a = (double *)calloc(PART_HEIGHT(size) * MATRIX_WIDTH(size),sizeof(double));
	int rank_addition = rank * PART_HEIGHT(size);
	for (int i = 0; i < PART_HEIGHT(size); i++) {
		for (int j = 0; j < MATRIX_WIDTH(size); j++) {
			if (i == (j - rank_addition)) {
				a[i * MATRIX_WIDTH(size) + j] = 2.0;
			}
			else {
				a[i * MATRIX_WIDTH(size) + j] = 1.0;
			}
		}
	}
	
	double *b = (double *)calloc(MATRIX_WIDTH(size),sizeof(double));
	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		b[i] = MATRIX_HEIGHT(size) + 1;
	}

	double *x = (double *)calloc(MATRIX_WIDTH(size),sizeof(double));

	double result = 1;
	double *big_vector = (double *)calloc(MATRIX_WIDTH(size),sizeof(double));
	double *AxMult = NULL;
	double *AxMinB = NULL;
	while ((EPS < result)) {
		//printf("Process %d result : %f\n",rank,result);
		AxMult = MPI_MatrixMultiplication(a,x,rank);
		MPI_Allgather( AxMult,
					   PART_HEIGHT(size),
					   MPI_DOUBLE,
					   big_vector,
					   PART_HEIGHT(size),
					   MPI_DOUBLE,
					   MPI_COMM_WORLD );
		AxMinB = substract(big_vector,b);
		result = countNorm(AxMinB)/countNorm(b);		

		substract(x,multDouble(AxMinB,TETTA));
	}

	if (rank == 1) printfMatrix(x);
	
	free(a);
	free(b);
	free(x);
	free(big_vector);
	MPI_Finalize();
	return 0;
}