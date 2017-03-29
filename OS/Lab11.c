#include <stdlib.h>
#include <stdio.h>
#include <wait.h>
#include <unistd.h>
#include <sys/types.h>

#define TEST_PROGRAM_NAME "cat"
#define TEST_ARG1_NAME "Lab11.c"
#define MAX_ENV_VALUES 10

extern char **environ;

//envp[] -- array of inviroment variables that we pass to the process
int my_execvpe(const char *file,char *const argv[], char *const envp[]) {
	environ = envp;
	execvp(file,argv);
	return 0;
}

int main(int argc, char **argv) {
	char *test_env[MAX_ENV_VALUES] = {
		"TestValue1=1",
		"TestValue2=2",
		NULL				
	};
	
	if (-1 == my_execvpe(argv[1],argv + 1,test_env)) {
		perror("Could not execute file");
		return EXIT_FAILURE;
	}
	return EXIT_SUCCESS;
}