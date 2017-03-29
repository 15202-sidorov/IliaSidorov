#define _XOPEN_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

typedef void (* handler_t)( int ); //pointer to the function, which will handle
								   //the signal set by int

void action(int sig) {
	printf("End of story");
	return;
}

int main() {
	
	//sigset(SIGQUIT, action);
	int count = 0;
	char c = 0;
	while(1) {
		scanf("%c\n",&c);
		if ( == c) {
			count++;
			printf("Increased");
		}
	}	
	return 0;
}