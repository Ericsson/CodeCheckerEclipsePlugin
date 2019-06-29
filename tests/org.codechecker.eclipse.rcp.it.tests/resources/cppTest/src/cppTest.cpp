//============================================================================
// Name        : cppTest.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
using namespace std;

void test() {
  int *p = (int*)malloc(sizeof(int));
  free(p);
  *p = 1; // warn: use after free
}

void test2() {
  long *p = (long*)malloc(sizeof(short));
  free(p);
}

void f(int *p){};

void testUseMiddleArgAfterDelete(int *p) {
  delete p;
  f(p); // warn: use after free
}

int main() {
	int *i = new int;
	int a = *i;
	cout << "Hello world!" << endl; // prints !!!Hello World!!!
	return 0;
}
