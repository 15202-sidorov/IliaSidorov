#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>


#define LINES_AMOUNT 10

pthread_mutex_t mutex;
pthread_cond_t mainThreadReady;
pthread_cond_t childThreadReady;

//should check out so called not deadblocking type of mutexes
//also atributes and so forth about mutexes

//condition variable
//PROCESS_SHARED PROCESS_PRIVATE
//opaque type

//the body of a new routine ( will be passed as a pointer)
void *new_start_routine( void * arg ) {

	for (int i = 0; i < LINES_AMOUNT; i++) {
		if (i != LINES_AMOUNT - 1) pthread_mutex_lock(&mutex);
		printf("CHILD\n");
		pthread_cond_signal(&childThreadReady);
		if (i != LINES_AMOUNT - 1) {
			pthread_cond_wait(&mainThreadReady,&mutex);
		}
		else {
			pthread_mutex_unlock(&mutex);
		}
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

	if (0 != pthread_cond_init(&mainThreadReady, NULL)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}

	if (0 != pthread_cond_init(&childThreadReady, NULL)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}

	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}

	for (int i = 0; i < LINES_AMOUNT; i++) {
		if (i != LINES_AMOUNT - 1) pthread_mutex_lock(&mutex);
		printf("PARENT\n");
		pthread_cond_signal(&mainThreadReady);
		if (i != LINES_AMOUNT - 1) {
			pthread_cond_wait(&childThreadReady, &mutex);
		}
		else {
			pthread_mutex_unlock(&mutex);
		}
	}

	if (0 != pthread_join(thread,NULL)) {
		perror("Could not join the routine\n");
		return 1;
	}


	if (0 != pthread_cond_destroy(&mainThreadReady)) {
		perror("Could not destroy condition for mutex\n");
		return 1;
	}

	if (0 != pthread_cond_destroy(&childThreadReady)) {
		perror("Could not create condition for mutex\n");
		return 1;
	}


	if (0 != pthread_mutex_destroy(&mutex)) {
		perror("Could not destroy mutex\n");
		return 1;
	}

	if (0 != pthread_mutexattr_destroy(&mutex_attr)) {
		perror("Could not destroy mutex attributes\n");
		return 1;
	}
	

	return 0;
}