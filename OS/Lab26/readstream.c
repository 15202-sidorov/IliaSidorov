#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#define MAX_STRING_SIZE 50

int main() {
	printf("I'm recieving the line ...\n" );
	char r[MAX_STRING_SIZE];
	fgets(r,sizeof(r),stdin);
	int i = 0;
	while ('\n' != r[i]) {
		r[i] = toupper(r[i]);
		printf("%c",r[i]);
		i++;
	}
	printf("\n");
	return 0;
}