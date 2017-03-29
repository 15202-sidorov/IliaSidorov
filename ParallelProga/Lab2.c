#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <omp.h>

#define MATRIX_HEIGHT 4
#define MATRIX_WIDTH  4

#define EPS 0.00001
#define TETTA 0.01

//MPI_Cart_sub()

double countNorm(const double *vector) {
	double result = 0;
	//#pragma omp parallel for reduction(+:result)
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		result += (vector[i] * vector[i]);
	}
	result = sqrt(result);
	return result;
}

void printfMatrix(const double *m1) {
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		printf("%f\n",m1[i]);
	}
}

int main() {
	double a[MATRIX_WIDTH * MATRIX_HEIGHT] = {0};
	double b[MATRIX_WIDTH] = {0};
	a[0] = 1;
	a[5] = 1;
	a[10] = 1;
	a[15] = 1;

	b[1] = 1;
	b[3] = 2;

	double x[MATRIX_WIDTH];
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		x[i] = 1;
	}

	double result = 1;
	double AxMinB[MATRIX_WIDTH];
	while (result > EPS) {
		for (int i = 0; i < MATRIX_WIDTH; i++) {
			AxMinB[i] = 0;
		}

		#pragma omp parallel for 
		for (int i = 0; i < MATRIX_HEIGHT; i++) {
			for (int j = 0; j < MATRIX_WIDTH; j++) {
				AxMinB[j] += a[i*MATRIX_WIDTH + j]*x[j] - b[j];
			}
		}

		result = countNorm(AxMinB)/countNorm(b);

		#pragma omp parallel for 
		for (int j = 0; j < MATRIX_WIDTH; j++) {
			x[j] -= AxMinB[j] * TETTA;
		}
	}
	printfMatrix(x);

	return 0;
}