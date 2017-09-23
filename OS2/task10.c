#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>


#define LINES_AMOUNT 1000

pthread_mutex_t mutex;
pthread_mutex_t mutex2;
pthread_cond_t condition;
pthread_cond_t condition2;

//should check out so called not deadblocking type of mutexes
//also atributes and so forth about mutexes

//condition variable
//PROCESS_SHARED PROCESS_PRIVATE
//opaque type

//the body of a new routine ( will be passed as a pointer)
void *new_start_routine( void * arg ) {

	for (int i = 0; i < LINES_AMOUNT; i++) {
		pthread_mutex_lock(&mutex);
		printf("New routine printf\n");
		fflush(stdout);
		pthread_mutex_unlock(&mutex);
		pthread_cond_signal(&condition);
		pthread_cond_wait(&condition2,&mutex2);
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
	
	if (0 != pthread_mutex_init(&mutex,&mutex_attr)) {
		perror("Could not create mutex\n");
		return 1;
	}

	if (0 != pthread_mutex_init(&mutex2,&mutex_attr)) {
		perror("Could not create mutex\n");
		return 1;
	}

	if (0 != pthread_cond_init(&condition, NULL)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}

	if (0 != pthread_cond_init(&condition2, NULL)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}

	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		pthread_mutex_lock(&mutex);
		printf("Parent routine printf\n");
		fflush(stdout);
		pthread_cond_wait(&condition, &mutex);
	}

	if (0 != pthread_join(thread,NULL)) {
		perror("Could not join the routine\n");
		return 1;
	}


	if (0 != pthread_cond_destroy(&condition)) {
		perror("Could not destroy condition for mutex\n");
		return 1;
	}

	if (0 != pthread_cond_destroy(&condition2)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}


	if (0 != pthread_mutex_destroy(&mutex)) {
		perror("Could not destroy mutex\n");
		return 1;
	}

	if (0 != pthread_mutex_destroy(&mutex2)) {
		perror("Could not destroy mutex\n");
		return 1;
	}

	if (0 != pthread_mutexattr_destroy(&mutex_attr)) {
		perror("Could not destroy mutex attributes\n");
		return 1;
	}
	

	return 0;
}