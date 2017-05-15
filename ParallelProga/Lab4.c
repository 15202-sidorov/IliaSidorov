#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <mpi.h>

#define a 1e5
#define EPS 1e-8


//количество узлов в каждой части
#define in 20
#define jn 20
#define kn 20

#define PART_HEIGHT(size) in / size

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



int main() {
	MPI_Init(&argc,&argv);
	int rank;
	int size;
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);

	//значения искомой функции на текущей терации и предыдущей в данном блоке
	double F[2][PART_HEIGHT(size) + 1][jn+1][kn+1]; 
	double X, Y, Z;
	double max, N, t1, t2;
	double owx, owy, owz, c;
	double Fi, Fj, Fk, F1;
	int flag = 1;

	//длина по X, Y, Z области (задаем область)
	X = 2.0;
	Y = 2.0;
	Z = 2.0;
	// L0 -- значение функции в предыдущей итерации
	// L1 -- текуцей итерации
	L0 = 1;
	L1 = 0;

	//длины между узлами сеточной области
	hx = X/in;   	
	hy = Y/jn;
	hz = Z/kn;
	owx = pow(hx,2);
	owy = pow(hy,2);
	owz = pow(hz,2);
	c = 2/owx + 2/owy + 2/owz + a; 

	//инициализируем границы каждой подобласти
	Inic(size);
	MPI_Request sentRequest[2] = {0};
	MPI_Request recieveRequest[2] = {0};

	double buttomValuesBuffer[jn+1][kn+1];
	double topValuesBuffer[jn + 1][kn + 1];

	while (flag == 0) { 
		//sending and starting to recieve the bottom
		if (0 != rank) {
			MPI_Isend(&(F[L0][0]),jn * kn,MPI_DOUBLE, (rank+1) % size,123, comm, &sendRequest[0]);
			MPI_Irecv(buttomValuesBuffer,jn * kn,MPI_DOUBLE, (rank+size-1) % size, 123, comm, &recieveRequest[1]);
		}
		
		//sending and starting to recieve the top
		if (rank != size - 1) {
			MPI_Isend(&(F[L0][PART_HEIGHT(size) - 1)*jn*kn]), jn*kn, MPI_DOUBLE, rank + 1, 124, MPI_COMM_WORLD, &sendRequest[1]);
       		MPI_Irecv(topValuesBuffer, kn * jn, MPI_DOUBLE, rank + 1, 124, MPI_COMM_WORLD, &recieveRequest[0]);
		}

		//counting center
		L0 = 1 - L0;
		L1 = 1 - L1;
		for( i = 1; i < PART_HEIGHT(size); i++ ) {
			for( j = 1; j < jn; j++ ) { 
				for( k = 1; k < kn; k++ ) { 
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
			for( j = 1; j < jn; j++ ) { 
				for( k = 1; k < kn; k++ ) {  
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
			i = i + PART_HEIGHT(size);
			for( j = 1; j < jn; j++ ) { 
				for( k = 1; k < kn; k++ ) {  
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



	MPI_Finalize();
}