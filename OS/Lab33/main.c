#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <unistd.h>


//nubmers of semaphors
#define A 0
#define B 1
#define C 2
#define MUDULE 3

int semid = 0;

void quithandler(int sig) {
	if (-1 == semctl(semid,0,IPC_RMID,0)) {
		perror("Could not delete queue");
		exit(1);
	}
	exit(0);
}

int main(int argc, char **argv) {
	signal(SIGINT,quithandler);
	//creating a group of semaphores
	semid = semget(getuid(), 4, IPC_CREAT | 0666);

	if (-1 == semid) {
		perror("Semaphore failed to be created");
		return 1;
	}

	//further, we start making detailes in each process

	if (0 == fork()) {
		if (-1 == execv("createA.out",0)) {
			perror("Could not make exec on A");
			return 1;
		}
		return 0;
	}

	if (0 == fork()) {
		if (-1 == execv("createB.out",0)) {
			perror("Could not make exec on B");
			return 1;
		}
		return 0;
	}

	if (0 == fork()) {
		if (-1 == execv("createC.out",0)) {
			perror("Could not make exec on C");
			return 1;
		}
		return 0;
	}

	if (0 == fork()) {
		if (-1 == execv("createModule.out",0)) {
			perror("Could not make exec on Module");
			return 1;
		}
		return 0;
	}

	//the process of creating itself

	while (1) {
		//should make module and C-detail
		struct sembuf decSems[2] = {
			{C, -1, SEM_UNDO},
			{MUDULE, -1, SEM_UNDO}
		} ;
		
		if (-1 == semop(semid,decSems,2)){
			perror("Could not substract from semaphore");
			return 2;
		}
		printf("Widget is made!\n");
	}


	return 0;
}