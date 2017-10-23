#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <semaphore.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>

#define LINES_AMOUNT 10

//-lrt!!

///Именнованные семафоры!!!!

void printPARENT(sem_t *sem[2]) {
	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(sem[1]);
		printf("PARENT\n");
		sem_post(sem[0]);
	}

	return;
}

void printCHILD() {
	sem_t *sem[2];
	sem[0] = sem_open("/semaphore1",  O_RDWR);
	sem[1] = sem_open("/semaphore2",  O_RDWR);

	if ((SEM_FAILED == sem[1]) || (SEM_FAILED == sem[0])) {
		perror("Could not open or create semaphores 2\n");
		exit(1);
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(sem[0]);
		printf("CHILD\n");
		sem_post(sem[1]);
	}

	if (( 0 != sem_unlink("/semaphore1") ) || ( 0 != sem_unlink("/semaphore2"))) {
		perror("Could not unlink semaphore from process 2");
		return;
	}

	return;
}

int main( int argc, char **argv ) {
	sem_t *sem[2];
	sem[0] = sem_open("/semaphore1", O_CREAT | O_RDWR, S_IRWXU, 0);
	sem[1] = sem_open("/semaphore2", O_CREAT | O_RDWR, S_IRWXU, 1);

	if ((SEM_FAILED == sem[1]) || (SEM_FAILED == sem[0])) {
		perror("Could not create semaphores 1\n");
		return 1;
	}
	
	pid_t child_pid = fork();

	if (-1 == child_pid) {
		perror("Could not fork the process\n");
		return 1;
	}

	if (0 == child_pid) {
		printPARENT(sem);
		
		if ((-1 == sem_close(sem[0]) || (-1 == sem_close(sem[1])))) {
			perror("Could not close named semaphores\n");
		}
	
	}
	else {
		printCHILD();
	}

	return 0;
}