#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <mpi.h>
#include <math.h>

//main matrix's height and width
#define MATRIX_HEIGHT 4
#define MATRIX_WIDTH  4
#define EPS 0.00001
#define TETTA 0.01

//returns height of one matrix in one process
#define PART_HEIGHT(size) (MATRIX_HEIGHT) / (size)
#define STARTING_POINT(size,rank,itaration) (((rank) + (itaration)) % (size)) * (PART_HEIGHT(size))

void printfMatrix(double *m1) {
	for (int i = 0; i < MATRIX_HEIGHT; i++) {
		printf("%f\n",m1[i]);
	}
}

double countNorm(const double *vector) {
	double result = 0;
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}


double *substract(double *v1, double *v2, int size) {
	for(int i = 0; i < PART_HEIGHT(size); i++) {
		v1[i] = v1[i] - v2[i];
	}
	return v1;
}

double *multDouble(double *v1, double k, int size) {
	for(int i =0; i < PART_HEIGHT(size); i++) {
		v1[i] *= k;
	}
	return v1;
}

//size of vector is : PART_HEIGHT(size)
//size of m1 is : MATRIX_WIDTH * PART_HEIGHT(size)
double *MPI_MatrixMultiplication(double *m1, double *vector, int size, int rank) {
	double *result = (double *)calloc(PART_HEIGHT(size),sizeof(double));
	for (int itaration = 0; itaration < size; itaration++) {
		for (int i = 0; i < PART_HEIGHT(size); i++) {
			int starting_point = STARTING_POINT(size,rank,itaration);
			int end_point = starting_point + PART_HEIGHT(size);
			for (int j = starting_point; j < end_point; j++) {
				result[j - starting_point] += m1[i * MATRIX_WIDTH + j] * vector[j -starting_point];
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
			MPI_STATUS_IGNORE);
	}
	
	return result;
}

int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int size;
	int rank;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	
//init matrix here
	double *a = (double *)calloc(PART_HEIGHT(size) * MATRIX_WIDTH,sizeof(double));
	
	if (rank == 0) {
		a[0] = 1.0;
		a[5] = 1.0;
	}
	else if (rank == 1) {
		a[2] = 1.0;
		a[7] = 1.0;
	}
	
	double *x = (double *)calloc(PART_HEIGHT(size),sizeof(double));

	x[0] = 2;
	x[1] = 3;

	double *b = (double *)calloc(PART_HEIGHT(size),sizeof(double));
	b[0] = 8;
	b[1] = 10;

	double *final_b = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	
	MPI_Allgather(b,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  final_b,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  MPI_COMM_WORLD );
	double b_norm = countNorm(final_b);

	double result = 1;
	double *AxMinB = NULL;
	double *AxMult = NULL;
	double *final_result = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	while (EPS < result) {
		AxMult = MPI_MatrixMultiplication(a,x,size,rank);
		AxMinB = substract(AxMult,b,size);

		MPI_Allgather(AxMinB,
				PART_HEIGHT(size),
				MPI_DOUBLE,
				final_result,
				PART_HEIGHT(size),
				MPI_DOUBLE,
				MPI_COMM_WORLD);
		result = countNorm(final_result)/b_norm;
		substract(x,multDouble(AxMinB,TETTA,size),size);
	}
	double *final_x = (double *)calloc(MATRIX_WIDTH,sizeof(double));
	MPI_Allgather(x,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  final_x,
				  PART_HEIGHT(size),
				  MPI_DOUBLE,
				  MPI_COMM_WORLD);
	if (rank == 1) printfMatrix(final_x);
	free(final_x);
	free(final_result);
	free(x);
	free(b);
	free(a);
	MPI_Finalize();
	return 0;
}