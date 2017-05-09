#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <mpi.h>

//main matrix's height and width
#define MATRIX_HEIGHT(size_) size_ * 3
#define MATRIX_WIDTH(size_) size_ * 3
#define EPS 0.00001
#define TETTA 0.01

//returns height of one matrix in one process
#define PART_HEIGHT(size_) MATRIX_HEIGHT(size_) / (size_)

void init_Matrixes(double *a, double *b, int size, int rank) {
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

	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		b[i] = MATRIX_HEIGHT(size) + 1;
	}

	return;
}

void printfMatrix(double *m1 , int size) {
	for (int i = 0; i < MATRIX_HEIGHT(size); i++) {
		printf("%lf\n",m1[i]);
	}
}

double countNorm(const double *vector, int size) {
	double result = 0;
	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}

int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int rank;
	int size;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	
	double a[PART_HEIGHT(size) * MATRIX_WIDTH(size)];
	double b[MATRIX_WIDTH(size)];

	init_Matrixes(a,b,size,rank);


	double x[MATRIX_WIDTH(size)];

	double result = 1;
	double big_vector[MATRIX_WIDTH(size)];
	double AxMult[PART_HEIGHT(size)];
	double AxMinB[MATRIX_WIDTH(size)];

	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		x[i] = 4.0;
		AxMinB[i] = 0.0;
		if (i < PART_HEIGHT(size)) {
			AxMult[i] = 0.0;
		}
	}

	while ((EPS < result)) {
		
		for (int i = 0; i < PART_HEIGHT(size); i++) {
			AxMult[i] = 0;
			for (int j = 0; j < MATRIX_WIDTH(size); j++) {
				AxMult[i] += a[i * MATRIX_WIDTH(size) + j] * x[j];
			}
		}
	
		MPI_Allgather( AxMult,
					   PART_HEIGHT(size),
					   MPI_DOUBLE,
					   big_vector,
					   PART_HEIGHT(size),
					   MPI_DOUBLE,
					   MPI_COMM_WORLD );


		for(int i = 0; i < MATRIX_WIDTH(size); i++) {
			AxMinB[i] = big_vector[i] - b[i];
			x[i] -= (TETTA * AxMinB[i]);
		}
		
		result = countNorm(AxMinB, size) / countNorm(b, size);

	}

	MPI_Barrier(MPI_COMM_WORLD);
	if (rank == 0) printfMatrix(x,size);
	
	MPI_Finalize();
	return 0;
}