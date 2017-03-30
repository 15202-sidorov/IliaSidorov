#include <stdlib.h>
#include <stdio.h>
#include <wait.h>
#include <unistd.h>
#include <sys/types.h>

#define MAX_ENV_VALUES 100

extern char **environ;

//envp[] -- array of inviroment variables that we pass to the process
int my_execve(const char *file,char *const argv[], char * const envp[]) {
	char **tmp_environ = environ;

	environ = envp;
	printf("Old enviroment : \n");
	while (*tmp_environ != NULL) {
		//should be the same
		printf("%s\n",*tmp_environ);
		tmp_environ++;
	}
	printf("----------------\n");
	printf("New enviroment : \n");
	if (-1 == execvp(file,argv)) {
		return -1;
	}
	return 0;
}

int main(int argc, char **argv) {
	char * const test_env[MAX_ENV_VALUES] = {
		"test1=value1",
		"test2=value2",
		"test3=value3",
		NULL
	};

	if (-1 == my_execve(argv[1],argv + 1,test_env)) {
		perror("Could not execute file");
		return EXIT_FAILURE;
	}
	return EXIT_SUCCESS;
}