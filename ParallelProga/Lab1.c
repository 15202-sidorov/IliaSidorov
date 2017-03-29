#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <mpi.h>

//main matrix's height and width
#define MATRIX_HEIGHT 4
#define MATRIX_WIDTH  4
#define EPS 0.00001
#define TETTA 0.01

//returns height of one matrix in one process
#define PART_HEIGHT(size) MATRIX_HEIGHT / (size)

double *multiplyMatrix(double *m1, double *vector, int size) {
	double *result = (double *)malloc(PART_HEIGHT(size)*sizeof(double));
	for (int i = 0; i < PART_HEIGHT(size); i++) {
		for (int j = 0; j < MATRIX_WIDTH; j++) {
			result[i] += m1[i * MATRIX_WIDTH + j] * vector[j];
		}
	}
	return result;
}

void printfMatrix(double *m1) {
	for (int i = 0; i < MATRIX_HEIGHT; i++) {
		printf("%lf\n",m1[i]);
	}
}

double *MPI_MatrixMultiplication(double *m1, double *vector, int size, int rank) {
	double *result = multiplyMatrix(m1,vector,size);

	return result;
}

double countNorm(const double *vector) {
	double result = 0;
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}

double *substract(double *v1, double *v2) {
	for(int i = 0; i < MATRIX_WIDTH; i++) {
		v1[i] = v1[i] - v2[i];
	}
	return v1;
}

double *multDouble(double *v1, double k) {
	for(int i =0; i < MATRIX_WIDTH; i++) {
		v1[i] *= k;
	}
	return v1;
}

int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int size;
	int rank;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	
//init matrix
//общий случай
	double *a = (double *)calloc(PART_HEIGHT(size) * MATRIX_WIDTH,sizeof(double));
	if (rank == 0) {
		a[0] = 1;
		a[5] = 1;
	} else if (rank == 1) {
		a[2] = 1;
		a[7] = 1;
	}
	
	double *b = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	b[0] = 14;
	b[1] = 20;
	b[2] = 11;
	b[3] = 4;

	double *x = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	x[0] = 1;
	x[1] = 1;
	x[2] = 1;
	x[3] = 1;

	double result = 1;
	double *big_vector = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	double *AxMult = NULL;
	double *AxMinB = NULL;
	while ((EPS < result)) {
		AxMult = MPI_MatrixMultiplication(a,x,size,rank);
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