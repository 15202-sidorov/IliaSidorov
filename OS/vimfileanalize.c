#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/mman.h>
#include <string.h>


#define FileName "vimfileanalize.c"
#define MAXSTRING 200
#define TIME_OUT 5

char *file = NULL;
off_t fileLength = 0;

typedef 
	struct Note {
		off_t location; //offset of the string in file
		int length; //length of the given string
	} Note;

void initNote(Note *new ,int location, int length) {
	new->location = location;
	new->length = length;
	return;
}

void createTable(Note *allNotes) {
	char inputChar = 0;
	int countLength = 0;
	int currentOffset = 0;
	int countOffset = 0;
	int line = 1;
	while (countOffset <= fileLength) {
		inputChar = *file;
		countLength++;
		if ('\n' == inputChar) {
			initNote(allNotes + line,currentOffset,countLength);
			line++;
			countLength = 0;
			currentOffset = countOffset + 1;
		}
		countOffset++;
		file++;
	}
	file -= countOffset;
}

void showString(int line, const Note *table) {
	if ((line >= MAXSTRING) || (line < 0)) {
		perror("Entered wrong line\n");
		return;
	}

	char * currentChar = file + table[line].location;

	char *resultString = (char *)calloc(table[line].length + 1,sizeof(char));

	strncpy(resultString,currentChar,table[line].length);
	resultString[table[line].length] = '\0';
	printf("%s",resultString);
	
	free(resultString);
}

void showEverything(int sig) {
	int count = 0;
	while (count < fileLength) {
		printf("%c",*file);
		file++;
		count++;
	}
	kill(getpid(),SIGALRM);
	return;
}

int main(int argc, char **argv) {
	
	int fd = open(FileName,O_RDONLY);
	if (-1 == fd) {
		return 1;
	}
	
	fileLength = (size_t)lseek(fd,0,SEEK_END);
	file = mmap(NULL,fileLength,PROT_READ,MAP_PRIVATE,fd,0);
	int counter = 0;
	
	Note table[MAXSTRING] = {0};
	createTable(table);


	int inputLine = 1;
	if (signal(SIGALRM,showEverything) == SIG_ERR) {
		perror("Could not set the signal\n");
		return 1;
	}
	while (inputLine != 0) {
		alarm(TIME_OUT);
		scanf("%d",&inputLine);
		alarm(0);
		showString(inputLine,table);
	}
	close(fd);
	return 0;
}
