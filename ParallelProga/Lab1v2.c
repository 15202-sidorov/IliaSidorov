#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mpi.h>
#include <math.h>

//main matrix's height and width
#define MATRIX_HEIGHT(size) (size) * 2
#define MATRIX_WIDTH(size) (size) * 2
#define EPS 0.00001
#define TETTA 0.01

//returns height of one matrix in one process
#define PART_HEIGHT(size) (MATRIX_HEIGHT(size)) / (size)
#define STARTING_POINT(size,itaration) (itaration) * (PART_HEIGHT((size)))

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

	for (int i = 0; i < PART_HEIGHT(size); i++) {
		b[i] = MATRIX_HEIGHT(size) + 1;
	}

	return;
}

void printfMatrix(double *m1, int size) {
	for (int i = 0; i < MATRIX_HEIGHT(size); i++) {
		printf("%f\n",m1[i]);
	}
}

double countNorm(const double *vector , int size) {
	double result = 0;
	for (int i = 0; i < MATRIX_WIDTH(size); i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}

void MPI_MatrixMultiplication(double *result, double *m1, double *vector, int size, int rank) {
	for (int i = 0; i < PART_HEIGHT(size); i++) {
		result[i] = 0;
	}	
	for (int itaration = 0; itaration < size; itaration++) {
		for (int i = 0; i < PART_HEIGHT(size); i++) {
			int starting_point = STARTING_POINT(size,itaration);
			int end_point = starting_point + PART_HEIGHT(size);
			for (int j = starting_point; j < end_point; j++) {
				result[i] += m1[i * MATRIX_WIDTH(size) + j] * vector[j - starting_point];
			}
		}

		MPI_Sendrecv_replace(
			vector,
			PART_HEIGHT(size),
			MPI_DOUBLE,
			(rank + 1) % size,
			123,
			(rank + size - 1) % size,
			123,
			MPI_COMM_WORLD,
			MPI_STATUS_IGNORE );
		MPI_Barrier(MPI_COMM_WORLD);
	}
	
	return;
}

int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int size;
	int rank;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	
	double a[PART_HEIGHT(size) * MATRIX_WIDTH(size)];
	double x[PART_HEIGHT(size)];
	double b[PART_HEIGHT(size)];

	init_Matrixes(a,b,size,rank);
	for (int i = 0; i < PART_HEIGHT(size); i++) {
		x[i] = 3;
	}

	double result = 1;
	double AxMinB[PART_HEIGHT(size)];
	double AxMult[PART_HEIGHT(size)];
	double final_result[MATRIX_WIDTH(size)];
	double final_b[MATRIX_WIDTH(size)];
	double final_x[MATRIX_WIDTH(size)];

	MPI_Allgather(b,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  final_b,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  MPI_COMM_WORLD );
	double b_norm = countNorm(final_b,size);

	while (EPS < result) {
		MPI_MatrixMultiplication(AxMult,a,x,size,rank);
		
		for (int i = 0; i < PART_HEIGHT(size); i++) {
			AxMinB[i] = AxMult[i] - b[i];
		}

		MPI_Allgather(AxMinB,
				PART_HEIGHT(size),
				MPI_DOUBLE,
				final_result,
				PART_HEIGHT(size),
				MPI_DOUBLE,
				MPI_COMM_WORLD);

		result = countNorm(final_result,size) / b_norm;
		for (int i = 0; i < PART_HEIGHT(size); i++) {
			x[i] -= TETTA * final_result[i];
		}
	}

	MPI_Allgather(x,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  final_x,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  MPI_COMM_WORLD);
	
	if (rank == 0) printfMatrix(final_x,size);
	
	MPI_Finalize();
	return 0;
}