#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <omp.h>

#define MATRIX_HEIGHT 6
#define MATRIX_WIDTH  6

#define EPS 0.00001
#define TETTA 0.01

//MPI_Cart_sub()

double countNorm(const double *vector) {
	double result = 0;
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

	for (int i = 0; i < MATRIX_HEIGHT; i++) {
		for (int j = 0; j < MATRIX_WIDTH; j++) {
			if (i == j) {
				a[i * MATRIX_WIDTH + j] = 2.0;
			}
			else {
				a[i * MATRIX_WIDTH + j] = 1.0;
			}
		}
		b[i] = MATRIX_WIDTH + 1;
	}

	double x[MATRIX_WIDTH];
	for (int i = 0; i < MATRIX_WIDTH; i++) {
		x[i] = 2;
	}

	double result = 1;
	double AxMinB[MATRIX_WIDTH];
	double AxMult[MATRIX_WIDTH];

#pragma omp parallel 
{	
	while (result > EPS) {
		for (int i = 0; i < MATRIX_WIDTH; i++) {
			AxMult[i] = 0;
		}

		for (int i = 0; i < MATRIX_HEIGHT; i++) {
			for (int j = 0; j < MATRIX_WIDTH; j++) {
				AxMult[i] += a[i*MATRIX_WIDTH + j]*x[j];
			}
			AxMinB[i] = AxMult[i] - b[i];
		}

		result = countNorm(AxMinB)/countNorm(b);

		for (int j = 0; j < MATRIX_WIDTH; j++) {
			x[j] -= AxMinB[j] * TETTA;
		}
	}
}
	printfMatrix(x);

	return 0;
}