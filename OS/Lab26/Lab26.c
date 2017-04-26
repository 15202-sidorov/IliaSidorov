#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <wait.h>

#define READ_PATH "readstream.out"
#define MAX_STRING_SIZE 50

//should include READ_PATH in PATH variable before running

void writeProcess(FILE *writeStream) {
	char toGoString[MAX_STRING_SIZE];
	printf("Enter the line you'd like to pass : \n");
	fgets(toGoString,sizeof(toGoString),stdin);
	toGoString[sizeof(toGoString) + 1] = '\0';
	fprintf(writeStream,"%s",toGoString);
	return;
}


int main() {
	FILE *writeStream = popen(READ_PATH,"w");
	writeProcess(writeStream);
	pclose(writeStream);
	return 0;
}