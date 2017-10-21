#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>

#define LINES_AMOUNT 10

pthread_mutex_t mutex[LINES_AMOUNT];

//should check out so called not deadblocking type of mutexes
//also atributes and so forth about mutexes

//condition variable
//PROCESS_SHARED PROCESS_PRIVATE
//opaque type

//the body of a new routine ( will be passed as a pointer)
void *new_start_routine( void * arg ) {
	for (int i = 1; i < LINES_AMOUNT; i += 2) {
		pthread_mutex_lock(mutex + i);
	}

	for (int i = 1; i < LINES_AMOUNT; i += 2) {
		pthread_mutex_lock(mutex + i - 1);
		pthread_mutex_unlock(mutex + i - 1);
		printf("CHILD\n");
		pthread_mutex_unlock(mutex + i);
	
	}

	return 0;
}


int main( int argc, char **argv ) {
	
	pthread_t thread;
	pthread_mutexattr_t mutex_attr;

	if (0 != pthread_mutexattr_init(&mutex_attr)) {
		perror("Could not init mutex attributes\n");
		return 1;
	}
	
	if (0 != pthread_mutexattr_settype(&mutex_attr, PTHREAD_MUTEX_ERRORCHECK)) {
		perror("Could not set mutex attributes type\n");
		return 1;
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		if (0 != pthread_mutex_init(mutex + i, &mutex_attr)) {
			perror("Could not initialize mutex\n");
			return 1;
		}

		if ( !(i % 2) ) {
			pthread_mutex_lock(mutex + i);
		}
	}
	
	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}

	for (int i = 0; i < LINES_AMOUNT; i += 2) {
		printf("PARENT\n");
		pthread_mutex_unlock(mutex + i);
		pthread_mutex_lock(mutex + i + 1);
		pthread_mutex_unlock(mutex + i + 1);
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		if (0 != pthread_mutex_destroy(mutex + i)) {
			perror("Could not destroy mutex\n");
			return 1;
		}
	}

	if (0 != pthread_mutexattr_destroy(&mutex_attr)) {
		perror("Could not destroy mutex attributes\n");
		return 1;
	}
	

	return 0;
}