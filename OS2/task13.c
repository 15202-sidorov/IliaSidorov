#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <semaphore.h>

#define LINES_AMOUNT 10

sem_t sem1;
sem_t sem2;

//PROCESS_SHARED PROCESS_PRIVATE

//the body of a new routine ( will be passed as a pointer )
void *new_start_routine( void * arg ) {

	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(&sem1);
		printf("CHILD\n");
		sem_post(&sem2);
	}

	return 0;
}


int main( int argc, char **argv ) {
	
	pthread_t thread;

	if (0 != sem_init(&sem1, 0, 0)) {
		perror("Could not initialize semaphore\n");
		return 1;
	}

	if (0 != sem_init(&sem2, 0, 1)) {
		perror("Could not initialize semaphore\n");
		return 1;
	}

	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(&sem2);
		printf("PARENT\n");
		sem_post(&sem1);
	}

	if (0 != pthread_join(thread,NULL)) {
		perror("Could not join the routine\n");
		return 1;
	}

	if (0 != sem_destroy(&sem1)) {
		perror("Could not destroy semaphore\n");
		return 1;
	}

	if (0 != sem_destroy(&sem2)) {
		perror("Could not destroy semaphore\n");
		return 1;
	}
	
	

	return 0;
}