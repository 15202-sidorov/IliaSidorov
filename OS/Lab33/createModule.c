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
#define MODULE 3

int main(int argc, char **argv) {
	int semid = semget(getuid(),4,0);
	if (-1 == semid) {
		perror("Could not get semophore");
		return 1;
	}
	struct sembuf decSems[2] = {
		{A, -1, SEM_UNDO},
		{B, -1, SEM_UNDO}
	};

	struct sembuf incOwn[1] = {
		{MODULE, 1, SEM_UNDO}
	};

	//creating modules
	while (1) {
		if (-1 == semop(semid, decSems,2)) {
			perror("Could not substract from A and B");
			return 2;
		}

		printf("New module was produced!\n");

		if (-1 == semop(semid, incOwn, 1)) {
			perror("Could not increace module's semaphor");
			return 3;
		}

	}
	return 0;
}