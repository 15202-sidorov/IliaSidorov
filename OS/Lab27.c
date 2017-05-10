#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#define CHUNCK_SIZE 64

int main(int argc, char **argv) {
	FILE *input_file = fopen(argv[1],"r");
	if (NULL == input_file) {
		perror("Could not open file");
		return 1;
	}

	//that is the stream we are going to pass information by to wc
	FILE *output_stream = popen("wc -l","w");
	if (NULL == output_stream) {
		perror("could not create pipe");
		return 2;
	}
	
	char currentLine[CHUNCK_SIZE];
	while (NULL != fgets(currentLine,CHUNCK_SIZE,input_file)) {
		if ('\n' == *currentLine) {
			fputs(currentLine, output_stream);
		}
	}

	pclose(output_stream);
	fclose(input_file);
	return 0;
}