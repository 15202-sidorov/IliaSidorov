#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <mpi.h>

#define a 1e5
#define EPS 1e-8

//количество узлов в каждой части
#define in 30
#define jn 30
#define kn 30


#define PART_HEIGHT(size) in / size

double F[2][in + 1][jn+1][kn+1]; 
double X, Y, Z;
double max, N, t1, t2;
double owx, owy, owz, c;
double Fi, Fj, Fk, F1;
double hx, hy, hz;

//используем сеточную функцию, значения в узлах сетки


//f - main function for approximation
double Fper(double x, double y, double z) {
	return x*x + y*y + z*z;
}

//Right part of the equation
double Ro(double x, double y, double z){ 
	return 6 - a*(x*x + y*y + z*z);
}

//инициализируем границы каждого попро
void Inic(int size, int rank)    
{ 
	int i, j, k;
	for(i = 0; i <= PART_HEIGHT(size); i++) { 
		for(j = 0; j <= jn; j++) { 
			for(k = 0; k <= kn; k++) { 
				if( ( i != 0 ) && ( j != 0 ) && ( k != 0 ) 
					&& (i != in) 
					&& (j != jn) 
					&& (k != kn)
				)
				{ 
					//внутри нули
					F[0][i][j][k] = 0;
					F[1][i][j][k] = 0;
				}
				else { 
					//по краям точная функция решения
					F[0][i][j][k] = Fper((i + rank * PART_HEIGHT(size))*hx, j*hy, k*hz);  
					F[1][i][j][k] = Fper((i + rank * PART_HEIGHT(size))*hx, j*hy, k*hz);  
				}
			}
		}
	}	 
}



int main(int argc, char **argv) {
	MPI_Init(&argc,&argv);
	int rank;
	int size;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	printf("I'm going for it");

	//значения искомой функции на текущей терации и предыдущей в данном блоке
	
	int flag = 1;
	double max, N, t1, t2;
	double owx, owy, owz, c;
	double Fi, Fj, Fk, F1;

	//длина по X, Y, Z области (задаем область)
	double X = 2.0;
	double Y = 2.0;
	double Z = 2.0;
	// L0 -- значение функции в предыдущей итерации
	// L1 -- текуцей итерации
	int L0 = 1;
	int L1 = 0;

	//длины между узлами сеточной области
	hx = X/in;   	
	hy = Y/jn;
	hz = Z/kn;
	owx = pow(hx,2);
	owy = pow(hy,2);
	owz = pow(hz,2);
	c = 2/owx + 2/owy + 2/owz + a; 

	//инициализируем границы каждой подобласти
	Inic(size,rank);
	MPI_Request sendRequest[2] = {0};
	MPI_Request recieveRequest[2] = {0};

	double buttomValuesBuffer[jn + 1][kn + 1];
	double topValuesBuffer[jn + 1][kn + 1];
	printf("I'm going for it");
	while (flag == 0) { 
		//sending and starting to recieve the bottom

		if (0 != rank) {
			MPI_Isend(&(F[L0][0]),jn * kn,MPI_DOUBLE, (rank+1) % size,123, MPI_COMM_WORLD, &sendRequest[0]);
			MPI_Irecv(buttomValuesBuffer,jn * kn,MPI_DOUBLE, (rank+size-1) % size, 123, MPI_COMM_WORLD, &recieveRequest[1]);
		}
		
		//sending and starting to recieve the top
		if (rank != size - 1) {
			MPI_Isend(&(F[L0][PART_HEIGHT(size) * jn * kn]), jn*kn, MPI_DOUBLE, rank + 1, 124, MPI_COMM_WORLD, &sendRequest[1]);
       		MPI_Irecv(topValuesBuffer, kn * jn, MPI_DOUBLE, rank + 1, 124, MPI_COMM_WORLD, &recieveRequest[0]);
		}

		//counting center
		L0 = 1 - L0;
		L1 = 1 - L1;
		int i = 0;
		int j = 0;
		int k = 0;
		for( i = 1; i <= PART_HEIGHT(size); i++ ) {
			for( j = 1; j <= jn; j++ ) { 
				for( k = 1; k <= kn; k++ ) { 
					Fi = (F[L0][i+1][j][k] + F[L0][i-1][j][k]) / owx;
					Fj = (F[L0][i][j+1][k] + F[L0][i][j-1][k]) / owy;
					Fk = (F[L0][i][j][k+1] + F[L0][i][j][k-1]) / owz;
					F[L1][i][j][k] = (Fi + Fj + Fk - Ro(i*hx,j*hy,k*hz)) / c;
					if(fabs(F[L1][i][j][k] - F[L0][i][j][k]) > EPS) {
							flag = 0;
					}
				}
			}
		}

		//waiting to actually recieve all data
		if(rank != 0) {
        	MPI_Wait(&recieveRequest[1], MPI_STATUS_IGNORE);
        	MPI_Wait(&sendRequest[0], MPI_STATUS_IGNORE);
    	}
    	if(rank != size - 1) {
        	MPI_Wait(&recieveRequest[0], MPI_STATUS_IGNORE);
        	MPI_Wait(&sendRequest[1], MPI_STATUS_IGNORE);
    	}


    	if (rank != 0) {
    		i = rank * PART_HEIGHT(size);
			for( j = 1; j <= jn; j++ ) { 
				for( k = 1; k <= kn; k++ ) {  
					Fi = (F[L0][i+1][j][k] + buttomValuesBuffer[j][k]) / owx;
					Fj = (F[L0][i][j+1][k] + F[L0][i][j-1][k]) / owy;
					Fk = (F[L0][i][j][k+1] + F[L0][i][j][k-1]) / owz;
					F[L1][i][j][k] = (Fi + Fj + Fk - Ro(i*hx,j*hy,k*hz)) / c;
					if(fabs(F[L1][i][j][k] - F[L0][i][j][k]) > EPS) {
						flag = 0;
					}
				}
			} 
    	}
    	
    	if (rank != size - 1) {
			i = PART_HEIGHT(size);
			for( j = 1; j <= jn; j++ ) { 
				for( k = 1; k <= kn; k++ ) {  
					Fi = (F[L0][i+1][j][k] + topValuesBuffer[j][k]) / owx;
					Fj = (F[L0][i][j+1][k] + F[L0][i][j-1][k]) / owy;
					Fk = (F[L0][i][j][k+1] + F[L0][i][j][k-1]) / owz;
					F[L1][i][j][k] = (Fi + Fj + Fk - Ro(i*hx,j*hy,k*hz)) / c;
					if(fabs(F[L1][i][j][k] - F[L0][i][j][k]) > EPS) {
						flag = 0;
				}
			}
		}
	}
}

	double maxtmp = 0.0;
	for (int i = 1; i <= PART_HEIGHT(size); i++) {
		for (int j = 1; j < jn; j++) {
			for (int k = 1; k < kn; k++) {
				if (max < 
					(maxtmp = fabs(F[L1][i][j][k] - Fper(i * hx, j * hy, k * hz)))) {
					max = maxtmp;
				}
			}
		}
	}

	double finalMax = 0.0;
	MPI_Allreduce(&max, &finalMax, 1, MPI_DOUBLE, MPI_MAX, MPI_COMM_WORLD);

    if(rank == 0) {
        max = finalMax;
        printf("Max differ = %lf\n", max);
    }	



	MPI_Finalize();
}
