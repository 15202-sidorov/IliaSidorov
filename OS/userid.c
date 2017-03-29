#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/resource.h>

//check out the mode charachters

#define FILE_NAME "UserFile"

int main() {

	printf("User ID : %d\n",getuid());
	printf("Effective user id : %d\n",geteuid());
	
	FILE *file = fopen(FILE_NAME,"w");
	if (NULL == file) {
		perror("The file was not opened");
		return 1;
	}

	fclose(file);

	setuid(getuid()); // set effective user id on the process
	printf("User ID : %d\n",getuid());
	printf("Effective user id : %d\n",geteuid());

	file = fopen(FILE_NAME,"w");
	if (NULL == file) {
		perror("The file was not opened");
		return 1;
	}
	fclose(file);

	return 0;
}