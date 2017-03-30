#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

extern char **environ;

int main(int argc, char **argv) {
	while (NULL != *environ) {
		printf("%s\n",*environ);
		environ++;
	}
	
	return 0;
}