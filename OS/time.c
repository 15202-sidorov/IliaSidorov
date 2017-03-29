#define _XOPEN_SOURCE

#include <stdlib.h>
#include <stdio.h>
#include <time.h>

#define TIME_ZONE "TZ=Europe/Berlin"
//time_t - time from the start of epoch
//struc tm - calendar time


int main() {
    if ( putenv(TIME_ZONE) ) {
    	fprintf(stderr, "Could not set the TIME_ZONE");
    	return 1;
    }
	//tzset();
    time_t t1 = time(0);
    struct tm *t2 = localtime(&t1);
    printf("Local time : %s\n",asctime(t2));

	return 0;
}
//tm_year, tm_min ... tzset
//tzname, timezone - extern variables 


