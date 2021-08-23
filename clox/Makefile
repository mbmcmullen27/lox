clox: main.o memory.o chunk.o debug.o value.o
	clang main.o memory.o chunk.o debug.o value.o -o clox
main.o: main.c common.h chunk.h debug.h
	clang -c main.c -o main.o
memory.o: memory.c memory.h
	clang -c memory.c -o memory.o
chunk.o: chunk.c chunk.h memory.h
	clang -c chunk.c -o chunk.o
debug.o: debug.c debug.h
	clang -c debug.c -o debug.o
value.o: value.c value.h
	clang -c value.c -o value.o
clean:
	rm clox *.o