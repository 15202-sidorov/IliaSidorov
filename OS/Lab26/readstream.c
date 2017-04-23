#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#define MAX_STRING_SIZE 50

int main() {
	printf("I'm recieving the line ..." );
	char r[MAX_STRING_SIZE];
	fscanf(stdin,"%s",r);
	for (int i = 0; i < sizeof(r); i++) {
		r[i] = toupper(r[i]);
		printf("%c",r[i]);
	}
	return 0;
}