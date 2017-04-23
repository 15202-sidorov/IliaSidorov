#include <stdio.h>
#include <sys/types.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <grp.h>
#include <pwd.h>
#include <time.h>
#include <string.h>

#define COUNT_RIGHTS 3

int printInformation(char *filepath) {
	//all needed information about file will be here 
	struct stat fileInfo = {0};
	if (-1 == stat(filepath,&fileInfo)){
		return 1;
	}
	//now information about the file in in structure

	//see whether the file is a directory or ordinary file
	switch(fileInfo.st_mode & S_IFMT) {
		case S_IFDIR:
			printf("d ");
			break;
		case S_IFREG:
			printf("- ");
			break;
		default:
			printf("? ");
	}

	//see permissions for owner

	char charRights[] = {'r', 'w', 'x'};
    mode_t rights[] = {
        S_IRUSR, S_IWUSR, S_IXUSR,
        S_IRGRP, S_IWGRP, S_IXGRP,
        S_IROTH, S_IWOTH, S_IXOTH
    };

    char str[] = "---------";

    int count = sizeof(rights) / sizeof(mode_t);
    for(int i = 0; i < count; ++i) {
        if(fileInfo.st_mode & rights[i]) {
            str[i] = charRights[i % COUNT_RIGHTS];
        }
    }

    printf("%s ", str);

    //getting i_nodes of file
    printf("%d ",fileInfo.st_nlink);

    //get user info

    struct passwd *pwdinfo = getpwuid(fileInfo.st_uid);
    printf("%s ",pwdinfo->pw_name);

    struct group *groupinfo = getgrgid(fileInfo.st_gid);
    printf("%s ", groupinfo->gr_name);

    //get size of file
    if (S_ISREG(fileInfo.st_mode)) {
    	printf("%.d ",fileInfo.st_size);
    }
   

    //last modifiction 

    printf("%.24s ",ctime(&fileInfo.st_mtime));
    char *start = filepath;
    //file's name
    int isPath = 0;
    while (*filepath != '\0') {
    	filepath++;
    	if (*filepath == '/') {
    		isPath = 1;
    	}
    }
    if (isPath){
		while(*filepath != '/') {
    		filepath--;
    	}
    	filepath++;
    	printf("%s\n",filepath);
    }
    else {
    	printf("%s\n",start);
    }

   	return 0;
}

int main(int argc, char **argv) {
	int i = 1;
	while (argv[i] != NULL) {
		if (-1 == printInformation(argv[i])) {
			fprintf(stderr, "Could not get information %s",argv[i]);
			return 1;
		}
		i++;
	}
	return 0;
}