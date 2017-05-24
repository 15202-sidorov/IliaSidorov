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
		perror("Could not get Semaphors");
		return 1;
	}
	while (1) {
		sleep(4);
		printf("C is made!\n");

		struct sembuf incOwn[1] = {
			{C, 1, SEM_UNDO}
		} ;

		if (-1 == semop(semid,incOwn,1)) {
			perror("Could not increace C's semaphor");
			return 2;
		}
	}

	return 0;
}