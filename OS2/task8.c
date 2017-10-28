#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <signal.h>

#define ITARATIONS_NUMBER 1000

long THREAD_NUMBER = 0;
int isCanceled = 0;
pthread_t *thread;
pthread_barrier_t  barrier;

pthread_mutex_t farestAccess;
int farestThread = 0;

typedef struct {
	int thread_id;
	int threads_count;
} thread_attributes;

void sigint_handler( int sig ) {
	if (sig == SIGINT) {
		isCanceled = 1;
	}
	return;
}

void *count_sum( void * arg ) {
	double *result = (double *)calloc(1,sizeof(double));
	thread_attributes attr = *(thread_attributes *)arg;
	int padding = attr.threads_count*ITARATIONS_NUMBER;

	long start = ITARATIONS_NUMBER*attr.thread_id;
	long end = start + ITARATIONS_NUMBER;
	while ( !isCanceled ) {
		pthread_mutex_lock(&farestAccess);
		if (farestThread < end) {
			farestThread = end;
		}
		pthread_mutex_unlock(&farestAccess);
		for (long i = start; i < end; i++) {
			result[0] += 1.0/(i*4.0 + 1.0);
			result[0] -= 1.0/(i*4.0 + 3.0);
		}	
		start += padding;
		end += padding;		
	}

	pthread_mutex_lock(&farestAccess);
	
	while (farestThread > end) {
		for (long i = start; i < end; i++) {
			result[0] += 1.0/(i*4.0 + 1.0);
			result[0] -= 1.0/(i*4.0 + 3.0);
		}	
		start += padding;
		end += padding;	
	}
	pthread_mutex_unlock(&farestAccess);
	printf("%ld\n", end);
	
	pthread_exit(result);
}


int main( int argc, char **argv ) {
	if (argc < 2) {
		printf("Threads amount is not entered\n");
		return 1;
	}
	struct sigaction action;
	action.sa_handler = sigint_handler;
	sigaction(SIGINT,&action,NULL);
	double pi = 0;
	THREAD_NUMBER = atoi(argv[1]);
	thread = (pthread_t *)malloc(THREAD_NUMBER * sizeof(pthread_t));
	thread_attributes attributes[THREAD_NUMBER];
	pthread_mutex_init(&farestAccess, NULL);

	for (int i = 0; i < THREAD_NUMBER; i++) {
		attributes[i].thread_id = i;
		attributes[i].threads_count = THREAD_NUMBER;
		if (0 != pthread_create(thread + i , NULL, count_sum, &attributes[i])) {
			perror("Could not create thread\n");
			return 1;
		}
	}

	printf("All threads are created\n");

	int isRight = 1;
	double *result;

	//getting sums from all the threads
	for (int i = 0; i < THREAD_NUMBER; i++) {
		if (0 != pthread_join(*(thread + i), (void *)(&result))) { // *retval should be double *
			perror("Could not join thread\n");
			return 1;
		}

		if (NULL == result) {
			fprintf(stderr, "Thread returned nullptr\n");
			isRight = 0;
		}
		else {
			pi += *result;
			free(result);
			printf("pi is added\n");
		}
	}

	pthread_mutex_destroy(&farestAccess);

	if ( isRight )  {
		printf("Pi is : %f\n",pi * 4.0);
		free(thread);
		return 0;
	}
	else {
		printf("Could not count pi value\n");
		free(thread);
		return 1;
	}

	return 0;
}