#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#define THREAD_COUNT 4
#define STRING_COUNT 5

typedef struct {
	int len;
	char **data;
} string_array;

string_array *init_string_array( int len ) {
	string_array *result = (string_array *)malloc(sizeof(string_array));
	result->len = len;
	result->data = (char **)malloc(len * sizeof(char *));
	return result;
}

void print_strings( const string_array *input ) {
	for (int i = 0; i < input->len; i++) {
		printf("%s\n",input->data[i]);
	}
	return;
}

void *start_routine( void * input_strings ) {
	string_array *strings = (string_array *) input_strings;
	print_strings(strings);

	return 0;
}


int main(int argc, char **argv) {
	if (argc - 1 != STRING_COUNT) {
		fprintf(stderr,"Not enough strings entered\n");
		return 1;
	}

	pthread_t threads_ids[THREAD_COUNT] = {0};
	for (int i = 0; i < THREAD_COUNT; i++) {
		pthread_create(threads_ids[i], NULL, start_routine, NULL);
	}
	

	return 0;
