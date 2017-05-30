#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <unistd.h>
#include <sys/shm.h>

#define SHM_SIZE 64

int semid = 0;
int shmid = 0;

int main(int argc, char **argv) {
	int semid, shmid;
	char* message;
	struct sembuf inprocess = {0, -1, 0};
	struct sembuf consumed =  {1,  1, 0};
	
	semid = semget(getuid(),2,0);

	if(-1 == semid) {
		perror("consumer semget");
		return 1;
	}

	shmid = shmget(getuid(),SHM_SIZE,0);
	if (-1 == shmid) {
		perror("consumer shmget");
		return 1;
	}
	
	message = shmat(shmid, 0, 0);
	semop(semid,&consumed,1);
	
	while (1) {
		if(-1 == semop(semid, &inprocess, 1)) {
			break;
		}
		printf("%s", message);
		if (-1 == semop(semid, &consumed, 1)) {
			perror("could not consume");
			shmdt(message);
			return 4;
		}
	}
	shmdt(message);

	return 0;
}