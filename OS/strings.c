#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef 
	struct node{
		struct node *next;
		struct node *prev;
		char *data;
	} Node;

Node *initNode(Node *prevNode, Node *nextNode,char *s){
	Node *newList = (Node *)malloc(sizeof(Node));
	if (NULL == newList){
		fprintf(stderr, " BAD allocation ");
		return NULL;
	}
	newList->data = s;
	newList->next = nextNode;
	newList->prev = prevNode;
	return newList; 
}

void destroyNode(Node *l){
	if (l){
		free(l->data);
		free(l);
	}
	return;
}

typedef
	struct list{
		Node *frontNode;
		Node *backNode;
	} List;

List * initList(){
	List *newList = (List *)malloc(sizeof(List));
	if (NULL == newList){
		fprintf(stderr," BAD allocation ");
	}
	newList->frontNode = NULL;
	newList->backNode = NULL;
	return newList;
}

List *push_front(List *l ,char *s){
	Node *newNode = NULL;
	if (NULL != l->frontNode){
		newNode = initNode(l->frontNode,NULL,s);
	}
	else{
		newNode = initNode(NULL,NULL,s);
		l->backNode = newNode;
	}
	l->frontNode = newNode;
	return l;
}	

List *push_back(List *l,  char *s){
	Node *newNode = NULL;
	if (NULL != l->backNode){
		newNode = initNode(NULL,l->backNode,s); 
	}
	else{
		newNode = initNode(NULL,NULL,s);
		l->frontNode = newNode;
	}
	l->backNode = newNode;
	return l;
}

void printList(List *l){
	Node *current = l->backNode;
	while(current->next != NULL){
		printf("%s",current->data);
		current = current->next;
	}
	printf("%s\n",current->data);
}

void destroyList(List *l){
	while (NULL != l->backNode->next){
		Node *d = l->backNode;
		l->backNode = l->backNode->next;
		destroyNode(d);
	}
	destroyNode(l->backNode);
	return;
}

int main(){
	char ptr[1000];
	List *l = initList();
	while (ptr[0] != '.'){
		fgets(ptr,1000,stdin);
		int length = strlen(ptr);
		char *s = (char *)malloc(sizeof(char)* (length+1));
		strncpy(s,ptr,length);
		push_back(l,s);
	}	

	printList(l);
	destroyList(l);
	
	return 0;
}